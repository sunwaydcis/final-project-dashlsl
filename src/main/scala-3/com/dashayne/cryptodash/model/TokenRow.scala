package com.dashayne.cryptodash.view

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.scene.control.Button
import javafx.scene.image.ImageView

class TokenRow(
                logo: ImageView,
                name: String,
                symbol: String,
                addButton: Button
              ):

  private val logoProp: ObjectProperty[ImageView] = new SimpleObjectProperty(logo)
  private val nameProp: ObjectProperty[String] = new SimpleObjectProperty(name)
  private val symbolProp: ObjectProperty[String] = new SimpleObjectProperty(symbol)
  private val addButtonProp: ObjectProperty[Button] = new SimpleObjectProperty(addButton)

  def getLogo: ImageView = logoProp.get()
  def logoProperty: ObjectProperty[ImageView] = logoProp

  def getName: String = nameProp.get()
  def nameProperty: ObjectProperty[String] = nameProp

  def getSymbol: String = symbolProp.get()
  def symbolProperty: ObjectProperty[String] = symbolProp

  def getAddButton: Button = addButtonProp.get()
  def addButtonProperty: ObjectProperty[Button] = addButtonProp
