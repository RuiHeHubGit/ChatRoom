package com.herui.chatroom.common;

/**
 * Created by HeRui on 2018/11/3.
 */
public interface SessionListener<T> {
    void onOpen(TextMessageSession session);
    void onMessage(TextMessageSession session, T msg);
    void onError(Exception e);
    void onClone(TextMessageSession session);
}
