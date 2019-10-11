#!/usr/bin/env amm
import scala.sys.process._
import $file.common_utils
import $file.db_ops
import scala.language.postfixOps

@main
def copy_db(source: Map[String, String], sink: Map[String, String]): Unit = {
  val source_with_output_arg = add_output_dir_arg_if_absent(source)
  val sink_with_input_arg = sink + ("i" -> s"${source_with_output_arg("o")}/${source_with_output_arg("d")}")
  db_ops.copy_db(mongodump, source_with_output_arg, mongorestore, sink_with_input_arg)
}

def add_output_dir_arg_if_absent(args: Map[String, String]): Map[String, String] = {
  common_utils.add_arg_if_absent(args, "o", get_dump_dir(args))
}

def get_dump_dir(args: Map[String, String]): String = {
  args.getOrElse("o",
    s"${args("d")}_dump_from_${args("h")}".replaceAll("[^a-zA-Z0-9]", "_"))
}

@main
def dump(source: Map[String, String]): Unit = {
  mongodump(add_output_dir_arg_if_absent(source)) !
}

def mongodump(args: Map[String, String]): ProcessBuilder = {
  val initialCommand = s"mongodump --host ${args("h")} --db ${args("d")}" +
    s" -o ${args("o")}"
  val commandToRun = initialCommand + " " +
    common_utils.construct_command_line_arguments_from_map (args - ("h", "d"))
  commandToRun
}

@main
def restore(sink: Map[String, String]): Unit = {
  mongorestore(sink) !
}

def mongorestore(args: Map[String, String]): ProcessBuilder = {
  val initialCommand = s"mongorestore --host ${args("h")} --db ${args("d")} --username ${args("u")} --password ${args("p")}" +
    s" --authenticationDatabase=${args.getOrElse("a", "admin")} ${args("i")}"
  val commandToRun = initialCommand + " " +
    common_utils.construct_command_line_arguments_from_map (args - ("h", "d", "u", "p", "a", "i"))
  commandToRun
}
