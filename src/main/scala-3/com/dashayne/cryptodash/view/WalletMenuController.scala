package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}
import javafx.scene.layout.{HBox, VBox}
import javafx.animation.{PauseTransition, TranslateTransition}
import javafx.util.Duration

import java.util.concurrent.Executors
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import com.dashayne.cryptodash.model.{BalancesManager, Token}
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import javafx.application.Platform

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
  @FXML
  private var logoutButton: Button = _
  @FXML
  private var tokenList: VBox = _

  private val scheduler = Executors.newScheduledThreadPool(1)
  private var fullWalletAddress: String = ""
  private var tokens: Seq[Token] = Seq(
    Token("Ethereum", "ETH", "https://cryptologos.cc/logos/ethereum-eth-logo.png"),
    Token("Bitcoin", "BTC", "https://cryptologos.cc/logos/bitcoin-btc-logo.png"),
    Token("Solana", "SOL", "https://cryptologos.cc/logos/solana-sol-logo.png")
  )

  private val tokenTileCache: mutable.Map[String, (HBox, Label)] = mutable.Map()

  def setWalletDetails(name: String, address: String): Unit =
    walletName.setText(name)
    walletAddress.setText(formatAddress(address))
    fullWalletAddress = address
    initializeTokenTiles()
    fetchAndSetTokenPrices()
    fetchAndSetEthereumBalance(address)
    schedulePeriodicUpdates(address)

  @FXML
  def initialize(): Unit =
    println("Initializing WalletMenuController...")
    playSlideDownAnimation()
    receiveButton.setOnAction(_ => handleCopyAddress())
    exportButton.setOnAction(_ => println("Export wallet private key."))
    logoutButton.setOnAction(_ => handleLogout())

  private def handleLogout(): Unit =
    println("Logging out...")
    Platform.runLater(() => {
      val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/CreateWallet.fxml"))
      val root: Parent = loader.load()
      val stage = logoutButton.getScene.getWindow.asInstanceOf[Stage]
      stage.setScene(new Scene(root))
    })

  private def playSlideDownAnimation(): Unit =
    topContainer.setTranslateY(-(topContainer.getPrefHeight * 2))

    val slideDown = new TranslateTransition(Duration.millis(1000), topContainer)
    slideDown.setFromY(-(topContainer.getPrefHeight))
    slideDown.setToY(0)
    slideDown.setCycleCount(1)
    slideDown.setAutoReverse(false)

    val pause = new PauseTransition(Duration.millis(500)) // Add a short delay
    pause.setOnFinished(_ => slideDown.play())
    pause.play()

  private def initializeTokenTiles(): Unit =
    tokens.foreach { token =>
      val tokenTile = TokenTileFactory.createTokenTile(token)
      tokenTileCache(token.symbol) = (tokenTile.tile, tokenTile.priceLabel)
      tokenList.getChildren.add(tokenTile.tile)
    }

  private def fetchAndSetTokenPrices(): Unit =
    Future:
      BalancesManager.getAllTokenPrices(tokens.map(_.symbol))
    .onComplete:
      case Success(Right(updatedTokens)) =>
        tokens = updatedTokens
        updateTokenPrices()
      case Success(Left(error)) =>
        println(s"Failed to fetch token prices: $error")
      case Failure(ex) =>
        println(s"Error fetching token prices: ${ex.getMessage}")

  private def fetchAndSetEthereumBalance(address: String): Unit =
    Future:
      BalancesManager.getEthereumBalance(address)
    .onComplete:
      case Success(balanceTry) =>
        balanceTry match
          case Success(balance) =>
            val ethPrice = tokens.find(_.symbol == "ETH").map(_.price).getOrElse(BigDecimal(0))
            val totalUsdValue = BigDecimal(balance.toString) * ethPrice
            val cleanedBalance = new java.math.BigDecimal(balance.toString).stripTrailingZeros().toPlainString()
            Platform.runLater(() => {
              ethAmount.setText(s"$cleanedBalance ETH")
              usdAmount.setText(f"$$${totalUsdValue}%.2f")
            })
          case Failure(ex) =>
            println(s"Failed to fetch Ethereum balance: ${ex.getMessage}")
      case Failure(ex) =>
        println(s"Error fetching Ethereum balance: ${ex.getMessage}")

  private def updateTokenPrices(): Unit =
    Platform.runLater(() => {
      tokens.foreach { token =>
        tokenTileCache.get(token.symbol).foreach { case (_, priceLabel) =>
          priceLabel.setText(f"$$${token.price}%.2f")
        }
      }
    })

  private def schedulePeriodicUpdates(address: String): Unit =
    scheduler.scheduleAtFixedRate(
      () =>
        Platform.runLater(() => {
          fetchAndSetTokenPrices()
          fetchAndSetEthereumBalance(address)
        }),
      5,
      5,
      java.util.concurrent.TimeUnit.SECONDS
    )

  private def handleCopyAddress(): Unit =
    println(s"Copied address to clipboard: $fullWalletAddress")

  private def formatAddress(address: String): String =
    if address.length > 10 then
      s"${address.take(6)}...${address.takeRight(4)}"
    else address
