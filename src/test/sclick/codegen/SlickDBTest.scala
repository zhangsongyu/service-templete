package sclick.codegen

import java.util.regex.Pattern

import org.junit.Test
import s.com.eoi.conf.MgrDBConf



class SlickDBTest {

  @Test
  def testSourceCodeGenerator = {
    val dbConf = MgrDBConf()
    val jdbcUrl = dbConf.drives().getOrElse("")
    val dbUser = dbConf.user().getOrElse("")
    val dbPassword = dbConf.password().getOrElse("")
    val as = Array[String](//"jdbc:mysql://192.168.31.46:3306/itoaManagement?user=root&password=User@123",
      "slick.jdbc.MySQLProfile", //slick.driver.MySQLDriver,JdbcProfile
      "com.mysql.jdbc.Driver", //com.mysql.jdbc.Driver,MysqlDataSource,com.mysql.cj.jdbc.Driver
      jdbcUrl, "./", "dao", dbUser, dbPassword, "true", "slick.codegen.SourceCodeGenerator", "true")
    slick.codegen.SourceCodeGenerator.main(as)
  }


  @Test
  def testViewDataByFile: Unit = {
    val testData =
      """
        | >>> 开始从本地获取'/yzyh/resources/config/Channel.xml'
        | @@@ Acceptor0.Processor2.Worker478:ResourcePlugin:getLocalFile
        |
        | 17 十一月 2016 09:45:50,541  INFO 1479201472996|1462 frame.login.Login
        | >>> ==2==colConditionMap:{tellerid=101006}
        | @@@ Acceptor0.Processor2.Worker478:101006:info
        |
        | 17 十一月 2016 09:45:51,546 DEBUG 1479201472996|1462 frame.login.Login
        | >>> 开始从本地获取'resources/config/Channel.xml'
        | @@@ Acceptor0.Processor2.Worker478:ResourcePlugin:getLocalFile
        |
        | 17 十一月 2016 09:45:52,546 DEBUG 1479201472996|1462 frame.login.Login
        | >>> 无法从本地找到resources/config/Channel.xml
        | @@@ Acceptor0.Processor2.Worker478:ResourcePlugin:getLocalFile
        |
        | 17 十一月 2016 09:45:53,547 DEBUG 1479201472996|1462 frame.login.Login
        | >>> 成功加载class: 'frame.login.Login'. hashCode:2075866512
        | @@@ Acceptor0.Processor2.Worker478:ProjectClassLoader:loadClass
        |
        | 17 十一月 2016 09:45:54,547 DEBUG 1479201472996|1462 frame.login.Login
        | >>> 开始从本地获取'/yzyh/resources/config/Channel.xml'
        | @@@ Acceptor0.Processor2.Worker478:ResourcePlugin:getLocalFile
        |
      """.stripMargin
    val src = testData.split("\n")
    var ret = List[String]()
    var temp = List[String]()
    val reg = Pattern.compile("""^ \d{2} \S* \d{4} \d{2}:\d{2}:\d{2},\d+""")
    src.foreach {
      s => {
        val mp = reg.matcher(s)
        if (mp.find) {
          if (reg.matcher(temp.head).find()) ret :+= temp.mkString("\n")
          temp = List[String]()
          temp :+= s
        } else {
          temp :+= s
        }
      }
    }
    ret.foreach(println)
    println()
  }


}