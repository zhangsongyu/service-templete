package s.com.eoi.util

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import akka.http.scaladsl.server.{Route, RouteResult}
import org.slf4j.LoggerFactory


object HttpLogHelper {
  private val log = LoggerFactory.getLogger(this.getClass)

  def requestMethodAndResponseStatusAsInfo(requestId: String)(req: HttpRequest): RouteResult => Unit = {
    case RouteResult.Complete(res) =>
      log.info(
        s"""|
            |[requestId]:[${requestId}]
            |[request]
            |  uri:[${req.uri}] method:[${req.method.value}]
            |  headers:[${req.headers.mkString("\n           ")}]
            |  entity:[${req.entity}]
            |[response]
            |  headers:[${res.headers.mkString("\n           ")}]
            |  entity:[${res.entity.httpEntity}]
            |""".stripMargin)
    case RouteResult.Rejected(rejections) =>
      log.warn(
        s"""|
            |[requestId]:[${requestId}]
            |[request]
            |  uri:[${req.uri}] method:[${req.method.value}]
            |  headers:[${req.headers.mkString("\n           ")}]
            |  entity:[${req.entity}]
            |[rejection]
            |  rejection reason:[${rejections.mkString(" , ")}]
            |""".stripMargin)
    case default =>
      log.error(s"""无法匹配到`RouterResult.Complete`和`RouterResult.Rejection`,$default , HttpLogHelper""")
      None
  }

  def logRequestAndResponse(requestId: String) = DebuggingDirectives.logRequestResult(LoggingMagnet(_ =>requestMethodAndResponseStatusAsInfo(requestId)))

  def apply(r: Route)(requestId: String) = {
    logRequestAndResponse(requestId)(r)
  }
}