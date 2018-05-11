package s.com.eoi.conf

import java.util
import java.util.ArrayList

/**
  * Created by zhi on 2017/5/4.
  */

class KafkaTopicConf extends ClientConf[KafkaTopicConf] {

  val configs = getConfigs("topic")

  val ITOA_TO_GW_Req: String = get("itoa_to_gw_req").getOrElse("ITOA_TO_GW_REQ")
  val ITOA_TO_GW_Rsp: String = get("itoa_to_gw_rsp").getOrElse("ITOA_TO_GW_RSP")
  val GW_TO_ITOA_Req: String = get("gw_to_itoa_req").getOrElse("GW_TO_ITOA_REQ")
  val GW_TO_ITOA_Rsp: String = get("gw_to_itoa_rsp").getOrElse("GW_TO_ITOA_RSP")

  val ITOA_TO_SM_Req: String = get("itoa_to_sm_req").getOrElse("ITOA_TO_SM_REQ")
  val ITOA_TO_SM_Rsp: String = get("itoa_to_sm_rsp").getOrElse("ITOA_TO_SM_RSP")
  val SM_TO_ITOA_Req: String = get("sm_to_itoa_req").getOrElse("SM_TO_ITOA_REQ")
  val SM_TO_ITOA_Rsp: String = get("sm_to_itoa_rsp").getOrElse("SM_TO_ITOA_RSP")

  val ITOA_TO_HUB_Req: String = get("itoa_to_hub_req").getOrElse("ITOA_TO_HUB_REQ")
  val ITOA_TO_HUB_Rsp: String = get("itoa_to_hub_rsp").getOrElse("ITOA_TO_HUB_RSP")
  val HUB_TO_ITOA_Req: String = get("hub_to_itoa_req").getOrElse("HUB_TO_ITOA_REQ")
  val HUB_TO_ITOA_Rsp: String = get("hub_to_itoa_rsp").getOrElse("HUB_TO_ITOA_RSP")

  val ITOA_TO_ALARM_Req: String = get("itoa_to_alarm_req").getOrElse("ITOA_TO_ALARM_REQ")
  val ITOA_TO_ALARM_Rsp: String = get("itoa_to_alarm_rsp").getOrElse("ITOA_TO_ALARM_RSP")
  val ALARM_TO_ITOA_Req: String = get("alarm_to_itoa_req").getOrElse("ALARM_TO_ITOA_REQ")
  val ALARM_TO_ITOA_Rsp: String = get("alarm_to_itoa_rsp").getOrElse("ALARM_TO_ITOA_RSP")

  val ITOA_TO_DM_Req: String = get("itoa_to_dm_req").getOrElse("ITOA_TO_DM_REQ")
  val ITOA_TO_DM_Rsp: String = get("itoa_to_dm_rsp").getOrElse("ITOA_TO_DM_RSP")
  val DM_TO_ITOA_Req: String = get("dm_to_itoa_req").getOrElse("DM_TO_ITOA_REQ")
  val DM_TO_ITOA_Rsp: String = get("dm_to_itoa_rsp").getOrElse("DM_TO_ITOA_RSP")

  val ITOA_TO_PM_Req: String = get("itoa_to_pm_req").getOrElse("ITOA_TO_PM_REQ")
  val ITOA_TO_PM_Rsp: String = get("itoa_to_pm_rsp").getOrElse("ITOA_TO_PM_RSP")
  val PM_TO_ITOA_Req: String = get("pm_to_itoa_req").getOrElse("PM_TO_ITOA_REQ")
  val PM_TO_ITOA_Rsp: String = get("pm_to_itoa_rsp").getOrElse("PM_TO_ITOA_RSP")
  val AlarmEventTopic: String = get("alarmEventTopic").getOrElse("eventOut") //告警通知或者脚本执行专用topic

  // 被监听的topics
  val topics = new ArrayList[String](
    util.Arrays.asList(
      ITOA_TO_GW_Rsp, ITOA_TO_DM_Rsp, ITOA_TO_SM_Rsp, ITOA_TO_ALARM_Rsp, ITOA_TO_HUB_Rsp, ITOA_TO_PM_Rsp,
      GW_TO_ITOA_Req, DM_TO_ITOA_Req, SM_TO_ITOA_Req, ALARM_TO_ITOA_Req, HUB_TO_ITOA_Req, PM_TO_ITOA_Req
    )
  )

  // 监听topic为itoa主动请求返回的topic，例如: ITOA_TO_GW_Rsp
  val rspTopics = List(ITOA_TO_GW_Rsp, ITOA_TO_DM_Rsp, ITOA_TO_SM_Rsp, ITOA_TO_ALARM_Rsp, ITOA_TO_HUB_Rsp, ITOA_TO_PM_Rsp)
}

object KafkaTopicConf {
  def apply() = new KafkaTopicConf()

  val topicConf = KafkaTopicConf()

  // 创建需要特殊对待的topic,
  val specialTopics = List(topicConf.ITOA_TO_SM_Req, topicConf.ITOA_TO_ALARM_Req, topicConf.ITOA_TO_PM_Req)
}