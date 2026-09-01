package com.lessonwalker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents an exercise within a lesson.
 * Exercises have a type (quiz, coding, etc.) and may have optional fields
 * like answer or hint depending on the type.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Exercise {

    private int id;
    private String type;
    private String question;
    private String answer;
    private String hint;

    public Exercise() {
    }

    public Exercise(int id, String type, String question) {
        this.id = id;
        this.type = type;
        this.question = question;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        return "Exercise{id=" + id + ", type='" + type + "', question='" + question + "'}";
    }
}
