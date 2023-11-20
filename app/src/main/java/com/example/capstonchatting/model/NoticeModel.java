// NoticeModel.java
package com.example.capstonchatting.model;

public class NoticeModel {
    private String notice;

    public NoticeModel() {
        // 기본 생성자가 필요합니다.
    }

    public NoticeModel(String notice) {
        this.notice = notice;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
