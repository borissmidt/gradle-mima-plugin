package the.flowering.branches.mima

import java.io.File

import com.typesafe.tools.mima.core.util.log.Logging
import com.typesafe.tools.mima.core.{Problem, ProblemFilter, ProblemFilters}
import com.typesafe.tools.mima.lib.MiMaLib
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.{Property, SetProperty}
import org.gradle.api.tasks.{CompileClasspath, Input, TaskAction}
import org.gradle.api.{DefaultTask, GradleException}
import org.gradle.internal.exceptions.Contextual

import scala.collection.JavaConverters._
import scala.tools.nsc.classpath.AggregateClassPath
import scala.tools.nsc.util.ClassPath
@Contextual
case class MiMaException(message: String, cause: Throwable) extends GradleException(message, cause)

/**
  * Copyright (C) 15.04.20 - REstore NV
  */
sealed trait Direction
object Direction {

  sealed trait Backward extends Direction {
    override val toString: String = "backward"
  }

  sealed trait Forward extends Direction {
    override val toString: String = "forward"
  }

  case object Backward extends Backward

  case object Forward extends Forward

  case object Both extends Backward with Forward {
    override val toString: String = "both"
  }

  def unapply(s: String): Option[Direction] = s match {
    case "backward" | "backwards" => Some(Backward)
    case "forward" | "forwards" => Some(Forward)
    case "both" => Some(Both)
    case _ => None
  }
}

class ReportBinaryIssues extends DefaultTask {
  private val log = getProject.getLogger
  private val wrappedLogger = new Logging {
    override def info(str: String): Unit = log.info(str, "")

    override def debugLog(str: String): Unit = log.debug(str, "")

    override def warn(str: String): Unit = log.warn(str, "")

    override def error(str: String): Unit = log.error(str, "")
  }

  def objects = getProject.getObjects

  private val failOnException: Property[java.lang.Boolean] =
    objects.property(classOf[java.lang.Boolean])
  //  private val exclude: SetProperty[Exclude] =
  //    objects.setProperty(classOf[Exclude])
  private val reportSignatureProblems: Property[java.lang.Boolean] =
  objects.property(classOf[java.lang.Boolean])
  private val direction: Property[String] = objects.property(classOf[String])

  private val compareToVersions: SetProperty[String] =
    objects.setProperty(classOf[String])

  private val currentArtifact: Property[FileCollection] =
    objects.property(classOf[FileCollection])

  @Input def getCompareToVersions(): SetProperty[String] =
    compareToVersions

  @CompileClasspath def getCurrentArtifact(): Property[FileCollection] =
    currentArtifact

  //
  //  @Input
  //  def getExclude(): SetProperty[Exclude] = exclude

  @Input
  def getReportSignatureProblems(): Property[java.lang.Boolean] = reportSignatureProblems

  @Input
  def getDirection(): Property[String] = direction

  @Input
  def getFailOnException(): Property[java.lang.Boolean] = failOnException

  @TaskAction
  def checkForMimaErrors() = {
    val direction = Direction
      .unapply(this.direction.get())
      .getOrElse(
        throw MiMaException(
          "direction needs to be one of backward | forward | both",
          new IllegalArgumentException()
        )
      )
    val failOnException = this.failOnException.get()

    //    val filters = this.exclude
    //      .get()
    //      .asScala
    //      .flatMap(pt =>
    //        pt.getPackages
    //          .asScala
    //          .map(ProblemFilters.exclude(pt.getName, _))
    //      )
    //      .toList
    val groupName = getProject.getExtensions.getByType(classOf[MimaExtension]).groupName().get()

    val previousArtifacts = ResolveOldApi
      .oldApiProvider(
        getProject,
        this.compareToVersions
          .get()
          .asScala
          .map(groupName.withVersion(_).asString())
          .asJava
      )
      .get()
      .asScala

    val currentArtifact = this.currentArtifact
      .get()
      .getFiles
      .asScala
      .head

    println(s"direction: ${direction}", "")
    println(
      s"testing compatibility with versions: ${compareToVersions.get().asScala.mkString(", ")}}"
    )

    println(s"currentArtifact: ${currentArtifact}", "")

    previousArtifacts.foreach {
      case (groupVersionName, previousArtifact) =>
        reportErrors(
          direction,
          failOnException,
          List(),
          getProject.files(previousArtifact.jars).getFiles.asScala.head,
          currentArtifact,
          groupVersionName
        )
    }

  }

  private def reportErrors(
      direction: Direction,
      failOnError: Boolean,
      filters: List[ProblemFilter],
      previousArtifact: File,
      currentArtifact: File,
      comparingTo: GroupNameVersion
    ) = {
    def isReported(classification: String)(problem: Problem): Boolean =
      filters.forall { filter =>
        if (filter(problem)) {
          true
        } else {
          log.quiet(s"Filtered out: ${problem.description(classification)}")
          log.quiet(s"    filtered by: $filter")
          false
        }
      }

    val (bcProblems, fcProblems) =
      runMima(
        AggregateClassPath.createAggregate(),
        direction,
        previousArtifact,
        currentArtifact
      )

    val bcErrors = bcProblems.filter(isReported("current"))
    val fcErrors = fcProblems.filter(isReported("other"))
    val count = bcErrors.length + fcErrors.length
    val filteredCount = bcProblems.length + fcProblems.length - bcErrors.length - fcErrors.length
    val filteredMsg =
      if (filteredCount > 0) s" (filtered $filteredCount)" else ""
    log.warn(s"found binary incompatibilities with version ${comparingTo.asString()}")
    reportToConsole(comparingTo, bcErrors, fcErrors, count, filteredMsg)

    if (count > 0) {

      if (failOnError)
        throw MiMaException(
          s"found binary incompatibilities with ${comparingTo.asString()}",
          new RuntimeException()
        )
    }
  }

  //todo add lambda to decide where to output the report?
  private def reportToConsole(
      comparingTo: GroupNameVersion,
      bcErrors: List[Problem],
      fcErrors: List[Problem],
      count: Int,
      filteredMsg: String
    ) = {
    println(
      s"\n\n##################### ${comparingTo.asString()} ##################################################################\n"
    )
    println(
      s"Found $count potential binary incompatibilities while checking against ${comparingTo
        .asString()} using filters $filteredMsg",
      ""
    )

    def errorBlock(errors: List[Problem]) = {
      errors
        .map(e => e.toString.split('(').head + ": " + e.description(comparingTo.version))
        .mkString("\n- ")
    }

    println(
      s"""forwardErrors: 
         |- ${errorBlock(fcErrors)}
         |""".stripMargin
    )
    println(
      s"""backwardErrors:
         |- ${errorBlock(bcErrors)}
         |""".stripMargin
    )
  }

  private def collectProblems(cp: ClassPath, oldJar: File, newJar: File) = { () =>
    new MiMaLib(cp, wrappedLogger)
      .collectProblems(oldJar, newJar)
  }

  private def runMima(
      classpath: ClassPath,
      direction: Direction,
      prevJar: File,
      newJar: File
    ): (List[Problem], List[Problem]) = {
    val checkBC = collectProblems(classpath, prevJar, newJar)
    val checkFC = collectProblems(classpath, newJar, prevJar)

    direction match {
      case Direction.Backward => (checkBC(), Nil)
      case Direction.Forward => (Nil, checkFC())
      case Direction.Both => (checkBC(), checkFC())
    }
  }

}
