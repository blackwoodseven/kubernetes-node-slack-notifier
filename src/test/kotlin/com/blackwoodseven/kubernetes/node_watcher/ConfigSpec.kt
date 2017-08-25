package com.blackwoodseven.kubernetes.node_watcher

import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class ConfigSpec : Spek({
    describe("Construction") {
        it("should construct correctly") {
            val config = Config(
                    "a",
                    "b",
                    "c",
                    "d"
            )

            config.k8sUsername shouldEqual "a"
            config.k8sPassword shouldEqual "b"
            config.k8sHostname shouldEqual "c"
            config.slackWebhook shouldEqual "d"
        }

        it("should construct correctly") {
            val config = Config(
                    null,
                    null,
                    "c",
                    slackWebhook = "d"
            )

            config.k8sUsername shouldEqual null
            config.k8sPassword shouldEqual null
            config.k8sHostname shouldEqual "kubernetes.default"
            config.slackWebhook shouldEqual "d"
        }
    }


    describe("parseConfig") {
        it("should parse the given environment variables") {
            val mockEnv = mapOf(
                    "K8S_USERNAME" to "admin",
                    "K8S_PASSWORD" to "password",
                    "K8S_HOSTNAME" to "k8s.example.com",
                    "SLACK_WEBHOOK" to "some slack webhook"
            )
            setEnvironment(mockEnv)

            val config = Config.parseConfig()

            config.k8sUsername shouldEqual "admin"
            config.k8sPassword shouldEqual "password"
            config.k8sHostname shouldEqual "k8s.example.com"
            config.slackWebhook shouldEqual "some slack webhook"
        }

        it("should parse with no username/password") {
            val mockEnv = mapOf(
                    "K8S_HOSTNAME" to "k8s.example.com",
                    "SLACK_WEBHOOK" to "some slack webhook"
            )
            setEnvironment(mockEnv)

            val config = Config.parseConfig()

            config.k8sUsername.shouldBeNull()
            config.k8sPassword.shouldBeNull()
            config.k8sHostname shouldEqual "k8s.example.com"
            config.slackWebhook shouldEqual "some slack webhook"
        }

        it("should parse with no k8sHostname") {
            val mockEnv = mapOf(
                    "SLACK_WEBHOOK" to "some slack webhook"
            )
            setEnvironment(mockEnv)

            val config = Config.parseConfig()

            config.k8sUsername.shouldBeNull()
            config.k8sPassword.shouldBeNull()
            config.k8sHostname shouldEqual "kubernetes.default"
            config.slackWebhook shouldEqual "some slack webhook"
        }
    }
})
