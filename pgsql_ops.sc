#!/usr/bin/env amm
import scala.sys.process._
import $file.common_utils
import $file.db_ops
import scala.language.postfixOps

@main
def copy_db(source: Map[String, String], sink: Map[String, String]): Unit = {
  val source_with_output_arg = add_output_dir_arg_if_absent(source)
  val sink_with_input_arg = sink + ("w" -> s"${source_with_output_arg("f")}")
  db_ops.copy_db(pg_dump, source_with_output_arg, pg_restore, sink_with_input_arg)
}

def add_output_dir_arg_if_absent(args: Map[String, String]): Map[String, String] = {
  common_utils.add_arg_if_absent(args, "f", get_dump_dir(args))
}

def get_dump_dir(args: Map[String, String]): String = {
  args.getOrElse("f",
    s"${args.getOrElse("n", args("d"))}_dump_from_${args("h")}".replaceAll("[^a-zA-Z0-9]", "_"))
}

@main
def dump(source: Map[String, String]): Unit = {
  pg_dump(add_output_dir_arg_if_absent(source)) !
}

def pg_dump(args: Map[String, String]): ProcessBuilder = {
  val initialCommand = s"pg_dump --host=${args("h")} -d ${args("d")} " + s"-U ${args("U")}" + s" -f ${args("f")}"
  val commandToRun = initialCommand + " " +
    common_utils.construct_command_line_arguments_from_map (args - ("h", "d", "U", "f"))
  commandToRun
}

@main
def restore(sink: Map[String, String]): Unit = {
  pg_restore(sink) !
}

def pg_restore(args: Map[String, String]): ProcessBuilder = {
  val initialCommand = s"pg_restore --host=${args("h")} -F d -d ${args("d")} " +
    s"-U ${args("U")} " + s"-w ${args("w")}"
  val commandToRun = initialCommand + " " +
    common_utils.construct_command_line_arguments_from_map (args - ("h", "d", "U", "w", "F"))
  commandToRun
}
