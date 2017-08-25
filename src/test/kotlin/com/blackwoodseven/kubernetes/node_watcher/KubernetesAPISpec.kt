package com.blackwoodseven.kubernetes.node_watcher

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.nio.file.Files
import java.util.*

class KubernetesAPISpec : Spek({
    describe("buildAuthorizationString") {
        it("should return a basic auth string when given username and password") {
            val auth = KubernetesAPI.buildAuthorizationString(
                    "admin",
                    "password",
                    null
            )

            val expectedBase64 = Base64.getEncoder().encodeToString("admin:password".toByteArray())
            auth shouldEqual "Basic $expectedBase64"
        }

        it("should return a Bearer auth string when not given username and password") {
            val fs = Jimfs.newFileSystem(Configuration.unix())
            val tokenPath = fs.getPath("/var/run/secrets/kubernetes.io/serviceaccount/token")
            Files.createDirectories(tokenPath.parent)
            Files.write(tokenPath, "some token".toByteArray())

            val auth = KubernetesAPI.buildAuthorizationString(null, null, fs)
            auth shouldEqual "Bearer some token"
        }
    }
})
