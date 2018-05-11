package s.com.eoi.util.mail
//created by Nthan蒋
//mail用法在测试类中
import java.util.Properties
import javax.mail.{Session => MailSession}

import scala.concurrent.ExecutionContext

object Defaults {
  val session = MailSession.getDefaultInstance(new Properties())

  implicit val executionContext = ExecutionContext.Implicits.global
}