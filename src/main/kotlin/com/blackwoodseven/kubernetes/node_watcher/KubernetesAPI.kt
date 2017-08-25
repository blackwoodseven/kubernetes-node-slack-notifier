package com.blackwoodseven.kubernetes.node_watcher

import mu.KotlinLogging
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.Reader
import java.nio.charset.Charset
import java.nio.file.FileSystem
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

class KubernetesAPI(private val hostname: String, username: String?, password: String?, fileSystem: FileSystem?) {
    private val client = OkHttpClient.Builder().addInterceptor {
        it.proceed(
                it.request().newBuilder()
                        .addHeader("Authorization", buildAuthorizationString(username, password, fileSystem))
                        .build())
    }.build()

    fun fetchNodeList(): NodeList {
        val initReq = Request.Builder()
                .url("https://$hostname/api/v1/nodes")
                .build()
        logger.info { "Requesting initial nodes..." }
        val response = client.newCall(initReq).execute()
        val body = response.body()?.string()!!

        return ResponseProcessor().parseNodeList(body)
    }

    fun fetchNodeChangeStream(resourceVersion: String): Reader? {
        val req = Request.Builder()
                .url("https://$hostname/api/v1/nodes?watch=true&resourceVersion=$resourceVersion")
                .build()
        logger.info { "Watching node changes..." }
        val resp = client.newCall(req).execute()
        return resp.body()?.charStream()
    }

    companion object {
        fun buildAuthorizationString(username: String?, password: String?, fileSystem: FileSystem?): String {
            return if (username != null && password != null) {
                Credentials.basic(username, password)
            } else if (fileSystem != null && Files.exists(fileSystem.getPath("/var/run/secrets/kubernetes.io/serviceaccount/token"))) {
                val tokenFile = fileSystem.getPath("/var/run/secrets/kubernetes.io/serviceaccount/token")
                val token = Files.readAllBytes(tokenFile).toString(Charset.forName("UTF-8"))
                "Bearer $token"
            } else {
                throw IllegalArgumentException("Authorization setup failed, you must provide either username/password, or a tokenfile")
            }
        }
    }
}
