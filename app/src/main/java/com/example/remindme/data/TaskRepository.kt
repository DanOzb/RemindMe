package com.example.remindme.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun observeActive(): Flow<List<TaskEntity>> = dao.observeActive()

    fun observeCompleted(): Flow<List<TaskEntity>> = dao.observeCompleted()

    fun observe(id: Int): Flow<TaskEntity?> = dao.observeById(id)

    suspend fun get(id: Int): TaskEntity? = dao.getById(id)

    /** Inserts when id is 0, otherwise updates. Returns the task's id. */
    suspend fun save(task: TaskEntity): Int =
        if (task.id == 0) dao.insert(task).toInt()
        else task.id.also { dao.update(task) }

    suspend fun delete(id: Int) = dao.deleteById(id)

    suspend fun setCompleted(id: Int, completed: Boolean) =
        dao.setCompleted(id, completed, if (completed) System.currentTimeMillis() else null)

    suspend fun setRadiusForAll(radiusMeters: Int) = dao.setRadiusForAll(radiusMeters)

    suspend fun getActive(): List<TaskEntity> = dao.getActive()

    suspend fun getActiveByIds(ids: List<Int>): List<TaskEntity> = dao.getActiveByIds(ids)

    suspend fun markFired(ids: List<Int>) = dao.setLastFiredAt(ids, System.currentTimeMillis())
}
