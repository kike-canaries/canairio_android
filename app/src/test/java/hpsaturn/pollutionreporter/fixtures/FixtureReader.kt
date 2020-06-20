package hpsaturn.pollutionreporter.fixtures

import java.io.File

// TODO - This is a quick hack but we recommend to implement a cleaner way to get the path.
private const val PATH_TO_FIXTURE = "src/test/java/hpsaturn/pollutionreporter/fixtures/"

fun readFixture(fixture: JsonFixture): String = File("${PATH_TO_FIXTURE}station_feed.json")
        .readLines().joinToString(" ")

enum class JsonFixture(val fileName: String) {
    STATION_FEED("station_feed.json")
}