package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, Button}
import javafx.scene.layout.VBox
import javafx.animation.TranslateTransition
import javafx.util.Duration
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}
import com.dashayne.cryptodash.model.{BalancesManager, ApiClient, WalletManager}
import java.math.BigDecimal
import java.io.File
import java.util.concurrent.{Executors, TimeUnit}

class WalletMenuController:

  @FXML
  private var topContainer: VBox = _

  @FXML
  private var walletName: Label = _

  @FXML
  private var walletAddress: Label = _

  @FXML
  private var ethAmount: Label = _

  @FXML
  private var usdAmount: Label = _

  @FXML
  private var receiveButton: Button = _

  @FXML
  private var sendButton: Button = _

  @FXML
  private var exportButton: Button = _

  private val apiClient = new ApiClient()
  private val scheduler = Executors.newScheduledThreadPool(1)
  private var fullWalletAddress: String = "" // Store the full address for copying

  def setWalletDetails(name: String, address: String): Unit =
    walletName.setText(name)
    walletAddress.setText(formatAddress(address))
    fullWalletAddress = address // Save the full address for copying
    fetchAndSetEthereumBalance(address)
    schedulePeriodicBalanceCheck(address)

  @FXML
  def initialize(): Unit =
    playSlideDownAnimation()
    receiveButton.setOnAction(_ => handleCopyAddress())
    exportButton.setOnAction(_ => handleExportPrivateKey())

  private def playSlideDownAnimation(): Unit =
    // Start with the top container off-screen
    topContainer.setTranslateY(-topContainer.getPrefHeight)

    // Create a TranslateTransition for the slide-down effect
    val slideDown = new TranslateTransition()
    slideDown.setNode(topContainer)
    slideDown.setDuration(Duration.millis(1500)) // Animation duration (1.5 seconds)
    slideDown.setFromY(-topContainer.getPrefHeight) // Start position
    slideDown.setToY(0) // Final position (on-screen)
    slideDown.setCycleCount(1) // Play only once
    slideDown.setAutoReverse(false)

    // Play the animation
    slideDown.play()

  private def handleCopyAddress(): Unit =
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    val selection = new StringSelection(fullWalletAddress) // Copy the full address
    clipboard.setContents(selection, null)
    println(s"Copied address to clipboard: $fullWalletAddress")

  private def handleExportPrivateKey(): Unit =
    println(s"Exporting private key for wallet address: $fullWalletAddress")
    val walletFile = findWalletFile(fullWalletAddress)
    walletFile match
      case Some(file) =>
        WalletManager.loadWallet("securepassword", file.getName) match
          case Success(credentials) =>
            val privateKey = credentials.getEcKeyPair.getPrivateKey.toString(16)
            println(s"Private Key: $privateKey")
          case Failure(ex) =>
            println(s"Failed to load wallet: ${ex.getMessage}")
      case None =>
        println(s"Wallet file not found for address: $fullWalletAddress")

  private def findWalletFile(address: String): Option[File] =
    val walletDir = new File(System.getProperty("user.home") + "/cryptodash_wallets")
    if walletDir.exists() && walletDir.isDirectory then
      walletDir.listFiles().find(file => file.getName.contains(address.drop(2))) // Match address without "0x"
    else None

  private def fetchAndSetEthereumBalance(address: String): Unit =
    Future:
      BalancesManager.getEthereumBalance(address)
    .onComplete:
      case Success(balanceTry) =>
        balanceTry match
          case Success(balance) =>
            val formattedBalance = f"$balance%.6f ETH"
            javafx.application.Platform.runLater:
              () => ethAmount.setText(formattedBalance)
            fetchAndDisplayUSDValue(balance)
          case Failure(ex) =>
            println(s"Failed to fetch Ethereum balance: ${ex.getMessage}")
            javafx.application.Platform.runLater:
              () => ethAmount.setText("Error fetching balance")
      case Failure(ex) =>
        println(s"Error in fetching balance task: ${ex.getMessage}")
        javafx.application.Platform.runLater:
          () => ethAmount.setText("Error fetching balance")

  private def fetchAndDisplayUSDValue(balance: BigDecimal): Unit =
    Future:
      apiClient.getEthPrice()
    .onComplete:
      case Success(priceTry) =>
        priceTry match
          case Right(price) =>
            val totalValue = BalancesManager.calculateTotalValueInUSD(balance, price)
            val formattedValue = f"$$${totalValue}%.2f"
            println(price)
            javafx.application.Platform.runLater:
              () => usdAmount.setText(formattedValue)
          case Left(error) =>
            println(s"Failed to fetch ETH price: $error")
            javafx.application.Platform.runLater:
              () => usdAmount.setText("Error fetching price")
      case Failure(ex) =>
        println(s"Error in fetching ETH price task: ${ex.getMessage}")
        javafx.application.Platform.runLater:
          () => usdAmount.setText("Error fetching price")

  private def schedulePeriodicBalanceCheck(address: String): Unit =
    scheduler.scheduleAtFixedRate(
      () =>
        javafx.application.Platform.runLater:
          () => fetchAndSetEthereumBalance(address),
      5, // Initial delay
      5, // Period
      TimeUnit.SECONDS
    )

  def stopScheduler(): Unit =
    scheduler.shutdown()

  private def formatAddress(address: String): String =
    if address.length > 10 then
      s"${address.take(6)}...${address.takeRight(4)}"
    else address
