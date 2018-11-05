package com.herui.chatroom.client;

import com.herui.chatroom.common.SessionListener;
import com.herui.chatroom.common.TextMessageSession;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by HeRui on 2018/11/3.
 */
public class Client implements SessionListener {
    private String url = "127.0.0.1";
    private int port = 9999;
    private TextMessageSession session;

    public void start() {
        start(url, port);
    }

    public void start(String url, int port) {
        this.url = url;
        this.port = port;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        try {
            socket = new Socket(url, port);
            session = new TextMessageSession(socket, this);
            System.out.println("输入用户名：");
            String line = scanner.nextLine();
            session.getProperty().put("username", line);
            session.sendMessage(line);
            do {
                line = scanner.nextLine();
                session.sendMessage(line);
            } while (line != null);
        } catch (IOException e) {
            onError(e);
        } finally {
            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            scanner.close();
        }
    }

    @Override
    public void onOpen(TextMessageSession session) {
        session.setId(session.readLine());
        System.out.println("client connected,session:"+session);
    }

    @Override
    public void onMessage(TextMessageSession session, Object msg) {
        System.out.println(msg);
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getLocalizedMessage());
    }

    @Override
    public void onClone(TextMessageSession session) {
        System.out.println("client disconnect,session:"+session);
    }
}
