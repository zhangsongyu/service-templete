package s.com.eoi.common

/**
  * Created by wangdi on 17/2/27.
  */
class SqlRunException(message: String = "", cause: Throwable = null)
  extends RuntimeException(message, cause)

case class SqlRunSetUniqueRepeatException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

case class SqlRunNotExistException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

case class SqlRunExistException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

case class SqlRunUpdateNoneException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

// 无权限不足
case class SqlRunIllegalArgumentException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

case class SqlRunDeleteInUsedException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)

//////////////////////////////////////
class ItoaHttpException(message: String = "", cause: Throwable = null)
  extends RuntimeException(message, cause)

case class HttpNullException(message: String = "", cause: Throwable = null)
  extends ItoaHttpException(message, cause)

//非法访问
case class HttpAuthException(message: String = "", cause: Throwable = null)
  extends ItoaHttpException(message, cause)

//////////////////////////////////////
case class TypeMismatchException(message: String = "", cause: Throwable = null)
  extends RuntimeException(message, cause) {}

case class ParameterException(msg: String, cause: Throwable = null)
  extends RuntimeException(msg, cause) //参数错误

case class DoubleConfirmException(msg: String = "", cause: Throwable = null)
  extends RuntimeException(msg, cause) //再确认

case class SqlOtherException(message: String = "", cause: Throwable = null)
  extends SqlRunException(message, cause)
