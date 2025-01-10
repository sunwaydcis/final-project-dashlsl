package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField, ButtonType}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.control.ButtonType.OK
import javafx.stage.Stage
import scalafx.scene.Scene
import scalafx.Includes.*
import org.web3j.crypto.Credentials
import com.dashayne.cryptodash.model.{Wallet, WalletManager}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

import java.io.File
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent

import scala.util.{Failure, Success}

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
          val walletAddress = WalletManager.getWalletAddress("securepassword", walletFile.getName).getOrElse("")

          if walletAddress.nonEmpty then
            val wallet = new Wallet(walletFile.getName, walletAddress, walletName)

            // Create an HBox to hold the wallet button and delete button
            val walletEntry = new HBox()
            walletEntry.setSpacing(5) // Reduce spacing between buttons
            walletEntry.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 2 0 2 0;") // Adjust alignment and padding

            // Wallet button
            val button = new Button(walletName)
            button.setPrefWidth(300)
            button.getStyleClass.add("wallet-list-item")
            button.setOnMouseClicked((_: MouseEvent) => handleWalletSelection(wallet))

            // Delete button
            val deleteButton = new Button("X")
            deleteButton.setPrefWidth(10)
            deleteButton.getStyleClass.add("delete-wallet-button")
            deleteButton.setOnAction(_ => handleWalletDeletion(walletFile, wallet))

            walletEntry.getChildren.addAll(button, deleteButton)
            walletList.getChildren.add(walletEntry)
        }


  private def handleWalletSelection(wallet: Wallet): Unit =
    val loader = new javafx.fxml.FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/WalletMenu.fxml"))
    val root = loader.load[javafx.scene.Parent]()
    val stage = createWalletButton.getScene.getWindow.asInstanceOf[Stage]
    stage.setScene(new Scene(root))
    val controller = loader.getController[WalletMenuController]
    controller.setWallet(wallet)

  private def handleWalletDeletion(walletFile: File, wallet: Wallet): Unit =
    val alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION)
    alert.setTitle("Delete Wallet")
    alert.setHeaderText("Confirm Deletion")
    alert.setContentText(s"Are you sure you want to delete the wallet: ${wallet.getName}?")

    val result = alert.showAndWait()

    if result.isPresent && result.get == javafx.scene.control.ButtonType.OK then
      if walletFile.exists() && walletFile.delete() then
        println(s"Wallet ${wallet.getName} deleted successfully.")
        loadWalletList() // Refresh the wallet list after deletion
      else
        println(s"Failed to delete wallet ${wallet.getName}.")
