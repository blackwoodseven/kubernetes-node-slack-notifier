package com.blackwoodseven.kubernetes.node_watcher

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant

class ResponseProcessorSpec: Spek({
    describe("NodeAddress") {
        it("should construct correctly") {
            val ipAddress = mock(IPAddress::class)
            val nodeAddress = NodeAddress(
                    "Test",
                    ipAddress
            )

            nodeAddress.type shouldEqual "Test"
            nodeAddress.address shouldEqual ipAddress
        }
    }

    describe("NodeStatus") {
        it("should construct correctly") {
            val addressList = listOf(mock(NodeAddress::class))
            val nodeStatus = NodeStatus(
                    addressList
            )

            nodeStatus.addresses shouldEqual addressList
        }
    }

    describe("NodeMetadata") {
        it("should construct correctly") {
            val instant = Instant.now()
            val nodeMetadata = NodeMetadata(
                    "name",
                    instant
            )

            nodeMetadata.creationTimestamp shouldEqual instant
            nodeMetadata.name shouldEqual "name"
        }
    }

    describe("Node") {
        it("should construct correctly") {

            val metadata = mock(NodeMetadata::class)
            val status = mock(NodeStatus::class)

            val node = Node(metadata, status)

            node.metadata shouldEqual metadata
            node.status shouldEqual status
        }

        describe("externalIP") {
            it("should return the ExternalIP from the list of addresses") {
                val node = Node(
                        mock(NodeMetadata::class),
                        NodeStatus(
                                listOf(
                                        NodeAddress(
                                                "InternalIP",
                                                IPAddressString("10.0.0.1").address
                                        ),
                                        NodeAddress(
                                                "ExternalIP",
                                                IPAddressString("172.20.79.235").address
                                        ),
                                        NodeAddress(
                                                "ExternalDNS",
                                                IPAddressString("node.kubernetes.default").address
                                        )
                                )
                        )
                )

                node.externalIP shouldEqual IPAddressString("172.20.79.235").address
            }

            it("should return null if there's no ExternalIP") {
                val node = Node(
                        mock(NodeMetadata::class),
                        NodeStatus(
                                listOf(
                                        NodeAddress(
                                                "InternalIP",
                                                IPAddressString("10.0.0.1").address
                                        ),
                                        NodeAddress(
                                                "ExternalDNS",
                                                IPAddressString("node.kubernetes.default").address
                                        )
                                )
                        )
                )

                node.externalIP.shouldBeNull()
            }

            it("should return null if there an empty ExternalIP") {
                val node = Node(
                        mock(NodeMetadata::class),
                        NodeStatus(
                                listOf(
                                        NodeAddress(
                                                "InternalIP",
                                                IPAddressString("10.0.0.1").address
                                        ),
                                        NodeAddress(
                                                "ExternalIP",
                                                null
                                        ),
                                        NodeAddress(
                                                "ExternalDNS",
                                                IPAddressString("node.kubernetes.default").address
                                        )
                                )
                        )
                )

                node.externalIP.shouldBeNull()
            }
        }

    }

    describe("NodeListMetadata") {
        it("should construct correctly") {
            val nodeListMetadata = NodeListMetadata("1234")
            nodeListMetadata.resourceVersion shouldEqual "1234"
        }
    }

    describe("NodeList") {
        it("should construct correctly") {
            val nodeListMock = listOf(mock(Node::class))
            val nodeListMetadata = mock(NodeListMetadata::class)

            val nodeList = NodeList(nodeListMock, nodeListMetadata)

            nodeList.items shouldEqual nodeListMock
            nodeList.metadata shouldEqual nodeListMetadata
        }
    }

    describe("NodeChange") {
        it("should construct correctly") {
            val node = mock(Node::class)
            val nodeChange = NodeChange(
                    NodeChangeType.MODIFIED,
                    node
            )

            nodeChange.type shouldEqual NodeChangeType.MODIFIED
            nodeChange.`object` shouldEqual node
        }
    }

    describe("ResponseProcessor") {
        it("should be able to parse kubernetes node lists") {
            val nodeList = ResponseProcessor().parseNodeList(nodeListString)

            nodeList shouldEqual
                    NodeList(
                            listOf(
                                    Node(
                                            NodeMetadata(
                                                    "ip-172-20-116-86.eu-west-1.compute.internal",
                                                    Instant.parse("2017-08-22T14:06:04Z")
                                            ),
                                            NodeStatus(
                                                    listOf(
                                                            NodeAddress(
                                                                    "InternalIP",
                                                                    IPAddressString("172.20.116.86").address
                                                            ),
                                                            NodeAddress(
                                                                    "LegacyHostIP",
                                                                    IPAddressString("172.20.116.86").address
                                                            ),
                                                            NodeAddress(
                                                                    "ExternalIP",
                                                                    IPAddressString("54.229.183.136").address
                                                            ),
                                                            NodeAddress(
                                                                    "InternalDNS",
                                                                    IPAddressString("ip-172-20-116-86.eu-west-1.compute.internal").address
                                                            ),
                                                            NodeAddress(
                                                                    "ExternalDNS",
                                                                    IPAddressString("ec2-54-229-183-136.eu-west-1.compute.amazonaws.com").address
                                                            ),
                                                            NodeAddress(
                                                                    "Hostname",
                                                                    IPAddressString("ip-172-20-116-86.eu-west-1.compute.internal").address
                                                            )
                                                    )
                                            )
                                    ),
                                    Node(
                                            NodeMetadata(
                                                    "ip-172-20-44-207.eu-west-1.compute.internal",
                                                    Instant.parse("2017-07-18T15:42:59Z")
                                            ),
                                            NodeStatus(
                                                    listOf(
                                                            NodeAddress(
                                                                    "InternalIP",
                                                                    IPAddressString("172.20.44.207").address
                                                            ),
                                                            NodeAddress(
                                                                    "LegacyHostIP",
                                                                    IPAddressString("172.20.44.207").address
                                                            ),
                                                            NodeAddress(
                                                                    "ExternalIP",
                                                                    IPAddressString("54.171.151.127").address
                                                            ),
                                                            NodeAddress(
                                                                    "InternalDNS",
                                                                    IPAddressString("ip-172-20-44-207.eu-west-1.compute.internal").address
                                                            ),
                                                            NodeAddress(
                                                                    "ExternalDNS",
                                                                    IPAddressString("ec2-54-171-151-127.eu-west-1.compute.amazonaws.com").address
                                                            ),
                                                            NodeAddress(
                                                                    "Hostname",
                                                                    IPAddressString("ip-172-20-44-207.eu-west-1.compute.internal").address
                                                            )
                                                    )
                                            )
                                    )
                            ),
                            NodeListMetadata(
                                    "49691645"
                            )
                    )
        }

        it("should be able to parse kubernetes node changes") {
            val nodeChange = ResponseProcessor().parseNodeChange(nodeChangeString)

            nodeChange shouldEqual
                    NodeChange(
                            NodeChangeType.MODIFIED,
                            Node(
                                    NodeMetadata(
                                            "ip-172-20-79-235.eu-west-1.compute.internal",
                                            Instant.parse("2017-08-21T14:06:06Z")
                                    ),
                                    NodeStatus(
                                            listOf(
                                                    NodeAddress(
                                                            "InternalIP",
                                                            IPAddressString("172.20.79.235").address
                                                    ),
                                                    NodeAddress(
                                                            "LegacyHostIP",
                                                            IPAddressString("172.20.79.235").address
                                                    ),
                                                    NodeAddress(
                                                            "ExternalIP",
                                                            IPAddressString("34.240.9.80").address
                                                    ),
                                                    NodeAddress(
                                                            "InternalDNS",
                                                            IPAddressString("ip-172-20-79-235.eu-west-1.compute.internal").address
                                                    ),
                                                    NodeAddress(
                                                            "ExternalDNS",
                                                            IPAddressString("ec2-34-240-9-80.eu-west-1.compute.amazonaws.com").address
                                                    ),
                                                    NodeAddress(
                                                            "Hostname",
                                                            IPAddressString("ip-172-20-79-235.eu-west-1.compute.internal").address
                                                    )
                                            )
                                    )
                            )
                    )
        }
    }
})

val nodeListString = """
{
  "kind": "NodeList",
  "apiVersion": "v1",
  "metadata": {
    "selfLink": "/api/v1/nodes",
    "resourceVersion": "49691645"
  },
  "items": [
    {
      "metadata": {
        "name": "ip-172-20-116-86.eu-west-1.compute.internal",
        "selfLink": "/api/v1/nodesip-172-20-116-86.eu-west-1.compute.internal",
        "uid": "09332afe-8743-11e7-a6b8-0a30022dfb8c",
        "resourceVersion": "49691630",
        "creationTimestamp": "2017-08-22T14:06:04Z",
        "labels": {
          "beta.kubernetes.io/arch": "amd64",
          "beta.kubernetes.io/instance-type": "m4.xlarge",
          "beta.kubernetes.io/os": "linux",
          "failure-domain.beta.kubernetes.io/region": "eu-west-1",
          "failure-domain.beta.kubernetes.io/zone": "eu-west-1c",
          "instancegroup": "nodes",
          "kubernetes.io/hostname": "ip-172-20-116-86.eu-west-1.compute.internal",
          "kubernetes.io/role": "node",
          "lifecycle": "spot",
          "node-role.kubernetes.io/node": ""
        },
        "annotations": {
          "node.alpha.kubernetes.io/ttl": "0",
          "volumes.kubernetes.io/controller-managed-attach-detach": "true"
        }
      },
      "spec": {
        "podCIDR": "100.96.10.0/24",
        "externalID": "i-09af54f292341f816",
        "providerID": "aws:///eu-west-1c/i-09af54f292341f816"
      },
      "status": {
        "capacity": {
          "cpu": "4",
          "memory": "16435632Ki",
          "pods": "110"
        },
        "allocatable": {
          "cpu": "4",
          "memory": "15923632Ki",
          "pods": "110"
        },
        "conditions": [
          {
            "type": "OutOfDisk",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:21:58Z",
            "lastTransitionTime": "2017-08-22T14:06:04Z",
            "reason": "KubeletHasSufficientDisk",
            "message": "kubelet has sufficient disk space available"
          },
          {
            "type": "MemoryPressure",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:21:58Z",
            "lastTransitionTime": "2017-08-22T14:06:04Z",
            "reason": "KubeletHasSufficientMemory",
            "message": "kubelet has sufficient memory available"
          },
          {
            "type": "DiskPressure",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:21:58Z",
            "lastTransitionTime": "2017-08-22T14:06:04Z",
            "reason": "KubeletHasNoDiskPressure",
            "message": "kubelet has no disk pressure"
          },
          {
            "type": "Ready",
            "status": "True",
            "lastHeartbeatTime": "2017-08-24T08:21:58Z",
            "lastTransitionTime": "2017-08-22T14:06:24Z",
            "reason": "KubeletReady",
            "message": "kubelet is posting ready status"
          },
          {
            "type": "NetworkUnavailable",
            "status": "False",
            "lastHeartbeatTime": "2017-08-22T14:06:07Z",
            "lastTransitionTime": "2017-08-22T14:06:07Z",
            "reason": "RouteCreated",
            "message": "RouteController created a route"
          }
        ],
        "addresses": [
          {
            "type": "InternalIP",
            "address": "172.20.116.86"
          },
          {
            "type": "LegacyHostIP",
            "address": "172.20.116.86"
          },
          {
            "type": "ExternalIP",
            "address": "54.229.183.136"
          },
          {
            "type": "InternalDNS",
            "address": "ip-172-20-116-86.eu-west-1.compute.internal"
          },
          {
            "type": "ExternalDNS",
            "address": "ec2-54-229-183-136.eu-west-1.compute.amazonaws.com"
          },
          {
            "type": "Hostname",
            "address": "ip-172-20-116-86.eu-west-1.compute.internal"
          }
        ],
        "daemonEndpoints": {
          "kubeletEndpoint": {
            "Port": 10250
          }
        },
        "nodeInfo": {
          "machineID": "0e0bdb96caaa4ed39392030f50f6d181",
          "systemUUID": "EC2FAD8F-DC97-F4B4-D550-5F5B54CF376B",
          "bootID": "0527047a-d41a-4bbc-9834-14a247a6a4b2",
          "kernelVersion": "4.4.65-k8s",
          "osImage": "Debian GNU/Linux 8 (jessie)",
          "containerRuntimeVersion": "docker://1.12.6",
          "kubeletVersion": "v1.6.6",
          "kubeProxyVersion": "v1.6.6",
          "operatingSystem": "linux",
          "architecture": "amd64"
        },
        "images": [
          {
            "names": [
              "protokube:1.6.2"
            ],
            "sizeBytes": 377558745
          },
          {
            "names": [
              "gcr.io/google_containers/kube-proxy@sha256:de310fca6c751f766762736eb5e2dcf323bd8cd35e1c73dbf47110cf6a300e49",
              "gcr.io/google_containers/kube-proxy:v1.6.6"
            ],
            "sizeBytes": 108962336
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:e7487bda898ecf172e9b79321eb83a4ec3cef4dc438a2bd61824824a88fa8ad8",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.18"
            ],
            "sizeBytes": 69311614
          },
          {
            "names": [
              "quay.io/prometheus/node-exporter@sha256:b376a1b4f6734ed610b448603bc0560106c2e601471b49f72dda5bd40da095dd",
              "quay.io/prometheus/node-exporter:v0.14.0"
            ],
            "sizeBytes": 18897129
          },
          {
            "names": [
              "quay.io/prometheus/alertmanager@sha256:2843872cb4cd20da5b75286a5a2ac25a17ec1ae81738ba5f75d5ee8794b82eaf",
              "quay.io/prometheus/alertmanager:v0.7.1"
            ],
            "sizeBytes": 17634499
          },
          {
            "names": [
              "quay.io/coreos/configmap-reload@sha256:e2fd60ff0ae4500a75b80ebaa30e0e7deba9ad107833e8ca53f0047c42c5a057",
              "quay.io/coreos/configmap-reload:v0.0.1"
            ],
            "sizeBytes": 4785056
          },
          {
            "names": [
              "gcr.io/google_containers/pause-amd64@sha256:163ac025575b775d1c0f9bf0bdd0f086883171eb475b5068e7defa4ca9e76516",
              "gcr.io/google_containers/pause-amd64:3.0"
            ],
            "sizeBytes": 746888
          }
        ],
        "volumesInUse": [
          "kubernetes.io/aws-ebs/aws://eu-west-1c/vol-0719cacf4a513e9c3"
        ],
        "volumesAttached": [
          {
            "name": "kubernetes.io/aws-ebs/aws://eu-west-1c/vol-0719cacf4a513e9c3",
            "devicePath": "/dev/xvdbb"
          }
        ]
      }
    },
    {
      "metadata": {
        "name": "ip-172-20-44-207.eu-west-1.compute.internal",
        "selfLink": "/api/v1/nodesip-172-20-44-207.eu-west-1.compute.internal",
        "uid": "c6997eea-6bcf-11e7-a6b8-0a30022dfb8c",
        "resourceVersion": "49691643",
        "creationTimestamp": "2017-07-18T15:42:59Z",
        "labels": {
          "beta.kubernetes.io/arch": "amd64",
          "beta.kubernetes.io/instance-type": "m3.medium",
          "beta.kubernetes.io/os": "linux",
          "failure-domain.beta.kubernetes.io/region": "eu-west-1",
          "failure-domain.beta.kubernetes.io/zone": "eu-west-1a",
          "instancegroup": "nodes-sre",
          "kubernetes.io/hostname": "ip-172-20-44-207.eu-west-1.compute.internal",
          "kubernetes.io/role": "node",
          "lifecycle": "spot",
          "node-role.kubernetes.io/node": "",
          "team": "sre"
        },
        "annotations": {
          "node.alpha.kubernetes.io/ttl": "0",
          "volumes.kubernetes.io/controller-managed-attach-detach": "true"
        }
      },
      "spec": {
        "podCIDR": "100.96.2.0/24",
        "externalID": "i-0f6e9934d66c97561",
        "providerID": "aws:///eu-west-1a/i-0f6e9934d66c97561"
      },
      "status": {
        "capacity": {
          "cpu": "1",
          "memory": "3857300Ki",
          "pods": "110"
        },
        "allocatable": {
          "cpu": "1",
          "memory": "3345300Ki",
          "pods": "110"
        },
        "conditions": [
          {
            "type": "OutOfDisk",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:22:06Z",
            "lastTransitionTime": "2017-07-18T15:42:59Z",
            "reason": "KubeletHasSufficientDisk",
            "message": "kubelet has sufficient disk space available"
          },
          {
            "type": "MemoryPressure",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:22:06Z",
            "lastTransitionTime": "2017-07-18T15:42:59Z",
            "reason": "KubeletHasSufficientMemory",
            "message": "kubelet has sufficient memory available"
          },
          {
            "type": "DiskPressure",
            "status": "False",
            "lastHeartbeatTime": "2017-08-24T08:22:06Z",
            "lastTransitionTime": "2017-07-18T15:42:59Z",
            "reason": "KubeletHasNoDiskPressure",
            "message": "kubelet has no disk pressure"
          },
          {
            "type": "Ready",
            "status": "True",
            "lastHeartbeatTime": "2017-08-24T08:22:06Z",
            "lastTransitionTime": "2017-07-18T15:43:09Z",
            "reason": "KubeletReady",
            "message": "kubelet is posting ready status"
          },
          {
            "type": "NetworkUnavailable",
            "status": "False",
            "lastHeartbeatTime": "2017-07-18T15:43:01Z",
            "lastTransitionTime": "2017-07-18T15:43:01Z",
            "reason": "RouteCreated",
            "message": "RouteController created a route"
          }
        ],
        "addresses": [
          {
            "type": "InternalIP",
            "address": "172.20.44.207"
          },
          {
            "type": "LegacyHostIP",
            "address": "172.20.44.207"
          },
          {
            "type": "ExternalIP",
            "address": "54.171.151.127"
          },
          {
            "type": "InternalDNS",
            "address": "ip-172-20-44-207.eu-west-1.compute.internal"
          },
          {
            "type": "ExternalDNS",
            "address": "ec2-54-171-151-127.eu-west-1.compute.amazonaws.com"
          },
          {
            "type": "Hostname",
            "address": "ip-172-20-44-207.eu-west-1.compute.internal"
          }
        ],
        "daemonEndpoints": {
          "kubeletEndpoint": {
            "Port": 10250
          }
        },
        "nodeInfo": {
          "machineID": "aea83d1ed86c4cf8a2d36d632bf9d373",
          "systemUUID": "EC22C5C6-898B-75F7-E402-B1718FCAAA87",
          "bootID": "fa8b9262-c37d-4af3-adfd-109ad0e75825",
          "kernelVersion": "4.4.65-k8s",
          "osImage": "Debian GNU/Linux 8 (jessie)",
          "containerRuntimeVersion": "docker://1.12.6",
          "kubeletVersion": "v1.6.6",
          "kubeProxyVersion": "v1.6.6",
          "operatingSystem": "linux",
          "architecture": "amd64"
        },
        "images": [
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/graylog-kubernetes@sha256:8674327bdb44e0a7d0efb146db8c0b0cdadfd272be4d3594056a8c86bdad854a",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/graylog-kubernetes:master.5"
            ],
            "sizeBytes": 562567856
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/graylog-kubernetes@sha256:0eb57a68bb15df981ad525f23a4b45989ab1fb5686638dbadcd60054520d0e47",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/graylog-kubernetes:master.3"
            ],
            "sizeBytes": 562541394
          },
          {
            "names": [
              "mongo@sha256:aff0c497cff4f116583b99b21775a8844a17bcf5c69f7f3f6028013bf0d6c00c",
              "mongo:3.4.1"
            ],
            "sizeBytes": 401958962
          },
          {
            "names": [
              "protokube:1.6.2"
            ],
            "sizeBytes": 377558745
          },
          {
            "names": [
              "elasticsearch@sha256:336e82bf4a8edee630efcd112ee388fd52b1dd04b0c47300f3efa60ed67a266e",
              "elasticsearch:2.3.5"
            ],
            "sizeBytes": 346399824
          },
          {
            "names": [
              "grafana/grafana@sha256:68fe8467f5e7cb87fcace36c5517364c7195b30cbc73451294e7bbe9312db4ff",
              "grafana/grafana:4.1.1"
            ],
            "sizeBytes": 274673043
          },
          {
            "names": [
              "blackwoodseven/kubernetes-volume-backup@sha256:b746c994d3732a4070686e01ee4c84d58e8bd4133b63dbc4f9d3280a826a5295",
              "blackwoodseven/kubernetes-volume-backup:0.0.5"
            ],
            "sizeBytes": 244901434
          },
          {
            "names": [
              "blackwoodseven/kubernetes-configmap-healthcheck@sha256:e8f00454cba01c719fd434d6dc0993f47c1a0ff28411f4f9caa5a45c5814e2c4",
              "blackwoodseven/kubernetes-configmap-healthcheck:0.0.1"
            ],
            "sizeBytes": 243252685
          },
          {
            "names": [
              "gcr.io/google_containers/kube-proxy@sha256:de310fca6c751f766762736eb5e2dcf323bd8cd35e1c73dbf47110cf6a300e49",
              "gcr.io/google_containers/kube-proxy:v1.6.6"
            ],
            "sizeBytes": 108962336
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:7b02ddd76f676d921ac472179398cc8dc38d8578b9d0faa4ce9f81fc225553e0",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.15"
            ],
            "sizeBytes": 70598275
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:e7487bda898ecf172e9b79321eb83a4ec3cef4dc438a2bd61824824a88fa8ad8",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.18"
            ],
            "sizeBytes": 69311614
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:a2bf6330284a5a2d434a5968ecd748db76e271ff9a13084bf7e12739b2c0e3f1",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.17"
            ],
            "sizeBytes": 69311594
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:b88dd950e8216e352068c5b891a4a0813ee56e4e6c70ac0c0e025bcb8e01ffd1",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.16"
            ],
            "sizeBytes": 69311576
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:7a015bb29de7dd91e57cccb7f1dd55beb75c0304ab624f4b0557439690ca9bab",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.14"
            ],
            "sizeBytes": 69311576
          },
          {
            "names": [
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:3929e7c9d34f109cb7659178b44933c8630c021f3f8e9263e0d27f4db17507fb",
              "438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.13"
            ],
            "sizeBytes": 69140867
          },
          {
            "names": [
              "quay.io/prometheus/node-exporter@sha256:b376a1b4f6734ed610b448603bc0560106c2e601471b49f72dda5bd40da095dd",
              "quay.io/prometheus/node-exporter:v0.14.0"
            ],
            "sizeBytes": 18897129
          },
          {
            "names": [
              "gcr.io/google_containers/pause-amd64@sha256:163ac025575b775d1c0f9bf0bdd0f086883171eb475b5068e7defa4ca9e76516",
              "gcr.io/google_containers/pause-amd64:3.0"
            ],
            "sizeBytes": 746888
          }
        ],
        "volumesInUse": [
          "kubernetes.io/aws-ebs/aws://eu-west-1a/vol-0d8308c00caafa35a",
          "kubernetes.io/aws-ebs/aws://eu-west-1a/vol-008dc007f4f796b3f"
        ],
        "volumesAttached": [
          {
            "name": "kubernetes.io/aws-ebs/aws://eu-west-1a/vol-008dc007f4f796b3f",
            "devicePath": "/dev/xvdbc"
          },
          {
            "name": "kubernetes.io/aws-ebs/aws://eu-west-1a/vol-0d8308c00caafa35a",
            "devicePath": "/dev/xvdbb"
          }
        ]
      }
    }
  ]
}
"""


val nodeChangeString = """{"type":"MODIFIED","object":{"kind":"Node","apiVersion":"v1","metadata":{"name":"ip-172-20-79-235.eu-west-1.compute.internal","selfLink":"/api/v1/nodesip-172-20-79-235.eu-west-1.compute.internal","uid":"e00f5a4a-8679-11e7-a6b8-0a30022dfb8c","resourceVersion":"49696078","creationTimestamp":"2017-08-21T14:06:06Z","labels":{"beta.kubernetes.io/arch":"amd64","beta.kubernetes.io/instance-type":"m4.xlarge","beta.kubernetes.io/os":"linux","failure-domain.beta.kubernetes.io/region":"eu-west-1","failure-domain.beta.kubernetes.io/zone":"eu-west-1b","instancegroup":"nodes","kubernetes.io/hostname":"ip-172-20-79-235.eu-west-1.compute.internal","kubernetes.io/role":"node","lifecycle":"spot","node-role.kubernetes.io/node":""},"annotations":{"node.alpha.kubernetes.io/ttl":"0","volumes.kubernetes.io/controller-managed-attach-detach":"true"}},"spec":{"podCIDR":"100.96.7.0/24","externalID":"i-043235df01b4f2705","providerID":"aws:///eu-west-1b/i-043235df01b4f2705"},"status":{"capacity":{"cpu":"4","memory":"16435632Ki","pods":"110"},"allocatable":{"cpu":"4","memory":"15923632Ki","pods":"110"},"conditions":[{"type":"OutOfDisk","status":"False","lastHeartbeatTime":"2017-08-24T09:08:23Z","lastTransitionTime":"2017-08-21T14:06:06Z","reason":"KubeletHasSufficientDisk","message":"kubelet has sufficient disk space available"},{"type":"MemoryPressure","status":"False","lastHeartbeatTime":"2017-08-24T09:08:23Z","lastTransitionTime":"2017-08-21T14:06:06Z","reason":"KubeletHasSufficientMemory","message":"kubelet has sufficient memory available"},{"type":"DiskPressure","status":"False","lastHeartbeatTime":"2017-08-24T09:08:23Z","lastTransitionTime":"2017-08-21T14:06:06Z","reason":"KubeletHasNoDiskPressure","message":"kubelet has no disk pressure"},{"type":"Ready","status":"True","lastHeartbeatTime":"2017-08-24T09:08:23Z","lastTransitionTime":"2017-08-21T14:06:26Z","reason":"KubeletReady","message":"kubelet is posting ready status"},{"type":"NetworkUnavailable","status":"False","lastHeartbeatTime":"2017-08-21T14:06:15Z","lastTransitionTime":"2017-08-21T14:06:15Z","reason":"RouteCreated","message":"RouteController created a route"}],"addresses":[{"type":"InternalIP","address":"172.20.79.235"},{"type":"LegacyHostIP","address":"172.20.79.235"},{"type":"ExternalIP","address":"34.240.9.80"},{"type":"InternalDNS","address":"ip-172-20-79-235.eu-west-1.compute.internal"},{"type":"ExternalDNS","address":"ec2-34-240-9-80.eu-west-1.compute.amazonaws.com"},{"type":"Hostname","address":"ip-172-20-79-235.eu-west-1.compute.internal"}],"daemonEndpoints":{"kubeletEndpoint":{"Port":10250}},"nodeInfo":{"machineID":"445ef8d30117406ea341017c102f8c9b","systemUUID":"EC24A4F0-C240-734F-71E7-25ED559022FB","bootID":"e2592488-e673-4fc3-9559-cd7513a48b85","kernelVersion":"4.4.65-k8s","osImage":"Debian GNU/Linux 8 (jessie)","containerRuntimeVersion":"docker://1.12.6","kubeletVersion":"v1.6.6","kubeProxyVersion":"v1.6.6","operatingSystem":"linux","architecture":"amd64"},"images":[{"names":["a5huynh/oauth2_proxy@sha256:c0bb7c46f82021bdd7cdb876ede2c095611b3b3ce70c333d824979b7d6df2d6f","a5huynh/oauth2_proxy:2.1"],"sizeBytes":714600702},{"names":["protokube:1.6.2"],"sizeBytes":377558745},{"names":["blackwoodseven/kubernetes-configmap-healthcheck@sha256:e8f00454cba01c719fd434d6dc0993f47c1a0ff28411f4f9caa5a45c5814e2c4","blackwoodseven/kubernetes-configmap-healthcheck:0.0.1"],"sizeBytes":243252685},{"names":["gcr.io/google_containers/kube-proxy@sha256:de310fca6c751f766762736eb5e2dcf323bd8cd35e1c73dbf47110cf6a300e49","gcr.io/google_containers/kube-proxy:v1.6.6"],"sizeBytes":108962336},{"names":["438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes@sha256:e7487bda898ecf172e9b79321eb83a4ec3cef4dc438a2bd61824824a88fa8ad8","438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/fluentd-kubernetes:master.18"],"sizeBytes":69311614},{"names":["nginx@sha256:aa0daf2b17c370a1da371a767110a43b390a9db90b90d2d1b07862dc81754d61","nginx:1.11.10-alpine"],"sizeBytes":54272805},{"names":["gcr.io/google_containers/k8s-dns-kube-dns-amd64@sha256:33914315e600dfb756e550828307dfa2b21fb6db24fe3fe495e33d1022f9245d","gcr.io/google_containers/k8s-dns-kube-dns-amd64:1.14.1"],"sizeBytes":52357032},{"names":["gcr.io/google_containers/k8s-dns-dnsmasq-nanny-amd64@sha256:89c9a1d3cfbf370a9c1a949f39f92c1dc2dbe8c3e6cc1802b7f2b48e4dfe9a9e","gcr.io/google_containers/k8s-dns-dnsmasq-nanny-amd64:1.14.1"],"sizeBytes":44844722},{"names":["gcr.io/google_containers/k8s-dns-sidecar-amd64@sha256:d33a91a5d65c223f410891001cd379ac734d036429e033865d700a4176e944b0","gcr.io/google_containers/k8s-dns-sidecar-amd64:1.14.1"],"sizeBytes":44517061},{"names":["438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/rabbitmq-server@sha256:8de7e558b3a9216d2edaf4ab7b7ac505decafeca0f1d29948ce06424fcf74948","438423213058.dkr.ecr.eu-west-1.amazonaws.com/blackwoodseven/rabbitmq-server:master.5"],"sizeBytes":38117745},{"names":["olalonde/oauth2_proxy@sha256:031e2fce6f7a03d674f37e26fd20338b5da1aae0a26133bdcf1feb35d38e95ca","olalonde/oauth2_proxy:latest"],"sizeBytes":36801086},{"names":["aronchick/hello-node@sha256:fa1f1197653c258bde04e4aebe078366375019e22b62d0c0c62b841933b26d99","aronchick/hello-node:2.0"],"sizeBytes":35092307},{"names":["quay.io/prometheus/node-exporter@sha256:b376a1b4f6734ed610b448603bc0560106c2e601471b49f72dda5bd40da095dd","quay.io/prometheus/node-exporter:v0.14.0"],"sizeBytes":18897129},{"names":["quay.io/prometheus/alertmanager@sha256:2843872cb4cd20da5b75286a5a2ac25a17ec1ae81738ba5f75d5ee8794b82eaf","quay.io/prometheus/alertmanager:v0.7.1"],"sizeBytes":17634499},{"names":["kbudde/rabbitmq-exporter@sha256:8973b6e17d350cf91fd5a0d12e0e5e8b48e2934bf326c89ec341a461bc937348","kbudde/rabbitmq-exporter:latest"],"sizeBytes":8624124},{"names":["quay.io/coreos/configmap-reload@sha256:e2fd60ff0ae4500a75b80ebaa30e0e7deba9ad107833e8ca53f0047c42c5a057","quay.io/coreos/configmap-reload:v0.0.1"],"sizeBytes":4785056},{"names":["gcr.io/google_containers/pause-amd64@sha256:163ac025575b775d1c0f9bf0bdd0f086883171eb475b5068e7defa4ca9e76516","gcr.io/google_containers/pause-amd64:3.0"],"sizeBytes":746888}],"volumesInUse":["kubernetes.io/aws-ebs/aws://eu-west-1b/vol-040b109405a569174","kubernetes.io/aws-ebs/aws://eu-west-1b/vol-00a408a014df301d2"],"volumesAttached":[{"name":"kubernetes.io/aws-ebs/aws://eu-west-1b/vol-00a408a014df301d2","devicePath":"/dev/xvdbc"},{"name":"kubernetes.io/aws-ebs/aws://eu-west-1b/vol-040b109405a569174","devicePath":"/dev/xvdbb"}]}}}"""
