package com.kingsmetric.marksman

import com.kingsmetric.importflow.FieldKey

enum class MarksmanLaneDetailedMetricAvailability {
    Available,
    Unavailable
}

enum class MarksmanLaneGroupAvailability {
    Complete,
    Partial
}

data class MarksmanLaneDetailedMetric(
    val field: FieldKey,
    val value: String?,
    val availability: MarksmanLaneDetailedMetricAvailability
)

data class MarksmanLaneDetailedMetricGroupResult(
    val group: MarksmanLaneMetricGroup,
    val metrics: List<MarksmanLaneDetailedMetric>,
    val availability: MarksmanLaneGroupAvailability
) {
    val isComplete: Boolean
        get() = availability == MarksmanLaneGroupAvailability.Complete
}

data class MarksmanLaneDetailedMetrics(
    val groups: List<MarksmanLaneDetailedMetricGroupResult>
) {
    fun group(group: MarksmanLaneMetricGroup): MarksmanLaneDetailedMetricGroupResult {
        return groups.first { it.group == group }
    }
}

class MarksmanLaneDetailedMetricsCalculator {

    fun calculate(analysis: MarksmanLaneAnalysisState.Eligible): MarksmanLaneDetailedMetrics {
        return MarksmanLaneDetailedMetrics(
            groups = marksmanLaneMetricGroups.map { definition ->
                val metrics = definition.fields.map { field ->
                    val value = analysis.input.valueFor(field)?.takeIf { it.isNotBlank() }
                    MarksmanLaneDetailedMetric(
                        field = field,
                        value = value,
                        availability = if (value == null) {
                            MarksmanLaneDetailedMetricAvailability.Unavailable
                        } else {
                            MarksmanLaneDetailedMetricAvailability.Available
                        }
                    )
                }

                MarksmanLaneDetailedMetricGroupResult(
                    group = definition.group,
                    metrics = metrics,
                    availability = if (metrics.all { it.availability == MarksmanLaneDetailedMetricAvailability.Available }) {
                        MarksmanLaneGroupAvailability.Complete
                    } else {
                        MarksmanLaneGroupAvailability.Partial
                    }
                )
            }
        )
    }
}
