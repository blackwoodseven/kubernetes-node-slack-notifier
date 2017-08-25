package com.blackwoodseven.kubernetes.node_watcher

import inet.ipaddr.IPAddressString
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant


class MainSpec : Spek({
    describe("the system should be sane") {
        it("should calculate correctly") {
            4 shouldEqual 2 + 2
        }
    }

    describe("processNodeChange") {
        it("should do nothing for MODIFIED") {
            val nodeMap = mapOf(
                    "node1" to IPAddressString("127.0.0.1").address,
                    "node2" to IPAddressString("127.0.0.2").address
            )

            val nodeChange = NodeChange(
                    NodeChangeType.MODIFIED,
                    mock(Node::class)
            )

            val result = processNodeChange(nodeChange, nodeMap)

            result shouldEqual mapOf(
                    "node1" to IPAddressString("127.0.0.1").address,
                    "node2" to IPAddressString("127.0.0.2").address
            )
        }

        it("should return a new map with the new node for ADDED") {
            val nodeMap = mapOf(
                    "node1" to IPAddressString("127.0.0.1").address,
                    "node2" to IPAddressString("127.0.0.2").address
            )
            val newNode = Node(
                    NodeMetadata(
                            "node3",
                            Instant.now()
                    ),
                    NodeStatus(
                            listOf(
                                    NodeAddress(
                                            "ExternalIP",
                                            IPAddressString("127.0.0.3").address
                                    )
                            )
                    )
            )

            val nodeChange = NodeChange(
                    NodeChangeType.ADDED,
                    newNode
            )

            val result = processNodeChange(nodeChange, nodeMap)

            result shouldEqual mapOf(
                    "node1" to IPAddressString("127.0.0.1").address,
                    "node2" to IPAddressString("127.0.0.2").address,
                    "node3" to IPAddressString("127.0.0.3").address
            )
        }

        it("should return a new map without the removed node for DELETED") {
            val nodeMap = mapOf(
                    "node1" to IPAddressString("127.0.0.1").address,
                    "node2" to IPAddressString("127.0.0.2").address
            )
            val deletedNode = Node(
                    NodeMetadata(
                            "node2",
                            Instant.now()
                    ),
                    mock(NodeStatus::class)
            )

            val nodeChange = NodeChange(
                    NodeChangeType.DELETED,
                    deletedNode
            )

            val result = processNodeChange(nodeChange, nodeMap)

            result shouldEqual mapOf(
                    "node1" to IPAddressString("127.0.0.1").address
            )
        }
    }
})

