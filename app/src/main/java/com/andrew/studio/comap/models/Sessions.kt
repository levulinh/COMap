package com.andrew.studio.comap.models

data class Sessions(var code: Int, var createdAt: Long) {
    companion object {
        val TABLE_NAME = "sessions"
        val COLUMN_CODE = "code"
        val COLUMN_CREATED_AT = "createdAt"
    }
}