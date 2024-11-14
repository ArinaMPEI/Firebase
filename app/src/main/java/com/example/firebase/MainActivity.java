package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button logout_btn;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewSections;
    private SectionAdapter sectionAdapter;
    private List<Section> sectionList;
    private Button adminPanelButton;
    private SectionViewModel sectionViewModel;
    private String ADMIN_UID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ADMIN_UID = getString(R.string.admin_uid);
        logout_btn = findViewById(R.id.logout_btn);
        adminPanelButton = findViewById(R.id.adminPanelButton);
        recyclerViewSections = findViewById(R.id.recyclerViewSections);
        mAuth = FirebaseAuth.getInstance();
        // Инициализация RecyclerView и адаптера
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(this));
        sectionList = new ArrayList<>();
        sectionAdapter = new SectionAdapter(sectionList, this);
        recyclerViewSections.setAdapter(sectionAdapter);

        // Получение ViewModel и добавление наблюдателя на LiveData
        sectionViewModel = new ViewModelProvider(this).get(SectionViewModel.class);
        sectionViewModel.getSectionListLiveData().observe(this, sections -> {
            if (sections != null) {
                sectionAdapter.updateSections(sections);
            }
        });
        // Кнопка выхода
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent (MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        // Проверка доступа администратора
        checkAdminAccess();
        // Переход в панель администратора
        adminPanelButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });
    }
    private void checkAdminAccess() {
        // Проверка UID текущего пользователя
        String currentUserUid = mAuth.getCurrentUser().getUid();
        if (ADMIN_UID.equals(currentUserUid)) {
            adminPanelButton.setVisibility(View.VISIBLE); // Делаем кнопку видимой
        }
    }

}