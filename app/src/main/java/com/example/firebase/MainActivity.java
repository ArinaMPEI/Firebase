package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button logout_btn;
    private FirebaseAuth mAuth;
    private TextView textView;
    private String email;
    private DatabaseReference ref;
    private RecyclerView recyclerViewSections;
    private SectionAdapter sectionAdapter;
    private List<Section> sectionList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout_btn = findViewById(R.id.logout_btn);
        mAuth = FirebaseAuth.getInstance();
        recyclerViewSections = findViewById(R.id.recyclerViewSections);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(this));
        sectionList = new ArrayList<>();
        sectionAdapter = new SectionAdapter(sectionList, this);
        recyclerViewSections.setAdapter(sectionAdapter);
        loadSections();

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent (MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadSections() {
        ref = FirebaseDatabase.getInstance().getReference("Sections");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    Section section = sectionSnapshot.getValue(Section.class);
                    if (section != null) {
                        section.setId(sectionSnapshot.getKey()); // Устанавливаем ID
                        sectionList.add(section);
                        Log.d("MainActivity", "Section loaded: " + section.getName());
                    } else {
                        Log.d("MainActivity", "Error: section is null");
                    }
                }
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Не удалось подгузить темы", Toast.LENGTH_SHORT).show();
            }
        });
    }
}