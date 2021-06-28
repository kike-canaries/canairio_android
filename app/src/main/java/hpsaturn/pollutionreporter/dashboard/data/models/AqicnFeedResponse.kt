package hpsaturn.pollutionreporter.dashboard.data.models


/**
 * Air quality information fetched from Aqicn API.
 * @property data Air quality station data.
 * @property status Status code, can be ok or error.
 * See more here: https://aqicn.org/json-api/doc/
 */
data class AqicnFeedResponse(
    val data: Data,
    val status: String
)