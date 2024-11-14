package com.example.firebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
    private List<Section> sections;
    private Context context;

    public SectionAdapter(List<Section> sections, Context context) {
        this.sections = sections;
        this.context = context;
    }
    //onCreateViewHolder: возвращает объект ViewHolder, хранящийся данные по одному объекту Section.
    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }
    //onBindViewHolder: выполняет привязку объекта ViewHolder к объекту Section по определенной позиции.
    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sections.get(position);
        holder.sectionName.setText(section.getName());
        // Устанавливаем обработчик клика для элемента списка
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WordListActivity.class);
                intent.putExtra("section_id", section.getId());
                context.startActivity(intent);
            }
        });
    }
    // Метод для обновления списка
    public void updateSections(List<Section> newSections) {
        sections.clear();
        sections.addAll(newSections);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return sections.size(); //возвращает количество объектов в списке
    }
    //Для хранения данных в классе адаптера определен статический класс SectionViewHolder,
    // который использует определенные в item_section.xml элементы управления.
    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionName;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.section_name);
        }
    }
}