package com.dashayne.cryptodash

import javafx.fxml.FXMLLoader
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes.*
import scalafx.scene as sfxs
import javafx.scene as jfxs
import scalafx.collections.ObservableBuffer

object MainApp extends JFXApp3:
  // Window root pane
  var roots: Option[sfxs.layout.AnchorPane] = None

  override def start(): Unit =

    val rootResource = getClass.getResource("/com/dashayne/cryptodash/view/RootLayout.fxml")
    if (rootResource == null) throw new IllegalStateException("RootLayout FXML file not found.")

    val testResponseResource = getClass.getResource("/com/dashayne/cryptodash/view/TestResponse.fxml")
    if (testResponseResource == null) throw new IllegalStateException("TestResponse FXML file not found.")

    val loader = new FXMLLoader(rootResource)
    loader.load()

    roots = Option(loader.getRoot[jfxs.layout.AnchorPane])

    // Load TestResponse.fxml into the primary stage
    val testResponseLoader = new FXMLLoader(testResponseResource)
    val testResponseRoot = testResponseLoader.load[jfxs.layout.AnchorPane]

    stage = new PrimaryStage():
      title = "CryptoDash"
      resizable = false
      scene = new Scene():
        root = testResponseRoot
