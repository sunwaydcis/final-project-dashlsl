package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.text.Text
import sttp.client3.*

class TestResponseController:
  // FXML elements
  @FXML private var URLInput: TextField = _
  @FXML private var SendCall: Button = _
  @FXML private var ResponseText: Text = _

  // Backend for HTTP requests
  private val backend = HttpURLConnectionBackend()

  @FXML
  def initialize(): Unit =
    SendCall.setOnAction { _ =>
      val url = Option(URLInput.getText).map(_.trim).getOrElse("")
      if url.nonEmpty then
        val headers = Map(
          "accept" -> "application/json",
          "x-cg-pro-api-key" -> "CG-QSsiJWo5w6SAMfttPxcDeGn6"
        )
        val request = basicRequest.get(uri"$url").headers(headers)
        val response = request.send(backend)
        response.body match
          case Right(body) => ResponseText.setText(body)
          case Left(error) => ResponseText.setText(s"Error: $error")
      else
        ResponseText.setText("Please enter a valid URL.")
    }