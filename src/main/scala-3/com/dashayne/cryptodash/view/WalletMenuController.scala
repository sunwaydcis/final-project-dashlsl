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
import com.dashayne.cryptodash.model.{BalancesManager, Favourites, Token, Wallet, WalletManager}
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import javafx.application.Platform
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

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
  @FXML
  private var addTokenButton: Button = _

  private var wallet: Wallet = _

  private val scheduler = Executors.newScheduledThreadPool(1)
  private var fullWalletAddress: String = ""
  private var tokens: Seq[Token] = _
  private val tokenTileCache: mutable.Map[String, (HBox, Label)] = mutable.Map()

  @FXML
  def initialize(): Unit =
    println("Initializing WalletMenuController...")
    playSlideDownAnimation()
    receiveButton.setOnAction(_ => handleCopyAddress())
    sendButton.setOnAction(_ => openSendTransactionView())
    exportButton.setOnAction(_ => handleExport())
    logoutButton.setOnAction(_ => handleLogout())
    addTokenButton.setOnAction(_ => openAddTokenView())

  private def handleLogout(): Unit =
    println("Logging out...")
    scheduler.shutdown()
    Platform.runLater(() => {
      val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/CreateWallet.fxml"))
      val root: Parent = loader.load()
      val stage = logoutButton.getScene.getWindow.asInstanceOf[Stage]
      stage.setScene(new Scene(root))
    })

  private def openAddTokenView(): Unit =
    Platform.runLater(() =>
      try
        val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/AddToken.fxml"))
        val root: Parent = loader.load()

        // Get the controller and pass the wallet address
        val controller = loader.getController[AddTokenController]
        controller.setWallet(wallet)

        val stage = new Stage()
        stage.setTitle("Add Token")
        stage.setScene(new Scene(root))
        stage.setOnCloseRequest(_ =>
          wallet.refreshFavourites() // Refresh wallet's favourites after closing the view
          initializeTokenTiles() // Reinitialize token tiles
          fetchAndSetTokenPrices() // Refresh prices
        )
        stage.show()

      catch
        case ex: Exception =>
          println(s"Failed to open Add Token view: ${ex.getMessage}")
    )


  private def handleExport(): Unit =
    println("Exporting private key...")
    val password = "securepassword" // Replace with actual logic to get the user's password

    WalletManager.getPrivateKey(password, wallet.getFileName) match
      case Success(privateKey) =>
        val clipboard = Clipboard.getSystemClipboard
        val content = new ClipboardContent
        content.putString(privateKey)
        clipboard.setContent(content)
        println(s"Private key for wallet '${wallet.getName}' copied to clipboard!")
      case Failure(ex) =>
        println(s"Failed to export private key for wallet '${wallet.getName}': ${ex.getMessage}")


  private def openSendTransactionView(): Unit =
    Platform.runLater(() =>
      try
        val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/SendTransaction.fxml"))
        val root: Parent = loader.load()

        // Pass the wallet instance to the controller
        val controller = loader.getController[SendTransactionController]
        controller.setWallet(wallet)

        val stage = new Stage()
        stage.setTitle("Send Transaction")
        stage.setScene(new Scene(root))
        stage.show()
      catch
        case ex: Exception =>
          println(s"Failed to open Send Transaction view: ${ex.getMessage}")
    )

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
    tokenList.getChildren.clear()
    tokenTileCache.clear() // Clear the cache to avoid stale entries

    wallet.getFavourites.foreach { tokenKey =>
      Favourites.getTokenDetails(tokenKey) match
        case Some(details) =>
          val name = details("name")
          val symbol = details("symbol")
          val imageUrl = s"https://cryptologos.cc/logos/thumbs/$tokenKey.png"
          val token = Token(name, symbol, imageUrl)
          val tokenTile = TokenTileFactory.createTokenTile(token)
          tokenTileCache.put(symbol, (tokenTile.tile, tokenTile.priceLabel)) // Cache for price updates
          tokenList.getChildren.add(tokenTile.tile)
        case None =>
          println(s"Token details not found for key: $tokenKey")
    }


  private def fetchAndSetTokenPrices(): Unit =
    if wallet == null then
      println("No wallet instance found.")
      return

    val favourites = wallet.getFavourites

    // Always include ETH and ensure unique symbols
    val symbols = ("ETH" +: favourites.flatMap(tokenKey =>
      Favourites.getTokenDetails(tokenKey).map(_("symbol"))
    )).distinct

    if symbols.isEmpty then
      println("No valid tokens found for the user.")
      return

    // Reset tokens to avoid stale data
    tokens = Seq.empty

    println(symbols)
    println(favourites)

    Future:
      BalancesManager.getAllTokenPrices(symbols)
    .onComplete:
      case Success(Right(updatedTokens)) =>
        if updatedTokens.isEmpty then
          println("No tokens returned from the API.")
        else
          // Filter out tokens with empty `prices` or errors
          tokens = updatedTokens
          println(s"Updated tokens: $tokens") // Debugging statement
          updateTokenPrices()
          fetchAndSetEthereumBalance(wallet.getAddress) // Re-fetch ETH balance with the updated prices
      case Success(Left(error)) =>
        println(s"Failed to fetch token prices: $error")
      case Failure(ex) =>
        println(s"Error fetching token prices: ${ex.getMessage}")


  private def fetchAndSetEthereumBalance(address: String): Unit =
    if tokens == null || tokens.isEmpty then
      println("No tokens available to calculate Ethereum balance. Ensure tokens are fetched first.")
      return

    Future:
      BalancesManager.getEthereumBalance(address)
    .onComplete:
      case Success(balanceTry) =>
        balanceTry match
          case Success(balance) =>
            val ethPrice = tokens.find(_.symbol == "ETH").map(_.price).getOrElse(BigDecimal(0))
            if ethPrice == BigDecimal(0) then
              println("ETH price not found in tokens. Ensure tokens include ETH.")
            else
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
          if token.invalid then
            priceLabel.setText("No Price Data")
          else
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
      0,
      12,
      java.util.concurrent.TimeUnit.SECONDS
    )

  private def handleCopyAddress(): Unit =
    val clipboard = Clipboard.getSystemClipboard
    val content = new ClipboardContent
    content.putString(wallet.getAddress) // Copy the wallet address to clipboard
    clipboard.setContent(content)
    println(s"Copied address to clipboard: ${wallet.getAddress}")


  private def formatAddress(address: String): String =
    if address.length > 10 then
      s"${address.take(6)}...${address.takeRight(4)}"
    else address

  def setWallet(wallet: Wallet): Unit =
    this.wallet = wallet
    walletName.setText(wallet.getName)
    walletAddress.setText(formatAddress(wallet.getAddress))
    initializeTokenTiles()
    fetchAndSetTokenPrices()
    fetchAndSetEthereumBalance(wallet.getAddress)
    schedulePeriodicUpdates(wallet.getAddress)

