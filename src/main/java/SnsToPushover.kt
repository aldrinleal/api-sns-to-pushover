package io.ingenieux.sns2pushover

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.util.SignatureChecker
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ingenieux.lambada.runtime.ApiGateway
import io.ingenieux.lambada.runtime.LambadaFunction
import io.ingenieux.lambada.runtime.LambadaUtils
import io.ingenieux.lambada.runtime.Patch
import io.ingenieux.lambada.runtime.model.PassthroughRequest
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.charset.Charset
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class SnsToPushover {
    @LambadaFunction(name = "s2p_showLambdaMetrics",
            memorySize = 512,
            timeout = 120,
            api = arrayOf(ApiGateway(path = "/sns/health/lambda/{function}", method = ApiGateway.MethodType.GET))
    )
    fun showLambdaMetrics(inputStream: InputStream, outputStream: OutputStream, context: Context) {
        val req = LambadaUtils.getRequest(MAPPER, inputStream)

        MAPPER.writeValue(outputStream, CloudWatchService.showMetricsFor(req.params.path["function"]!!))
    }

    @LambadaFunction(
            name = "s2p_handle",
            memorySize = 512,
            timeout = 60,
            api = arrayOf(ApiGateway(path =
            "/sns/pushover/{user}",
                    patches = arrayOf(
                            Patch(patchType = Patch.PatchType.Replace,
                                    path = "/consumes/0",
                                    patchValue = "\"text/pĺain\""),
                            Patch(patchType = Patch.PatchType.Move,
                                    from = "/x-amazon-apigateway-integration/requestTemplates/application~1json",
                                    path = "/x-amazon-apigateway-integration/requestTemplates/text~1plain")
                    )
            )))
    fun handle(inputStream: InputStream, outputStream: OutputStream, ctx: Context) {
        //println("Context: " + MAPPER.writeValueAsString(ctx))

        if (ctx?.clientContext?.custom?.containsKey("warm") ?: false) {
            MAPPER.writeValue(outputStream, mapOf(
                    "warm" to "warmed"
            ))

            return;
        }

        val req = LambadaUtils.getRequest(MAPPER, SnsRequest::class.java, inputStream)

        verifySignature(req)

        when (req.body.type) {
            "Notification" -> onNotification(req)
            "SubscriptionConfirmation" -> onSubscribe(req)
            "UnsubscribeConfirmation" -> onUnsubscribe(req)
            else -> throw IllegalArgumentException("Invalid Body Type: ${req.body.type}")
        }
    }

    @LambadaFunction(
            name = "s2p_warm",
            memorySize = 512,
            timeout = 60,
            api = arrayOf(ApiGateway(path =
            "/sns/warm", method = ApiGateway.MethodType.GET
            )))
    fun warm(inputStream: InputStream, outputStream: OutputStream, ctx: Context) {
        val lambdaClient = AWSLambdaClient()

        val clientContext = Base64.getEncoder().encode(MAPPER.writeValueAsString(mapOf(
                "custom" to mapOf(
                        "warm" to "true"
                )
        )).toByteArray()).toString(Charset.defaultCharset())

        val invokeRequest = InvokeRequest()
                .withFunctionName("s2p_handle")
                .withClientContext(clientContext)

        val result = lambdaClient.invoke(invokeRequest)

        val resultPayload = result.payload.array().toString(Charset.defaultCharset())

        println("result: ${result} (payload: ${resultPayload}")
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
                message = contentsAsYaml,
                url = req.body.unsubscribeUrl,
                urlTitle = "Unsubscribe Notification")

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
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

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