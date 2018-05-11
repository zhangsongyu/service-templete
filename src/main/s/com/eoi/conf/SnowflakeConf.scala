package s.com.eoi.conf

class SnowflakeConf extends ClientConf[SnowflakeConf] {

  val configs = getConfigs("snowflake")

  def worker_id(): Option[Int] = get[Int]("worker_id")

  def datacenter_id(): Option[Int] = get[Int]("datacenter_id")

}

object SnowflakeConf {
  def apply() = new SnowflakeConf()
}
