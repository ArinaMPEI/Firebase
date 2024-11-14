package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class WordListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewWords;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private DatabaseReference ref;
    private String sectionId;
    private TextView progressTextView;
    private Button buttonStartStudy;
    private int progress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        recyclerViewWords = findViewById(R.id.recyclerViewWords);
        recyclerViewWords.setLayoutManager(new LinearLayoutManager(this));
        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(wordList, this);
        recyclerViewWords.setAdapter(wordAdapter);
        progressTextView = findViewById(R.id.progressTextView);
        buttonStartStudy = findViewById(R.id.buttonStartStudy);

        sectionId = getIntent().getStringExtra("section_id");

        loadWords();

        buttonStartStudy.setOnClickListener(v -> {
            if (progress == 100) {
                // Если все слова выучены, показываем Toast и не переходим в StudyActivity
                Toast.makeText(WordListActivity.this, "Поздравляем, все слова выучены!", Toast.LENGTH_LONG).show();
            } else {
                // Если не все слова выучены, продолжаем переход в StudyActivity
                Intent intent = new Intent(WordListActivity.this, StudyActivity.class);
                intent.putExtra("section_id", sectionId);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        calculateAndDisplayProgress(); // Обновляем прогресс при возврате к активности
    }
    private void calculateAndDisplayProgress() {
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance()
                .getReference("UserWords")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(sectionId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int learnedCount = 0;
                int totalCount = (int) snapshot.getChildrenCount();

                for (DataSnapshot wordSnapshot : snapshot.getChildren()) {
                    Boolean isLearned = wordSnapshot.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isLearned)) {
                        learnedCount++;
                    }
                }

                progress = totalCount > 0 ? (learnedCount * 100 / totalCount) : 0;
                progressTextView.setText(progress + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WordListActivity.this, "Не удалось загрузить прогресс", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWords() {
        ref = FirebaseDatabase.getInstance().getReference("Words").child(sectionId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wordList.clear();
                for (DataSnapshot wordSnapshot : snapshot.getChildren()) {
                    Word word = wordSnapshot.getValue(Word.class);
                    wordList.add(word);
                }
                wordAdapter.notifyDataSetChanged();
                calculateAndDisplayProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WordListActivity.this, "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
            }
        });
    }
}