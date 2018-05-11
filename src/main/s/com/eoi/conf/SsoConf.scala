package s.com.eoi.conf

class SsoConf extends ClientConf[SsoConf] {
  val configs = getConfigs("sso")

  def classname = get("classname").getOrElse("s.com.eoi.service.user.SSODefaultServiceImp")

  def authserver = get("authserver").getOrElse("http://localhost")

  def isRawLogin = get("isRawLogin").getOrElse(true)
}

object SsoConf {
  def apply() = new SsoConf()
}
