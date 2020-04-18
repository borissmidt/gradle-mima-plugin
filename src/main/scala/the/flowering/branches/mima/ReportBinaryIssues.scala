package the.flowering.branches.mima

import java.io.File

import com.typesafe.tools.mima.core.util.log.Logging
import com.typesafe.tools.mima.core.{Problem, ProblemFilter, ProblemFilters}
import com.typesafe.tools.mima.lib.MiMaLib
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.{Property, SetProperty}
import org.gradle.api.tasks.{CompileClasspath, Input, SourceSet, TaskAction}
import org.gradle.api.{DefaultTask, GradleException}
import org.gradle.internal.exceptions.Contextual
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.tools.nsc.classpath.AggregateClassPath
import scala.tools.nsc.util.ClassPath
@Contextual
case class MiMaException(message: String, cause: Throwable)
    extends GradleException(message, cause)

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
    case "forward" | "forwards"   => Some(Forward)
    case "both"                   => Some(Both)
    case _                        => None
  }
}

class ReportBinaryIssues extends DefaultTask {
  private val log = getProject.getLogger
  private val wrappedLogger = new Logging{
    override def info(str: String): Unit = log.info(str, "")

    override def debugLog(str: String): Unit = log.debug(str, "")

    override def warn(str: String): Unit = log.warn(str, "")

    override def error(str: String): Unit = log.error(str, "")
  }

  def objects = getProject.getObjects
  private val failOnException: Property[Boolean] =
    objects.property(classOf[Boolean])
  private val exclude: SetProperty[Exclude] =
    objects.setProperty(classOf[Exclude])
  private val reportSignatureProblems: Property[Boolean] =
    objects.property(classOf[Boolean])
  private val direction: Property[String] = objects.property(classOf[String])
  private val previousArtifact: Property[FileCollection] =
    objects.property(classOf[FileCollection])
  private val currentArtifact: Property[FileCollection] =
    objects.property(classOf[FileCollection])


  @Input def getPreviousArtifact(): Property[FileCollection] =
    previousArtifact

  @CompileClasspath def getCurrentArtifact(): Property[FileCollection] =
    currentArtifact

  @Input
  def getExclude(): SetProperty[Exclude] = exclude
  @Input
  def getReportSignatureProblems(): Property[Boolean] = reportSignatureProblems

  @Input
  def getDirection(): Property[String] = direction
  @Input
  def getFailOnException(): Property[Boolean] = failOnException

  @TaskAction
  def execute() = {
    val direction = Direction
      .unapply(this.direction.get())
      .getOrElse(
        throw MiMaException(
          "direction needs to be one of backward | forward | both",
          new IllegalArgumentException()
        )
      )
    val failOnException = this.failOnException.get()

    val filters = this.exclude
      .get()
      .asScala
      .flatMap(
        pt =>
          pt.packages
            .get()
            .asScala
            .map(ProblemFilters.exclude(pt.problemType, _))
      )
      .toList

    val previousArtifact = this.previousArtifact
      .get()
      .asScala
      .headOption
      .getOrElse(
        throw MiMaException(
          "missing artifact setting",
          new IllegalArgumentException()
        )
      )

    //todo get project artifact
    reportErrors(
      direction,
      failOnException,
      filters,
      previousArtifact,
      currentArtifact
        .get()
        .getFiles
        .asScala
        .toSet
    )
  }

  private def reportErrors(direction: Direction,
                           failOnError: Boolean,
                           filters: List[ProblemFilter],
                           previousArtifact: File,
                           currentArtifact: Set[File]) = {
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
      runMima(AggregateClassPath.createAggregate(), direction, previousArtifact, currentArtifact.head)

    val bcErrors = bcProblems.filter(isReported("current"))
    val fcErrors = fcProblems.filter(isReported("other"))
    val count = bcErrors.length + fcErrors.length
    val filteredCount = bcProblems.length + fcProblems.length - bcErrors.length - fcErrors.length
    val filteredMsg =
      if (filteredCount > 0) s" (filtered $filteredCount)" else ""

    log.debug(s"direction: ${direction}", "")
    log.debug(s"previousArtifact: ${previousArtifact}", "")
    log.debug(s"currentArtifact: ${currentArtifact}", "")
    log.debug(
      s"Found $count potential binary incompatibilities while checking against $filteredMsg",
      ""
    )

    log.info(
      s"Found $count potential binary incompatibilities while checking against $filteredMsg",
      ""
    )

    log.lifecycle(
      s"forwardErrors: ${fcErrors.map(_.description(getProject.getExtensions.findByType(classOf[MimaExtension]).oldVersion.get())).mkString("\n")}"
    )
    log.lifecycle(
      s"backwardErrors: ${bcErrors.map(_.description(getProject.getExtensions.findByType(classOf[MimaExtension]).oldVersion.get())).mkString("\n")}"
    )
    if (count > 0) {
      log.warn("found binary incompatibilities")
      if (failOnError)
        throw MiMaException(
          "found binary incompatibilities",
          new RuntimeException()
        )
    }
  }

  private def collectProblems(cp: ClassPath, oldJar: File, newJar: File) = {
    () =>
      new MiMaLib(cp, wrappedLogger)
        .collectProblems(oldJar, newJar)
  }

  private def runMima(classpath: ClassPath,
                      direction: Direction,
                      prevJar: File,
                      newJar: File): (List[Problem], List[Problem]) = {
    val checkBC = collectProblems(classpath, prevJar, newJar)
    val checkFC = collectProblems(classpath, newJar, prevJar)

    direction match {
      case Direction.Backward => (checkBC(), Nil)
      case Direction.Forward  => (Nil, checkFC())
      case Direction.Both     => (checkBC(), checkFC())
    }
  }

}
