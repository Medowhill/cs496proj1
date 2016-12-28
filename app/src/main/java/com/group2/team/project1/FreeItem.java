package com.group2.team.project1;

// item for C tab (memo)
public class FreeItem {

    private long date;
    private String content, address;
    private boolean photo;

    public FreeItem(long date, String content, String address, boolean photo) {
        this.date = date;
        this.content = content;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
