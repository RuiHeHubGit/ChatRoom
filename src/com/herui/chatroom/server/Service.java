package com.herui.chatroom.server;

import com.herui.chatroom.session.Session;
import com.herui.chatroom.session.SessionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by HeRui on 2018/11/3.
 */
public class Service implements SessionListener<String> {
    private static int port = 9999;
    private static ServerSocket serverSocket;
    private static List<Session<String>> sessions;

    private Service(){}

    public static void start() {
        start(port);
    }

    private synchronized static void start(int port) {
        if (serverSocket != null) {
            return;
        }
        sessions = new Vector<>();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is started on port:"+port);
            while (true) {
                new Session<>(serverSocket.accept(), new Service());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
    }

    private static String getNowTimeString(String fmt) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fmt);
        return simpleDateFormat.format(new Date());
    }

    @Override
    public void onOpen(Session<String> session) {
        session.getProperty().put("username", session.readStringMsg());
        session.sendStringMsg(session.getId());
        sessions.add(session);
        System.out.println("client connected:"+session);
        for (Session s : sessions) {
            s.sendMsg("["+session.getProperty().get("username")+"] "+ getNowTimeString("MM-dd HH:mm:ss")+":加入了聊天室！");
        }
    }

    @Override
    public void onMessage(Session<String> session, String msg) {
        System.out.println("["+session.getProperty().get("username")+"] "+ getNowTimeString("yyyy-MM-dd HH:mm:ss")+":\n"+msg);
        for (Session s : sessions) {
            s.sendMsg("["+session.getProperty().get("username")+"] "+ getNowTimeString("MM-dd HH:mm:ss")+":\n"+msg);
        }
    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getLocalizedMessage());
    }

    @Override
    public void onClone(Session<String> session) {
        sessions.remove(session);
        System.out.println("client disconnect:"+session);
        for (Session s : sessions) {
            s.sendMsg("["+session.getProperty().get("username")+"] "+ getNowTimeString("MM-dd HH:mm:ss")+":离开了聊天室！");
        }
    }


}
