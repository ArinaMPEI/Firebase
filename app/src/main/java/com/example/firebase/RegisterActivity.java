package com.example.firebase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.mindrot.jbcrypt.BCrypt;


public class RegisterActivity extends AppCompatActivity {
    private EditText email_register;
    private EditText password_register;
    private Button btn_register;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        email_register = findViewById(R.id.email_register);
        password_register = findViewById(R.id.password_register);
        btn_register = findViewById(R.id.btn_register);
        // Зафиксировать экран в портретной ориентации
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_register.getText().toString();
                String password = password_register.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Хэшируем пароль перед сохранением
                    String hashedPassword = hashPassword(password);

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        ref.child("Users").child(mAuth.getCurrentUser().getUid())
                                                .child("email").setValue(email);
                                        ref.child("Users").child(mAuth.getCurrentUser().getUid())
                                                .child("password").setValue(hashedPassword);
                                        String userId = mAuth.getCurrentUser().getUid();
                                        // Добавляем нового пользователя в UserWords
                                        initializeUserWords(userId);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private String hashPassword(String password) {
        // Хэшируем пароль с использованием bcrypt
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    private void initializeUserWords(String userId) {
        DatabaseReference userWordsRef = FirebaseDatabase.getInstance().getReference("UserWords").child(userId);
        DatabaseReference wordsRef = FirebaseDatabase.getInstance().getReference("Words");

        // Очищаем старые данные для пользователя перед добавлением
        userWordsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Загружаем слова после успешного удаления старых данных
                wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                            String sectionId = sectionSnapshot.getKey();
                            for (DataSnapshot wordSnapshot : sectionSnapshot.getChildren()) {
                                String frenchWord = wordSnapshot.child("word").getValue(String.class);
                                // Проверяем, что французское слово не null
                                if (frenchWord != null) {
                                    // Используем французское слово как ключ для состояния изучения
                                    userWordsRef.child(sectionId).child(frenchWord).setValue(false);
                                }
                            }
                        }
                        Toast.makeText(RegisterActivity.this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Обработка ошибки
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Не удалось инициализировать слова", Toast.LENGTH_SHORT).show();
            }
        });
    }


}