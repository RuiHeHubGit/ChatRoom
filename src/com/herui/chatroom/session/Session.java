package com.herui.chatroom.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Session<T> {
    private String id;
    private HashMap property;
    private Socket socket;
    private SessionListener<T> listener;
    private Date createTime;
    private volatile boolean closed;
    private InputStream in;
    private OutputStream out;
    private SessionEncode<String> stringEncode;
    private SessionDecode<String> stringDecode;
    private SessionEncode<T> encode;
    private SessionDecode<T> decode;

    public Session(Socket socket, SessionListener<T> listener) {
        if(socket == null) {
            throw new IllegalArgumentException("must set Socket");
        }
        if(listener == null) {
            throw new IllegalArgumentException("must set SessionListener");
        }

        this.socket = socket;
        this.listener = listener;

        init();
    }

    private void init() {
        this.id = UUID.randomUUID().toString();
        this.property = new HashMap();
        this.createTime = new Date();
        this.stringEncode = new StringSessionEncode();
        this.stringDecode = new StringSessionDecode();
        this.closed = true;
    }

    public synchronized void open() {
        if(closed) {
            new Thread(()->{
                try {
                    if (this.encode == null) {
                        this.encode = (SessionEncode<T>) stringEncode;
                    }
                    if (this.decode == null) {
                        this.decode = (SessionDecode<T>) stringDecode;
                    }
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    this.encode.init(Session.this, out);
                    this.decode.init(Session.this, in);
                    listener.onOpen(this);
                    closed = false;
                    T data;
                    while (!closed && (data = decode.decode()) != null) {
                        listener.onMessage(Session.this, data);
                    }
                } catch (IOException e) {
                    listener.onError(e);
                } finally {
                    closed = true;
                    listener.onClone(Session.this);

                    if(in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    }

                    if(out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    }

                    try {
                        Session.this.socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Session.this.socket = null;
                }
            }).start();
        }
    }

    public synchronized void close() {
        if(!closed && socket != null) {
            closed = true;
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                listener.onError(e);
            }
        }
    }

    public T readMsg() {
        try {
            return decode.decode();
        } catch (IOException e) {
            listener.onError(e);
        }
        return null;
    }

    public void sendMsg(T data) {
        try {
            encode.encode(data);
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

    public HashMap getProperty() {
        return property;
    }

    public SessionListener getListener() {
        return listener;
    }

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", property=" + property +
                ", createTime=" + createTime +
                '}';
    }
}
