//> using scala 2.13.16
//> using dep com.lihaoyi::utest::0.8.9
//> using dep io.get-coursier:interface:1.0.28

package io.github.scala_cli.zip

import coursierapi._
import utest._

import java.io.{ByteArrayOutputStream, FileInputStream, InputStream}
import java.util.zip.ZipEntry

import scala.collection.mutable

object CustomZipInputStreamTests extends TestSuite {
  private def readAllBytes(is: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val buf  = Array.ofDim[Byte](16 * 1024)
    var read = 0
    while ({
      read = is.read(buf)
      read >= 0
    })
      if (read > 0)
        baos.write(buf, 0, read)
    baos.toByteArray
  }
  val tests: Tests = Tests {
    test("simple test") {

      val cache = Cache.create()
      val f     = cache.get(Artifact.of(
        "https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.8/scala-library-2.13.8.jar"
      ))

      val entries = new mutable.ListBuffer[(String, Int)]

      var is: InputStream = null
      try {
        is = new FileInputStream(f)
        val zis           = new ZipInputStream(is)
        var ent: ZipEntry = null
        while ({
          ent = zis.getNextEntry
          ent != null
        }) {
          val b = readAllBytes(zis)
          entries += ent.getName -> b.length
        }
      }
      finally
        if (is != null)
          is.close()

      val map = entries.toMap

      assert(map.get("scala/util/hashing/package.class").contains(792))
      assert(map.get("scala/util/Right.class").contains(5011))
    }
  }
}
