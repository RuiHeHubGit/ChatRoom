package com.herui.chatroom.common;

import javax.management.OperationsException;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by HeRui on 2018/11/3.
 */
public class TextMessageSession {
    private String id;
    private HashMap property;
    private Socket socket;
    private SessionListener listener;
    private Date createTime;
    private volatile boolean closed;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    public TextMessageSession(Socket socket, SessionListener listener) {
        if(socket == null) {
            throw new IllegalArgumentException("must set Socket");
        }
        if(listener == null) {
            throw new IllegalArgumentException("must set SessionListener");
        }
        this.socket = socket;
        this.id = UUID.randomUUID().toString();
        this.property = new HashMap();
        this.listener = listener;
        this.createTime = new Date();

        new Thread(()->{
            try {
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                listener.onOpen(this);
                String line = null;
                while (!closed && (line=reader.readLine()) != null) {
                    listener.onMessage(TextMessageSession.this, line);
                }
            } catch (IOException e) {
                listener.onError(e);
            } finally {
                closed = true;
                listener.onClone(TextMessageSession.this);

                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                writer = null;

                try {
                    reader.close();
                    reader = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                reader = null;

                try {
                    TextMessageSession.this.socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TextMessageSession.this.socket = null;
            }
        }).start();
    }

    public void close() {
        if(socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                listener.onError(e);
            }
        }
        closed = true;
    }

    public void sendMessage(String msg) {
        if(writer == null) {
            listener.onError(new OperationsException("TextMessageSession is closed"));
            return;
        }
        try {
            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            listener.onError(e);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public HashMap getProperty() {
        return property;
    }

    public boolean isClosed() {
        return closed;
    }

    public String readLine() {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            listener.onError(e);
        }
        return line;
    }

    @Override
    public String toString() {
        return "TextMessageSession{" +
                "id='" + id + '\'' +
                ", property=" + property +
                ", createTime=" + createTime +
                '}';
    }
}
