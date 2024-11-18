package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

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
    Switch switcher;
    boolean nightMODE;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ShimmerTextView welcome_text;
    Shimmer shimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ADMIN_UID = getString(R.string.admin_uid);
        logout_btn = findViewById(R.id.logout_btn);
        adminPanelButton = findViewById(R.id.adminPanelButton);
        recyclerViewSections = findViewById(R.id.recyclerViewSections);
        mAuth = FirebaseAuth.getInstance();
        welcome_text = (ShimmerTextView)findViewById(R.id.welcome_text);
        shimmer = new Shimmer()
                .setDuration(1000)
                .setStartDelay(300)
                .setDirection(Shimmer.ANIMATION_DIRECTION_RTL);
        shimmer.start(welcome_text);
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

        switcher = findViewById(R.id.switcher);
        // мы использовали sharedPreferences для сохранения режима при выходе из приложения и повторном входе
        sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE);
        nightMODE = sharedPreferences.getBoolean("night", false);
        if (nightMODE) {
            switcher.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switcher.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if (nightMODE){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean( "night", false);

                }else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", true);

                }
                editor.apply();
            }
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