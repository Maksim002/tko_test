package ru.telecor.gm.mobile.droid.model.mappers

import ru.telecor.gm.mobile.droid.entities.db.TaskExtended
import ru.telecor.gm.mobile.droid.entities.db.TaskProcessingResult
import ru.telecor.gm.mobile.droid.entities.task.TaskRelations
import ru.telecor.gm.mobile.droid.model.data.db.dao.TaskDraftProcessingResultDao
import ru.telecor.gm.mobile.droid.model.data.db.dao.TaskProcessingResultDao
import javax.inject.Inject

class ConverterMappers @Inject constructor(
    private val taskProcessingResultDao: TaskProcessingResultDao,
    private val taskDraftProcessingResultDao: TaskDraftProcessingResultDao
) {

    private fun taskExtendedCon(task: TaskExtended, taskProcessingResult: TaskProcessingResult?, draftExist: Boolean): TaskRelations {
        return TaskRelations(task, taskProcessingResult,draftExist)
    }

    fun relationsTuTaskExtended(
        tasks: List<TaskExtended>?
    ): List<TaskRelations> {
        val res = ArrayList<TaskRelations>()
        tasks?.map {
            val taskResult  = taskProcessingResultDao.getById(it.id.toLong())
            val draftExist  = taskDraftProcessingResultDao.checkExist(it.id.toLong()) && taskResult == null
            res.add(taskExtendedCon(it, taskResult,draftExist))
        }
        return res
    }
}