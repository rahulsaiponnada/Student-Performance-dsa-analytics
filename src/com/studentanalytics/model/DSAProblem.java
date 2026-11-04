package com.studentanalytics.model;

public class DSAProblem {
    private int problemId;
    private String title;
    private String topic;
    private String difficulty;
    private boolean solved;

    public DSAProblem(int problemId, String title, String topic,
                      String difficulty, boolean solved) {
        this.problemId = problemId;
        this.title = title;
        this.topic = topic;
        this.difficulty = difficulty;
        this.solved = solved;
    }

    public int getProblemId() { return problemId; }
    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public boolean isSolved() { return solved; }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }
}