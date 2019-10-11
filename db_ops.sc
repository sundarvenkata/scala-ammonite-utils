import scala.sys.process._
import scala.language.postfixOps
def copy_db(dump: Map[String, String] => ProcessBuilder, source: Map[String, String],
            restore: Map[String, String] => ProcessBuilder, sink: Map[String, String]): Unit = {
  dump(source) #&& restore(sink) !
}