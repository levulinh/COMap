package com.andrew.studio.comap.config

object Config {
    private const val BASE_URL = "http://co-buddy.herokuapp.com"
    const val NEW_SESSION_URL = "$BASE_URL/session/new"
    const val UPDATE_LOCATION_URL = "$BASE_URL/session/update"
    const val GET_ALL_DATA = "$BASE_URL/session/buck"
    const val GET_SESSION_STATUS = "$BASE_URL/session/online"
    const val SET_SESSION_STATUS = "$BASE_URL/session/online"
}
