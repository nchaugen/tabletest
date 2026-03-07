import org.tabletest.junit.TypeConverter

class YesNoConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromYesNo(value: String): Boolean = value == "yes"
    }
}
