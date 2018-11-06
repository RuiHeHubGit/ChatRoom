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
        this(socket, listener, null, null);
    }

    public Session(Socket socket, SessionListener<T> listener, SessionEncode<T> encode, SessionDecode<T> decode) {
        if(socket == null) {
            throw new IllegalArgumentException("must set Socket");
        }
        if(listener == null) {
            throw new IllegalArgumentException("must set SessionListener");
        }

        this.socket = socket;
        this.listener = listener;
        this.encode = encode;
        this.decode = decode;

        init();
    }

    private void init() {
        this.id = UUID.randomUUID().toString();
        this.property = new HashMap();
        this.createTime = new Date();
        this.stringEncode = new StringSessionEncode();
        this.stringDecode = new StringSessionDecode();
        this.closed = true;
        open();
    }

    private void open() {
        if(closed) {
            new Thread(()->{
                try {
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    this.stringDecode.init(Session.this, in);
                    this.stringEncode.init(Session.this, out);

                    if (this.encode == null) {
                        this.encode = (SessionEncode<T>) stringEncode;
                    } else {
                        this.decode.init(Session.this, in);
                    }
                    if (this.decode == null) {
                        this.decode = (SessionDecode<T>) stringDecode;
                    } else {
                        this.encode.init(Session.this, out);
                    }

                    listener.onOpen(this);
                    closed = false;
                    T data;
                    while (!closed && (data = decode.decode()) != null) {
                        listener.onMessage(Session.this, data);
                    }
                } catch (IOException e) {
                    listener.onError(e);
                } finally {
                    close();
                }
            }).start();
        }
    }

    public void close() {
        if(!closed && socket != null) {
            closed = true;
            listener.onClone(Session.this);

            this.encode.destroy();
            this.decode.destroy();
            this.stringEncode.destroy();
            this.stringDecode.destroy();

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

    public String readStringMsg() {
        try {
            return stringDecode.decode();
        } catch (IOException e) {
            listener.onError(e);
        }
        return null;
    }

    public void sendStringMsg(String data) {
        try {
            stringEncode.encode(data);
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

    public SessionEncode<T> getEncode() {
        return encode;
    }

    public SessionDecode<T> getDecode() {
        return decode;
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
