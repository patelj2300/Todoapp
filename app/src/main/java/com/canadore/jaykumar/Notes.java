package com.canadore.jaykumar;

public class Notes {
    private String key;
    private String note;


    public Notes() {
        // Empty constructor required for Firebase
    }


    public Notes(String key, String note) {
        this.key = key;
        this.note = note;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
