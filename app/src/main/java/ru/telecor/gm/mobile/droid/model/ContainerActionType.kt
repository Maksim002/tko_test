package ru.telecor.gm.mobile.droid.model

enum class ContainerActionType(val viewName: String) {
    MAINTAIN("Инвентаризация"),
    REPLACE( "Установить ёмкость"),
    PICKUP( "Забрать с заменой"),
    TAKE( "Подбор"),
    TAKE_EMPTY("Забрать без замены"),
    TRANSIT("Перевозка"),
    TRANSIT_V2("Перевозка (МПС - МПС)"),
    PICKUP_WITH_DETOUR("Подбор с объездом"),
    WASH("Дезинфекция"),
    SERVICE("Ремонт ёмкости"),
    INVENTORY("Инвентаризация"),
    BRANDING("Брендировать ёмкость"),
    IDENTIFICATION("Установить идентификатор"),
    REMOVE_COVER("Снять крышку"),
    INSTALL_COVER("Установить крышку"),
}