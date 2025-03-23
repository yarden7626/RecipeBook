package com.example.recipebook;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    // הגדרת משתנים גלובליים
    private EditText emailEditText, passwordEditText; // שדות קלט לאימייל וסיסמה
    private Button loginButton, signUpButton; // כפתורי כניסה והרשמה
    private AppDatabase database; // אובייקט גישה לבסיס הנתונים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // אתחול בסיס הנתונים
        database = AppDatabase.getInstance(this);

        // קישור המשתנים לאלמנטים בממשק המשתמש
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // כניסת משתמש
        loginButton.setOnClickListener(v -> loginUser());

        // הרשמת משתמש חדש
        signUpButton.setOnClickListener(v -> signUpUser());
    }

    // פונקציה לטיפול בהתחברות משתמש
    private void loginUser() {
        // קבלת ערכים משדות הקלט
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // בדיקת תקינות הקלט
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ביצוע בדיקת התחברות ב-Thread נפרד
        new Thread(() -> {
            // חיפוש משתמש בבסיס הנתונים
            User user = database.userDao().login(email, password);
            
            // עדכון הממשק בתוצאות הבדיקה
            runOnUiThread(() -> {
                if (user != null) {
                    // התחברות הצליחה - מעבר למסך הראשי
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish(); // סגירת מסך הכניסה
                } else {
                    // התחברות נכשלה - הצגת הודעת שגיאה
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    // פונקציה לטיפול בהרשמת משתמש חדש
    private void signUpUser() {
        // קבלת ערכים משדות הקלט
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // בדיקת תקינות הקלט
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ביצוע הרשמה ב-Thread נפרד
        new Thread(() -> {
            // בדיקה אם האימייל כבר קיים
            User existingUser = database.userDao().getUserByEmail(email);
            if (existingUser != null) {
                // האימייל כבר קיים - הצגת הודעת שגיאה
                runOnUiThread(() -> 
                    Toast.makeText(LoginActivity.this, "Email already exists", Toast.LENGTH_LONG).show()
                );
                return;
            }

            // יצירת משתמש חדש ושמירתו בבסיס הנתונים
            User newUser = new User(email, password);
            database.userDao().insert(newUser);
            
            // הרשמה הצליחה - מעבר למסך הראשי
            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish(); // סגירת מסך הכניסה
            });
        }).start();
    }
}