#!/usr/bin/env amm
import $file.common_utils
import scala.sys.process._

val BASH_HISTORY_FILE = s"${System.getProperty("user.home")}/.bash_history"
val COMMAND_MONITOR_HOSTS_FILE = s"${System.getProperty("user.home")}/.cmdmonhosts"
val FILES_TO_MONITOR_FILE = s"${System.getProperty("user.home")}/.cmdmonfiles"
val COMMAND_HISTORY_FILE = s"${System.getProperty("user.home")}/allhist"
val add_entry_to_hosts_file = common_utils.add_entry_to_file_if_absent(COMMAND_MONITOR_HOSTS_FILE)(_)
val add_entry_to_monitored_files_list = common_utils.add_entry_to_file_if_absent(FILES_TO_MONITOR_FILE)(_)


def enable_passwordless_login_for_host(user: String, host: String): ProcessBuilder = {
  val key = Process(s"head -1 ${System.getProperty("user.dir")}/.ssh/id_rsa.pub").lineStream.take(1).head
  val grepCommand = "grep " + s"'$key' ~/.ssh/authorized_keys"
  val echoCommand = "echo " + s"'$key' >> ~/.ssh/authorized_keys"
  val commandToRun = s"ssh $user@$host bash" + " -c \"" + s"$grepCommand ||  $echoCommand" + "\""
  commandToRun
}

@main
def add_host(user: String, host: String): Unit = {
  enable_passwordless_login_for_host(user, host) #&& add_entry_to_hosts_file(s"$user@$host") !
}

@main
def add_files_to_monitor(files: String*): Unit = {
  files.map(add_entry_to_monitored_files_list)
}

def get_cmd_hist_from_host(user_host: String, files: List[String]): Stream[String] = {
  val filesToCat = files.mkString(" ")
  (s"ssh $user_host bash" + " -c \"" + s"cat $filesToCat" + "\"").lineStream_!
}

@main
def load_cmd_hist(): Unit = {
  val filesToMonitor = common_utils.getFileContents(FILES_TO_MONITOR_FILE)
  val temp_file = s"$COMMAND_HISTORY_FILE.temp"
  common_utils.getFileContents(COMMAND_MONITOR_HOSTS_FILE)
    .map(user_host => get_cmd_hist_from_host(user_host, filesToMonitor))
    .foreach(stream => common_utils.write_stream_to_file(temp_file, stream))  
  common_utils.write_stream_to_file(temp_file, common_utils.getFileContents(BASH_HISTORY_FILE))
  Seq("bash", "-c", s"sort $temp_file | uniq > $COMMAND_HISTORY_FILE").!!
}
