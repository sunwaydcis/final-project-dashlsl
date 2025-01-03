package com.dashayne.cryptodash

import com.dashayne.cryptodash.model.WalletManager
import com.dashayne.cryptodash.view.WalletMenuController
import javafx.fxml.FXMLLoader
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes.*
import scalafx.scene as sfxs
import javafx.scene as jfxs
import scalafx.collections.ObservableBuffer
import scala.util.{Try, Success, Failure}
import java.io.File

object MainApp extends JFXApp3:

  var roots: Option[sfxs.layout.AnchorPane] = None

  override def start(): Unit =
    try
      val walletDirectory = new File(System.getProperty("user.home") + "/cryptodash_wallets")

      // Determine which FXML to load based on wallet existence
      if walletDirectory.exists() && walletDirectory.listFiles().exists(_.getName.endsWith(".json")) then
        println("Wallet found. Loading WalletMenu.fxml...")
        val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/WalletMenu.fxml"))
        val root = loader.load[jfxs.layout.AnchorPane]()
        val controller = loader.getController[WalletMenuController]

        // Load wallet details
        val walletFile = walletDirectory.listFiles().find(_.getName.endsWith(".json")).get
        val credentials = WalletManager.loadWallet("securepassword", walletFile.getName).get
        controller.setWalletDetails(WalletManager.getWalletName(walletFile.getName).get, credentials.getAddress)

        roots = Option(root)
      else
        println("No wallet found. Loading CreateWallet.fxml...")
        val loader = new FXMLLoader(getClass.getResource("/com/dashayne/cryptodash/view/CreateWallet.fxml"))
        val root = loader.load[jfxs.layout.AnchorPane]()
        roots = Option(root)

      // Ensure roots is set and attach it to the scene
      roots match
        case Some(rootPane) =>
          println("Loading root pane into the scene...")
          stage = new PrimaryStage:
            title = "CryptoDash"
            scene = new Scene:
              root = rootPane
        case None =>
          throw new IllegalStateException("Failed to initialize root pane!")
    catch
      case ex: Exception =>
        ex.printStackTrace()
        System.err.println("Failed to start application due to an error.")
