package com.herui.chatroom.session;

import java.io.IOException;
import java.io.OutputStream;

public interface SessionEncode<T> {
    void init(Session<T> session, OutputStream out);
    void encode(T data) throws IOException;
}
