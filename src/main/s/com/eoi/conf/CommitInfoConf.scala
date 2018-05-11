package s.com.eoi.conf

class CommitInfoConf extends ClientConf[CommitInfoConf] {
  val configs = getConfigs("git", "gitinfo.properties")

  val commitId = get("commit.id.abbrev").getOrElse("")
  val tagName = get("closest.tag.name").getOrElse("")
  val commitTime = get("commit.time").getOrElse("")
  val buildTime = get("build.time").getOrElse("")
  val buildVersion = get("build.version").getOrElse("")
}

object CommitInfoConf{
  def apply() = new CommitInfoConf()
}
