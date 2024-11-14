package com.example.firebase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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
    private AdminViewModel adminViewModel;
    private List<String> sectionIds;
    private String selectedSectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        editTextWord = findViewById(R.id.editTextWord);
        editTextTranslation = findViewById(R.id.editTextTranslation);
        buttonAddWord = findViewById(R.id.buttonAddWord);
        sectionSpinner = findViewById(R.id.sectionSpinner);

        // Подписываемся на обновления списка разделов
        adminViewModel.getSectionNames().observe(this, sectionNames -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sectionNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sectionSpinner.setAdapter(adapter);
        });

        // Сохраняем sectionIds при обновлении
        adminViewModel.getSectionIds().observe(this, ids -> sectionIds = ids);

        // Загрузка разделов при запуске
        adminViewModel.loadSections();

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSectionId = sectionIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSectionId = null;
            }
        });

        buttonAddWord.setOnClickListener(v -> {
            String word = editTextWord.getText().toString().trim();
            String translation = editTextTranslation.getText().toString().trim();

            if (selectedSectionId == null) {
                Toast.makeText(AdminActivity.this, "Пожалуйста, выберите раздел", Toast.LENGTH_SHORT).show();
            } else if (!word.isEmpty() && !translation.isEmpty()) {
                adminViewModel.addWord(selectedSectionId, word, translation);
            } else {
                Toast.makeText(AdminActivity.this, "Пожалуйста, заполните оба поля", Toast.LENGTH_SHORT).show();
            }
        });

        // Обрабатываем результат добавления слова
        adminViewModel.getWordAdded().observe(this, isAdded -> {
            if (isAdded != null && isAdded) {
                Toast.makeText(this, "Слово добавлено", Toast.LENGTH_SHORT).show();
                editTextWord.setText("");
                editTextTranslation.setText("");
            } else if (isAdded != null) {
                Toast.makeText(this, "Ошибка при добавлении слова", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onBackButtonClicked(View view) {
        // Действие при нажатии кнопки "Назад"
        finish(); // Закрывает текущую активность и возвращается к предыдущей
    }
}