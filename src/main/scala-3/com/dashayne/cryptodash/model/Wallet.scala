package com.dashayne.cryptodash.model

import scala.util.{Try, Success, Failure}

class Wallet(
              fileNameS: String,
              addressS: String,
              nameS: String
            ):
  // Constant variables initialized from constructor parameters
  private val fileName: String = fileNameS
  private val address: String = addressS
  private val name: String = nameS

  // Preloaded favourites for this wallet
  private var favourites: Seq[String] = loadFavourites()

  // Load favourites from the Favourites model
  private def loadFavourites(): Seq[String] =
    Favourites.getFavouritesByAddress(address) match
      case Success(favs) => favs
      case Failure(_) =>
        println(s"Failed to load favourites for wallet: $address")
        Seq.empty

  // Refresh the favourites from the model
  def refreshFavourites(): Unit =
    favourites = loadFavourites()

  // Get current favourites
  def getFavourites: Seq[String] = favourites

  // Getters for the fileName, address, and name
  def getFileName: String = fileName
  def getAddress: String = address
  def getName: String = name

  override def toString: String =
    s"Wallet(name=$name, address=$address, fileName=$fileName)"
