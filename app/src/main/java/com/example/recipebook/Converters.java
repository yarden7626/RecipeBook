package com.example.recipebook;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.List;

// מחלקה לביצוע המרות בין טיפוסי נתונים מורכבים לטיפוסים פשוטים שניתן לשמור בבסיס הנתונים
public class Converters {


    // המרה מרשימת מחרוזות למחרוזת אחת מופרדת בפסיקים
    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) return "";
        return String.join(",", list);
    }

    // המרה ממחרוזת מופרדת בפסיקים לרשימת מחרוזות
    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.isEmpty()) return Arrays.asList();
        return Arrays.asList(data.split(","));
    }
} 