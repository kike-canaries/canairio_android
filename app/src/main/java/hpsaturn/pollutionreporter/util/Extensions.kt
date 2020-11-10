package hpsaturn.pollutionreporter.util

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlin.math.round

/**
 * Combines a liveData `this` with the other liveData [liveData] using the [block] combination function.
 */
fun <T, K, R> LiveData<T>.combineWith(liveData: LiveData<K>, block: (T?, K?) -> R): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) { result.value = block(this.value, liveData.value) }
    result.addSource(liveData) { result.value = block(this.value, liveData.value) }
    return result
}

/**
 * Creates a new [Location] with the given [latitude] and [longitude].
 */
fun Location.createWith(latitude: Double, longitude: Double): Location {
    val location = Location("")
    location.longitude = longitude
    location.latitude = latitude
    return location
}

/**
 * Rounds `this` to [decimals] numbers.
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}