package eu.bioemergences.mhammons.latomate.gitlab_api.projects

import io.circe.parser.decode
import io.circe.generic.auto._
import org.scalatest.{DiagrammedAssertions, WordSpec}

import scala.io.Source

class SimpleProjectSpecification extends WordSpec with DiagrammedAssertions {
  val testJSONSimple = {
    val is =
      getClass.getClassLoader.getResourceAsStream("json/SimpleProject.json")
    val json = Source
      .fromInputStream(is)
      .mkString
    is.close()
    json
  }

  val testJSONComplex = {
    val is =
      getClass.getClassLoader.getResourceAsStream("json/ComplexProject.json")
    val json = Source
      .fromInputStream(is)
      .mkString
    is.close()
    json
  }

  val incompleteJSON = {
    val is =
      getClass.getClassLoader.getResourceAsStream("json/IncompleteProject.json")
    val json = Source.fromInputStream(is).mkString
    is.close()
    json
  }

  "A Project" should {
    "parse from simple JSON" in {
      val p = decode[SimpleProject](testJSONSimple)

      assert(p.isRight && p.exists(_.id == 4))
    }

    "parse from complex project JSON" in {
      val p = decode[SimpleProject](testJSONComplex)

      assert(p.isRight && p.exists(_.id == 4))
    }

    "parse from incomplete JSON" in {
      val p = decode[SimpleProject](incompleteJSON)

      assert(p.isRight && p.exists(_.id == 4))
    }
  }

}
