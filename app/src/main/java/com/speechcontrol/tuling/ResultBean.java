package com.speechcontrol.tuling;

/**
 * Created by smartOrange_4 on 2017/10/10.
 */

public class ResultBean {
    private int code;
    private String text;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "code=" + code +
                ", text='" + text + '\'' +
                '}';
    }
}
