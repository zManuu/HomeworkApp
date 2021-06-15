package de.zmanuu.homework.core;

public class HomeworkEntry {

    private String date;
    private String task;

    public HomeworkEntry(String date, String task) {
        this.date = date;
        this.task = task;
    }

    public String getDate() {
        return date;
    }
    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
