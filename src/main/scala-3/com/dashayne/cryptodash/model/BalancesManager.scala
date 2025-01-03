package com.dashayne.cryptodash.model

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.utils.Convert

import scala.util.{Try, Success, Failure}
import java.math.BigDecimal
import java.math.BigInteger

object BalancesManager:

  // Initialize Web3j with an Ethereum node endpoint (e.g., Infura)
  private val web3j: Web3j = Web3j.build(new HttpService("https://eth-sepolia.g.alchemy.com/v2/v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"))

  /**
   * Fetch the Ethereum balance for a given wallet address.
   * @param address The wallet address.
   * @return The balance in Ether as a `BigDecimal`.
   */
  def getEthereumBalance(address: String): Try[BigDecimal] =
    Try {
      // Fetch the balance in Wei
      val ethGetBalance: EthGetBalance = web3j
        .ethGetBalance(address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
        .send()

      // Convert Wei to Ether
      val balanceInWei: BigInteger = ethGetBalance.getBalance
      Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER)
    }

  /**
   * Get the wallet address from WalletManager.
   * @param walletFileName The wallet file name.
   * @param password The wallet password.
   * @return The wallet address as a `String`.
   */
  def getAddressFromWalletManager(walletFileName: String, password: String): Try[String] =
    WalletManager.loadWallet(password, walletFileName).map(_.getAddress)

  def calculateTotalValueInUSD(balance: BigDecimal, pricePerEth: BigDecimal): BigDecimal =
    balance.multiply(pricePerEth)