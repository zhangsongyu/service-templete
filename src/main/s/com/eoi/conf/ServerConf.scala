package s.com.eoi.conf

/**
  * Created by jacky on 16/11/14.
  */
class ServerConf() extends ClientConf[ServerConf] {

  val configs = getConfigs("server")

  def ip(): Option[String] = get("ip")

  def port(): Option[Int] = get[Int]("port")

  def urlSecret(): Option[String] = get[String]("urlSecret")

  def sessionTimeoutMinute(): Int = get[Int]("sessionTimeout").getOrElse(20)

  def groupId(): String = get[String]("groupId").getOrElse("itoa")

  def isRedis: Boolean = get[Boolean]("isRedis").getOrElse(true)

  def idCount: Int = get[Int]("idCount").getOrElse(50)
}

object ServerConf {
  def apply() = new ServerConf()
}
