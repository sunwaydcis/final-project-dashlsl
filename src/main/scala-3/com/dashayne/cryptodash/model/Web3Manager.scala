package com.dashayne.cryptodash.model

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert

import java.math.BigDecimal
import java.util.regex.Pattern

abstract class Web3Manager:
  protected val web3j: Web3j = Web3j.build(new HttpService("https://eth-sepolia.g.alchemy.com/v2/v4Q7lRu_eLH3PV_jDZFU823z8Xk9oDL4"))
  private val ethAddressPattern: Pattern = Pattern.compile("^0x[a-fA-F0-9]{40}$")

  def isValidEthereumAddress(address: String): Boolean =
    ethAddressPattern.matcher(address).matches()

  def weiToEth(wei: java.math.BigInteger): BigDecimal =
    Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER)
