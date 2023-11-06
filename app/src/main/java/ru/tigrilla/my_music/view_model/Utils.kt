package ru.tigrilla.my_music.view_model

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@SuppressLint("RestrictedApi")
fun <T> Flow<List<T>>.runOnLiveData(scope: CoroutineScope, liveData: MutableLiveData<List<T>>) = onEach {
    if (ArchTaskExecutor.getInstance().isMainThread) {
        liveData.value = it
    } else {
        liveData.postValue(it)
    }
}.launchIn(scope)

fun <T, R : Comparable<R>> Flow<List<T>>.sortEachBy(block: (T) -> R) = onEach {
    it.sortedBy(block)
}
