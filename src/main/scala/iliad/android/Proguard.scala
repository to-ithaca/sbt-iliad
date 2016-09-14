package iliad
package android

import iliad.io.IOReader
import iliad.android.io.ProGuardAPI

import sbt._
import sbt.classpath.ClasspathUtilities
import Keys._

import cats.data.Reader

import proguard.{ClassPathEntry, ClassPath, ConfigurationParser, Configuration => ProguardConfig}

/** Task for proguard obfuscation */
object Proguard {
  import layoutKeys._
  import proguardKeys._

  private def createOutputDirectories(jar: File, log: File): Reader[IO.type, Unit] = for {
    _ <- IOReader.createDirectoryIfNotExists(jar)
    _ <- IOReader.createDirectoryIfNotExists(log)
  } yield ()

  private def runTask(log: Logger, configFile: File, libraries: Seq[File], inputClasspath: Classpath, baseOutput: File, jarOutput: File) = {

    val inputs = inputClasspath.files.filterNot(libraries.toSet)
      .filter(ClasspathUtilities.isArchive)

    val config = Config(configFile, libraries, inputs, baseOutput, jarOutput)
    val proguardOperation = config.operation

    val ioOperations = createOutputDirectories(jarOutput, config.logOutput)

    log.info(s"Using proguard configuration ${config.configFile.absolutePath}")
    log.info("Using input jars:")
    config.inputs.foreach(i => log.info(i.absolutePath))
    log.info(s"Using library jars:")
    config.libraries.foreach(l => log.info(l.absolutePath))
    log.info(s"Used proguard target folder ${config.jarOutput.absolutePath}")

    log.info("Creating output directories")
    ioOperations.run(IO)

    log.info(s"Starting proguard obfuscation")
    proguardOperation(ProGuardAPI)
    log.info("Finished proguard obfuscation")
  }

  /** Task for proguard obfuscation
    *
    * Depends on the compile task.
    * Reads proguard config from [[proguardConfig]].
    * Extracts jars to obfuscate from [[proguardInputClasspath]].
    * Outputs obfuscated jars to [[proguardJars]].
    * Logs are written to the [[proguardOut]] / logs directory.
    */
  private def runProguardTask = Def.task {
    runTask(streams.value.log,
      proguardConfig.value,
      proguardLibraryJars.value,
      proguardInputClasspath.value,
      proguardOut.value,
      proguardJars.value)
  }

  /** Skips proguard obfuscation
    *
    * Extracts jars from [[proguardKeys.proguardInputClasspath]].
    * Writes untouched jars to [[proguardJars]].
    */
  def skipProguardTask = Def.task {
    val log = streams.value.log
    val inputs = proguardInputClasspath.value.files.filterNot(proguardLibraryJars.value.toSet)
    val output = proguardJars.value
    val ioPairs = inputs.map {i =>
      i -> output / i.name
    }

    val ioOperation = for {
      _ <- IOReader.createDirectory(output)
      _ <- IOReader.copy(ioPairs, overwrite = true)
    } yield()

    log.info("Skipping proguard - copying jars to proguard folder")
    ioOperation.run(IO)
    log.info("Finished copying jars")
  }

  def apply() = Def.taskDyn(if(skipProguard.value) skipProguardTask else runProguardTask)
}

/** Immutable representation of [[proguard.Configuration]] */
private case class Config(configFile: File, libraries: Seq[File], inputs: Seq[File], baseOutput: File, jarOutput: File) {

  val logOutput = baseOutput / "logs"
  private val logFiles = LogFiles(logOutput)
  private def proguardConfig: ProguardConfig = {
    val config = new ProguardConfig

    /** Parses proguard configuration */
    val parser = new ConfigurationParser(configFile, System.getProperties)
    parser.parse(config)

    /** Adds library jars */
    val libraryClassPath = new ClassPath
    libraries.foreach(l => libraryClassPath.add(new ClassPathEntry(l, false)))
    config.libraryJars = libraryClassPath

    /** Registers log files */
    //FIXME: why are log files disturbing proguard caching?
    //the lastModified time of the log files is not what we expect it to be - what is modifying them?
//    config.printSeeds = logFiles.seeds
//    config.printUsage = logFiles.usage
//    config.printMapping = logFiles.mapping
//    config.printConfiguration = logFiles.configuration
//    config.dump = logFiles.dump

    /** Adds jars to obfuscate as input - output pairs */
    config.programJars = programJars

    config
  }

  private def asEntries(inOut: (File, File)): Seq[ClassPathEntry] = Seq(
    new ClassPathEntry(inOut._1, false),
    new ClassPathEntry(inOut._2, true)
  )

  private def programJars: ClassPath = {
    val cp = new ClassPath
    inputs.map(f => f -> jarOutput / f.getName).flatMap(asEntries).zipWithIndex.foreach {
      case (entry, idx) => cp.add(idx, entry)
    }
    cp
  }

  /** Impure function to execute proguard */
  def operation: ProGuardAPI.type => Unit = { proguard =>
    proguard.execute(proguardConfig)
  }
}

/** Log files generated during proguard obfuscation */
private case class LogFiles(baseDirectory: File) {
  val seeds: File = baseDirectory / "seeds.txt"
  val usage: File = baseDirectory / "usage.txt"
  val mapping: File = baseDirectory / "mapping.txt"
  val configuration: File = baseDirectory / "configuration.txt"
  val dump: File = baseDirectory / "dump.txt"
}
