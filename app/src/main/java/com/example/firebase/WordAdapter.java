package com.example.firebase;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;
import java.util.Locale;

import android.widget.ImageButton;
import android.widget.TextView;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private List<Word> words;
    private Context context;
    TextToSpeech tts;
    public WordAdapter(List<Word> words, Context context) {
        this.words = words;
        this.context = context;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = words.get(position);
        holder.wordText.setText(word.getWord());
        holder.translationText.setText(word.getTranslation());

        // Воспроизведение аудио с помощью Text-to-Speech
        holder.audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i==TextToSpeech.SUCCESS){
                            tts.setLanguage(Locale.FRENCH);
                            tts.setSpeechRate(1.0f);
                            tts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText, translationText;
        ImageButton audioButton;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.word_text);
            translationText = itemView.findViewById(R.id.translation_text);
            audioButton = itemView.findViewById(R.id.audio_button);
        }
    }
}