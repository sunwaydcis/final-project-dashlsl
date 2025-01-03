package com.dashayne.cryptodash.model

import sttp.client3.*
import sttp.model.{MediaType, Header}

class ApiClient:

  // Backend used for making requests (can be shared for multiple calls)
  private val backend = HttpURLConnectionBackend()

  /**
   * Make a GET request to the specified URL with optional headers
   *
   * @param apiUrl The API URL to call
   * @param headers Optional headers to include in the request
   * @return The response body as a String, or an error message
   */
  def get(apiUrl: String, headers: Map[String, String] = Map.empty): Either[String, String] =
    val request = basicRequest.get(uri"$apiUrl").headers(headers)
    val response = request.send(backend)
    response.body // Return the response body (Right for success, Left for error)

  /**
   * Make a POST request to the specified URL with a JSON payload and optional headers
   *
   * @param apiUrl The API URL to call
   * @param jsonPayload The JSON string to send in the body
   * @param headers Optional headers to include in the request
   * @return The response body as a String, or an error message
   */
  def post(apiUrl: String, jsonPayload: String, headers: Map[String, String] = Map.empty): Either[String, String] =
    val request = basicRequest
      .post(uri"$apiUrl")
      .contentType(MediaType.ApplicationJson)
      .headers(headers)
      .body(jsonPayload)
    val response = request.send(backend)
    response.body // Return the response body (Right for success, Left for error)

  // Cleanup resources (if necessary)
  def close(): Unit = backend.close()
