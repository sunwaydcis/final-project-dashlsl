package com.dashayne.cryptodash.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, TableColumn, TableView}
import javafx.scene.control.cell.PropertyValueFactory
import com.dashayne.cryptodash.model.{Favourites, Wallet}
import javafx.application.Platform
import javafx.scene.image.{Image, ImageView}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

class AddTokenController:

  @FXML
  private var tokenTable: TableView[TokenRow] = _
  @FXML
  private var logoColumn: TableColumn[TokenRow, ImageView] = _
  @FXML
  private var nameColumn: TableColumn[TokenRow, String] = _
  @FXML
  private var symbolColumn: TableColumn[TokenRow, String] = _
  @FXML
  private var addButtonColumn: TableColumn[TokenRow, Button] = _

  private var wallet: Wallet = _

  @FXML
  def initialize(): Unit =
    logoColumn.setCellValueFactory(new PropertyValueFactory[TokenRow, ImageView]("logo"))
    nameColumn.setCellValueFactory(new PropertyValueFactory[TokenRow, String]("name"))
    symbolColumn.setCellValueFactory(new PropertyValueFactory[TokenRow, String]("symbol"))
    addButtonColumn.setCellValueFactory(new PropertyValueFactory[TokenRow, Button]("addButton"))

  def setWallet(wallet: Wallet): Unit =
    this.wallet = wallet
    println(s"Wallet set: ${wallet.getName}")
    populateTokenTable()

  private def populateTokenTable(): Unit =
    if wallet == null then
      println("Wallet not set. Cannot populate token table.")
      return

    tokenTable.getItems.clear() // Clear existing rows
    val tokens = Favourites.getAllTokens.toSeq

    val batchSize = 20
    tokens.grouped(batchSize).foreach { batch =>
      Future {
        val rows = batch.map { case (key, details) =>
          val name = details("name")
          val symbol = details("symbol")
          val id = details("id")
          val imageUrl = s"https://cryptologos.cc/logos/thumbs/$key.png"

          val isFavourite = wallet.getFavourites.contains(id)

          val actionButton = new Button(if isFavourite then "Remove" else "Add")
          actionButton.setOnAction(_ =>
            if isFavourite then
              removeTokenFromFavourites(id, actionButton)
            else
              addTokenToFavorites(id, actionButton)
          )

          val logo = new ImageView(imageUrl)
          logo.setFitHeight(24)
          logo.setFitWidth(24)

          TokenRow(logo, name, symbol, actionButton)
        }

        Platform.runLater(() =>
          tokenTable.getItems.addAll(rows: _*)
          nameColumn.setSortType(TableColumn.SortType.ASCENDING)
          tokenTable.getSortOrder.add(nameColumn)
          tokenTable.sort()
        )
      }
    }

  private def addTokenToFavorites(tokenKey: String, button: Button): Unit =
    Favourites.addFavourite(wallet.getAddress, tokenKey) match
      case Success(_) =>
        println(s"Added $tokenKey to favorites.")
        wallet.refreshFavourites() // Refresh the wallet's favourites
        refreshTokenRow(tokenKey, button, isFavourite = true)
      case Failure(ex) =>
        println(s"Failed to add $tokenKey to favourites: ${ex.getMessage}")

  private def removeTokenFromFavourites(tokenKey: String, button: Button): Unit =
    Favourites.removeFavourite(wallet.getAddress, tokenKey) match
      case Success(_) =>
        println(s"Removed $tokenKey from favourites.")
        wallet.refreshFavourites() // Refresh the wallet's favourites
        refreshTokenRow(tokenKey, button, isFavourite = false)
      case Failure(ex) =>
        println(s"Failed to remove $tokenKey from favourites: ${ex.getMessage}")

  private def refreshTokenRow(tokenKey: String, button: Button, isFavourite: Boolean): Unit =
    Platform.runLater(() =>
      // Update the button text based on the new state
      button.setText(if isFavourite then "Remove" else "Add")

      // Update the button's action to reflect the new state
      button.setOnAction(_ =>
        if isFavourite then
          removeTokenFromFavourites(tokenKey, button)
        else
          addTokenToFavorites(tokenKey, button)
      )
    )
