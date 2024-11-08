package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends AppCompatActivity {
    private TextView textViewWordIndex, textViewFrenchWord;
    private EditText editTextAnswer;
    private Button buttonSubmit, buttonStopStudy;
    private DatabaseReference wordsRef;
    private DatabaseReference userWordsRef;
    private String sectionId;
    private String userId;
    private List<Word> wordsToLearn;
    private int currentWordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        textViewWordIndex = findViewById(R.id.textViewWordIndex);
        textViewFrenchWord = findViewById(R.id.textViewFrenchWord);
        editTextAnswer = findViewById(R.id.editTextAnswer);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonStopStudy = findViewById(R.id.buttonStopStudy);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sectionId = getIntent().getStringExtra("section_id");
        wordsToLearn = new ArrayList<>();

        loadWords();

        buttonSubmit.setOnClickListener(v -> checkAnswer());
        buttonStopStudy.setOnClickListener(v -> finish());
    }

    private void loadWords() {
        wordsRef = FirebaseDatabase.getInstance().getReference("Words").child(sectionId);
        userWordsRef = FirebaseDatabase.getInstance().getReference("UserWords").child(userId).child(sectionId);

        wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int wordCount = (int) snapshot.getChildrenCount();
                final int[] loadedWords = {0};

                for (DataSnapshot wordSnapshot : snapshot.getChildren()) {
                    Word word = wordSnapshot.getValue(Word.class);
                    String wordId = wordSnapshot.getKey();

                    userWordsRef.child(wordId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userWordSnapshot) {
                            Boolean isLearned = userWordSnapshot.getValue(Boolean.class);

                            if (isLearned == null || !isLearned) {
                                wordsToLearn.add(word);
                            }

                            loadedWords[0]++;
                            if (loadedWords[0] == wordCount) {
                                displayCurrentWord();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(StudyActivity.this, "Ошибка загрузки прогресса", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudyActivity.this, "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCurrentWord() {
        if (currentWordIndex < wordsToLearn.size()) {
            Word currentWord = wordsToLearn.get(currentWordIndex);
            textViewFrenchWord.setText(currentWord.getWord());
            textViewWordIndex.setText("Слово " + (currentWordIndex + 1) + " из " + wordsToLearn.size());
        } else {
            Toast.makeText(this, "Вы прошли все слова в этом разделе!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkAnswer() {
        if (currentWordIndex >= wordsToLearn.size()) return;

        String userAnswer = editTextAnswer.getText().toString().trim();
        Word currentWord = wordsToLearn.get(currentWordIndex);
        String correctAnswer = currentWord.getTranslation();
        String wordId = wordsRef.child(currentWord.getWord()).getKey();

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            userWordsRef.child(wordId).setValue(true);
            Toast.makeText(this, "Правильно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неверно! Правильный ответ: " + correctAnswer, Toast.LENGTH_LONG).show();
            userWordsRef.child(wordId).setValue(false);
        }

        editTextAnswer.setText("");
        currentWordIndex++;
        displayCurrentWord();
    }
}