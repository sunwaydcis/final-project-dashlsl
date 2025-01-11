package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import com.dashayne.cryptodash.model.{BalancesManager, TransactionManager, Wallet, WalletManager}
import com.dashayne.cryptodash.utils.Utils.{copyToClipboard, showAlert}
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
  private var wallet: Wallet = _

  def setWallet(wallet: Wallet): Unit =
    this.wallet = wallet
    fetchEthereumBalance()

  @FXML
  def initialize(): Unit =
    maxButton.setOnAction(_ => handleMax())
    sendButton.setOnAction(_ => handleSend())

  private def fetchEthereumBalance(): Unit =
    BalancesManager.getEthereumBalance(wallet.getAddress) match
      case Success(balance) =>
        currentBalance = BigDecimal(balance)
        println(s"Fetched balance: $currentBalance ETH")
      case Failure(ex) =>
        println(s"Failed to fetch balance: ${ex.getMessage}")

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
    val senderAddress = wallet.getAddress
    val privateKey = WalletManager.getPrivateKey("securepassword", wallet.getFileName)
      .getOrElse(throw new IllegalStateException("Failed to retrieve private key"))

    println(s"Creating a send transaction to $recipientAddress for $amount ETH...")
    TransactionManager.sendTransaction(senderAddress, privateKey, recipientAddress, amount) match
      case Success(txHash) =>
        println(s"Transaction successful! Tx hash: $txHash")
        copyToClipboard(txHash)
        showAlert("Transaction Successful", s"Your transaction was successful!\nTx Hash: $txHash\n(Copied to clipboard)")
        closeWindow()
      case Failure(ex) =>
        println(s"Transaction failed: ${ex.getMessage}")
        showAlert("Transaction Failed", s"Failed to complete the transaction: ${ex.getMessage}")


  private def closeWindow(): Unit =
    val stage = sendButton.getScene.getWindow.asInstanceOf[javafx.stage.Stage]
    stage.close()
