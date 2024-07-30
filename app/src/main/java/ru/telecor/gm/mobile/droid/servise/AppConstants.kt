package ru.telecor.gm.mobile.droid.servise

/**
 * Project Truck Crew
 * Package ru.telecor.gm.mobile.droid
 *
 *
 *
 * Created by Artem Skopincev (aka sharpyx) 14.05.2021
 * Copyright © 2020 TKOInform. All rights reserved.
 */
object AppConstants {

    const val YANDEX_CLIENT = "179"
    const val YANDEX_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIBOwIBAAJBANJwZ2ndPH+ARCYOWpM136QdNQJND8tBXSjJjglQHSGTQk+Q+OyH\n" +
            "VQK5OZqmG2ns1HfUIB4STWNOI0VRuBWElJkCAwEAAQJAJbNHg5rEBN+Y1eyKY4cq\n" +
            "Sr+EyKW/p51NMEpzrM1yepoQyKFqfJcqAwwcqYw5U76NPvgMVbjzBi/oP/8UodZw\n" +
            "gQIhAPoaX0MyYByZ0Qd8x9pM4lNYwhA03EPQUDJg50KBOKVJAiEA12aejXSE4H3h\n" +
            "M/o4omc+EiZYvm9G/A2Cf9dIXbSshNECIQCj4x4jiWIju8OWtXS8nv78AMCCIDFq\n" +
            "GppCuO8xzD2TSQIgLrXuRDa0agpiP+dPSMiiyUTPkdyHxDkZx6dj5g48K0ECIQDn\n" +
            "r99jtIz5Ge4N3zwd6SZM3psFVwS8Nb1neNpa5kAnHg==\n" +
            "-----END RSA PRIVATE KEY-----"
    const val YANDEX_NAVI_PACKAGE = "ru.yandex.yandexnavi"

    internal val APP_DB_NAME = "gm_app_database.db"
    internal val EMPTY_EMAIL_ERROR = 1001
    internal val INVALID_EMAIL_ERROR = 1002
    internal val EMPTY_PASSWORD_ERROR = 1003
    internal val LOGIN_FAILURE = 1004
    internal val NULL_INDEX = -1L

    enum class LoggedInMode constructor(val type: Int) {
        LOGGED_IN_MODE_LOGGED_OUT(0),
        LOGGED_IN_MODE_GOOGLE(1),
        LOGGED_IN_MODE_FB(2),
        LOGGED_IN_MODE_SERVER(3)
    }
}