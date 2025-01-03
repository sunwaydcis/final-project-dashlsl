package com.dashayne.cryptodash.model

import org.web3j.crypto.{WalletUtils, Credentials}
import java.io.File
import scala.util.{Try, Success, Failure}

object WalletManager:
  private val walletDirectory = new File(System.getProperty("user.home") + "/cryptodash_wallets")
  if (!walletDirectory.exists()) walletDirectory.mkdir()

  def createWallet(password: String, walletName: String): Try[(String, String)] =
    Try {
      val walletFileName = WalletUtils.generateNewWalletFile(password, walletDirectory, false)
      val walletFile = new File(walletDirectory, walletFileName)
      val credentials = WalletUtils.loadCredentials(password, walletFile)
      val walletAddress = credentials.getAddress
      saveWalletName(walletFileName, walletName)
      (walletName, walletAddress)
    }

  def loadWallet(password: String, walletFileName: String): Try[Credentials] =
    Try(WalletUtils.loadCredentials(password, new File(walletDirectory, walletFileName)))

  private def saveWalletName(walletFileName: String, walletName: String): Unit =
    val walletNameFile = new File(walletDirectory, walletFileName + ".name")
    val writer = new java.io.PrintWriter(walletNameFile)
    writer.write(walletName)
    writer.close()

  def getWalletName(walletFileName: String): Option[String] =
    val walletNameFile = new File(walletDirectory, walletFileName + ".name")
    if (walletNameFile.exists()) Some(scala.io.Source.fromFile(walletNameFile).mkString)
    else None
