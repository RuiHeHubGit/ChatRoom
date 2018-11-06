package com.herui.chatroom.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringSessionDecode implements SessionDecode<String>{
    private BufferedReader reader;

    @Override
    public void init(Session session, InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public String decode() throws IOException {
        return reader.readLine();
    }

    @Override
    public void destroy() {
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
            reader = null;
        }
    }
}
