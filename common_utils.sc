import java.io.{BufferedWriter, Closeable, File, FileOutputStream, FileWriter, OutputStreamWriter}

import scala.sys.process.ProcessBuilder

def construct_generic_command(args: String*): ProcessBuilder = {
  Seq("/bin/sh", "-c", args.mkString(" "))
}

val generic = construct_generic_command(_: String)

def construct_command_line_arguments_from_map(args: Map[String, String]): String = {
  val include_if_present = (v: String) => if (v == "") "" else s" $v"
  args.map {
    case (k,v) if k.length > 1  => s"--$k" + include_if_present(v)
    case (k,v) if k.length == 1  => s"-$k" + include_if_present(v)
  }.mkString(" ")
}

def add_arg_if_absent(args: Map[String, String], arg: String, arg_val: String): Map[String, String] = {
  if (args contains arg) args else args + (arg -> arg_val)
}

def getFileContents(filename: String): List[String] = {
  val source = scala.io.Source.fromFile(filename)
  val lines = try source.getLines().toList finally source.close()
  lines
}

def add_entry_to_file_if_absent(filename: String)(entry: String) : ProcessBuilder = {
  getFileContents(filename).find(line => line.equals(entry)) match {
    case Some(_) => s"echo Entry already in command monitor hosts file!"
    case _ =>
      val fileHandle = new File(filename)
      fileHandle.createNewFile()
      val bw = new BufferedWriter(new FileWriter(fileHandle, true))
      try bw.write(entry + System.lineSeparator) finally bw.close()
      s"echo Entry added to command monitor hosts file!"
  }
}



def write_stream_to_file(filename: String, stream: Seq[String]): Unit = {
  val fileHandle = new File(filename)
  fileHandle.createNewFile()
  using(new BufferedWriter(new FileWriter(fileHandle, true))) {
    writer =>
      for (x <- stream) {
        writer.write(x + System.lineSeparator())  // however you want to format it
      }
  }
}

def using[T <: Closeable, R](resource: T)(block: T => R): R = {
  try { block(resource) }
  finally { resource.close() }
}