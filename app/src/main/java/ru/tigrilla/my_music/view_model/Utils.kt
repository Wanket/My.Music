package ru.tigrilla.my_music.view_model

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Duration

@SuppressLint("RestrictedApi")
fun <T> Flow<List<T>>.runOnLiveData(scope: CoroutineScope, liveData: MutableLiveData<List<T>>) = onEach {
    if (ArchTaskExecutor.getInstance().isMainThread) {
        liveData.value = it
    } else {
        liveData.postValue(it)
    }
}.launchIn(scope)

fun Duration.format(): String {
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = seconds % 60

    return if (h > 0) {
        "%02d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}
