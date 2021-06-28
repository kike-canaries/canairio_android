package hpsaturn.pollutionreporter.util

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.concurrent.Executor

class AutoSuccessTask<TResult>(private val data: TResult) : Task<TResult>() {


    override fun isComplete(): Boolean = throw NotImplementedError("Method not implemented")

    override fun getException(): Exception? = throw NotImplementedError("Method not implemented")

    override fun addOnFailureListener(p0: OnFailureListener): Task<TResult> =
        throw NotImplementedError("Method not implemented")

    override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<TResult> =
        throw NotImplementedError("Method not implemented")

    override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<TResult> =
        throw NotImplementedError("Method not implemented")

    override fun getResult(): TResult? = data

    override fun <X : Throwable?> getResult(p0: Class<X>): TResult? =
        throw NotImplementedError("Method not implemented")

    override fun addOnSuccessListener(onSuccessListener: OnSuccessListener<in TResult>): Task<TResult> {
        onSuccessListener.onSuccess(data)
        return this
    }

    override fun addOnSuccessListener(
        p0: Executor,
        p1: OnSuccessListener<in TResult>
    ): Task<TResult> = throw NotImplementedError("Method not implemented")

    override fun addOnSuccessListener(
        p0: Activity,
        p1: OnSuccessListener<in TResult>
    ): Task<TResult> = throw NotImplementedError("Method not implemented")

    override fun isSuccessful(): Boolean = throw NotImplementedError("Method not implemented")

    override fun isCanceled(): Boolean = throw NotImplementedError("Method not implemented")
}