package com.dashayne.cryptodash.view

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.scene.control.Button
import javafx.scene.image.ImageView

/**
 * Represents a row in the UI that displays token information.
 * Each row includes the token's logo, name, symbol, and an "Add" button for interaction.
 * @param logo      The ImageView representing the token's logo.
 * @param name      The name of the token (e.g., "Bitcoin").
 * @param symbol    The symbol of the token. (e.g., "BTC").
 * @param addButton The button used to add the token to favourites.
 */
class TokenRow(
                logo: ImageView,
                name: String,
                symbol: String,
                addButton: Button
              ):

  // Define properties for each field to enable binding and reactive updates in the UI
  private val logoProp: ObjectProperty[ImageView] = new SimpleObjectProperty(logo)
  private val nameProp: ObjectProperty[String] = new SimpleObjectProperty(name)
  private val symbolProp: ObjectProperty[String] = new SimpleObjectProperty(symbol)
  private val addButtonProp: ObjectProperty[Button] = new SimpleObjectProperty(addButton)

  // Getters and property accessors for reactive UI updates
  def getLogo: ImageView = logoProp.get()
  def logoProperty: ObjectProperty[ImageView] = logoProp

  def getName: String = nameProp.get()
  def nameProperty: ObjectProperty[String] = nameProp

  def getSymbol: String = symbolProp.get()
  def symbolProperty: ObjectProperty[String] = symbolProp

  def getAddButton: Button = addButtonProp.get()
  def addButtonProperty: ObjectProperty[Button] = addButtonProp
