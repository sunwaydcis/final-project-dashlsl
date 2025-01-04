package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import com.dashayne.cryptodash.model.{BalancesManager, TransactionManager, WalletManager}
import javafx.application.Platform

import scala.util.{Failure, Success, Try}
import scala.math.BigDecimal

class SendTransactionController:

  @FXML
  private var recipientAddressField: TextField = _
  @FXML
  private var amountField: TextField = _
  @FXML
  private var maxButton: Button = _
  @FXML
  private var sendButton: Button = _

  private var currentBalance: BigDecimal = BigDecimal(0)

  @FXML
  def initialize(): Unit =
    fetchEthereumBalance()
    maxButton.setOnAction(_ => handleMax())
    sendButton.setOnAction(_ => handleSend())

  private def fetchEthereumBalance(): Unit =
    WalletManager.getLoggedInWalletAddress match
      case Some(address) =>
        BalancesManager.getEthereumBalance(address) match
          case Success(balance) =>
            currentBalance = BigDecimal(balance)
            println(s"Fetched balance: $currentBalance ETH")
          case Failure(ex) =>
            println(s"Failed to fetch balance: ${ex.getMessage}")
      case None =>
        println("No Wallet Logged in")

  private def handleMax(): Unit =
    if currentBalance > BigDecimal(0) then
      amountField.setText(currentBalance.toString())
    else
      println("Balance is zero or could not be fetched.")

  private def handleSend(): Unit =
    val recipientAddress = recipientAddressField.getText.trim
    val amount = Try(BigDecimal(amountField.getText.trim))

    if recipientAddress.isBlank then
      println("Recipient address cannot be empty.")
    else if amount.isFailure || amount.get <= BigDecimal(0) then
      println("Enter a valid amount greater than zero.")
    else if amount.get > currentBalance then
      println("Insufficient balance.")
    else
      println(s"Sending ${amount.get} ETH to $recipientAddress...")
      sendTransaction(recipientAddress, amount.get)

  private def sendTransaction(recipientAddress: String, amount: BigDecimal): Unit =
    val senderAddress = WalletManager.getLoggedInWalletAddress.getOrElse("")
    val privateKey = WalletManager.getPrivateKey("securepassword", WalletManager.getLoggedInWalletFileName.getOrElse(""))
      .getOrElse(throw new IllegalStateException("Failed to retrieve private key"))

    println(s"Creating a send transaction to $recipientAddress for $amount ETH...")
    TransactionManager.sendTransaction(senderAddress, privateKey, recipientAddress, amount) match
      case Success(txHash) =>
        println(s"Transaction successful! Tx hash: $txHash")
        closeWindow()
      case Failure(ex) =>
        println(s"Transaction failed: ${ex.getMessage}")

  private def closeWindow(): Unit =
    val stage = sendButton.getScene.getWindow.asInstanceOf[javafx.stage.Stage]
    stage.close()



