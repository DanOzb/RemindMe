package com.example.remindme.data

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun repeatRuleToJson(rule: RepeatRule?): String? = rule?.let { json.encodeToString(it) }

    @TypeConverter
    fun jsonToRepeatRule(value: String?): RepeatRule? = value?.let { json.decodeFromString<RepeatRule>(it) }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
