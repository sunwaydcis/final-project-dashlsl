package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.layout.VBox
import javafx.stage.Stage
import scalafx.scene.Scene
import scalafx.Includes._
import org.web3j.crypto.Credentials
import com.dashayne.cryptodash.model.WalletManager
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import java.io.File
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import scala.util.{Success, Failure}

class CreateWalletController:

  @FXML
  private var createWalletButton: Button = _
  @FXML
  private var walletNameInput: TextField = _
  @FXML
  private var walletList: VBox = _

  private val walletDirectory = new File(System.getProperty("user.home") + "/cryptodash_wallets")

  @FXML
  def initialize(): Unit =
    println(s"createWalletButton: $createWalletButton")
    println(s"walletNameInput: $walletNameInput")
    println(s"walletList: $walletList")

    createWalletButton.setOnAction(_ => handleCreateWallet())
    loadWalletList()

  private def handleCreateWallet(): Unit =
    val walletName = walletNameInput.getText.trim
    if walletName.isEmpty then
      println("Wallet Name Cannot be Empty")
      return

    val password = "securepassword"
    WalletManager.createWallet(password, walletName) match
      case Success((name, address)) =>
        walletNameInput.clear()
        loadWalletList()
      case Failure(ex: Throwable) =>
        println(s"Failed to create wallet: ${ex.getMessage}")

  private def loadWalletList(): Unit =
    walletList.getChildren.clear()
    if walletDirectory.exists() && walletDirectory.isDirectory then
      walletDirectory.listFiles()
        .filter(_.getName.endsWith(".json"))
        .foreach { walletFile =>
          val walletName = WalletManager.getWalletName(walletFile.getName).getOrElse("Unknown Wallet")
          val button = new Button(walletName)
          button.setPrefWidth(300)
          button.getStyleClass.add("wallet-list-item")
          button.setOnMouseClicked((_: MouseEvent) => handleWalletSelection(walletFile.getName))
          walletList.getChildren.add(button)
        }

  private def handleWalletSelection(walletFileName: String): Unit =
    val password = "securepassword"
    WalletManager.loadWallet(password, walletFileName) match
      case Success(credentials) =>
        val walletName = WalletManager.getWalletName(walletFileName).getOrElse("Unknown Wallet")
        val loader = new javafx.fxml.FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/WalletMenu.fxml"))
        val root = loader.load[javafx.scene.Parent]()
        val stage = createWalletButton.getScene.getWindow.asInstanceOf[Stage]
        stage.setScene(new Scene(root))
        val controller = loader.getController[WalletMenuController]
        controller.setWalletDetails(walletName, credentials.getAddress)
      case Failure(ex) =>
        println(s"Failed to load wallet: ${ex.getMessage}")

