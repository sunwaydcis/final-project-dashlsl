package com.dashayne.cryptodash.model

case class Token(
                  name: String,
                  symbol: String,
                  imageUrl: String,
                  price: BigDecimal = BigDecimal(0), // Default price
                  lastUpdated: String = "" // Default last updated time
                )
