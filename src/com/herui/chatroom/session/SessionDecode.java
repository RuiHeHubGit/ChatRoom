package com.herui.chatroom.session;

import java.io.IOException;
import java.io.InputStream;

public interface SessionDecode<T> {
    void init(Session session, InputStream in);
    T decode() throws IOException;
    void destroy();
}
