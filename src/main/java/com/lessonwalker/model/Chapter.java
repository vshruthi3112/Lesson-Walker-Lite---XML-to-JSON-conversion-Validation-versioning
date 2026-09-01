package com.lessonwalker.model;

/**
 * Represents a single chapter within a lesson.
 * Contains an id, title, and content body.
 */
public class Chapter {

    private int id;
    private String title;
    private String content;

    public Chapter() {
    }

    public Chapter(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Chapter{id=" + id + ", title='" + title + "'}";
    }
}
