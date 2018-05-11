package s.com.eoi.util
//created by Nthan蒋
//mail用法在测试类中
import javax.mail.internet.InternetAddress

/** An agreeable email interface for scala. */
package object courier {

  implicit class addr(name: String){
    def `@`(domain: String): InternetAddress = new InternetAddress(s"$name@$domain")
    def at = `@` _
    /** In case whole string is email address already */
    def addr = new InternetAddress(name)
  }
}