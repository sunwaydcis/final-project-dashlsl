package com.dashayne.cryptodash.view

import javafx.scene.control.Label
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, VBox}
import com.dashayne.cryptodash.model.Token

object TokenTileFactory:

  case class TokenTile(tile: HBox, priceLabel: Label)

  def createTokenTile(token: Token): TokenTile =
    // Create an HBox for the token tile
    val tile = new HBox()
    tile.getStyleClass.add("crypto-token-tile") // Add the style class
    tile.setSpacing(12)

    // Create an ImageView for the token logo and center it
    val logo = new ImageView()
    logo.setFitWidth(35)
    logo.setFitHeight(35)
    if token.imageUrl.nonEmpty then
      try logo.setImage(new Image(token.imageUrl))
      catch case _: Exception => println(s"Failed to load image for ${token.name}")

    val logoContainer = new VBox(logo)
    logoContainer.setAlignment(javafx.geometry.Pos.CENTER) // Center the image

    // Create a VBox for the token details
    val details = new VBox()
    details.setSpacing(0)

    // Create labels for the token price and name
    val priceLabel = new Label("$0.000")
    priceLabel.getStyleClass.add("crypto-token-price") // Add style class if needed

    val nameLabel = new Label(token.name)
    nameLabel.getStyleClass.add("crypto-token-name") // Add style class if needed

    // Add priceLabel above nameLabel
    details.getChildren.addAll(priceLabel, nameLabel)

    // Add logo and details to the tile
    tile.getChildren.addAll(logoContainer, details)

    TokenTile(tile, priceLabel)
