package de.zmanuu.homework.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.gson.Gson;
import de.zmanuu.homework.MainActivity;
import de.zmanuu.homework.R;
import de.zmanuu.homework.util.KeyboardController;

import java.util.*;

public class Application {

    private HomeworkList homeworkList;
    private Gson gson;
    private int usedIDs;
    private MainActivity mainActivity;
    private SharedPreferences preferences;
    private Map<TextView, HomeworkEntry> entryMap;
    private TextView lastUsed;
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
            HomeworkEntry entry = new HomeworkEntry("23.03.2021", "Hausaufgabe");
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
            KeyboardController.hideKeyboard(mainActivity, mainActivity.findViewById(R.id.entry_info_div_aufgabe)); // close keyboard
            entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE));

            // make entries clickable
            for (int i=0; i<layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(true);
            }
        });

        // set action on back-button
        mainActivity.findViewById(R.id.back_btn).setOnClickListener(view -> {
            KeyboardController.hideKeyboard(mainActivity, mainActivity.findViewById(R.id.entry_info_div_aufgabe)); // close keyboard
            entryInfoViews.forEach(v -> v.setVisibility(View.INVISIBLE)); // hide info-container

            // make entry-buttons clickable
            LinearLayout layout = mainActivity.findViewById(R.id.main_layout_list);
            for (int i=0; i<layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(true);
            }
        });

        // set action on save-button
        mainActivity.findViewById(R.id.save_btn).setOnClickListener(view -> {
            String newName = (((EditText) mainActivity.findViewById(R.id.entry_info_div_aufgabe)).getText().toString());
            HomeworkEntry editedEntry = entryMap.get(lastUsed);
            editedEntry.setTask(newName);
            lastUsed.setText(editedEntry.getTask());
            String newDate = ((EditText) mainActivity.findViewById(R.id.entry_info_date)).getText().toString();
            editedEntry.setDate(newDate);
            saveEntries();
            KeyboardController.hideKeyboard(mainActivity, mainActivity.findViewById(R.id.entry_info_div_aufgabe)); // close keyboard
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

        LinearLayout entryDiv = new LinearLayout(mainActivity);
        entryDiv.setOrientation(LinearLayout.HORIZONTAL);
        entryDiv.setBackgroundResource(R.drawable.rounded_button);
        LinearLayout.LayoutParams entryDivLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 160);
        entryDivLayout.setMargins(0, 15, 0, 15);

        TextView textField = new TextView(mainActivity);
        textField.setText(entry.getTask());
        textField.setTextSize(15);
        textField.setId(usedIDs);
        textField.setTextColor(Color.WHITE);
        textField.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textFieldLayout = new LinearLayout.LayoutParams(880, LinearLayout.LayoutParams.MATCH_PARENT);
        entryDiv.addView(textField, textFieldLayout);

        ImageButton infoButton = new ImageButton(mainActivity);
        infoButton.setBackgroundResource(R.drawable.img_menu_edit);
        infoButton.setForegroundGravity(Gravity.CENTER);
        infoButton.setOnClickListener(view -> {

            // make entry buttons un-clickable
            for (int i = 0; i < layout.getChildCount(); i++) {
                View children = layout.getChildAt(i);
                children.setClickable(false);
            }

            // set texts of div to the texts of entry
            ((TextView) mainActivity.findViewById(R.id.entry_info_div_aufgabe)).setText(entry.getTask());
            ((TextView) mainActivity.findViewById(R.id.entry_info_date)).setText(entry.getDate());

            entryInfoViews.forEach(v -> v.setVisibility(View.VISIBLE)); // show div
            lastUsed = textField; // set the lastUsed var to the entry button that was clicked

        });
        LinearLayout.LayoutParams infoButtonLayout = new LinearLayout.LayoutParams(100, 100);
        infoButtonLayout.setMargins(0, 30, 0, 30);
        entryDiv.addView(infoButton, infoButtonLayout);

        Space marginRight = new Space(mainActivity);
        LinearLayout.LayoutParams marginRightLayout = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.MATCH_PARENT);
        entryDiv.addView(marginRight, marginRightLayout);

        layout.addView(entryDiv, entryDivLayout);

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
