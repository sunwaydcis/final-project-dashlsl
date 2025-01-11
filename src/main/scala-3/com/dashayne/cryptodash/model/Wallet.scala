package com.dashayne.cryptodash.model

import scala.util.{Try, Success, Failure}

/**
 * Represents a wallet with associated metadata and favorite tokens.
 * @param fileNameS The filename associated with the wallet (e.g., keystore file).
 * @param addressS  The Ethereum address of the wallet.
 * @param nameS     The display name of the wallet.
 */
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

  /**
   * Loads the wallet's favorite tokens from the Favourites model.
   * @return A sequence of favorite token symbols. Returns an empty sequence if the loading fails.
   */
  private def loadFavourites(): Seq[String] =
    Favourites.getFavouritesByAddress(address) match
      case Success(favs) => favs
      case Failure(_) =>
        println(s"Failed to load favourites for wallet: $address")
        Seq.empty

  /**
   * Refreshes the wallet's favorite tokens by reloading them from the model.
   */
  def refreshFavourites(): Unit =
    favourites = loadFavourites()

  /**
   * Retrieves the current list of favorite tokens.
   * @return A sequence of favorite token symbols.
   */
  def getFavourites: Seq[String] = favourites

  // Getters for the fileName, address, and name
  def getFileName: String = fileName
  def getAddress: String = address
  def getName: String = name

  /**
   * Provides a string representation of the wallet for debugging and logging purposes.
   * @return A formatted string containing the wallet's name, address, and file name.
   */
  override def toString: String =
    s"Wallet(name=$name, address=$address, fileName=$fileName)"
