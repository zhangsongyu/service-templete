package s.com.eoi.conf

/**
  * Created by enze on 2017/7/7.
  */

class QuartzSchedulerConf extends ClientConf[QuartzSchedulerConf] {
  val configs = getConfigs("itoa.scheduler", "quartz.conf")

  lazy val NO_OF_INSTANCE: String = get("noOf.instance").get

  lazy val NAME_BAKJOB_ACTOR: String = get("actors.name.bakjob").get
  lazy val CRON_BAKJOB_EXPRESSION: String = get("cron.bakjob").get
  lazy val NAME_RESTOREPOLL_ACTOR: String = get("actors.name.restorepoll").get
  lazy val CRON_RESTOREPOLL_EXPRESSION: String = get("cron.restorepoll").get
  lazy val NAME_TASKPOLL_ACTOR: String = get("actors.name.taskpoll").get
  lazy val CRON_TASKPOLL_EXPRESSION: String = get("cron.taskpoll").get

}

object QuartzSchedulerConf {
  def apply(): QuartzSchedulerConf = new QuartzSchedulerConf()
}
