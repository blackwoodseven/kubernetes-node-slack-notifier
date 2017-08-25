package com.blackwoodseven.kubernetes.node_watcher

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.github.salomonbrys.kotson.string
import com.google.gson.GsonBuilder
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import java.time.Instant

data class NodeAddress(
        val type: String,
        val address: IPAddress?
)

data class NodeStatus(
        val addresses: List<NodeAddress>
)


data class NodeMetadata(
        val name: String,
        val creationTimestamp: Instant
)

data class Node(
        val metadata: NodeMetadata,
        val status: NodeStatus
) {
    val externalIP: IPAddress?
        get() {
            return this.status.addresses.singleOrNull { it.type == "ExternalIP" }?.address
        }
}

data class NodeListMetadata(
        val resourceVersion: String
)

data class NodeList(
        val items: List<Node>,
        val metadata: NodeListMetadata
)

enum class NodeChangeType {
    MODIFIED, ADDED, DELETED
}

data class NodeChange(
        val type: NodeChangeType,
        val `object`: Node
)

class ResponseProcessor {
    private val gson = GsonBuilder()
            .registerTypeAdapter<IPAddress> {
                deserialize {
                    IPAddressString(it.json.string).address
                }
            }
            .registerTypeAdapter<Instant> {
                deserialize { Instant.parse(it.json.string) }
            }
            .create()

    fun parseNodeList(rawJson: String): NodeList {
        return gson.fromJson(rawJson)
    }

    fun parseNodeChange(rawJson: String): NodeChange {
        return gson.fromJson(rawJson)
    }
}


