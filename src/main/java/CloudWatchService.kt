package io.ingenieux.sns2pushover

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.*
import com.amazonaws.services.cloudwatch.model.Dimension
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

object CloudWatchService {
    val cloudWatch = AmazonCloudWatchClient()

    fun showMetricsFor(functionName: String): Map<String, Map<String, String>> {
        val results: MutableMap<String, Map<String, String>> = LinkedHashMap()

        val dimensions = DimensionFilter()
                .withName("FunctionName")
                .withValue(functionName)

        val listMetricsRequest = ListMetricsRequest()
                .withNamespace("AWS/Lambda")
                .withDimensions(dimensions)

        val listMetricsResult = cloudWatch.listMetrics(listMetricsRequest)

        results.putAll(listMetricsResult.metrics.map { metric ->
            val getMetricStatisticsRequest = GetMetricStatisticsRequest()
                .withMetricName(metric.metricName)
                .withNamespace(metric.namespace)
                .withDimensions(Dimension().withName("FunctionName").withValue(functionName))
                .withStartTime(Date(System.currentTimeMillis() - 5 * 60000L))
                .withEndTime(Date())
                .withPeriod(300)
                .withStatistics(Statistic.values().map { it.toString() })

            val getMetricsStatisticsResult = cloudWatch.getMetricStatistics(getMetricStatisticsRequest)

            return getMetricsStatisticsResult.datapoints.map { datapoint ->
                val key = "${metric.namespace}:${metric.metricName}:${getMetricsStatisticsResult.label}:${datapoint.timestamp}"
                val datapointAsMap = mapOf(
                        "min" to datapoint.minimum.toString(),
                        "max" to datapoint.maximum.toString(),
                        "average" to datapoint.average.toString(),
                        "sampleCount" to datapoint.sampleCount.toString(),
                        "sum" to datapoint.sum.toString(),
                        "unit" to datapoint.unit.toString()
                )

                Pair<String, Map<String, String>>(key, datapointAsMap)
            }.toMap()
        })

        return results
    }
}