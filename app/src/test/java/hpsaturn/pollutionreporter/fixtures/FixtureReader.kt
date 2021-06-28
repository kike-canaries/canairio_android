package hpsaturn.pollutionreporter.fixtures

import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type

// TODO - This is a quick hack but we recommend to implement a cleaner way to get the path.
private const val PATH_TO_FIXTURE = "src/test/java/hpsaturn/pollutionreporter/fixtures/"

fun readFixture(fixture: JsonFixture): String = File("${PATH_TO_FIXTURE}station_feed.json")
    .readLines().joinToString(" ")

fun <T>readFixture(fixture: JsonFixture, classOfT: Class<T>): T = Gson().fromJson(readFixture
    (fixture), classOfT)

enum class JsonFixture(val fileName: String) {
    STATION_FEED("station_feed.json")
}