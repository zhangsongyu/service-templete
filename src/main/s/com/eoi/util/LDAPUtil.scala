package s.com.eoi.util

import java.util
import javax.naming.directory.{InitialDirContext, SearchControls}
import javax.naming.{AuthenticationException, Context}

import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}


/**
  * Created by zhi on 2017/7/11.
  */
object LDAPUtil {
  private val factory = "com.sun.jndi.ldap.LdapCtxFactory"
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val setTimeoutKey = "com.sun.jndi.ldap.read.timeout"
  private val timeout = "1000"
  private val authentication = "simple" // "none", "simple", "strong"

  implicit val ec = ExecutorService.configServiceExecutionContext

  def isConnect(admin: String, pwd: String, baseDN: String, ldapUrl: String): Boolean = {
    null != ldapConnect(admin, pwd, baseDN, ldapUrl)
  }

  def login(userName: String, password: String, admin: String, pwd: String, baseDN: String, ldapUrl: String): Boolean = {
    val ret = Try {
      val ctx = ldapConnect(admin, pwd, baseDN, ldapUrl)
      if (ctx != null) {
        val controls = new SearchControls()
        controls.setTimeLimit(2000)
        controls.setReturningAttributes(Array[String]("givenName", "sn", "memberOf", "cn"))
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE | SearchControls.ONELEVEL_SCOPE)

        val answers = ctx.search(baseDN, s"sAMAccountName=$userName", controls)
        if (answers != null && answers.hasMore) {
          val obj = answers.nextElement()
          val userDN = obj.getNameInNamespace

          val env = new util.Hashtable[String, String]
          env.put(Context.INITIAL_CONTEXT_FACTORY, factory)
          env.put(Context.PROVIDER_URL, ldapUrl) // "LDAP://192.168.31.137:389/"
          env.put(Context.SECURITY_AUTHENTICATION, authentication) // "none", "simple", "strong"
          env.put(setTimeoutKey, timeout)
          env.put(Context.SECURITY_PRINCIPAL, userDN)
          env.put(Context.SECURITY_CREDENTIALS, password)
          new InitialDirContext(env)
          true
        } else false
      } else false
    }
    ret match {
      case Success(r) => r
      case Failure(r) =>
        logger.error("Ldap验证错误", r)
        false
    }
  }

  private def ldapConnect(admin: String, pwd: String, baseDN: String, ldapUrl: String) = {
    val principal = s"cn=$admin,$baseDN"
    val env = new util.Hashtable[String, String]
    env.put(Context.INITIAL_CONTEXT_FACTORY, factory)
    env.put(Context.PROVIDER_URL, ldapUrl) // "LDAP://192.168.31.137:389/"
    env.put(Context.SECURITY_AUTHENTICATION, authentication) // "none", "simple", "strong"
    env.put(setTimeoutKey, timeout)
    env.put(Context.SECURITY_PRINCIPAL, principal)
    env.put(Context.SECURITY_CREDENTIALS, pwd)
    try {
      Await.result(Future[InitialDirContext](new InitialDirContext(env)), 2 second)
    }
    catch {
      case e: AuthenticationException =>
        logger.error(s"LDAP login authentication error[${admin}] ldap:[$ldapUrl]")
        null
      case e: Exception =>
        logger.error(s"LDAP login error[$admin] ldap:[$ldapUrl]", e)
        null
    }
  }
}
