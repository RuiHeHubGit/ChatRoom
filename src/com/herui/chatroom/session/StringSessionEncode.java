package com.herui.chatroom.session;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class StringSessionEncode implements SessionEncode<String>{
    private BufferedWriter writer;

    @Override
    public void init(Session session, OutputStream out) {
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    @Override
    public void encode(String data) throws IOException {
        writer.write(data);
        writer.newLine();
        writer.flush();
    }

    @Override
    public void destroy() {
        if(writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
            writer = null;
        }
    }
}
