package s.com.eoi.common

/**
  * Created by zhi on 2017/2/22.
  */
object ConfigConst {
  val passwordSalt = "448DDD517D3ABB70045AEA6929F02367"
}

object HeaderConst {
  val Auth = "X-EOI-Auth"
  val SIGN = "SIGN"
  val Referer = "Referer"
}

object MsgCode {
  //处理成功返回码
  val success = "0000"

  //有重复记录返回码,{*}记录重复
  val notUnique = "0010"

  //不存在记录的返回码,{*}记录不存在
  val notExist = "0020"
  // 已存在
  val alreadyExist = "0021"

  //不允许为空的返回码
  val notNull = "0030"

  //类型不匹配的返回码或者数据输入不一致
  val notMatch = "0040"

  //超过最大长度的返回码
  val overLength = "0050"

  //超时
  val timeOut = "0060"
  //下发交互超时
  val kafkaTimeOut = "0061"

  //数据库插入错误情况返回码  01**
  val addError = "0100"

  //数据库删除错误情况返回码  02**
  val delError = "0200"
  val delInUsed = "0210"

  //数据库修改错误情况返回码  03**
  val updateError = "0300"
  val needConfirm = "0320"  //需要确认
  val queryError = "0400" //连接测试失败也用这个

  //参数错误
  val paramsWarn = "0500"

  // 其他
  val other = "0900"

  // 非法访问
  val unAuthorized = "5000"
  // 没有权限，权限不够
  val noRights = "6000"

  //上传文件失败
  val uploadFileError = "10000"
}

object RedisConst {
  val userPrefix = "USER_"
  val loginUser = "loginUser_"
}

object SearchCollectTypeConst {
  val contentEvent = "event"
  val contentChart = "chart"
  val contentTable = "table"
  val contentChartTable = "chart-table"

  val searchTypeEvent = "event"
  val searchTypeAggregation = "aggregation"

  def getContent(source: Option[String]) = {
    source match {
      case Some("event") => contentEvent
      case Some("chart") => contentChart
      case Some("table") => contentTable
      case Some("chart-table") => contentChartTable
      case None => contentEvent
      case _ => throw new TypeMismatchException()
    }
  }

  def getSearchType(source: Option[String]) = {
    source match {
      case Some("event") => searchTypeEvent
      case Some("aggregation") => searchTypeAggregation
      case None => searchTypeEvent
      case _ => throw new TypeMismatchException()
    }
  }
}

object BusinessStatus {
  val on = 1
  val off = 0
}

object GlobalStatus {
  val ENABLE = 1
  val DISABLE = 0
}

object KafkaHttpMessageReqTypeConst {
  val GET = "GET"
  val POST = "POST"
  val PUT = "PUT"
  val DELETE = "DELETE"
}

object FileTypeConst {
  val CSV = "csv"
  val EXCEL = "excel"
  val TXT = "txt"
  val JSON = "json"
}