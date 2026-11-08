package com.example.remindme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE completed = 0")
    fun observeActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE completed = 1")
    fun observeCompleted(): Flow<List<TaskEntity>>


    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Int): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Int): TaskEntity?


    /** Several tasks at the same place come back together, so they can all fire at once. */
    @Query("SELECT * FROM tasks WHERE completed = 0 AND id IN (:ids)")
    suspend fun getActiveByIds(ids: List<Int>): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE completed = 0")
    suspend fun getActive(): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** Pass completedAt = null when un-completing. Un-completing also forgets lastFiredAt, so it can fire again. */
    @Query(
        """
        UPDATE tasks
        SET completed = :completed, completedAt = :completedAt,
            lastFiredAt = CASE WHEN :completed THEN lastFiredAt ELSE NULL END
        WHERE id = :id
        """
    )
    suspend fun setCompleted(id: Int, completed: Boolean, completedAt: Long?)

    /** The radius is one app-wide setting: applies it to every reminder that has a place. */
    @Query("UPDATE tasks SET radiusMeters = :radiusMeters WHERE radiusMeters IS NOT NULL")
    suspend fun setRadiusForAll(radiusMeters: Int)

    @Query("UPDATE tasks SET lastFiredAt = :firedAt WHERE id = :id")
    suspend fun setLastFiredAt(id: Int, firedAt: Long)

    @Query("UPDATE tasks SET lastFiredAt = :firedAt WHERE id IN (:ids)")
    suspend fun setLastFiredAt(ids: List<Int>, firedAt: Long)
}
