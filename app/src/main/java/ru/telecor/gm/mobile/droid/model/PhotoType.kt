package ru.telecor.gm.mobile.droid.model

enum class PhotoType(val photoTag: String, val serverName: String, val viewName: String, var status: String) {
    TASK_TROUBLE("TTR", "ROUTE_TASK_FAILURE_REASON_PHOTO","Невывоз", "all"),
    LOAD_BEFORE("LB", "PHOTO_BEFORE_SHIPPING","До погрузки", "all"),
    LOAD_AFTER("LA", "PHOTO_AFTER_SHIPPING","После погрузки", "all"),
    LOAD_TROUBLE("LT", "ROUTE_TASK_ACTUAL_REASON_PHOTO","Проблемы с погрузкой", "all"),
    LOAD_TROUBLE_BLOCKAGE("LTB", "ROUTE_TASK_ACTUAL_REASON_PHOTO","Проблемы с погрузкой", "all"),

    CONTAINER_TROUBLE ( "CT" , "CONTAINER_TROUBLE", "Проблема работы с контейнером", "one"),
    CONTAINER_BEFORE  ( "CB" , "CONTAINER_BEFORE", "Фото контейнера ДО", "one"),
    CONTAINER_AFTER("CA", "CONTAINER_AFTER", "Фото контейнера ПОСЛЕ", "one"),
    DOCUMENTARY_PHOTO("DP", "DOCUMENTARY_PHOTO", "Фото документов", "one");

    companion object {
        fun fromPhotoTag(tag: String): PhotoType {
            when (tag) {
                "TTR" -> return TASK_TROUBLE
                "LB" -> return LOAD_BEFORE
                "LA" -> return LOAD_AFTER
                "LT" -> return LOAD_TROUBLE
                "LTB" -> return LOAD_TROUBLE_BLOCKAGE

                "CT" -> return CONTAINER_TROUBLE
                "CB" -> return CONTAINER_BEFORE
                "CA" -> return CONTAINER_AFTER
                "DP" -> return DOCUMENTARY_PHOTO
            }

            throw Exception("not found")
        }
    }
}