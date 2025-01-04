package com.dashayne.cryptodash.model

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.utils.Convert
import sttp.client3.*
import scala.util.{Try, Success, Failure}
import ujson.*
import java.math.BigDecimal
import java.math.BigInteger

object BalancesManager:

  private val web3j: Web3j = Web3j.build(new HttpService("https://eth-sepolia.g.alchemy.com/v2/v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"))
  private val apiClient = new ApiClient()
  private val baseUrl = "https://api.g.alchemy.com/prices/v1/tokens/by-symbol"
  private val apiKey = "v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"

  def getEthereumBalance(address: String): Try[BigDecimal] =
    Try:
      val ethGetBalance: EthGetBalance = web3j
        .ethGetBalance(address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
        .send()
      val balanceInWei: BigInteger = ethGetBalance.getBalance
      Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER)

  def getAllTokenPrices(symbols: Seq[String]): Either[String, Seq[Token]] =
    val apiUrl = s"$baseUrl?symbols=${symbols.mkString("&symbols=")}"
    val headers = Map(
      "accept" -> "application/json",
      "Authorization" -> s"Bearer $apiKey"
    )
    apiClient.get(apiUrl, headers) match
      case Right(response) =>
        Try:
          val json = ujson.read(response)
          val data = json("data").arr
          data.map { tokenData =>
            val symbol = tokenData("symbol").str
            val price = BigDecimal(tokenData("prices")(0)("value").str)
            val lastUpdated = tokenData("prices")(0)("lastUpdatedAt").str
            Token(name = symbol, symbol = symbol, imageUrl = "", price = price, lastUpdated = lastUpdated)
          }.toSeq
        .toEither.left.map(ex => s"Failed to parse JSON: ${ex.getMessage}")
      case Left(error) => Left(error)
