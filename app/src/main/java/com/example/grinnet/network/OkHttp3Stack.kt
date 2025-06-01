package com.example.grinnet.network

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.HttpResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OkHttp3Stack(private val client: OkHttpClient) : BaseHttpStack() {

    override fun executeRequest(
        request: Request<*>,
        additionalHeaders: Map<String, String>
    ): HttpResponse {
        val timeout = request.timeoutMs.toLong()
        val clientWithTimeout = client.newBuilder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()

        val builder = okhttp3.Request.Builder()
            .url(request.url)

        // Combina headers de Volley y headers adicionales
        val headers = HashMap<String, String>()
        headers.putAll(request.headers)
        headers.putAll(additionalHeaders)
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }

        // Configura el método y el cuerpo de la solicitud
        val body = request.body
        val contentType = request.bodyContentType
        when (request.method) {
            Request.Method.DEPRECATED_GET_OR_POST -> {
                if (body != null) {
                    builder.post(body.toRequestBody(contentType.toMediaTypeOrNull()))
                }
            }
            Request.Method.GET -> builder.get()
            Request.Method.DELETE -> {
                if (body != null) {
                    builder.delete(body.toRequestBody(contentType.toMediaTypeOrNull()))
                } else {
                    builder.delete()
                }
            }
            Request.Method.POST -> {
                builder.post(body?.toRequestBody(contentType.toMediaTypeOrNull()) ?: byteArrayOf()
                    .toRequestBody(contentType.toMediaTypeOrNull()))
            }
            Request.Method.PUT -> {
                builder.put(body?.toRequestBody(contentType.toMediaTypeOrNull()) ?: byteArrayOf()
                    .toRequestBody(contentType.toMediaTypeOrNull()))
            }
            Request.Method.HEAD -> builder.head()
            Request.Method.OPTIONS -> builder.method("OPTIONS", null)
            Request.Method.TRACE -> builder.method("TRACE", null)
            Request.Method.PATCH -> {
                builder.patch(body?.toRequestBody(contentType.toMediaTypeOrNull()) ?: byteArrayOf()
                    .toRequestBody(contentType.toMediaTypeOrNull()))
            }
            else -> throw IllegalStateException("Método desconocido: ${request.method}")
        }

        val okHttpRequest = builder.build()
        val response = clientWithTimeout.newCall(okHttpRequest).execute()

        val responseHeaders = response.headers.names().map { name ->
            com.android.volley.Header(name, response.header(name) ?: "")
        }

        return HttpResponse(
            response.code,
            responseHeaders,
            (response.body?.contentLength() ?: 0).toInt(),
            response.body?.byteStream()
        )
    }
}