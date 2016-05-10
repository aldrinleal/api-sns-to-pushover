package io.ingenieux.sns2pushover

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/*
 * Copyright (c) 2016 ingenieux Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
        @param:JsonProperty("MetricName") @get:com.fasterxml.jackson.annotation.JsonProperty("MetricName")
        var metricName: String? = null
        ,
        @param:JsonProperty("Namespace") @get:com.fasterxml.jackson.annotation.JsonProperty("Namespace")
        var namespace: String? = null
        ,
        @param:JsonProperty("Statistic") @get:com.fasterxml.jackson.annotation.JsonProperty("Statistic")
        var statistic: String? = null
        ,
        @param:JsonProperty("Unit") @get:com.fasterxml.jackson.annotation.JsonProperty("Unit")
        var unit: String? = null
        ,
        @param:JsonProperty("Dimensions") @get:com.fasterxml.jackson.annotation.JsonProperty("Dimensions")
        var dimensions: Array<Dimension>? = null
        ,
        @param:JsonProperty("Period") @get:com.fasterxml.jackson.annotation.JsonProperty("Period")
        var period: Int
        ,
        @param:JsonProperty("EvaluationPeriods") @get:com.fasterxml.jackson.annotation.JsonProperty("EvaluationPeriods")
        var evaluationPeriods: Int
        ,
        @param:JsonProperty("ComparisonOperator") @get:com.fasterxml.jackson.annotation.JsonProperty("ComparisonOperator")
        var comparisonOperator: String
        ,
        @param:JsonProperty("Threshold") @get:com.fasterxml.jackson.annotation.JsonProperty("Threshold")
        var threshold: Int
)

data class CloudWatchNotificationMessage @JsonCreator constructor(
        @param:JsonProperty("AlarmName") @get:com.fasterxml.jackson.annotation.JsonProperty("AlarmName")
        var alarmName: String? = null
        ,
        @param:JsonProperty("AlarmDescription") @get:com.fasterxml.jackson.annotation.JsonProperty("AlarmDescription")
        var alarmDescription: String? = null
        ,
        @param:JsonProperty("AWSAccountId") @get:com.fasterxml.jackson.annotation.JsonProperty("AWSAccountId")
        var awsAccountId: String? = null
        ,
        @param:JsonProperty("NewStateValue") @get:com.fasterxml.jackson.annotation.JsonProperty("NewStateValue")
        var newStateValue: String? = null
        ,
        @param:JsonProperty("NewStateReason") @get:com.fasterxml.jackson.annotation.JsonProperty("NewStateReason")
        var newStateReason: String? = null
        ,
        @param:JsonProperty("StateChangeTime") @get:com.fasterxml.jackson.annotation.JsonProperty("StateChangeTime")
        var stateChangeTime: Date? = null
        ,
        @param:JsonProperty("OldStateValue") @get:com.fasterxml.jackson.annotation.JsonProperty("OldStateValue")
        var oldStateValue: String? = null
        ,
        @param:JsonProperty("Region") @get:com.fasterxml.jackson.annotation.JsonProperty("Region")
        var region: String? = null
        ,
        @param:JsonProperty("Trigger") @get:com.fasterxml.jackson.annotation.JsonProperty("Trigger")
        var trigger: Trigger? = null
)

data class SnsRequest @JsonCreator constructor(
        @param:JsonProperty("Type") @get:com.fasterxml.jackson.annotation.JsonProperty("Type")
        var type: String = "",

        @param:JsonProperty("MessageId") @get:com.fasterxml.jackson.annotation.JsonProperty("MessageId")
        var messageId: String = "",

        @param:JsonProperty("Token") @get:com.fasterxml.jackson.annotation.JsonProperty("Token")
        var token: String? = "",

        @param:JsonProperty("TopicArn") @get:com.fasterxml.jackson.annotation.JsonProperty("TopicArn")
        var topicArn: String = "",

        @param:JsonProperty("Message") @get:com.fasterxml.jackson.annotation.JsonProperty("Message")
        var message: String = "",

        @param:JsonProperty("Subject") @get:com.fasterxml.jackson.annotation.JsonProperty("Subject")
        var subject: String? = "",

        @param:JsonProperty("SubscribeURL")
        @get:com.fasterxml.jackson.annotation.JsonProperty("SubscribeURL")
        var subscribeUrl: String? = "",

        @param:JsonProperty("Timestamp") @get:com.fasterxml.jackson.annotation.JsonProperty("Timestamp")
        var timestamp: Date = Date(),

        @param:JsonProperty("SignatureVersion") @get:com.fasterxml.jackson.annotation.JsonProperty("SignatureVersion")
        var signatureVersion: String = "",

        @param:JsonProperty("Signature") @get:com.fasterxml.jackson.annotation.JsonProperty("Signature")
        var signature: String = "",

        @param:JsonProperty("SigningCertURL") @get:com.fasterxml.jackson.annotation.JsonProperty("SigningCertURL")
        var signingCertUrl: String = "",

        @param:JsonProperty("UnsubscribeURL") @get:com.fasterxml.jackson.annotation.JsonProperty("UnsubscribeURL")
        var unsubscribeUrl: String? = ""
)
