package com.example.capstonchatting.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by myeongsic on 2017. 9. 25..
 */

public class ChatModel {


    public String roomId;
    public Map<String,Boolean> users = new HashMap<>(); //채팅방의 유저들
    public Map<String,Comment> comments = new HashMap<>();//채팅방의 대화내용


    public static class Comment {

        public String uid;
        public String message;
        public Object timestamp;

        public Map<String,Object> readUsers = new HashMap<>();
    }

}


