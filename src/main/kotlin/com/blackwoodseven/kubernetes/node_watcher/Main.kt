package com.blackwoodseven.kubernetes.node_watcher

import inet.ipaddr.IPAddress
import mu.KotlinLogging
import java.nio.file.FileSystems

private val logger = KotlinLogging.logger {}

fun processNodeChange(nodeChange: NodeChange, nodeMap: Map<String, IPAddress?>): Map<String, IPAddress?> {
    return when(nodeChange.type) {
        NodeChangeType.ADDED -> nodeMap + (nodeChange.`object`.metadata.name to nodeChange.`object`.externalIP)
        NodeChangeType.DELETED -> nodeMap - nodeChange.`object`.metadata.name
        else -> nodeMap
    }
}

fun processLine(line: String, nodeMap: Map<String, IPAddress?>, slackPoster: SlackPoster): Map<String, IPAddress?> {
    val nodeChange = ResponseProcessor().parseNodeChange(line)
    val newMap = processNodeChange(nodeChange, nodeMap)
    if (setOf(NodeChangeType.DELETED, NodeChangeType.ADDED).contains(nodeChange.type)) {
        slackPoster.post(nodeChange, newMap)
    }

    return newMap
}

fun setupShutdownHandler(slackPoster: SlackPoster) {
    Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    slackPoster.shutdownMessage()
                }
            }
    )
}

fun main(args : Array<String>) {
    val config = Config.parseConfig()

    val kubernetesAPI = KubernetesAPI(config.k8sHostname, config.k8sUsername, config.k8sPassword, FileSystems.getDefault())

    val nodeList = kubernetesAPI.fetchNodeList()

    val nodeMap = nodeList.items.map { it.metadata.name to it.externalIP }.toMap()
    logger.info { "Initial known nodes: ${nodeMap.values}" }
    val slackPoster = SlackPoster(config.slackWebhook)
    setupShutdownHandler(slackPoster)

    val changeCharStream = kubernetesAPI.fetchNodeChangeStream(nodeList.metadata.resourceVersion)
    changeCharStream?.useLines { lineSource ->
        lineSource.fold(nodeMap) { map, line ->
            processLine(line, map, slackPoster)
        }
    }
}
