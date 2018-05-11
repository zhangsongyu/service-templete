package s.com.eoi.common


import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server._
import s.com.eoi.util.Utils._

import scala.concurrent.Promise

/**
  * Created by zhi on 2017/1/14.
  */
final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
  private implicit val ec = ctx.executionContext

  val request = ctx.request

  def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)

  def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)
}

object ImperativeRequestContext {
  def imperativelyComplete(inner: ImperativeRequestContext => Unit): Route = {
    ctx: RequestContext =>
      val p = Promise[RouteResult]()
      inner(new ImperativeRequestContext(ctx, p))
      p.future
  }
}

case class ConsumerMessage(topic: String, kafkaHttpMessage: KafkaHttpMessage)

case class CtxRequest(req: Any, ctx: ImperativeRequestContext)

case class KafkaHttpMessage(url: String = null,
                            requestTime: String = System.currentTimeMillis().dateFormat(),
                            reqType: String = "POST" /*GET,POST,PUT,DELETE*/ ,
                            path: String = null,
                            params: Map[String, Any] = null,
                            body: Map[String, Any] = null,
                            var meta: Map[String, Any] = null, //"udid" ,"category",必须赋值
                            var result: Any = null)

case class KafkaMessage(topicName: String, kafkaHttpMessage: KafkaHttpMessage, keyOpt: Option[String] = None)