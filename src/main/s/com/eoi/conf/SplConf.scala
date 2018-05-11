package s.com.eoi.conf

/**
  * Created by zhi on 2017/8/25.
  */
class SplConf extends ClientConf[SplConf] {

  val configs = getConfigs("spl")

  def url(): String = get("url").getOrElse("")

  def isCheckPermission(): Boolean = get[Boolean]("isCheckPermission").getOrElse(true)

  def requestTimeout = get[Int]("requestTimeout").getOrElse(20)

  def dataSetHealthLevelIndex = get[String]("dataSetHealthLevelIndex").getOrElse("kafka_offset/producer")

}

object SplConf {
  def apply() = new SplConf()
}
