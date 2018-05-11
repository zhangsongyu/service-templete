package s.com.eoi.util

import java.security.{KeyPairGenerator, SecureRandom}
import java.util.Base64
import javax.crypto.spec.DESKeySpec
import javax.crypto.{Cipher, SecretKeyFactory}

import sun.security.rsa.{RSAPrivateCrtKeyImpl, RSAPublicKeyImpl}

//created by Nathan蒋航 2017.8.3
object DES {
  //秘钥可以使用UUID随机生成就行 :)
  private[this] val password = "bbbb31e7-cea1-41cb-92f2-4f17b74ca803" //这是秘钥

  def encrypt(source: String): String = {
    //加密
    val secureRandom = new SecureRandom()
    val dks = new DESKeySpec(password.getBytes())
    val keyFactory = SecretKeyFactory.getInstance("DES")
    val secretKey = keyFactory.generateSecret(dks)
    val cipher = Cipher.getInstance("DES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, secureRandom)
    Base64.getEncoder.encodeToString(cipher.doFinal(source.getBytes()))
  }

  //解密
  def decrypt(source: String): String = {
    val secureRandom = new SecureRandom()
    val dks = new DESKeySpec(password.getBytes())
    val keyFactory = SecretKeyFactory.getInstance("DES")
    val secretKey = keyFactory.generateSecret(dks)
    val cipher = Cipher.getInstance("DES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, secureRandom)
    new String(cipher.doFinal(Base64.getDecoder.decode(source)))
  }
}

object RSA {
  //公钥和私钥都是由Base64处理过的
  //公钥加密与私钥解密一对
  //私钥加密与公钥解密一对
  //不存在诸如公钥加密公钥解密这种情况
  private[this] val publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAgBTMNaUm9PQ2oKr3vohh2zLki5i60sQ+14sX9wWZmsPl49t2EqvzT+PuitIFGrfZWtcf+HCYX3y3OMtiJKOaAmvJgxz9MjMbZRjUIHvI7cKiy6FvwQACRuE5Nd19fzUX9HlF1jf/bck6D75zEAIXTynKF1NX1ydC0KnTARn8/QIDAQAB"
  private[this] val privateKey = "MIICdAIBADANBgkqhkiG9w0BAQEFAASCAl4wggJaAgEAAoGBAICAFMw1pSb09Dagqve+iGHbMuSLmLrSxD7Xixf3BZmaw+Xj23YSq/NP4+6K0gUat9la1x/4cJhffLc4y2Iko5oCa8mDHP0yMxtlGNQge8jtwqLLoW/BAAJG4Tk13X1/NRf0eUXWN/9tyToPvnMQAhdPKcoXU1fXJ0LQqdMBGfz9AgMBAAECgYAqArXJ9Zfi3p6g5XI/wMjxxSokUgdCvPlv2iQdV6M7DsAQ/qeXOI+C4pGQQU5cYNnOc8Z1K1IUXtN1S1oi4fuiOoncqQvupouTUX37863wAH+kwoYw2OT8gVRYs5wgCEHOyk8pbaZgjdR3d+4MRzZ3iYymB0pP33P8CKnWrwc0gQJBAOPhpnk8o+4gG7rP7i7j5fDvMv8FD8ZilRn9kTBUs9Ka60YcAFOrezhyBN65+BTZhH4tZ82JrRJaGcJu/BKp6bECQQCQWyqgcHJHj3OlAxSFfPu9JZOqWzGPQFWDq4w0+3hNl8W8IQ5lR0NHqCcQZ6vJNN7/S2PLX28/Ah7YG0Gwc88NAkBvo/31yVKad5p+NB4BVN8y4Uz/iRBIZcWcPRCjN+v9Eq6O42r5krpEVf+yyjMSq9nwIviGxptGsWOLUOonjpyRAj9mEA8TYG1sKwCYqpTXzIYwNWEyEUf5AIN1lNSMoT9tnOExIi4LbVckUr9L2gCbQcZoueTU5aTB573nDV5kY30CQA4m6sH7y3OJlQAaHv7Owvtyd7k6zoCxELqxp2gVjmqTMAGjlOgiHYA4PK7M4kVdHz1AMnEYx7lqOW4mzVRXcDk="

  private def generateKeyPair = {
    //返回一个Map，k分别为PublicKey和PrivateKey
    val kpg = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(1024, new SecureRandom())
    val kp = kpg.generateKeyPair()
    val public_key = kp.getPublic
    val private_key = kp.getPrivate
    Map("PublicKey" -> Base64.getEncoder.encodeToString(public_key.getEncoded), "PrivateKey" -> Base64.getEncoder.encodeToString(private_key.getEncoded))
  }

  //公钥加密
  def encrypt1(source: String) = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, new RSAPublicKeyImpl(Base64.getDecoder.decode(publicKey)))
    Base64.getEncoder.encodeToString(cipher.doFinal(source.getBytes))
  }

  //私钥解密
  def decrypt1(source: String) = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, RSAPrivateCrtKeyImpl.newKey(Base64.getDecoder.decode(privateKey)))
    new String(cipher.doFinal(Base64.getDecoder.decode(source)))
  }

  //私钥加密
  def encrypt2(source: String) = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, RSAPrivateCrtKeyImpl.newKey(Base64.getDecoder.decode(privateKey)))
    Base64.getEncoder.encodeToString(cipher.doFinal(source.getBytes()))
  }

  //公钥解密
  def decrypt2(source: String) = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, new RSAPublicKeyImpl(Base64.getDecoder.decode(publicKey)))
    new String(cipher.doFinal(Base64.getDecoder.decode(source)))
  }
}