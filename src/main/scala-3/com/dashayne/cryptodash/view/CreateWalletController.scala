package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.stage.Stage
import scalafx.scene.Scene
import scalafx.Includes._
import org.web3j.crypto.Credentials
import com.dashayne.cryptodash.model.WalletManager
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scala.util.{Success, Failure}

class CreateWalletController:

  @FXML
  private var createWalletButton: Button = _

  @FXML
  private var walletNameInput: TextField = _

  @FXML
  def initialize(): Unit =
    println(s"createWalletButton: $createWalletButton")
    println(s"walletNameInput: $walletNameInput")

    createWalletButton.setOnAction(_ => handleCreateWallet())

  private def handleCreateWallet(): Unit =
    val walletName = walletNameInput.getText.trim
    if walletName.isEmpty then
      print("Cannot be empty")
      return
//
    val password = "securepassword"
    WalletManager.createWallet(password, walletName) match
      case Success((name, address)) =>
        print("Success")
        val loader = new javafx.fxml.FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/WalletMenu.fxml"))
        val root = loader.load[javafx.scene.Parent]()
        val stage = createWalletButton.getScene.getWindow.asInstanceOf[Stage]
        stage.setScene(new Scene(root))
        val controller = loader.getController[WalletMenuController]
        controller.setWalletDetails(name, address)
      case Failure(ex: Throwable) =>
        print("Failure")
//        showAlert(AlertType.Error, "Error", s"Failed to create wallet: ${ex.getMessage}")
//
//  private def showAlert(alertType: Alert.AlertType, dialogTitle: String, content: String): Unit =
//    new Alert(alertType) {
//      initOwner(createWalletButton.getScene.getWindow)
//      this.setTitle(dialogTitle)
//      headerText = None
//      contentText = content
//    }.showAndWait()
//
//
