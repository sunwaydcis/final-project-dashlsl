package com.dashayne.cryptodash.model

import org.web3j.crypto.{Credentials, RawTransaction, TransactionEncoder}
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric

import java.math.BigInteger
import scala.util.{Failure, Success, Try}

object TransactionManager:

  // Initialize a Web3j instance to interact with the Ethereum blockchain
  private val web3j: Web3j = Web3j.build(new HttpService("https://eth-sepolia.g.alchemy.com/v2/v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"))

  /**
   * Sends an Ethereum transaction from the sender to the recipient.
   * @param senderAddress    The sender's Ethereum address.
   * @param privateKey       The private key of the sender's wallet.
   * @param recipientAddress The recipient's Ethereum address.
   * @param amount           The amount to send, in Ether.
   * @return A Try containing the transaction hash on success, or an error on failure.
   */
  def sendTransaction(
                       senderAddress: String,
                       privateKey: String,
                       recipientAddress: String,
                       amount: BigDecimal
                     ): Try[String] =
    if !BalancesManager.isValidEthereumAddress(recipientAddress) then
      Failure(new IllegalArgumentException(s"Invalid recipient address: $recipientAddress"))
    else
      Try:
        val credentials = Credentials.create(privateKey)
        val nonce = web3j.ethGetTransactionCount(senderAddress, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
          .send()
          .getTransactionCount

        val value = Convert.toWei(amount.bigDecimal, Convert.Unit.ETHER).toBigInteger
        val gasPrice = web3j.ethGasPrice().send().getGasPrice
        val gasLimit = estimateGas(senderAddress, recipientAddress, value).getOrElse(DefaultGasProvider.GAS_LIMIT)

        val rawTransaction = RawTransaction.createEtherTransaction(
          nonce,
          gasPrice,
          gasLimit,
          recipientAddress,
          value
        )

        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
        val hexValue = Numeric.toHexString(signedMessage)

        val sendTransactionResponse = web3j.ethSendRawTransaction(hexValue).send()
        if sendTransactionResponse.hasError then
          throw new RuntimeException(s"Transaction failed: ${sendTransactionResponse.getError.getMessage}")
        else
          sendTransactionResponse.getTransactionHash

  /**
   * Estimates the gas required for a transaction.
   * @param senderAddress    The sender's Ethereum address.
   * @param recipientAddress The recipient's Ethereum address.
   * @param value            The amount of Ether being transferred, in Wei.
   * @return A Try containing the estimated gas limit, or an error on failure.
   */
  private def estimateGas(
                           senderAddress: String,
                           recipientAddress: String,
                           value: BigInteger
                         ): Try[BigInteger] =
    Try:
      val transaction = Transaction.createEtherTransaction(
        senderAddress,
        null,
        null,
        null,
        recipientAddress,
        value
      )
      val estimate: EthEstimateGas = web3j.ethEstimateGas(transaction).send()
      if estimate.hasError then
        throw new RuntimeException(s"Gas estimation failed: ${estimate.getError.getMessage}")
      else
        estimate.getAmountUsed
