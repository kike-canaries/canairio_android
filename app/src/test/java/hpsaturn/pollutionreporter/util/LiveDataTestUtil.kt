package hpsaturn.pollutionreporter.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Safely handles observables from LiveData for testing.
 * Taken from: https://github.com/google/iosched/blob/84415833c606210f8cddc1458ea23c02a8401d3c/androidTest-shared/src/main/java/com/google/samples/apps/iosched/androidtest/util/LiveDataTestUtil.kt
 */
object LiveDataTestUtil {

    /**
     * Gets the value of a LiveData safely.
     */
    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T? {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data = o
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        return data
    }
}

/**
 * Observes a [LiveData] until the `block` is done executing.
 */
fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
    val observer = Observer<T> { }
    try {
        observeForever(observer)
        block()
    } finally {
        removeObserver(observer)
    }
}