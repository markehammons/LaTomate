package io.github.markehammons.latomate.gitlab_api.projects

import org.scalatest.{DiagrammedAssertions, WordSpec}
import io.circe.parser.decode
import io.circe.syntax._

import scala.io.Source

class FullProjectSpecification extends WordSpec with DiagrammedAssertions {
  val testJSONComplex = {
    val is =
      getClass.getClassLoader.getResourceAsStream("json/ComplexProject.json")
    val json = Source
      .fromInputStream(is)
      .mkString
    is.close()
    json
  }

  "A FullProject" should {
    "deserialize from json" in {
      val r = decode[FullProject](testJSONComplex)
      assert(r.isRight && r.exists(_.mergeRequestsEnabled == true))
    }

    "serialize into json" in {
      val r = decode[FullProject](testJSONComplex)

      val json = r
        .map(p => p.copy(statistics = p.statistics.copy(commitCount = 52)))
        .map(_.asJson.noSpaces)

      assert(json.isRight && json.exists(_.contains("""commit_count":52""")))
    }
  }
}
