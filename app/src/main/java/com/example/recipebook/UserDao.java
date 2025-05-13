package com.example.recipebook;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao // מציין שמדובר ב-Data Access Object (DAO) עבור בסיס נתונים של Room
public interface UserDao {

    @Insert // פעולה להוספת משתמש חדש לבסיס הנתונים
    long insert(User user); // מחזיר את ה-ID של המשתמש שנוסף

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email); // פעולה לחיפוש משתמש לפי דוא"ל

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password); // פעולה לבדוק אם קיימת התאמה בין דוא"ל וסיסמה עבור התחברות
}