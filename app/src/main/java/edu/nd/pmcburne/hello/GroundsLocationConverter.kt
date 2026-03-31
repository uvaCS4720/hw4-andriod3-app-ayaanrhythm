package edu.nd.pmcburne.hello

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GroundsLocationConverter {
    private val jsonParser = Gson()

    @TypeConverter
    fun tagListToJson(tags: List<String>): String {
        return jsonParser.toJson(tags)
    }

    @TypeConverter
    fun jsonToTag(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return jsonParser.fromJson(value, listType) ?: emptyList()
    }
}