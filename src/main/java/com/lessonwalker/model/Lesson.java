package com.lessonwalker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete lesson with title, chapters, and exercises.
 * This is the root model object produced by XML parsing and serialized to JSON.
 */
public class Lesson {

    private String title;
    private List<Chapter> chapters;
    private List<Exercise> exercises;

    public Lesson() {
        this.chapters = new ArrayList<>();
        this.exercises = new ArrayList<>();
    }

    public Lesson(String title, List<Chapter> chapters, List<Exercise> exercises) {
        this.title = title;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @Override
    public String toString() {
        return "Lesson{title='" + title + "', chapters=" + chapters.size() + ", exercises=" + exercises.size() + "}";
    }
}
