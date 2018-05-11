package s.com.eoi.util


object GenRegex2 {
  def apply(source: String, start: Int, end: Int, groupName: String) = canGenerate(source, start, end) match {
    case true => Some(genRegex(source, start, end, groupName))
    case false => None
  }

  def splunkStypeRegexExameIsInterset(list: List[(String, (Int, Int))]) = { //存在交集吗 首尾包含
    //    list.map{case (groupName,(x,y))=>Set(x to (y-1) :_*)}.reduce(_&_).isEmpty.unary_! // 为空的话，就是没有交集咯
    val temp = list.map { case (_, (x, y)) => Set(x to (y): _*) }
    !temp.forall(set => temp.filter(_ != set).forall(inSet => (inSet & set).isEmpty))
  }

  def splunkStypeRegexSplitSourceString(source: String, list: List[(String, (Int, Int))]) = {
    //source.substring(0,4) // 包头不包尾
    //source.substring(1) //包头val
    val array = list.zipWithIndex.toArray
    val EndIndex = array.length - 1 //传入的有几个组 groupName
    val splitTuple = array.map { case ((groupName, (x, y)), index) => index match {
      case 0 => array.length match {
        case 1 => (0, source.length - 1)
        case _ => (0, y)
      }
      case `EndIndex` => (array(index - 1)._1._2._2 + 1, source.length - 1)
      case _ => (array(index - 1)._1._2._2 + 1, y)
    }
    } //(0,y1)::(y1+1,y2)::(y2+1,y3) 但是 如果只有一组 (0,source.length-1) 首尾包含 source按此区间分割
    val splitStrings = splitTuple.map {
      case (x, y) =>
        if (y == source.length - 1)
          source.substring(x, y + 1)
        else
          source.substring(x, y + 2) //故意向后放一位(如果不是文本最末尾的话)
    }.toList // x y首尾包含
    val offsetedXY = array.map { case ((groupName, (x, y)), index) => index match {
      case 0 => (x, y)
      case _ =>
        val lastY = array(index - 1)._1._2._2 //上一个Y的值 应该的偏移量为lastY-0+1
        (x - lastY - 1, y - lastY - 1) //首尾包含
    }
    }
    val groupNames = list.map(_._1)
    splunkZipGenRegex(groupNames, offsetedXY.toList, splitStrings)
  }

  def splunkZipGenRegex(groupNames: List[String], offsetedXY: List[(Int, Int)], splitStrings: List[String]): List[Option[String]] = (offsetedXY, splitStrings, groupNames) match {
    case ((x, y) :: offsetedXYTail, source :: splitStringsTail, groupName :: groupNamesTail) => GenRegex2(source, x, y, groupName) :: splunkZipGenRegex(groupNamesTail, offsetedXYTail, splitStringsTail)
    case (Nil, Nil, Nil) => Nil
  }

  def splunkStypeRegexChangeType(list: List[(String, Any)]): List[(String, (Int, Int))] = { //排序好了
    def loop(list: List[(String, Any)]): List[(String, (Int, Int))] = list match {
      case Nil => Nil
      case h :: t => h match {
        case (groupName, xy: Seq[Int]) => (groupName, (xy.head, xy.tail.head)) :: loop(t)
      }
    }

    loop(list).sortBy { case (_, (x, _)) => x }
  }


  private def genRegex(source: String, start: Int, end: Int, groupName: String): String = { //开头邻接特殊字符 结尾邻接特殊字符
    var result: String = ""
    if (start == 0 && end == source.length - 1) {
      result =s"""^(?<$groupName>.*)"""
      return result
    }
    if (start == 0) {
      //TODO 要考虑利用结尾特殊生成规则
      result = GenRegexUtils.genRegex(new String(source.toCharArray, start, end - start + 1))
      return s"""^(?<$groupName>$result)"""
    }
    if (end == source.length - 1) { //匹配到结尾
      result = isSpecial(charAt(source, start - 1)) match {
        case true =>
          val specialChar = source.charAt(start - 1).toString
          val numOfPattern = source.view(0, start).count(ch => ch.toString == specialChar)
          val strSelected = new String(source.toCharArray, start, end - start + 1)
          s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}(?<$groupName>.*)"""
        case false => //这里又为三种情况 ahead存在特殊字符 与不存在特殊字符
          findAheadSpecial(source, start) match {
            case -1 =>
              s"""^(?:${GenRegexUtils.genRegex(new String(source.toCharArray, 0, start))})(?<$groupName>.*)""" //匹配到结尾
            case 0 =>
              val specialChar = source.charAt(0).toString
              s"""^(?:${GenRegexUtils.genRegex(specialChar)})${GenRegexUtils.genRegex(new String(source.toCharArray, 1, start - 1))}(?<$groupName>.*)"""
            case n =>
              val specialChar = source.charAt(n).toString
              val numOfPattern = source.view(0, n + 1).count(ch => ch.toString == specialChar)
              s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}${GenRegexUtils.genRegex(new String(source.toCharArray, n + 1, start - n - 1))}(?<$groupName>.*)"""
          }
      }
      return result
    }
    val tuple = (isSpecial(charAt(source, start - 1)), isSpecial(charAt(source, end + 1)))
    result = tuple match {
      case (true, true) =>
        val specialChar = source.charAt(start - 1).toString
        val numOfPattern = source.view(0, start).count(ch => ch.toString == specialChar)
        val strSelected = new String(source.toCharArray, start, end - start + 1)
        strSelected.exists(ch => ch == source.charAt(end + 1)) match {
          case true => s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
          case false => s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}(?<$groupName>[^${GenRegexUtils.genRegex(source.charAt(end + 1).toString)}]*)"""
        }
      case (true, false) =>
        val specialChar = source.charAt(start - 1).toString
        val numOfPattern = source.view(0, start).count(ch => ch.toString == specialChar)
        val strSelected = new String(source.toCharArray, start, end - start + 1)
        s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
      case (false, true) =>
        findAheadSpecial(source, start) match {
          case -1 =>
            val strSelected = new String(source.toCharArray, start, end - start + 1)
            strSelected.exists(ch => ch == source.charAt(end + 1)) match {
              case true => s"""^(?:${GenRegexUtils.genRegex(new String(source.toCharArray, 0, start))})(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
              case false => s"""^(?:${GenRegexUtils.genRegex(new String(source.toCharArray, 0, start))})(?<$groupName>[^${GenRegexUtils.genRegex(source.charAt(end + 1).toString)}]*)"""
            }
          case 0 => //该分支没有测试
            val specialChar = source.charAt(0).toString
            val strSelected = new String(source.toCharArray, start, end - start + 1)
            strSelected.exists(ch => ch == source.charAt(end + 1)) match {
              case true => s"""^(?:${GenRegexUtils.genRegex(specialChar)})${GenRegexUtils.genRegex(new String(source.toCharArray, 1, start - 1))}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
              case false => s"""^(?:${GenRegexUtils.genRegex(specialChar)})${GenRegexUtils.genRegex(new String(source.toCharArray, 1, start - 1))}(?<$groupName>[^${GenRegexUtils.genRegex(source.charAt(end + 1).toString)}]*)""" //todo
            }
          case n =>
            val specialChar = source.charAt(n).toString
            val numOfPattern = source.view(0, n + 1).count(ch => ch.toString == specialChar)
            val strSelected = new String(source.toCharArray, start, end - start + 1)
            strSelected.exists(ch => ch == source.charAt(end + 1)) match {
              case true => s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}${GenRegexUtils.genRegex(new String(source.toCharArray, n + 1, start - n - 1))}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
              case false => s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex(specialChar)}){$numOfPattern}${GenRegexUtils.genRegex(new String(source.toCharArray, n + 1, start - n - 1))}(?<$groupName>[^${GenRegexUtils.genRegex(source.charAt(end + 1).toString)}]*)"""
            }
        }
      case (false, false) => findAheadSpecial(source, start) match {
        case -1 =>
          val strSelected = new String(source.toCharArray, start, end - start + 1)
          s"""^(?:${GenRegexUtils.genRegex(new String(source.toCharArray, 0, start))})(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
        case 0 => //该分支没有测试
          val specialChar = source.charAt(0).toString
          val strSelected = new String(source.toCharArray, start, end - start + 1)
          s"""^(?:${GenRegexUtils.genRegex(specialChar)})${GenRegexUtils.genRegex(new String(source.toCharArray, 1, start - 1))}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
        case n =>
          val specialChar = source.charAt(n).toString
          val numOfPattern = source.view(0, n + 1).count(ch => ch.toString == specialChar)
          val strSelected = new String(source.toCharArray, start, end - start + 1)
          s"""^(?:[^${GenRegexUtils.genRegex(specialChar)}]*${GenRegexUtils.genRegex((specialChar))}){$numOfPattern}${GenRegexUtils.genRegex(new String(source.toCharArray, n + 1, start - n - 1))}(?<$groupName>${GenRegexUtils.genRegex(strSelected)})"""
      }

    }
    return result
  }

  //  private def findSpecialFromTailtoHead(source:String,start:Int):String=
  private def charAt(string: String, at: Int) = string.charAt(at).toString

  private def findAheadSpecial(source: String, start: Int): Int = {
    source.view(0, start).zipWithIndex.reverse.find { case (ch, _) => isSpecial(ch.toString) } match {
      case None => -1 //找不到了
      case Some((_, i)) => i //找到咯
    }
  }

  private def canGenerate(source: String, start: Int, end: Int): Boolean = { //判断传入的字符串片段可不可以生成正则表达式 首尾都包含在内
    val len = source.length
    (start, end + 1) match {
      case (0, `len`) => true
      case (0, _) => !isSamePattern(source.charAt(end).toString, source.charAt(end + 1).toString)
      case (_, `len`) => !isSamePattern(source.charAt(start - 1).toString, source.charAt(start).toString)
      case (_, _) => !isSamePattern(source.charAt(start - 1).toString, source.charAt(start).toString) && !isSamePattern(source.charAt(end).toString, source.charAt(end + 1).toString)
    }
  }

  private def isSamePattern(str1: String, str2: String): Boolean = {
    if (isCharacters(str1) && isCharacters(str2)) //是汉字的话，也是相同模式
      return true
    if (isAlphasOrNums(str1) && isAlphasOrNums(str2))
      return true
    if (str1.equals(str2)) //它们之间相等的话，肯定是相同模式咯
      return true
    return false
  }

  private def isSpecial(string: String): Boolean = {
    val Specials ="""[^ 0-9a-zA-Z\u4e00-\u9fa5]+""".r
    return string.matches(Specials.regex)
  }

  private def isNums(string: String): Boolean = {
    val Nums ="""\d+""".r
    return string.matches(Nums.regex)
  }

  private def isAlphas(string: String): Boolean = {
    val Alphas ="""[a-zA-Z]+""".r
    return string.matches(Alphas.regex)
  }

  private def isAlphasOrNums(string: String): Boolean = {
    val AlphasOrNums ="""[a-zA-Z0-9]+""".r
    return string.matches(AlphasOrNums.regex)
  }

  private def isCharacters(string: String): Boolean = {
    val Characters ="""[\u4e00-\u9fa5]+""".r
    return string.matches(Characters.regex)
  }

  object GenRegexUtils {
    val ChineaseStart =s"""${"\\" + "u" + "4e00"}"""
    val ChineaseEnd =s"""${"\\" + "u" + "9fa5"}"""

    private val Nums ="""^(\d+)(.*)$""".r
    //匹配一串数字 +(一个或多个)
    private val AlphasAndNums =
      """^([\w]+)(.*)$""".r
    //匹配一串字母
    private val ChineaseCharacters =
      """^([\u4e00-\u9fa5]+)(.*)$""".r //匹配一串汉字
    private val Space =
      """^([ ]+)(.*)$""".r //匹配一串空格
    private val OtherSpecialCharacter =
      """^([^ 0-9a-zA-Z\u4e00-\u9fa5])(.*)$""".r //不是一个数字 字母 汉字 ,那么只能是特殊字符
    private val ZeroString =
      """^()$""".r //空字符串
    def genRegex(source: String): String = source match {
      case ZeroString(_) =>
        ""
      case ChineaseCharacters(first, rest) =>
        s"""[$ChineaseStart-$ChineaseEnd]+""" + genRegex(rest)
      case AlphasAndNums(first, rest) =>
        """\w+""" + genRegex(rest)
      case OtherSpecialCharacter(first, rest) =>
        val temp = first match {
          case "." => """\."""
          case "?" => """\?"""
          case "*" => """\*"""
          case "+" => """\+"""
          case "[" => """\["""
          case "]" => """\]"""
          case "(" => """\("""
          case ")" => """\)"""
          case ":" => """\:"""
          case """\""" => """\\"""
          case "^" => """\^"""
          case t => t
        }
        temp + genRegex(rest)
      case Space(first, rest) =>
        """\s+""" + genRegex(rest)
    }
  }

}

object GenRegex {
  private val Nums ="""^(\d+)(.*)$""".r
  //匹配一串数字 +(一个或多个)
  private val AlphasAndNums =
    """^([\w]+)(.*)$""".r
  //匹配一串字母
  private val ChineaseCharacters =
    """^([\u4e00-\u9fa5]+)(.*)$""".r //匹配一串汉字
  private val Space =
    """^([ ]+)(.*)$""".r //匹配一串空格
  private val OtherSpecialCharacter =
    """^([^ 0-9a-zA-Z\u4e00-\u9fa5])(.*)$""".r //不是一个数字 字母 汉字 空格,那么只能是特殊字符
  private val ZeroString =
    """^()$""".r //空字符串
  def genRegex(source: String): String = source match {
    case Space(first, rest) =>
      """\s+""" + genRegex(rest)
    case ZeroString(_) =>
      ""
    case AlphasAndNums(first, rest) =>
      """\w+""" + genRegex(rest)
    case ChineaseCharacters(first, rest) =>
      """[\u4e00-\u9fa5]+""" + genRegex(rest)
    case OtherSpecialCharacter(first, rest) =>
      val temp = first match {
        case "." => """\."""
        case "?" => """\?"""
        case "*" => """\*"""
        case "+" => """\+"""
        case "[" => """\["""
        case "]" => """\]"""
        case "(" => """\("""
        case ")" => """\)"""
        case ":" => """\:"""
        case """\""" => """\\"""
        case t => t
      }
      temp + genRegex(rest)
  }
}

object GenRegex1 {
  private val Nums ="""^(\d+)(.*)$""".r
  //匹配一串数字 +(一个或多个)
  private val AlphasAndNums =
    """^([\w]+)(.*)$""".r
  //匹配一串字母
  private val ChineaseCharacters =
    """^([\u4e00-\u9fa5]+)(.*)$""".r //匹配一串汉字
  private val Space =
    """^([ ]+)(.*)$""".r //匹配一串空格
  private val OtherSpecialCharacter =
    """^([^ 0-9a-zA-Z\u4e00-\u9fa5])(.*)$""".r //不是一个数字 字母 汉字 空格,那么只能是特殊字符
  private val ZeroString =
    """^()$""".r //空字符串
  def removeStringFromHead(str: String, head: String) = str.startsWith(head) match {
    case false => str
    case true => str.substring(head.length)
  }

  def extractFromString(str: String, pattern: String) = { //Some(first,head)
    pattern.r.findFirstMatchIn(str) match {
      case None => None
      case Some(matcher) =>
        val head = matcher.group(1) //first
        Some(head, removeStringFromHead(str, head))
    }
  }

  def genRegex(source: String): String = source match {
    case ExtractRegex(_, rest, pat) =>
      pat + genRegex(rest)
    case Space(first, rest) =>
      """\s+""" + genRegex(rest)
    case ZeroString(_) =>
      ""
    case AlphasAndNums(first, rest) =>
      """\w+""" + genRegex(rest)
    case ChineaseCharacters(first, rest) =>
      """[\u4e00-\u9fa5]+""" + genRegex(rest)
    case OtherSpecialCharacter(first, rest) =>
      val temp = first match {
        case "." => """\."""
        case "?" => """\?"""
        case "*" => """\*"""
        case "+" => """\+"""
        case "[" => """\["""
        case "]" => """\]"""
        case "(" => """\("""
        case ")" => """\)"""
        case ":" => """\:"""
        case """\""" => """\\"""
        case t => t
      }
      temp + genRegex(rest)
  }

  object ExtractRegex {

    import regexRule._

    def unapply(source: String): Option[(String, String, String)] = { //分别为first、rest、以及对应的模式
      val list = List(
        "%{COMBINEDAPACHELOG}" -> APACHELOG.COMBINEDAPACHELOG,
        "%{SYSLOGBASE}" -> SYSLOG.SYSLOGBASE,
        "%{IPV6}" -> SYSLOG.IPV6,
        "%{IPV4}" -> SYSLOG.IPV4,
        "%{DATE}" -> DATE.DATE,
        "%{LOGLEVEL}" -> LEVEL.LOGLEVEL,
        "%{PATH}" -> PATH.PATH
      )
      list.find { case (patternName, pattern) => !GenRegex1.extractFromString(source, pattern.pattern.pattern()).isEmpty } match {
        case None => None
        case Some((patternname, pattern)) =>
          val Some((first, rest)) = GenRegex1.extractFromString(source, pattern.pattern.pattern())
          Some((first, rest, patternname))
      }
    }
  }

}

package object regexRule {

  object APACHELOG {
    val COMBINEDAPACHELOG ="""^((?:(?:(?:((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?|(?<![0-9])(?:(?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))(?![0-9]))|\b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\.?|\b))) (?:[a-zA-Z][a-zA-Z0-9_.+-=:]+@\b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\.?|\b)|[a-zA-Z0-9._-]+) (?:[a-zA-Z0-9._-]+) \[(?:(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])/\b(?:Jan(?:uary|uar)?|Feb(?:ruary|ruar)?|M(?:a|ä)?r(?:ch|z)?|Apr(?:il)?|Ma(?:y|i)?|Jun(?:e|i)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|O(?:c|k)?t(?:ober)?|Nov(?:ember)?|De(?:c|z)(?:ember)?)\b/(?>\d\d){1,2}:(?!<[0-9])(?:2[0123]|[01]?[0-9]):(?:[0-5][0-9])(?::(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?))(?![0-9])( (?:[+-]?(?:[0-9]+)))?)\] "(?:(?:\b\w+\b) (?:\S+)(?: HTTP/(?:(?:(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\.[0-9]+)?)|(?:\.[0-9]+))))))?|(?:.*?))" (?:(?:(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\.[0-9]+)?)|(?:\.[0-9]+))))) (?:(?:(?:(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\.[0-9]+)?)|(?:\.[0-9]+)))))|-) (?:(?>(?<!\\)(?>"(?>\\.|[^\\"]+)+"|""|(?>'(?>\\.|[^\\']+)+')|''|(?>`(?>\\.|[^\\`]+)+`)|``))) (?:(?>(?<!\\)(?>"(?>\\.|[^\\"]+)+"|""|(?>'(?>\\.|[^\\']+)+')|''|(?>`(?>\\.|[^\\`]+)+`)|``))))(.*)""".r
  }

  object DATE {
    val MONTH ="""^((?:(?:Jan(?:uary|uar)?|Feb(?:ruary|ruar)?|M(?:a|ä)?r(?:ch|z)?|Apr(?:il)?|Ma(?:y|i)?|Jun(?:e|i)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|O(?:c|k)?t(?:ober)?|Nov(?:ember)?|De(?:c|z)(?:ember)?)))(.*)$""".r
    val MONTHNUM ="""0?[1-9]|1[0-2]""".r
    val MONTHDAY ="""^((?:0[1-9]|[12][0-9]|3[01]|[1-9]))(.*)$""".r
    val TIME ="""^((?:2[0123])|(?:[01]?[0-9]):(?:[0-5][0-9]):(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?))(.*)$""".r
    val DATE_US ="""^((?:(?:0?[1-9]|1[0-2])[/-](?:0[1-9]|[12][0-9]|3[01]|[1-9])[/-](?:\d\d){1,2}))(.*)$""".r
    val DATE_EU ="""^((?:(?:0[1-9]|[12][0-9]|3[01]|[1-9])[./-](?:0[1-9]|[12][0-9]|3[01]|[1-9])[./-](?:\d\d){1,2}))(.*)$""".r
    val HOUR ="""^((?:2[0123]|[01]?[0-9]))(.*)$""".r
    val MINUTE ="""^((?:[0-5][0-9]))(.*)$""".r
    val DAY ="""^((?:Mon(?:day)?|Tue(?:sday)?|Wed(?:nesday)?|Thu(?:rsday)?|Fri(?:day)?|Sat(?:urday)?|Sun(?:day)?))(.*)$""".r
    val TZ = """^((?:[PMCE][SD]T|UTC))(.*)$""".r
    val DATESTAMP_EVENTLOG ="""^((?>\d\d){1,2}(?:0[1-9]|1[0-2])(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])(?:2[0123]|[01]?[0-9])(?:[0-5][0-9])(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?))(.*)$""".r
    val DATE ="""^((?:0?[1-9]|1[0-2])[/-](?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])[/-](?>\d\d){1,2}|(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])[./-](?:0?[1-9]|1[0-2])[./-](?>\d\d){1,2})(.*)""".r
  }

  object SYSLOG {
    val SYSLOGBASE ="""^((?:\b(?:Jan(?:uary|uar)?|Feb(?:ruary|ruar)?|M(?:a|ä)?r(?:ch|z)?|Apr(?:il)?|Ma(?:y|i)?|Jun(?:e|i)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|O(?:c|k)?t(?:ober)?|Nov(?:ember)?|De(?:c|z)(?:ember)?)\b +(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9]) (?!<[0-9])(?:2[0123]|[01]?[0-9]):(?:[0-5][0-9])(?::(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?))(?![0-9])) (?:<(?:\b(?:[0-9]+)\b).(?:\b(?:[0-9]+)\b)> )?(?:(?:(?:((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?|(?<![0-9])(?:(?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))(?![0-9]))|\b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\.?|\b))) (?:[\x21-\x5a\x5c\x5e-\x7e]+)(?:\[(?:\b(?:[1-9][0-9]*)\b)\])?:)(.*)""".r
    //    val SYSLOGTIMESTAMP ="""^((?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\b +(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9]) (?!<[0-9])(?:2[0123]|[01]?[0-9]):(?:[0-5][0-9])(?::(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?))(?![0-9]))(.*)$""".r
    val IPV4 =
      """^((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9]))(.*)$""".r
    val IPV6 ="""^((?:(?:(?:[0-9A-Fa-f]{1,4}:){7}(?:[0-9A-Fa-f]{1,4}|:))|(?:(?:[0-9A-Fa-f]{1,4}:){6}(?::[0-9A-Fa-f]{1,4}|(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(?:(?:[0-9A-Fa-f]{1,4}:){5}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,2})|:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(?:(?:[0-9A-Fa-f]{1,4}:){4}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,3})|(?:(?::[0-9A-Fa-f]{1,4})?:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){3}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,4})|(?:(?::[0-9A-Fa-f]{1,4}){0,2}:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){2}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,5})|(?:(?::[0-9A-Fa-f]{1,4}){0,3}:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){1}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,6})|(?:(?::[0-9A-Fa-f]{1,4}){0,4}:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(?::(?:(?:(?::[0-9A-Fa-f]{1,4}){1,7})|(?:(?::[0-9A-Fa-f]{1,4}){0,5}:(?:(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(?:\.(?:25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))))(.*)$""".r
    val HOSTNAME ="""^((?:(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*))(.*)$""".r
    val PROG ="""^((?:[\w._/%-]+))(.*)$""".r
    val POSINT ="""^((?:[1-9][0-9]*))(.*)$""".r
  }

  object LEVEL {
    val LOGLEVEL ="""^((?:[Aa]lert|ALERT|[Tt]race|TRACE|[Dd]ebug|DEBUG|[Nn]otice|NOTICE|[Ii]nfo|INFO|[Ww]arn?(?:ing)?|WARN?(?:ING)?|[Ee]rr?(?:or)?|ERR?(?:OR)?|[Cc]rit?(?:ical)?|CRIT?(?:ICAL)?|[Ff]atal|FATAL|[Ss]evere|SEVERE|EMERG(?:ENCY)?|[Ee]merg(?:ency)?))(.*)$""".r
  }

  object PATH {
    val PATH ="""^((?:(/([\w_%!$@:.,~-]+|\\.)*)+|(?>[A-Za-z]+:|\\)(?:\\[^\\?*]*)+))(.*)""".r
  }

}