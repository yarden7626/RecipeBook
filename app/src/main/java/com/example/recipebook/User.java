package com.example.recipebook;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users") // מציין שמדובר בטבלה בשם "users" בבסיס הנתונים
public class User {

    @PrimaryKey(autoGenerate = true) // מציין כי ה-id יהיה המפתח הראשי
    private int id; // מזהה ייחודי של המשתמש

    private String email; // דוא"ל של המשתמש
    private String password; // סיסמת המשתמש

    // בונה כדי ליצור אובייקט משתמש עם דוא"ל וסיסמה
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // מקבל את ה-id של המשתמש
    public int getId() {
        return id;
    }

    // קובע את ה-id של המשתמש
    public void setId(int id) {
        this.id = id;
    }

    // מקבל את הדוא"ל של המשתמש
    public String getEmail() {
        return email;
    }

    // קובע את הדוא"ל של המשתמש
    public void setEmail(String email) {
        this.email = email;
    }

    // מקבל את הסיסמה של המשתמש
    public String getPassword() {
        return password;
    }

    // קובע את הסיסמה של המשתמש
    public void setPassword(String password) {
        this.password = password;
    }
}