package de.zmanuu.homework.core;

import java.util.ArrayList;
import java.util.List;

public class HomeworkList {

    private List<HomeworkEntry> entries;

    public void createList() {
        entries = new ArrayList<>();
    }
    public List<HomeworkEntry> getEntries() {
        return entries;
    }
}
