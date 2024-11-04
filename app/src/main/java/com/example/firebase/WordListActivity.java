package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        recyclerViewWords = findViewById(R.id.recyclerViewWords);
        recyclerViewWords.setLayoutManager(new LinearLayoutManager(this));
        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(wordList, this);
        recyclerViewWords.setAdapter(wordAdapter);

        // Получаем ID раздела, переданный из MainActivity
        sectionId = getIntent().getStringExtra("section_id");

        loadWords();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WordListActivity.this, "Не удалось загрузить слова", Toast.LENGTH_SHORT).show();
            }
        });
    }
}