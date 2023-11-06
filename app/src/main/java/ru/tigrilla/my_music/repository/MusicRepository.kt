package ru.tigrilla.my_music.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.tigrilla.my_music.repository.entity.Track


@Dao
interface MusicRepository {
    @Insert
    suspend fun insertTrack(track: Track)

    @Query("SELECT COUNT(*) > 0 FROM Track WHERE name = :name AND author = :author")
    suspend fun checkTrackExist(name: String, author: String): Boolean

    @Query("SELECT * FROM TRACK ORDER BY lastUse DESC")
    fun allTracksOrderByLastUse(): Flow<List<Track>>

    @Delete
    suspend fun deleteTrack(track: Track)

    @Update
    suspend fun updateTracks(tracks: List<Track>)
}