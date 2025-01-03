package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, Button}
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}
import com.dashayne.cryptodash.model.{BalancesManager, ApiClient}
import java.math.BigDecimal
import java.util.concurrent.{Executors, TimeUnit}

class WalletMenuController:

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

  private val apiClient = new ApiClient()
  private val scheduler = Executors.newScheduledThreadPool(1)

  def setWalletDetails(name: String, address: String): Unit =
    walletName.setText(name)
    walletAddress.setText(formatAddress(address))
    fetchAndSetEthereumBalance(address)
    schedulePeriodicBalanceCheck(address)

  @FXML
  def initialize(): Unit =
    println(s"receiveButton: $receiveButton")
    println(s"sendButton: $sendButton")
    println(s"ethAmount: $ethAmount")
    receiveButton.setOnAction(_ => handleCopyAddress())

  private def handleCopyAddress(): Unit =
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    val selection = new StringSelection(walletAddress.getText)
    clipboard.setContents(selection, null)

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
      20, // Initial delay
      20, // Period
      TimeUnit.SECONDS
    )

  def stopScheduler(): Unit =
    scheduler.shutdown()

  private def formatAddress(address: String): String =
    if address.length > 10 then
      s"${address.take(6)}...${address.takeRight(4)}"
    else address
