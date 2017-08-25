package com.blackwoodseven.kubernetes.node_watcher

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.nio.charset.Charset
import java.time.Instant

class SlackPosterSpec: Spek({
    describe("SlackAttachment") {
        val slackAttachment = SlackAttachment(
                "title",
                "text"
        )

        slackAttachment.title shouldEqual "title"
        slackAttachment.text shouldEqual "text"
    }

    describe("SlackMessage") {
        val attachments = listOf(mock(SlackAttachment::class))
        val slackMessage = SlackMessage(
                "text",
                attachments
        )

        slackMessage.text shouldEqual "text"
        slackMessage.attachments shouldEqual attachments
    }

    describe("SlackPoster") {
        describe("formatMessage") {
            it("should format a message correctly") {
                val nodeChange = NodeChange(
                        NodeChangeType.ADDED,
                        Node(
                                NodeMetadata(
                                        "somenode",
                                        Instant.now()
                                ),
                                NodeStatus(listOf(
                                        NodeAddress(
                                                "ExternalIP",
                                                IPAddressString("127.0.0.1").address
                                        )
                                ))
                        )
                )

                val nodeMap = mapOf<String, IPAddress?>(
                        "First Node" to IPAddressString("10.0.0.1").address,
                        "Second Node" to IPAddressString("127.0.0.1").address
                )

                val slackMessage = SlackPoster("").formatMessage(nodeChange, nodeMap)

                slackMessage shouldEqual
                        SlackMessage(
                                "The node `127.0.0.1` has been added",
                                listOf(SlackAttachment(
                                        "Current Nodes:",
                                        "10.0.0.1\n127.0.0.1"
                                ))
                        )
            }
        }

        describe("convertMessageToJson") {
            it("should convert the SlackMessage to a json string") {
                val slackMessage = SlackMessage(
                        "The node `127.0.0.1` has been added",
                        listOf(SlackAttachment(
                                "Current Nodes:",
                                "10.0.0.1\n127.0.0.1"
                        ))
                )

                val json = SlackPoster("").convertMessageToJson(slackMessage)

                json shouldEqual
                        "{\"text\":\"The node `127.0.0.1` has been added\",\"attachments\":[{\"title\":\"Current Nodes:\",\"text\":\"10.0.0.1\\n127.0.0.1\"}]}"
            }
        }

        it("Post messages to mocked slack") {
            val nodeChange = NodeChange(
                    NodeChangeType.ADDED,
                    Node(
                            NodeMetadata(
                                    "somenode",
                                    Instant.now()
                            ),
                            NodeStatus(listOf(
                                    NodeAddress(
                                            "ExternalIP",
                                            IPAddressString("127.0.0.1").address
                                    )
                            ))
                    )
            )

            val nodeMap = mapOf<String, IPAddress?>(
                    "First Node" to IPAddressString("10.0.0.1").address,
                    "Second Node" to IPAddressString("127.0.0.1").address
            )

            MockWebServer().use { server ->
                server.enqueue(MockResponse())

                SlackPoster(server.url("/services/key1/key2/key3").toString()).post(
                        nodeChange,
                        nodeMap
                )

                val req = server.takeRequest()
                req.path shouldEqual "/services/key1/key2/key3"
                req.body.readString(Charset.forName("UTF-8")) shouldEqual
                        "{\"text\":\"The node `127.0.0.1` has been added\",\"attachments\":[{\"title\":\"Current Nodes:\",\"text\":\"10.0.0.1\\n127.0.0.1\"}]}"
            }
        }
    }
})
