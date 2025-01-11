package com.dashayne.cryptodash.model

/**
 * Represents a cryptocurrency token with various attributes.
 * @param name        The full name of the cryptocurrency (e.g., "Bitcoin").
 * @param symbol      The symbol representing the cryptocurrency (e.g., "BTC").
 * @param imageUrl    URL of the token's logo or image.
 * @param price       Current price of the token in the specified currency. Defaults to 0 if not provided.
 * @param lastUpdated Timestamp of the last update for the token's price. Defaults to an empty string if not provided.
 * @param invalid     Indicates whether the token is marked as invalid. Defaults to false.
 */
case class Token(
                  name: String,
                  symbol: String,
                  imageUrl: String,
                  price: BigDecimal = BigDecimal(0),
                  lastUpdated: String = "",
                  invalid: Boolean = false
                )
