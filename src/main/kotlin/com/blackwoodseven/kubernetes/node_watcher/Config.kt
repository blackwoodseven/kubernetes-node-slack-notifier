package com.blackwoodseven.kubernetes.node_watcher

data class Config(
        val k8sUsername: String?,
        val k8sPassword: String?,
        val k8sHostname: String,
        val slackWebhook: String
) {
    companion object {
        fun parseConfig(): Config {
            return Config(
                    System.getenv("K8S_USERNAME"),
                    System.getenv("K8S_PASSWORD"),
                    System.getenv("K8S_HOSTNAME") ?: "kubernetes.default",
                    System.getenv("SLACK_WEBHOOK")
            )
        }
    }
}

