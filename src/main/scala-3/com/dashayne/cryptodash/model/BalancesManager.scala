package com.dashayne.cryptodash.model

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.utils.Convert
import sttp.client3.*

import scala.util.{Failure, Success, Try}
import ujson.*

import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern

object BalancesManager:

  private val web3j: Web3j = Web3j.build(new HttpService("https://eth-sepolia.g.alchemy.com/v2/v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"))
  private val apiClient = new ApiClient()
  private val baseUrl = "https://api.g.alchemy.com/prices/v1/tokens/by-symbol"
  private val apiKey = "v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"

  private val ethAddressPattern: Pattern = Pattern.compile("^0x[a-fA-F0-9]{40}$")

  def isValidEthereumAddress(address: String): Boolean =
    ethAddressPattern.matcher(address).matches()

  def getEthereumBalance(address: String): Try[BigDecimal] =
    if !isValidEthereumAddress(address) then
      Failure(new IllegalArgumentException(s"Invalid Ethereum address: $address"))
    else
      Try:
        val ethGetBalance: EthGetBalance = web3j
          .ethGetBalance(address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
          .send()
        val balanceInWei: BigInteger = ethGetBalance.getBalance
        Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER)

  def getAllTokenPrices(symbols: Seq[String]): Either[String, Seq[Token]] =
    val updatedSymbols = "ETH" +: symbols.distinct

    val apiUrl = s"$baseUrl?symbols=${updatedSymbols.mkString("&symbols=")}"
    val headers = Map(
      "accept" -> "application/json",
      "Authorization" -> s"Bearer $apiKey"
    )
    apiClient.get(apiUrl, headers) match
      case Right(response) =>
        Try:
          val json = ujson.read(response)
          val data = json("data").arr

          data.flatMap { tokenData =>
            val symbol = tokenData("symbol").str
            val prices = tokenData("prices").arr

            if prices.nonEmpty then
              val price = BigDecimal(prices(0)("value").str)
              val lastUpdated = prices(0)("lastUpdatedAt").str
              Some(Token(name = symbol, symbol = symbol, imageUrl = "", price = price, lastUpdated = lastUpdated))
            else
              println(s"Skipping token $symbol due to empty prices array.")
              Some(Token(name = symbol, symbol = symbol, imageUrl = "", price = BigDecimal(0), invalid = true))
          }.toSeq
        .toEither.left.map(ex => s"Failed to parse JSON: ${ex.getMessage}")
      case Left(error) => Left(error)

