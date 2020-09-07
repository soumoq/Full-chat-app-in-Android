package com.arobit.chatall;

public class Message {
    private String date;
    private String message;
    private String name;
    private String time;
    private String dp;

    private Message() {
    }

    public Message(String date, String message, String name, String time, String dp) {
        this.date = date;
        this.message = message;
        this.name = name;
        this.time = time;
        this.dp = dp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }
}
