package ru.telecor.gm.mobile.droid.entities.processing

import com.google.gson.annotations.SerializedName
import ru.telecor.gm.mobile.droid.entities.ContainerFailureReason
import ru.telecor.gm.mobile.droid.entities.db.PhotoProcessingForApi

/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid.entities.processing
 *
 *
 *
 * Created by Artem Skopincev (aka sharpyx) 17.09.2020
 * Copyright © 2020 TKOInform. All rights reserved.
 */

data class ContainerStatus(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("containerFailureReason")
    val containerFailureReason: ContainerFailureReason? = null,
    @SerializedName("containerTypeId")
    val containerTypeId: String? = null,
    @SerializedName("contractId")
    val contractId: String? = null,
    @SerializedName("createTime")
    val createTime: Long? = null,
    @SerializedName("statusType")
    val statusType: ContainerStatusType? = null,
    @SerializedName("volumeAct")
    var volumeAct: Double? = null,
    @SerializedName("volumePercent")
    var volumePercent: Double? = null,
    @SerializedName("weight")
    var weight: Int? = null,
    @SerializedName("photos")
    var photos: List<PhotoProcessingForApi>? = null,
    @SerializedName("rfid")
    var rfid: String? = null,
    @SerializedName("staff")
    var staff: Staff? = null,
    @SerializedName("allGroupContainersId")
    var allGroupContainersId: List<Long>? = null
){
    companion object {
        fun containerStatusUi(photo: MutableList<ContainerStatusOr>? = null): List<ContainerStatus> {
            val containerStatusesFin: ArrayList<ContainerStatus> = arrayListOf()
            photo?.forEach {
                containerStatusesFin.add(
                    ContainerStatus(
                        it.id, it.containerFailureReason, it.containerTypeId, it.contractId,
                        it.createTime, it.statusType, it.volumeAct, it.volumePercent, it.weight,
                        it.photos, it.rfid, it.staff
                    )
                )
                if (containerStatusesFin.size == photo.size) {
                    return containerStatusesFin
                }
            }
            return arrayListOf()
        }
    }
}

data class ContainerStatusOr(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("containerFailureReason")
    val containerFailureReason: ContainerFailureReason? = null,
    @SerializedName("containerTypeId")
    val containerTypeId: String? = null,
    @SerializedName("contractId")
    val contractId: String? = null,
    @SerializedName("createTime")
    val createTime: Long? = null,
    @SerializedName("statusType")
    val statusType: ContainerStatusType? = null,
    @SerializedName("volumeAct")
    var volumeAct: Double? = null,
    @SerializedName("volumePercent")
    var volumePercent: Double? = null,
    @SerializedName("weight")
    var weight: Int? = null,
    @SerializedName("photos")
    var photos: List<PhotoProcessingForApi>? = null,
    @SerializedName("rfid")
    var rfid: String? = null,
    @SerializedName("staff")
    var staff: Staff? = null
) {
    companion object {
        fun containerStatusOriginal(photo: MutableList<ContainerStatus>? = null): List<ContainerStatusOr> {
            val containerStatusesFin: ArrayList<ContainerStatusOr> = arrayListOf()
            photo?.forEach {
                containerStatusesFin.add(
                    ContainerStatusOr(
                        it.id, it.containerFailureReason, it.containerTypeId, it.contractId,
                        it.createTime, it.statusType, it.volumeAct, it.volumePercent, it.weight,
                        it.photos, it.rfid, it.staff
                    )
                )
                if (containerStatusesFin.size == photo.size) {
                    return containerStatusesFin
                }
            }
            return arrayListOf()
        }
    }
}
