package s.com.eoi.conf

class QuartzConf extends ClientConf[QuartzConf] {
  val configs = getConfigs("akka.quartz", "quartz.conf")

  val everyDaySeparateHoldColdData: String = get("schedules.everyDaySeparateHoldColdData.expression").get
  val everyDayCreateIndex: String = get("schedules.everyDayCreateIndex.expression").get
  val lastDayOfYearCreateIndex: String = get("schedules.lastDayOfYearCreateIndex.expression").get
  val lastDayOfMonthCreateIndex: String = get("schedules.lastDayOfMonthCreateIndex.expression").get
  val lastDayOfQuarterCreateIndex: String = get("schedules.lastDayOfQuarterCreateIndex.expression").get
}

object QuartzConf {
  def apply(): QuartzConf = new QuartzConf()
}