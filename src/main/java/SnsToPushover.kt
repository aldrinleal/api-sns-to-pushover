package io.ingenieux.sns2pushover

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.util.SignatureChecker
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ingenieux.lambada.runtime.ApiGateway
import io.ingenieux.lambada.runtime.LambadaFunction
import io.ingenieux.lambada.runtime.model.PassthroughRequest
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.charset.Charset
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class SnsToPushover {
    /**
     * {
    "Type" : "SubscriptionConfirmation",
    "MessageId" : "7ab9d48b-f17a-43f3-826a-17edab677f4e",
    "Token" : "2336412f37fb687f5d51e6e241d44a2cb136210086999f407bae05b5ca7bda4057604d0d1eb3a3de6a9397c03a355c5a2a68a5dadd92377792fb927b6341d767b8ea80252bec19a9cd5240a61ab4d3c9382ca51f88aa677b0d31892f148501f069ce82ddc3a6915775a5426a425689b5b53a2ed7134dfdd0b28eb075b413219c",
    "TopicArn" : "arn:aws:sns:us-east-1:235368163414:generic-notifications",
    "Message" : "You have chosen to subscribe to the topic arn:aws:sns:us-east-1:235368163414:generic-notifications.\nTo confirm the subscription, visit the SubscribeURL included in this message.",
    "SubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aws:sns:us-east-1:235368163414:generic-notifications&Token=2336412f37fb687f5d51e6e241d44a2cb136210086999f407bae05b5ca7bda4057604d0d1eb3a3de6a9397c03a355c5a2a68a5dadd92377792fb927b6341d767b8ea80252bec19a9cd5240a61ab4d3c9382ca51f88aa677b0d31892f148501f069ce82ddc3a6915775a5426a425689b5b53a2ed7134dfdd0b28eb075b413219c",
    "Timestamp" : "2016-05-03T23:51:01.632Z",
    "SignatureVersion" : "1",
    "Signature" : "GkI96HVDHsz8Rw2uNRuWYWI5VpDonT3EgyQyuSBpCE4+xLJ4/Xkx7+kkpm2A1HcMIoWedzSrPW4IwElRncfeA+2wbP3t78D0YYOosNlcgky+qYx2i4v5K3e81FnWfTyPUE23tVH68LFQ+KE+i1Flz0TumTgC69mYDZ1K6oHxI/Ef1i2J2M87WzI4+kXgJBvd5GBukA9dE5zz5AxGCLA+8cQvh7MZSZIA1ff20S/qBY8ixlErTXPM2yegXIUL/tdAuHnrqKLxrH9j2fPtm91Cjdb+zDKtgOxfDs21duKFSvfS3C0WflakM6gF0NM9Yds22r4phfxDYCaNWppOduMAOg==",
    "SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem"
    }
     */
    data class Dimension @JsonCreator constructor(
            @param:JsonProperty("name") @get:com.fasterxml.jackson.annotation.JsonProperty("name")
            val name: String
            ,
            @param:JsonProperty("value") @get:com.fasterxml.jackson.annotation.JsonProperty("value")
            val value: String
    )

    data class Trigger @JsonCreator constructor(
            @param:JsonProperty("MetricName") @get:JsonProperty("MetricName")
            var metricName: String? = null
            ,
            @param:JsonProperty("Namespace") @get:JsonProperty("Namespace")
            var namespace: String? = null
            ,
            @param:JsonProperty("Statistic") @get:JsonProperty("Statistic")
            var statistic: String? = null
            ,
            @param:JsonProperty("Unit") @get:JsonProperty("Unit")
            var unit: String? = null
            ,
            @param:JsonProperty("Dimensions") @get:JsonProperty("Dimensions")
            var dimensions: Array<Dimension>? = null
            ,
            @param:JsonProperty("Period") @get:JsonProperty("Period")
            var period: Integer
            ,
            @param:JsonProperty("EvaluationPeriods") @get:JsonProperty("EvaluationPeriods")
            var evaluationPeriods: Integer
            ,
            @param:JsonProperty("ComparisonOperator") @get:JsonProperty("ComparisonOperator")
            var comparisonOperator: String
            ,
            @param:JsonProperty("Threshold") @get:JsonProperty("Threshold")
            var threshold: Integer
    )

    data class CloudWatchNotificationMessage @JsonCreator constructor(
            @param:JsonProperty("AlarmName") @get:JsonProperty("AlarmName")
            var alarmName: String? = null
            ,
            @param:JsonProperty("AlarmDescription") @get:JsonProperty("AlarmDescription")
            var alarmDescription: String? = null
            ,
            @param:JsonProperty("AWSAccountId") @get:JsonProperty("AWSAccountId")
            var awsAccountId: String? = null
            ,
            @param:JsonProperty("NewStateValue") @get:JsonProperty("NewStateValue")
            var newStateValue: String? = null
            ,
            @param:JsonProperty("NewStateReason") @get:JsonProperty("NewStateReason")
            var newStateReason: String? = null
            ,
            @param:JsonProperty("StateChangeTime") @get:JsonProperty("StateChangeTime")
            var stateChangeTime: Date? = null
            ,
            @param:JsonProperty("OldStateValue") @get:JsonProperty("OldStateValue")
            var oldStateValue: String? = null
            ,
            @param:JsonProperty("Region") @get:JsonProperty("Region")
            var region: String? = null
            ,
            @param:JsonProperty("Trigger") @get:com.fasterxml.jackson.annotation.JsonProperty("Trigger")
            var trigger: Trigger? = null
    )

    data class SnsRequest @JsonCreator constructor(
            @param:JsonProperty("Type") @get:JsonProperty("Type")
            var type: String = "",

            @param:JsonProperty("MessageId") @get:JsonProperty("MessageId")
            var messageId: String = "",

            @param:JsonProperty("Token") @get:JsonProperty("Token")
            var token: String? = "",

            @param:JsonProperty("TopicArn") @get:JsonProperty("TopicArn")
            var topicArn: String = "",

            @param:JsonProperty("Message") @get:JsonProperty("Message")
            var message: String = "",

            @param:JsonProperty("Subject") @get:JsonProperty("Subject")
            var subject: String? = "",

            @param:JsonProperty("SubscribeURL")
            @get:JsonProperty("SubscribeURL")
            var subscribeUrl: String? = "",

            @param:JsonProperty("Timestamp") @get:JsonProperty("Timestamp")
            var timestamp: Date = Date(),

            @param:JsonProperty("SignatureVersion") @get:JsonProperty("SignatureVersion")
            var signatureVersion: String = "",

            @param:JsonProperty("Signature") @get:JsonProperty("Signature")
            var signature: String = "",

            @param:JsonProperty("SigningCertURL") @get:JsonProperty("SigningCertURL")
            var signingCertUrl: String = "",

            @param:JsonProperty("UnsubscribeURL") @get:JsonProperty("UnsubscribeURL")
            var unsubscribeUrl: String? = ""
    )

    @LambadaFunction(
            name = "s2p_handle",
            memorySize = 512,
            timeout = 60,
            api = arrayOf(ApiGateway(path =
            "/sns/pushover/{user}"
            )))
    fun handle(inputStream: InputStream, outputStream: OutputStream, ctx: Context) {
        val req = PassthroughRequest.getRequest(MAPPER, SnsRequest::class.java, inputStream)

        verifySignature(req)

        when (req.body.type) {
            "Notification" -> onNotification(req)
            "SubscriptionConfirmation" -> onSubscribe(req)
            "UnsubscribeConfirmation" -> onUnsubscribe(req)
            else -> throw IllegalArgumentException("Invalid Body Type: ${req.body.type}")
        }
    }

    fun onNotification(req: PassthroughRequest<SnsRequest>) {
        val reqAsString = MAPPER.writeValueAsString(req)

        println("onNotification: req=${reqAsString}")

        val cwMessage = MAPPER.readValue(req.body.message, CloudWatchNotificationMessage::class.java)

        val contentsAsYaml = YAML_MAPPER.writeValueAsString(cwMessage)

        val pushoverClient = PushoverClient(req.stageVariables["PUSHOVER_SERVICE_TOKEN"]!!)

        // TODO: Parse Device List

        val message = PushoverClient.Message(
                user = req.params.path["user"]!!,
                title = "cw2pushover: ${req.body.subject} (${cwMessage.alarmName}",
                message = contentsAsYaml)

        pushoverClient.sendMessage(message)
    }

    fun onSubscribe(req: PassthroughRequest<SnsRequest>) {
        val reqAsString = MAPPER.writeValueAsString(req)

        println("onSubscribe: req=${reqAsString}")

        val pushoverClient = PushoverClient(req.stageVariables["PUSHOVER_SERVICE_TOKEN"]!!)

        val validateUserResult = pushoverClient.validateUser(PushoverClient.UserValidationRequest(
                user = req.params.path["user"]!!
        )) as ObjectNode

        val validateUserResultStatus = validateUserResult.get("status")?.intValue() ?: -1

        check(1 == validateUserResultStatus, { "Invalid Status (last request: ${validateUserResult})" })

        val result = URL(req.body.subscribeUrl).openStream().readBytes().toString(Charset.defaultCharset())

        println(result)
    }

    fun onUnsubscribe(req: PassthroughRequest<SnsRequest>) {
        println("onUnsubscribe: req=${req}")
    }

    private fun verifySignature(req: PassthroughRequest<SnsRequest>) {
        val signingCert = req.body.signingCertUrl

        val cert = getCertificate(signingCert, req.body.timestamp)

        val signature = req.body.signature

        SIGNATURE_CHECKER.verifySignature(req.body.message, signature, cert.publicKey)
    }

    companion object {
        val PATTERN_SNS_KEY = Regex("^sns\\.[\\-\\w]+\\.amazonaws.com$");

        val MAPPER = jacksonObjectMapper()

        val YAML_MAPPER = ObjectMapper(YAMLFactory()).registerKotlinModule()

        val SNS_CLIENT = AmazonSNSClient()

        val SIGNATURE_CHECKER = SignatureChecker()

        val snsCertMap: MutableMap<String, X509Certificate> = LinkedHashMap()

        val CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509")

        fun getCertificate(url: String, timestamp: Date): X509Certificate {
            val cert = if (snsCertMap.containsKey(url)) {
                snsCertMap[url]!!
            } else {
                val urlToLoad = URL(url)

                check((urlToLoad.protocol ?: "").equals("https"), { "Invalid protocol for url: ${url}" })

                check(PATTERN_SNS_KEY.matches(urlToLoad.host), { "Invalid URL Host for certificate: ${url} " })

                val newCert = CERTIFICATE_FACTORY.generateCertificate(urlToLoad.openStream()) as X509Certificate

                snsCertMap[url] = newCert

                newCert
            }

            val bBeforeValid = cert.notBefore.before(timestamp)
            val bAfterValid = cert.notAfter.after(timestamp)

            check(bBeforeValid && bAfterValid, {
                "Invalid Cert Dates: curDate=${timestamp}; " +
                        "cert.notBefore=${cert.notBefore}; " +
                        "cert.notAfter=${cert.notAfter}"
            })

            return cert
        }
    }
}