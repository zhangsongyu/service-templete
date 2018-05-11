package s.com.eoi.util

object StringUtils {

  implicit class StringOPS(raw: String) {
    def isJsonStringArrayLiteral = { //是不是Json的String的数组的字符串字面量
      "\\[(?:(?:\"(.*)\")[,]?)*\\]".r.pattern.matcher(raw).matches()
    }
  }
}