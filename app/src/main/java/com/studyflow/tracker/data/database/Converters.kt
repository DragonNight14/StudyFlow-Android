package com.studyflow.tracker.data.database

import androidx.room.TypeConverter
import com.studyflow.tracker.data.model.Priority
import com.studyflow.tracker.data.model.AssignmentSource
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

    @TypeConverter
    fun fromAssignmentSource(source: AssignmentSource): String {
        return source.name
    }

    @TypeConverter
    fun toAssignmentSource(source: String): AssignmentSource {
        return AssignmentSource.valueOf(source)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
