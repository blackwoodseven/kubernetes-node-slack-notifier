package com.blackwoodseven.kubernetes.node_watcher

import com.hazelcast.config.DiscoveryStrategyConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.ILock
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory
import inet.ipaddr.IPAddress
import mu.KotlinLogging
import java.net.ConnectException
import java.nio.file.FileSystems
import java.util.concurrent.ConcurrentMap

private val logger = KotlinLogging.logger {}

fun processNodeChange(nodeChange: NodeChange, nodeMap: ConcurrentMap<String, IPAddress?>) {
    when(nodeChange.type) {
        NodeChangeType.ADDED -> nodeMap.put(nodeChange.`object`.metadata.name, nodeChange.`object`.externalIP)
        NodeChangeType.DELETED -> nodeMap.remove(nodeChange.`object`.metadata.name)
        else -> {}
    }
}

fun processLine(line: String, nodeMap: ConcurrentMap<String, IPAddress?>, slackPoster: SlackPoster) {
    val nodeChange = ResponseProcessor().parseNodeChange(line)
    processNodeChange(nodeChange, nodeMap)
    if (setOf(NodeChangeType.DELETED, NodeChangeType.ADDED).contains(nodeChange.type)) {
        slackPoster.post(nodeChange, nodeMap)
    }
}

fun setupShutdownHandler(leaderLock: ILock) {
    Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    leaderLock.unlock()
                }
            }
    )
}

fun initializeHazelcast(): HazelcastInstance {
    val hazelcastConfig = com.hazelcast.config.Config()
    hazelcastConfig.setProperty("hazelcast.discovery.enabled", "true")
    hazelcastConfig.networkConfig.port = 5701
    hazelcastConfig.networkConfig.isPortAutoIncrement = false
    hazelcastConfig.networkConfig.join.multicastConfig.isEnabled = false
    hazelcastConfig.networkConfig.join.tcpIpConfig.isEnabled = false

    val kubernetesDiscoveryStrategyFactory = HazelcastKubernetesDiscoveryStrategyFactory()
    val discoveryStrategyConfig = DiscoveryStrategyConfig(kubernetesDiscoveryStrategyFactory)
    hazelcastConfig.networkConfig.join.discoveryConfig.addDiscoveryStrategyConfig(discoveryStrategyConfig)

    return Hazelcast.newHazelcastInstance(hazelcastConfig)
}

fun main(args : Array<String>) {
    val config = Config.parseConfig()
    val hazelcast = initializeHazelcast()

    val leaderLock = hazelcast.getLock("leaderLock")
    val globalNodeMap = hazelcast.getMap<String, IPAddress?>("nodeMap")

    val slackPoster = SlackPoster(config.slackWebhook)
    setupShutdownHandler(leaderLock)

    val kubernetesAPI = KubernetesAPI(config.k8sHostname, config.k8sUsername, config.k8sPassword, FileSystems.getDefault())

    while(true) {
        logger.info { "Trying to acquire leader-lock..." }
        leaderLock.lock()
        logger.info { "Acquired leader-lock!" }

        logger.info { "Comparing globalNodeMap to Kubernetes API" }
        val nodeList: NodeList = try {
            kubernetesAPI.fetchNodeList()
        } catch (e: ConnectException) {
            logger.info { "Could not connect to the Kubernetes API to fetch list of all nodes, releasing leader lock." }
            leaderLock.unlock()
            continue
        }

        val nodeMap = nodeList.items.map { it.metadata.name to it.externalIP }.toMap()
        val missingNodes = globalNodeMap.keys - nodeMap.keys
        val newNodes = nodeMap.keys - globalNodeMap.keys
        missingNodes.forEach {
            val ip = globalNodeMap.remove(it)
            if (ip != null) {
                slackPoster.post(NodeChangeType.DELETED, ip, globalNodeMap)
            }
        }
        newNodes.forEach {
            val ip = globalNodeMap.put(it, nodeMap[it])
            if (ip != null) {
                slackPoster.post(NodeChangeType.ADDED, ip, globalNodeMap)
            }
        }

        logger.info { "Watching node change stream..." }
        val changeCharStream = kubernetesAPI.fetchNodeChangeStream(nodeList.metadata.resourceVersion)
        changeCharStream?.useLines { lineSource ->
            lineSource.forEach { line ->
                processLine(line, globalNodeMap, slackPoster)
            }
        }

        logger.info { "Releasing leader-lock" }
        leaderLock.unlock()
    }
}
