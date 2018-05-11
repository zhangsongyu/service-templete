package s.com.eoi.common

import org.slf4j.LoggerFactory


/**
  * Created by zhi on 2017/5/18.
  */
trait ServiceCommon {
  def respSuccess(retCode: String, retMessage: String, entity: Any, totalCount: Int = 1, aux: Option[Any] = None) = Map("retCode" -> retCode,
    "retMsg" -> retMessage, "entity" -> entity, "totalCount" -> totalCount, "aux" -> aux)

  def respFail(retCode: String, retMessage: Any, aux: Option[Any] = None) = Map("retCode" -> retCode,
    "retMsg" -> retMessage, "aux" -> aux)
}

object ServiceCommon extends ServiceCommon {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def slickException(ex: Throwable, errorMsg: String = "") = {
    ex match {
      case _: SqlRunSetUniqueRepeatException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.notUnique, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: SqlRunNotExistException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.notExist, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: SqlRunExistException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.alreadyExist, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: SqlRunUpdateNoneException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.notNull, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: SqlRunIllegalArgumentException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.noRights, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: SqlRunDeleteInUsedException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.delInUsed, s"数据库操作失败$errorMsg, ${ex.getMessage}")

      case _: ParameterException =>
        logger.warn("[parameter warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.paramsWarn, ex.getMessage) //参数错误

      case _: DoubleConfirmException =>
        logger.warn("[parameter warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.needConfirm, ex.getMessage) //需要确认

      case _: SqlOtherException =>
        logger.warn("[DB warn]", s"$errorMsg, ${ex.getMessage}")
        respFail(MsgCode.other, ex.getMessage)

      case _ =>
        logger.error(s"[DB Error:$errorMsg ]", ex)
        respFail(MsgCode.other, s"数据库操作失败$errorMsg, ${ex.getMessage}")
    }
  }
}

case class UpdateStatus(status: Int)