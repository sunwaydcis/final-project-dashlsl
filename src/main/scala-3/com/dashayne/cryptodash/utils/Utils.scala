package com.dashayne.cryptodash.utils

import javafx.scene.input.{ClipboardContent, Clipboard}
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

object Utils:

  def showAlert(title: String, message: String): Unit =
    val alert = new Alert(AlertType.INFORMATION)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()

  def copyToClipboard(text: String): Unit =
    val clipboard = Clipboard.getSystemClipboard
    val content = new ClipboardContent
    content.putString(text)
    clipboard.setContent(content)
    println(s"Copied to clipboard: $text")