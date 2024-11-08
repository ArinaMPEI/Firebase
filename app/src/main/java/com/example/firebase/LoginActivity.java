package com.example.firebase;

import org.checkerframework.checker.nullness.qual.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email_login;
    private EditText password_login;
    private Button btn_login;
    private TextView register_txt;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
        btn_login = findViewById(R.id.btn_login);
        register_txt = findViewById(R.id.register_txt);

        mAuth = FirebaseAuth.getInstance();
        // Зафиксировать экран в портретной ориентации
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        register_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent (LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email_login.getText().toString().isEmpty() || password_login.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.signInWithEmailAndPassword(email_login.getText().toString(), password_login.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(LoginActivity.this, "You have some errors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
    });
}
}
