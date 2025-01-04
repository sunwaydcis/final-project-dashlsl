package com.dashayne.cryptodash.model

import org.web3j.crypto.{WalletUtils, Credentials}
import java.io.File
import scala.util.{Try, Success, Failure}

object WalletManager:

  private val walletDirectory = new File(System.getProperty("user.home") + "/cryptodash_wallets")
  if (!walletDirectory.exists()) walletDirectory.mkdir()


  private var loggedInWalletFileName: Option[String] = None
  private var loggedInWalletAddress: Option[String] = None

  /**
   * Set the logged-in wallet file name.
   *
   * @param fileName The wallet file name.
   */
  def setLoggedInWalletFileName(fileName: String): Unit =
    loggedInWalletFileName = Some(fileName)

  /**
   * Get the logged-in wallet file name.
   *
   * @return The wallet file name if set.
   */
  def getLoggedInWalletFileName: Option[String] = loggedInWalletFileName
  
  def setLoggedInWalletAddress(address: String): Unit =
    loggedInWalletAddress = Some(address)
    
  def getLoggedInWalletAddress: Option[String] = loggedInWalletAddress

  /**
   * Create a new wallet with a name.
   * @param password The password for the wallet.
   * @param walletName The friendly name for the wallet.
   * @return A tuple containing the wallet name and address on success.
   */
  def createWallet(password: String, walletName: String): Try[(String, String)] =
    Try:
      val walletFileName = WalletUtils.generateNewWalletFile(password, walletDirectory, false)
      val walletFile = new File(walletDirectory, walletFileName)
      val credentials = WalletUtils.loadCredentials(password, walletFile)
      val walletAddress = credentials.getAddress
      saveWalletName(walletFileName, walletName)
      (walletName, walletAddress)

  /**
   * Get the private key for a wallet.
   *
   * @param password       The wallet password.
   * @param walletFileName The wallet file name.
   * @return The private key on success.
   */
  def getPrivateKey(password: String, walletFileName: String): Try[String] =
    loadWallet(password, walletFileName).map(_.getEcKeyPair.getPrivateKey.toString(16))

  /**
   * Load wallet credentials from a file.
   * @param password The password for the wallet.
   * @param walletFileName The name of the wallet file.
   * @return Wallet credentials on success.
   */
  def loadWallet(password: String, walletFileName: String): Try[Credentials] =
    Try(WalletUtils.loadCredentials(password, new File(walletDirectory, walletFileName)))

  /**
   * Save the friendly name for a wallet.
   * @param walletFileName The name of the wallet file.
   * @param walletName The friendly name to associate with the wallet.
   */
  private def saveWalletName(walletFileName: String, walletName: String): Unit =
    val walletNameFile = new File(walletDirectory, walletFileName + ".name")
    val writer = new java.io.PrintWriter(walletNameFile)
    writer.write(walletName)
    writer.close()

  /**
   * Get the friendly name for a wallet.
   * @param walletFileName The name of the wallet file.
   * @return The friendly name if it exists, otherwise None.
   */
  def getWalletName(walletFileName: String): Option[String] =
    val walletNameFile = new File(walletDirectory, walletFileName + ".name")
    if walletNameFile.exists() then Some(scala.io.Source.fromFile(walletNameFile).mkString)
    else None

  /**
   * List all stored wallets.
   * @return A sequence of tuples containing wallet file name and optional wallet name.
   */
  def listWallets(): Seq[(String, Option[String])] =
    walletDirectory.listFiles()
      .filter(_.getName.endsWith(".json"))
      .map(file => (file.getName, getWalletName(file.getName)))
      .toSeq

  /**
   * Get the Ethereum address directly from a wallet file.
   * @param password The wallet password.
   * @param walletFileName The wallet file name.
   * @return The Ethereum address on success.
   */
  def getWalletAddress(password: String, walletFileName: String): Try[String] =
    loadWallet(password, walletFileName).map(_.getAddress)
