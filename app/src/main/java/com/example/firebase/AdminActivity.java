package com.example.firebase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private EditText editTextWord;
    private EditText editTextTranslation;
    private Button buttonAddWord;
    private Spinner sectionSpinner;
    private List<String> sectionIds;
    private String selectedSectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        editTextWord = findViewById(R.id.editTextWord);
        editTextTranslation = findViewById(R.id.editTextTranslation);
        buttonAddWord = findViewById(R.id.buttonAddWord);
        sectionSpinner = findViewById(R.id.sectionSpinner);
        // Восстановление состояния, если есть сохраненные данные
        if (savedInstanceState != null) {
            String word = savedInstanceState.getString("word", "");
            String translation = savedInstanceState.getString("translation", "");
            selectedSectionId = savedInstanceState.getString("selectedSectionId", null);

            editTextWord.setText(word);
            editTextTranslation.setText(translation);
        }
        loadSections();

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSectionId = sectionIds.get(position); // Устанавливаем выбранный sectionId
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSectionId = null;
            }
        });

        buttonAddWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = editTextWord.getText().toString().trim();
                String translation = editTextTranslation.getText().toString().trim();

                if (selectedSectionId == null) {
                    Toast.makeText(AdminActivity.this, "Пожалуйста, выберите раздел", Toast.LENGTH_SHORT).show();
                } else if (!word.isEmpty() && !translation.isEmpty()) {
                    addWord(selectedSectionId, word, translation);
                } else {
                    Toast.makeText(AdminActivity.this, "Пожалуйста, заполните оба поля", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("word", editTextWord.getText().toString());
        outState.putString("translation", editTextTranslation.getText().toString());
        outState.putString("selectedSectionId", selectedSectionId); // Сохраните также выбранный раздел
    }
    private void loadSections() {
        DatabaseReference sectionsRef = FirebaseDatabase.getInstance().getReference("Sections");
        sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionIds = new ArrayList<>();
                List<String> sectionNames = new ArrayList<>();

                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    String sectionId = sectionSnapshot.getKey();
                    String sectionName = sectionSnapshot.child("name").getValue(String.class);

                    if (sectionId != null && sectionName != null) {
                        sectionIds.add(sectionId);
                        sectionNames.add(sectionName);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminActivity.this,
                        android.R.layout.simple_spinner_item, sectionNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sectionSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Не удалось загрузить темы", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addWord(String sectionId, String word, String translation) {
        DatabaseReference wordsRef = FirebaseDatabase.getInstance().getReference("Words").child(sectionId);

        // Используем французское слово как ключ
        Map<String, Object> wordData = new HashMap<>();
        wordData.put("word", word);
        wordData.put("translation", translation);

        wordsRef.child(word).setValue(wordData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateUserWordsForNewWord(sectionId, word);
                Toast.makeText(AdminActivity.this, "Слово добавлено", Toast.LENGTH_SHORT).show();
                // Очищаем поля ввода после успешного добавления
                editTextWord.setText("");
                editTextTranslation.setText("");
            } else {
                Toast.makeText(AdminActivity.this, "Ошибка при добавлении слова", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserWordsForNewWord(String sectionId, String word) {
        DatabaseReference userWordsRef = FirebaseDatabase.getInstance().getReference("UserWords");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    userWordsRef.child(userId).child(sectionId).child(word).setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminActivity", "Ошибка при обновлении слов пользователя: " + error.getMessage());
            }
        });
    }
    public void onBackButtonClicked(View view) {
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Закрывает текущую AdminActivity
    }
}
