import org.tabletest.junit.TypeConverter;

public class YesNoConverter {
    @TypeConverter
    public static boolean fromYesNo(String value) {
        return "yes".equals(value);
    }
}
