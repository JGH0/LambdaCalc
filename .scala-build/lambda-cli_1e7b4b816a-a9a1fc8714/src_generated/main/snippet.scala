

final class snippet$_ {
def args = snippet_sc.args$
def scriptPath = """snippet.sc"""
/*<script>*/
println(lambda.naturalNumberToLambda(3))
/*</script>*/ /*<generated>*//*</generated>*/
}

object snippet_sc {
  private var args$opt0 = Option.empty[Array[String]]
  def args$set(args: Array[String]): Unit = {
    args$opt0 = Some(args)
  }
  def args$opt: Option[Array[String]] = args$opt0
  def args$: Array[String] = args$opt.getOrElse {
    sys.error("No arguments passed to this script")
  }

  lazy val script = new snippet$_

  def main(args: Array[String]): Unit = {
    args$set(args)
    val _ = script.hashCode() // hashCode to clear scalac warning about pure expression in statement position
  }
}

export snippet_sc.script as `snippet`

