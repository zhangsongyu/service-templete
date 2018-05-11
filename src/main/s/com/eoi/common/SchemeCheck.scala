package s.com.eoi.common

trait SchemeCheck[T] {
  def check(x: T): Either[ParameterException, T]
}

object SchemeCheck {

  implicit class SchemeCheckOps[A](x: A) {
    def check(implicit schemeCheck: SchemeCheck[A]): Either[ParameterException, A] = {
      schemeCheck.check(x)
    }
  }

}