package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class StudyActivity extends AppCompatActivity {
    private TextView textViewWordIndex, textViewFrenchWord;
    private EditText editTextAnswer;
    private Button buttonSubmit, buttonStopStudy;
    private List<Word> wordsToLearn;
    private int currentWordIndex = 0;
    private StudyViewModel studyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        textViewWordIndex = findViewById(R.id.textViewWordIndex);
        textViewFrenchWord = findViewById(R.id.textViewFrenchWord);
        editTextAnswer = findViewById(R.id.editTextAnswer);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonStopStudy = findViewById(R.id.buttonStopStudy);

        String sectionId = getIntent().getStringExtra("section_id");
        studyViewModel = new ViewModelProvider(this, new StudyViewModelFactory(getApplication(), sectionId))
                .get(StudyViewModel.class);

        // Наблюдаем за изменениями в списке слов
        studyViewModel.getWordsToLearnLiveData().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                wordsToLearn = words;
                if (wordsToLearn != null && !wordsToLearn.isEmpty()) {
                    displayCurrentWord();
                }
            }
        });

        buttonSubmit.setOnClickListener(v -> checkAnswer());
        buttonStopStudy.setOnClickListener(v -> finish());
    }

    private void displayCurrentWord() {
        if (wordsToLearn != null && !wordsToLearn.isEmpty() && currentWordIndex < wordsToLearn.size()) {
            Word currentWord = wordsToLearn.get(currentWordIndex);
            textViewFrenchWord.setText(currentWord.getWord());
            textViewWordIndex.setText("Слово " + (currentWordIndex + 1) + " из " + wordsToLearn.size());
        } else {
            Toast.makeText(this, "Вы прошли все слова в этом разделе!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkAnswer() {
        if (currentWordIndex >= wordsToLearn.size()) {
            // Поставьте условие, чтобы предотвратить выход за пределы
            Toast.makeText(this, "Вы прошли все слова в этом разделе!", Toast.LENGTH_SHORT).show();
            finish();
            return;  // Выход из метода
        }
        String userAnswer = editTextAnswer.getText().toString().trim();
        Word currentWord = wordsToLearn.get(currentWordIndex);
        String correctAnswer = currentWord.getTranslation();

        String wordId = studyViewModel.getWordId(currentWord.getWord());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("checkAnswer", "currentWordIndex: " + currentWordIndex);
        Log.d("checkAnswer", "wordId: " + wordId);
        Log.d("checkAnswer", "userId: " + userId);
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            studyViewModel.markWordAsLearned(wordId, userId, true);
            Toast.makeText(this, "Правильно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неверно! Правильный ответ: " + correctAnswer, Toast.LENGTH_LONG).show();
            studyViewModel.markWordAsNotLearned(wordId, userId, false);
        }

        editTextAnswer.setText("");
        currentWordIndex++;
        displayCurrentWord();
    }
}