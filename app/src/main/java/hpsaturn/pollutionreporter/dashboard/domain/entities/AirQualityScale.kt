package hpsaturn.pollutionreporter.dashboard.domain.entities

import hpsaturn.pollutionreporter.R

enum class AirQualityScale(val colorResourceId: Int, val nameResourceId: Int) {
    GOOD(R.color.scale_good, R.string.scale_good),
    MODERATE(R.color.scale_moderate, R.string.scale_moderate),
    UNHEALTHY_FOR_SENSITIVE_GROUPS(
        R.color.scale_unhealthy_for_sensitive_groups,
        R.string.scale_unhealthy_for_sensitive_groups
    ),
    UNHEALTHY(R.color.scale_unhealthy, R.string.scale_unhealthy),
    VERY_UNHEALTHY(R.color.scale_very_unhealthy, R.string.scale_very_unhealthy),
    HAZARDOUS(R.color.scale_hazardous, R.string.scale_hazardous)
}