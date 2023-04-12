package sg.edu.ntu.scse.labattendancesystem.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    @TypeConverter
    fun datetimeFromIsoTimeString(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun datetimeToIsoTimeString(date: LocalDateTime?): String? {
        return date?.format(dateTimeFormatter)
    }


    private val dateFormatter = DateTimeFormatter.ISO_DATE

    @TypeConverter
    fun dateFromIsoTimeString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun dateToIsoTimeString(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }
}