package s.com.eoi

import java.io.File

import akka.actor.{PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.apache.commons.cli._
import org.slf4j.LoggerFactory
import s.com.eoi.common._
import s.com.eoi.conf.{KafkaConf, ServerConf}
import s.com.eoi.routes.HandoverRoute
import s.com.eoi.util._
import s.com.eoi.util.eoiAkka.AkkaMonitorActor
import s.com.eoi.util.eoiAkka.ClusterMonitor

import scala.util.control.Exception._

/**
  * Created by jacky on 16/10/11.
  */

object Service extends CorsSupport {

  import ExecutorService.{mat, system}

  val log = LoggerFactory.getLogger(this.getClass)

  val serverConf = ServerConf()
  val akkaMonitorActor = system.actorOf(Props[AkkaMonitorActor], "akkaMonitorActor") //先于其他actor启动，需要利用name寻找该actor的实例，该actorName不可更改!!!
  val clusterMonitor = system.actorOf(Props[ClusterMonitor], "clusterMonitor")

  def main(args: Array[String]): Unit = {
    import ExecutorService.appServiceExecutionContext
    DatabaseService.checkDBSchema()
    cli(args)
    initLogConfig

    Http().bindAndHandle(corsHandler(route), serverConf.ip().getOrElse("0.0.0.0"), serverConf.port().getOrElse(9876))
      .onFailure {
        case ex: Exception =>
          log.error(s"start app error,bind to ${serverConf.ip()} ${serverConf.port()}", ex)
          throw ex
      }
  }

  def route = {

    val handoverRoute = new HandoverRoute()

    val route = handoverRoute.route
    route
  }

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex: ArithmeticException =>
        extractUri { uri =>
          log.warn(s"uri:[$uri] ,error msg:${ex.getMessage}", ex)
          complete(HttpResponse(InternalServerError, entity = ex.getMessage))
        }
      case ex: HttpNullException =>
        complete(HttpResponse(NotFound, entity = ex.getMessage))
      case ex: HttpAuthException =>
        complete(HttpResponse(Unauthorized, entity = ex.getMessage))
      case ex: Throwable =>
        log.error(s"error msg:${ex.getMessage}", ex)
        complete(HttpResponse(InternalServerError, entity = ex.getMessage))
    }

  private def cli(args: Array[String]) = {
    val opt = new Options()
    opt.addOption(Option.builder("f").argName("foreground").longOpt("foreground").hasArg(false).required(false).build())
    val helpStr = "[-f/--foreground]"
    val cliParser = new DefaultParser()
    catching(classOf[ParseException]) either cliParser.parse(opt, args) match {
      case Left(throwable) =>
        new HelpFormatter().printHelp(helpStr, opt)
      case Right(commandLine) =>
        if (commandLine.hasOption("f")) {
          log.info("[Start With Foreground Mode]")
        } else {
          System.out.close()
          System.err.close()
          log.info("[Start With Background Mode]")
        }
    }
  }

  private def initLogConfig(): Unit = {
    val logConfig = new File("./logback.xml")
    if (logConfig.exists() && logConfig.canRead) {
      val logContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      val config = new JoranConfigurator()

      config.setContext(logContext)
      logContext.reset()
      config.doConfigure(logConfig)
    }
  }
}