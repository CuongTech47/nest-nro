package com.ngocrong.ngocrong.network;

import com.ngocrong.ngocrong.server.Server;
import org.aspectj.bridge.IMessageHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Session implements ISession{

    public Socket socket;
    public DataInputStream dis;
    public DataOutputStream dos;
    public IMessageHandler messageHandler;
    private static final Lock lock = new ReentrantLock();
    private IService service;
    protected boolean isConnected, isLogin;
    private byte curR, curW;
    private final Sender sender;
    private Thread collectorThread;
    protected Thread sendThread;
    protected String version;
    protected byte zoomLevel;
    protected int width;
    protected int height;
    protected int device; // 0-PC, 1- APK, 2-IOS
    public User user;
    public Char _char;
    private boolean isSetClientInfo;
    public boolean isEnter = false;
    public String deviceInfo;
    public int id;
    public String ip;

    public Session(Socket socket , String ip, int id) throws IOException {
        this.socket = socket;
        this.id = id;
        this.ip = ip;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
        setHandler(new MessageHandler(this));
        messageHandler.onConnectOK();
        setService(new Service(this));
        sendThread = new Thread(sender = new Sender());
        collectorThread = new Thread(new MessageCollector());
        collectorThread.start();
        Server.ips.put(ip, Server.ips.getOrDefault(ip, 0) + 1);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void setHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void sendMessage(Message message) {
        sender.addMessage(message);
    }

    @Override
    public void setService(IService service) {
        this.service = service;
    }

    @Override
    public void close() {

    }

    @Override
    public void disconnect() {

    }


    ///

    public void setClientType(Message mss) throws IOException {
        if (!this.isSetClientInfo) {
            this.zoomLevel = mss.reader().readByte();
            this.width = mss.reader().readInt();
            this.height = mss.reader().readInt();
            device = mss.reader().readByte();
            version = mss.reader().readUTF();
            if (zoomLevel < 1 || zoomLevel > 4 || mss.reader().available() > 0) {
                disconnect();
                return;
            }
            if (!version.equals(Server.VERSION)) {
                ((Service) this.service).dialogMessage("Vui lòng tải phiên bản mới tại ngocrongonline.com");
                return;
            }
            this.isSetClientInfo = true;
            Service sv = (Service) this.service;
            sv.setResource();
            sv.sendResVersion();
        }
    }

    public void getImageSource(Message ms) {
        try {
            byte action = ms.reader().readByte();
            if (action == 1) {
                Service sv = (Service) service;
                String folder = "resources/data/" + zoomLevel;
                ArrayList<String> datas = new ArrayList<>();
                File file = new File(folder);
                addPath(datas, file);
                sv.size(datas.size());
                for (String path : datas) {
                    sv.download(path);
                }
                sv.downloadOk();
                sv.setLinkListServer();
            }
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }
    public static void addPath(ArrayList<String> paths, File file) {
        if (file.isFile()) {
            paths.add(file.getPath());
        } else {
            for (File f : file.listFiles()) {
                addPath(paths, f);
            }
        }
    }
}
