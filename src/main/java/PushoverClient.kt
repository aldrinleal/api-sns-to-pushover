package io.ingenieux.sns2pushover

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.StringUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import java.nio.charset.Charset
import java.util.*

class PushoverClient(val token: String) {
    data class UserValidationRequest(
            val user: String,
            val device: String? = null
    ) {
        fun getUrlParameters(): List<NameValuePair> {
            val result: MutableList<NameValuePair> = ArrayList()

            result.add(BasicNameValuePair("user", user))


            if (StringUtils.isNotBlank(device))
                result.add(BasicNameValuePair("device", device))
            return result
        }
    }

    data class Message(
            val user: String,
            val device: String? = null,
            val title: String,
            val message: String,
            val html: Boolean = false,
            val priority: Int? = null,
            val url: String? = null,
            val urlTitle: String? = null,
            val sound: String? = null) {
        fun getUrlParameters(): List<NameValuePair> {
            val result: MutableList<NameValuePair> = ArrayList()

            result.add(BasicNameValuePair("user", user))

            result.add(BasicNameValuePair("message", message))

            if (StringUtils.isNotBlank(device))
                result.add(BasicNameValuePair("device", device))

            if (StringUtils.isNotBlank(title))
                result.add(BasicNameValuePair("title", title))

            if (StringUtils.isNotBlank(url))
                result.add(BasicNameValuePair("url", url))

            if (StringUtils.isNotBlank(urlTitle))
                result.add(BasicNameValuePair("url_title", urlTitle))

            if (null != priority)
                result.add(BasicNameValuePair("priority", priority.toString()))

            if (StringUtils.isNotBlank(sound))
                result.add(BasicNameValuePair("sound", sound))

            result.add(BasicNameValuePair("html", if (html) "1" else "0"))

            return result.toList()
        }
    }

    fun sendMessage(m: Message) {
        val request = HttpPost("https://api.pushover.net/1/messages.json")

        val urlParameters = listOf(BasicNameValuePair("token", token))
                .plus(m.getUrlParameters())

        request.entity = UrlEncodedFormEntity(urlParameters)

        val response = HTTP_CLIENT.execute(request)

        val responseEntity = response.entity.content.readBytes().toString(Charset.defaultCharset())

        println("response: ${response} # ${responseEntity}")
    }

    fun validateUser(u: UserValidationRequest): JsonNode? {
        val request = HttpPost("https://api.pushover.net/1/users/validate.json")

        val urlParameters = listOf(BasicNameValuePair("token", token))
                .plus(u.getUrlParameters())

        request.entity = UrlEncodedFormEntity(urlParameters)

        val response = HTTP_CLIENT.execute(request)

        val responseEntity = response.entity.content.readBytes().toString(Charset.defaultCharset())

        println("response: ${response} # ${responseEntity}")

        val responseAsJson = MAPPER.readTree(responseEntity)

        return responseAsJson
    }

    companion object {
        val HTTP_CLIENT = HttpClientBuilder.create().build()

        val MAPPER = SnsToPushover.MAPPER
    }

}