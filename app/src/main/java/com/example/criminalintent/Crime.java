package com.example.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {

    private UUID id;
    private String title;
    private Date date;
    private boolean solved;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        this.id = id;
        this.date = new Date();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }
}
