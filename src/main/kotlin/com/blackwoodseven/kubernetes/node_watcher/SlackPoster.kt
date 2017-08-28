package com.blackwoodseven.kubernetes.node_watcher

import com.google.gson.Gson
import inet.ipaddr.IPAddress
import mu.KotlinLogging
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

private val logger = KotlinLogging.logger {}

class SlackPoster(private val webhook: String) {
    private val client = OkHttpClient.Builder().build()

    fun formatMessage(change: NodeChange, nodeList: Map<String, IPAddress?>): SlackMessage {
        return SlackMessage(
                "The node `${change.`object`.externalIP}` has been ${change.type.name.toLowerCase()}",
                listOf(SlackAttachment(
                        "Current Nodes:",
                        nodeList.values.filterNotNull().map { it.toString() }.sorted().joinToString("\n")
                ))
        )
    }

    fun formatMessage(action: NodeChangeType, ip: IPAddress, nodeList: Map<String, IPAddress?>): SlackMessage {
        return SlackMessage(
                "The node `$ip` has been ${action.name.toLowerCase()}",
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

    fun post(action: NodeChangeType, ip: IPAddress, nodeList: Map<String, IPAddress?>) {
        val slackMessage = formatMessage(action, ip, nodeList)
        sendSlackMessage(slackMessage)
    }

    fun post(change: NodeChange, nodeList: Map<String, IPAddress?>) {
        val externalIP = change.`object`.externalIP
        if (externalIP != null) {
            post(change.type, externalIP, nodeList)
        } else {
            logger.warn { "Tried to post null IP to slack..." }
        }
    }

    fun shutdownMessage() {
        val slackMessage = SlackMessage(
                "The node slack notifier has been asked to shutdown, this might be due to the node it's running on shutting down.\nWatch the next notification closely to see if a node has been removed.",
                emptyList()
        )
        sendSlackMessage(slackMessage)
    }

    private fun sendSlackMessage(slackMessage: SlackMessage) {
        val json = convertMessageToJson(slackMessage)
        val req = Request.Builder()
                .url(webhook)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
                .build()
        val response = client.newCall(req).execute()
        response.body()?.close()
    }
}
