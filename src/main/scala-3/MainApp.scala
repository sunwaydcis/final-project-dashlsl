package com.dashayne.cryptodash

import com.dashayne.cryptodash.view.CreateWalletController
import javafx.fxml.FXMLLoader
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.layout.AnchorPane
import javafx.scene as jfxs

object MainApp extends JFXApp3:

  override def start(): Unit =
    try
      val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/CreateWallet.fxml"))
      val rootPane: AnchorPane = loader.load[jfxs.layout.AnchorPane]() // Explicitly define rootPane
      stage = new PrimaryStage:
        title = "Dash Wallet"
        scene = new Scene:
          root = rootPane // Use explicitly defined rootPane here
      stage.setResizable(false)
    catch
      case ex: Exception =>
        ex.printStackTrace()
        System.err.println("Failed to start application due to an error.")
