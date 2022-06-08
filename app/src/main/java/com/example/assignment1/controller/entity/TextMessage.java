package com.example.assignment1.controller.entity;

public class TextMessage {
    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    private String text;

    public String getAuthor() {
        return author;
    }

    private String author;
    private int type;
    public static int TEXT_MESSAGE_OUTBOUND = 1;
    public static int TEXT_MESSAGE_INBOUND =  2;
    public TextMessage(String text,String author, int type){
        this.text = text;
        this.author = author;
        this.type = type;
    }

}
