package s.com.eoi.util

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `no-cache`}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.{Directive1, _}
import s.com.eoi.ITOAInfo
import s.com.eoi.common.HeaderConst
import s.com.eoi.conf.CommitInfoConf

/**
  * Created by jacky on 16/10/25.
  */
trait CorsSupport {
  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    mapResponseHeaders {
      headers =>
        //        `Access-Control-Allow-Origin`.* +:
        //          `Access-Control-Allow-Headers`("Authorization", HeaderConst.Auth, "Content-Type", "X-Requested-With", "Origin", "X-Requested-With", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent", "kbn-version") +:
        `Cache-Control`(`no-cache`, `max-age`(0)) +:
          RawHeader("x-itoa-version", ITOAInfo.currentVersion) +:
          RawHeader("x-itoa-commitId", CommitInfoConf().commitId) +:
          RawHeader("x-itoa-tagName", CommitInfoConf().tagName) +:
          RawHeader("x-itoa-commitTime", CommitInfoConf().commitTime) +:
          RawHeader("x-itoa-buildTime", CommitInfoConf().buildTime) +:
          headers
    }
    /*
    respondWithHeaders(
      `Access-Control-Allow-Origin`(HttpOriginRange.*),
      `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With", "Origin", "X-Requested-With", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent", "kbn-version"),
      `Access-Control-Max-Age`(1728000),
      `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)
    )
    */
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(OK)
      //.withHeaders(`Access-Control-Allow-Origin`(HttpOriginRange.*))
      .withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE))
      //.withHeaders(`Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With", "Origin", "X-Requested-With", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent", "kbn-version"))
      //.withHeaders(`Access-Control-Max-Age`(1728000))
    )
  }

  def corsHandler(r: Route) = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }

  def extractEoiAuth(headParam: String): Directive1[Option[String]] = {
    optionalHeaderValueByName(headParam).flatMap {
      case Some(auth) =>
        provide(Some(auth))
      case None =>
        parameterMap.flatMap { paramKV =>
          paramKV.get(HeaderConst.Auth) match {
            case Some(auth) =>
              provide(Some(auth))
            case None =>
              provide(None)
          }
        }
    }
  }
}