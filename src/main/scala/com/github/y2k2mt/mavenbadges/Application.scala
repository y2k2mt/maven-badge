package com.github.y2k2mt.mavenbadge

import java.net.URI
import scala.collection.JavaConversions._
import org.analogweb.core.Servers
import org.analogweb.core.response._
import analogweb._, json4s._
import dispatch._
import org.json4s._, jackson.JsonMethods._

object Application {

  def main(args: Array[String]) = {
    val port = sys.props.get("http.port").getOrElse("8000")
    val uri = "0.0.0.0"
    http(uri, port.toInt)(routes).run
  }

  val routes =
    get("/maven-central/{group}/{artifact}") { implicit r =>
      latestVersionOnMavenCentral(param("artifact"), param("group")) { version =>
        val to = s"http://search.maven.org/#artifactdetails|${param("group")}|${param("artifact")}|${version}|"
        Status(302, asText(to)).withHeader(Map("Location" -> to))
      }
    } ++
      get("/maven-central/{group}/{artifact}/badge.svg") { implicit r =>
        latestVersionOnMavenCentral(param("artifact"), param("group"))(version => Ok(asSvg(version)))
      }

  private[this] def latestVersionOnMavenCentral[R](a: String, g: String)(f: String => R) = {
    val central = url(s"http://search.maven.org/solrsearch/select?q=a:%22${a}%22%20g:%22${g}%22&wt=json")
    val jsonText = for (result <- Http(central OK as.String)) yield result
    jsonText map { txt =>
      val parsedJson = parse(txt)
      val latest = for {
        JObject(child) <- parsedJson
        JField("response", JObject(response)) <- child
        JField("numFound", JInt(numFound)) <- response
        JField("docs", JArray(docs)) <- response
        JObject(doc) <- docs if !docs.isEmpty
        JField("latestVersion", JString(latestVersion)) <- doc
      } yield latestVersion

      latest.headOption.map(f).getOrElse(NotFound(asText(s"Artifact [${g}:${a}] is not found.")))
    }
  }

  private[this] def asSvg(latestVersion: String) = {
    val svg = s"""<svg xmlns="http://www.w3.org/2000/svg" width="138" height="20"><linearGradient id="b" x2="0" y2="100%"><stop offset="0" stop-color="#bbb" stop-opacity=".1"/><stop offset="1" stop-opacity=".1"/></linearGradient><mask id="a"><rect width="138" height="20" rx="3" fill="#fff"/></mask><g mask="url(#a)"><path fill="#555" d="M0 0h92v20H0z"/><path fill="#4c1" d="M92 0h46v20H92z"/><path fill="url(#b)" d="M0 0h138v20H0z"/></g><g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11"><text x="46" y="15" fill="#010101" fill-opacity=".3">maven central</text><text x="46" y="14">maven central</text><text x="114" y="15" fill="#010101" fill-opacity=".3">${latestVersion}</text><text x="114" y="14">${latestVersion}</text></g></svg>"""
    asText(svg).typeAs("image/svg+xml")
  }
}

