package hpsaturn.pollutionreporter.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
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
 * Rounds `this` to [decimals] numbers.
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

suspend fun DatabaseReference.getSuspendValue(): DataSnapshot = suspendCoroutine { continuation ->
    addListenerForSingleValueEvent(ImpValueEventListener(
        onDataChange = { continuation.resume(it) },
        onError = { continuation.resumeWithException(it.toException()) }
    ))
}

class ImpValueEventListener(
    val onDataChange: (DataSnapshot) -> Unit,
    val onError: (DatabaseError) -> Unit
) : ValueEventListener {
    override fun onDataChange(data: DataSnapshot) = onDataChange.invoke(data)
    override fun onCancelled(error: DatabaseError) = onError.invoke(error)
}