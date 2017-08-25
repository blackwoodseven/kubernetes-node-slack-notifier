package com.blackwoodseven.kubernetes.node_watcher

import com.google.gson.Gson
import inet.ipaddr.IPAddress
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

data class SlackAttachment(
        val title: String,
        val text: String
)

data class SlackMessage(
        val text: String,
        val attachments: List<SlackAttachment>
)

class SlackPoster(private val webhook: String) {
    fun formatMessage(change: NodeChange, nodeList: Map<String, IPAddress?>): SlackMessage {
        return SlackMessage(
                "The node `${change.`object`.externalIP}` has been ${change.type.name.toLowerCase()}",
                listOf(SlackAttachment(
                        "Current Nodes:",
                        nodeList.values.filterNotNull().map { it.toString() }.sorted().joinToString("\n")
                ))
        )
    }

    fun convertMessageToJson(slackMessage: SlackMessage): String {
        val gson = Gson()
        return gson.toJson(slackMessage)
    }

    fun post(change: NodeChange, nodeList: Map<String, IPAddress?>) {
        val slackMessage = formatMessage(change, nodeList)
        val json = convertMessageToJson(slackMessage)

        val client = OkHttpClient.Builder().build()
        val req = Request.Builder()
                .url(webhook)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
                .build()
        val response = client.newCall(req).execute()
    }
}
