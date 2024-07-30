package ru.telecor.gm.mobile.droid.entities.task

import com.google.gson.annotations.SerializedName
import ru.telecor.gm.mobile.droid.entities.ActualReason
import ru.telecor.gm.mobile.droid.entities.ContainerType
import ru.telecor.gm.mobile.droid.entities.GarbageType
import ru.telecor.gm.mobile.droid.entities.TaskFailureReason
import ru.telecor.gm.mobile.droid.entities.db.PhotoProcessingForApi
import ru.telecor.gm.mobile.droid.entities.db.ProcessingPhoto
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatus
import ru.telecor.gm.mobile.droid.entities.processing.ContainerStatusOr
import ru.telecor.gm.mobile.droid.model.PhotoType
import java.io.File
import java.text.SimpleDateFormat

data class StatusTaskExtended(
    val id: Int,

    val containerType: ContainerType,

    val taskItemId: Int,

    val containerStatus: ContainerStatus? = null,

    val draftData: ContainerStatus? = null,

    val rule: String? = null,

    val containerAction: String? = null,

    val containerGroups: GarbageType? = null,

    val taskStatus: TaskFailureReason? = null,

    //для сортировки в контейнеры
    var posInGroup: String? = null,

    //уникальное айди для контейнеров в группе
    var groupId: String? = null,

    var privatePhotos: List<ProcessingPhoto>? = null
)
