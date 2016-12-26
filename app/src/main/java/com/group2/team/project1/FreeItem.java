package com.group2.team.project1;

/**
 * Created by q on 2016-12-26.
 */

public class FreeItem {

    private long date;
    private String content;
    private boolean photo;

    FreeItem(long date, String content, boolean photo) {
        this.date = date;
        this.content = content;
        this.photo = photo;
    }

    public long getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPhoto() {
        return photo;
    }

    public void setPhoto(boolean photo) {
        this.photo = photo;
    }
}
