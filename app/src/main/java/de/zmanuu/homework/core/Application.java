package de.zmanuu.homework.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.gson.Gson;
import de.zmanuu.homework.MainActivity;
import de.zmanuu.homework.R;

import java.util.*;

public class Application {

    private HomeworkList homeworkList;
    private Gson gson;
    private int usedIDs;
    private MainActivity mainActivity;
    private SharedPreferences preferences;
    private Map<Button, HomeworkEntry> entryMap;
    private Button lastUsed;
    private List<View> entryInfoViews;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Application() {
        try {
            startApp();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startApp() {
        mainActivity = MainActivity.getInstance();
        gson = new Gson();
        usedIDs = 50;
        entryMap = new HashMap<>();
        preferences = mainActivity.getSharedPreferences("HOMEWORK_DATA", Context.MODE_PRIVATE);
        entryInfoViews = Arrays.asList(new View[]{
                mainActivity.findViewById(R.id.entry_info_div),
                mainActivity.findViewById(R.id.entry_info_background)
        });
        entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE));

        // load example / default entries, if none exists
        if (!preferences.contains("ENTRIES")) {
            Log.e("HOMEWORK", "TEST");
            loadDefaultEntries();
        }

        // load entries from preferences
        homeworkList = gson.fromJson(preferences.getString("ENTRIES", "NULL"), HomeworkList.class);
        homeworkList.getEntries().forEach(this::addEntry);

        // set action on add-entry-button
        mainActivity.findViewById(R.id.add_entry_btn).setOnClickListener(view -> {
            HomeworkEntry entry = new HomeworkEntry("23.03.2021", "Test + '" + usedIDs + "'");
            homeworkList.getEntries().add(entry);
            addEntry(entry);
            ScrollView s = mainActivity.findViewById(R.id.main_layout_scroll);
            s.smoothScrollTo(0, mainActivity.findViewById(R.id.main_layout_list).getBottom());
        });

        // set action on delete-entry-button
        mainActivity.findViewById(R.id.delete_btn).setOnClickListener(view -> {
            HomeworkEntry entry = entryMap.get(lastUsed);
            LinearLayout layout = mainActivity.findViewById(R.id.main_layout_list);
            homeworkList.getEntries().remove(entry);
            entryMap.remove(lastUsed);
            layout.removeView(lastUsed);
            saveEntries();
            entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE));

            // make entries clickable
            for (int i=0; i<layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(true);
            }
        });

        // set action on back-button
        mainActivity.findViewById(R.id.back_btn).setOnClickListener(view -> {
            entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE)); // hide info-container

            // make entry-buttons clickable
            LinearLayout layout = mainActivity.findViewById(R.id.main_layout_list);
            for (int i=0; i<layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(true);
            }
        });

        // set action on apply-name-button
        mainActivity.findViewById(R.id.apply_name_btn).setOnClickListener(view -> {
            String newName = (((EditText) mainActivity.findViewById(R.id.entry_info_div_aufgabe)).getText().toString());
            HomeworkEntry editedEntry = entryMap.get(lastUsed);
            editedEntry.setTask(newName);
            lastUsed.setText(editedEntry.getDate() + " | " + editedEntry.getTask());
            saveEntries();
        });

        // set action on exit-yes-button
        mainActivity.findViewById(R.id.exit_yes).setOnClickListener(view -> {
            saveEntries();
            mainActivity.finish();
        });

        // set action on exit-no-button
        mainActivity.findViewById(R.id.exit_no).setOnClickListener(view -> {
            mainActivity.findViewById(R.id.exit_confirmation_div).setVisibility(View.INVISIBLE);
            mainActivity.findViewById(R.id.entry_info_background).setVisibility(View.INVISIBLE);
        });

    }

    private void loadDefaultEntries() {
        HomeworkList h = new HomeworkList();
        h.createList();
        for (int i=1; i<15; i++) {
            h.getEntries().add(new HomeworkEntry("20.03.2021:19:10", "Aufgabe '" + i + "'"));
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ENTRIES", gson.toJson(h, HomeworkList.class));
        editor.apply();
    }

    private void addEntry(@NonNull final HomeworkEntry entry) {
        LinearLayout layout = mainActivity.findViewById(R.id.main_layout_list);

        Button textField = new Button(mainActivity);
        textField.setText(entry.getDate() + " | " + entry.getTask());
        textField.setTextSize(15);
        textField.setBackground(mainActivity.getResources().getDrawable(R.drawable.rounded_button));
        textField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textField.setId(usedIDs);
        textField.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160);
        layoutParams.setMargins(0, 30, 0, 30);
        /*if (usedIDs == 50) { // the compared integer has to be the default (first) value of 'usedIDs'
            layoutParams.setMargins(0, 250, 0, 30);
        }*/
        textField.setOnClickListener(view -> {

            // make entry buttons un-clickable
            for (int i = 0; i < layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(false);
            }

            // set texts of div to the texts of entry
            ((TextView) mainActivity.findViewById(R.id.entry_info_div_aufgabe)).setText(entry.getTask());

            entryInfoViews.forEach(v -> v.setVisibility(View.VISIBLE)); // show div
            lastUsed = textField; // set the lastUsed var to the entry button that was clicked

        });
        layout.addView(textField, layoutParams);

        usedIDs++;

        saveEntries();

        entryMap.put(textField, entry);
    }

    private void saveEntries() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ENTRIES", gson.toJson(homeworkList, HomeworkList.class));
        editor.apply();
    }

    public void requestBackClick() {
        if (entryInfoViews.get(0).getVisibility() == View.VISIBLE) {
            entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE)); // hide info-container

            // make entry-buttons clickable
            LinearLayout layout = mainActivity.findViewById(R.id.main_layout_list);
            for (int i=0; i<layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(true);
            }
        } else {
            showExitConfirmationUI();
        }
    }

    public void showExitConfirmationUI() {
        mainActivity.findViewById(R.id.exit_confirmation_div).setVisibility(View.VISIBLE);
        mainActivity.findViewById(R.id.entry_info_background).setVisibility(View.VISIBLE);
    }

}
