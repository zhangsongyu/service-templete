package s.com.eoi.conf

/**
  * Created by zhi on 2016/12/14.
  */
class MgrDBConf extends ClientConf[MgrDBConf] {

  val configs = getConfigs("dbConnection")

  def drives() = get[String]("drives")

  def user() = get[String]("user")

  def password() = get[String]("password")

  def maxConnections() = get[Int]("maxConnections")
}

object MgrDBConf {
  def apply() = new MgrDBConf()
}
