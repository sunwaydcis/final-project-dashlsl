package com.dashayne.cryptodash.model

import org.web3j.crypto.{Credentials, RawTransaction, TransactionEncoder}
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.{EthEstimateGas, EthSendTransaction}
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.{Convert, Numeric}

import java.math.BigInteger
import scala.util.{Failure, Success, Try}

object TransactionManager extends Web3Manager:

  /**
   * Send a transaction from the sender to the recipient.
   *
   * @param senderAddress Sender's Ethereum address.
   * @param privateKey Sender's private key.
   * @param recipientAddress Recipient's Ethereum address.
   * @param amount Amount to send in Ether.
   * @return Transaction hash as a Try[String].
   */
  def sendTransaction(
                       senderAddress: String,
                       privateKey: String,
                       recipientAddress: String,
                       amount: BigDecimal
                     ): Try[String] =
    if !isValidEthereumAddress(recipientAddress) then
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

        val sendTransactionResponse: EthSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
        if sendTransactionResponse.hasError then
          throw new RuntimeException(s"Transaction failed: ${sendTransactionResponse.getError.getMessage}")
        else
          sendTransactionResponse.getTransactionHash

  /**
   * Estimate the gas required for a transaction.
   *
   * @param senderAddress Sender's Ethereum address.
   * @param recipientAddress Recipient's Ethereum address.
   * @param value Transaction value in Wei.
   * @return Estimated gas limit as a Try[BigInteger].
   */
  private def estimateGas(senderAddress: String, recipientAddress: String, value: BigInteger): Try[BigInteger] =
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
