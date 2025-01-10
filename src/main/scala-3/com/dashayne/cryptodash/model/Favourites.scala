package com.dashayne.cryptodash.model

import scala.util.{Try, Success, Failure}
import java.io.{File, PrintWriter}
import scala.io.Source
import ujson.*

object Favourites:

  private val favouritesFile = new File(System.getProperty("user.home") + "/cryptodash_wallets/favourites.json")

  // Ensure the favourites file exists
  if !favouritesFile.exists() then
    val writer = new PrintWriter(favouritesFile)
    writer.write("{}") // Start with an empty JSON object
    writer.close()

  // Load tokens.json into memory
  private val tokens: Map[String, Map[String, String]] =
    Try {
      val source = Source.fromInputStream(getClass.getResourceAsStream("/com/dashayne/cryptodash/view/tokens.json"))
      val jsonContent = source.mkString
      source.close()
      ujson.read(jsonContent).obj.map { case (key, value) =>
        key -> (value.obj.view.mapValues(_.str).toMap + ("id" -> key)) // Include "id" as the key
      }.toMap
    }.getOrElse {
      println("Failed to load tokens.json or file is empty.")
      Map.empty
    }

  def getFavouritesByAddress(address: String): Try[Seq[String]] =
    Try:
      val json = ujson.read(Source.fromFile(favouritesFile).mkString)
      json.obj.get(address) match
        case Some(favList) => favList.arr.map(_.str).toSeq
        case None => Seq.empty

  def addFavourite(address: String, tokenKey: String): Try[Unit] =
    if !tokens.contains(tokenKey) then
      Failure(new IllegalArgumentException(s"Invalid token key: $tokenKey"))
    else
      Try:
        val json = ujson.read(Source.fromFile(favouritesFile).mkString)

        val updatedFavourites = json.obj.get(address) match
          case Some(favList) =>
            val currentList = favList.arr.map(_.str)
            if currentList.contains(tokenKey) then currentList
            else currentList :+ tokenKey
          case None => Seq(tokenKey)

        json(address) = updatedFavourites
        val writer = new PrintWriter(favouritesFile)
        writer.write(json.render(2)) // Pretty print the updated JSON
        writer.close()

  def removeFavourite(address: String, tokenKey: String): Try[Unit] =
    Try:
      val json = ujson.read(Source.fromFile(favouritesFile).mkString)

      val updatedFavourites = json.obj.get(address) match
        case Some(favList) =>
          favList.arr.map(_.str).filterNot(_ == tokenKey) // Remove the tokenKey
        case None => Seq.empty

      json(address) = updatedFavourites

      val writer = new PrintWriter(favouritesFile)
      writer.write(json.render(2)) // Pretty print the updated JSON
      writer.close()


  def getTokenDetails(tokenKey: String): Option[Map[String, String]] =
    tokens.get(tokenKey)

  def getAllTokens: Map[String, Map[String, String]] =
    tokens
