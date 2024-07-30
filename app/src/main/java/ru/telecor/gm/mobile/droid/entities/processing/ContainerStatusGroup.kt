package ru.telecor.gm.mobile.droid.entities.processing

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import ru.telecor.gm.mobile.droid.entities.db.PhotoProcessingForApi

class ContainerStatusGroupAll (
    @SerializedName("containerStatuses")
    @Expose
    var containerStatuses: List<ContainerStatusGrop>? = null,

    @SerializedName("createTime")
    @Expose
    var createTime: Long? = null,

    @SerializedName("id")
    @Expose
    var id: Long? = null,

    @SerializedName("photos")
    @Expose
    var photos: List<PhotoProcessingForApi>? = null,

    @SerializedName("volume")
    @Expose
    var volume: Double? = null,

    @SerializedName("weight")
    @Expose
    var weight: Double? = null,

    @SerializedName("groupId")
    @Expose
    var groupId: Int? = null
)

class ContainerStatusGroup (
    @SerializedName("containerStatuses")
    @Expose
    var containerStatuses: List<ContainerStatusGrop>? = null,

    @SerializedName("createTime")
    @Expose
    var createTime: Long? = null,

    @SerializedName("id")
    @Expose
    var id: String? = null,

    @SerializedName("photos")
    @Expose
    var photos: List<PhotoProcessingForApi>? = null,

    @SerializedName("volume")
    @Expose
    var volume: Double? = null,

    @SerializedName("weight")
    @Expose
    var weight: Double? = null
){
    companion object {
        fun fromContainerStatusGroupAll(photo: MutableList<ContainerStatusGroupAll>? = null): List<ContainerStatusGroup> {
            val containerStatusesFin: ArrayList<ContainerStatusGroup> = arrayListOf()
            photo?.forEach {
                containerStatusesFin.add(ContainerStatusGroup(
                    containerStatuses = it.containerStatuses,
                    createTime = it.createTime,
                    photos = it.photos,
                ))
                if (containerStatusesFin.size == photo.size){
                    return containerStatusesFin
                }
            }
            return arrayListOf()
        }
    }
}