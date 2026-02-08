package com.pink.pfa.endpoints;

public class EchoRequest {
    private String text;

    public String getText () {
        return text;
    }

    public void setText (String text) {
        this.text = text;
    }
}
