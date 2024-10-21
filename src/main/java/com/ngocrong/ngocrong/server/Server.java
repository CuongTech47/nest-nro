package com.ngocrong.ngocrong.server;

import com.ngocrong.ngocrong.config.AppConfig;
import com.ngocrong.ngocrong.server.mysql.MySQLConnect;
import com.ngocrong.ngocrong.util.Utils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    public byte[][] backgroundVersion;
    public byte[][] smallVersion;
    public  int[] resVersion = new int[4];

    public static int COUNT_SESSION_ON_IP = 3;
    public static final String VERSION = "0.0.1";
    protected ServerSocket server;

    protected boolean start;
    protected int id;
    public boolean isMaintained;
    public static ConcurrentHashMap<String, Integer> ips = new ConcurrentHashMap<>();

    @Getter
    private final AppConfig appConfig;
    public Server() {
        appConfig = new AppConfig();
        appConfig.load();
    }

    public void init() {
        MySQLConnect.initialize(appConfig.getDbHost(), appConfig.getDbPort(), appConfig.getDbName(), appConfig.getDbUser(),
                appConfig.getDbPassword());
        initBGSmallVersion();
        initSmallVersion();
        initResVersion();
    }

    public void initBGSmallVersion() {
        try {
            backgroundVersion = new byte[4][];

            for (int i = 0; i < 4; i++) {
                File dir = new File("resources/image/" + (i + 1) + "/background/");
                File[] files = dir.listFiles();

                // Kiểm tra nếu không có file
                if (files == null || files.length == 0) {
                    logger.warn("No files found in directory: {}", dir.getPath());
                    continue;
                }

                // Sử dụng Stream để tìm maxId và kích thước file
                int maxId = -1;

                for (File f : files) {
                    int id = Integer.parseInt(f.getName().replace(".png", ""));
                    maxId = Math.max(maxId, id);
                }

                // Khởi tạo mảng với kích thước maxId + 1
                backgroundVersion[i] = new byte[maxId + 1];

                // Gán kích thước vào mảng
                for (File f : files) {
                    int id = Integer.parseInt(f.getName().replace(".png", ""));
                    // Sử dụng Files.size() để lấy kích thước file
                    byte sizeMod = (byte) (Files.size(f.toPath()) % 127);
                    backgroundVersion[i][id] = sizeMod;
                }
            }
        } catch (IOException e) {
            logger.error("IOException occurred while initializing background version", e);
        } catch (NumberFormatException e) {
            logger.warn("Error parsing file name to integer: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
        }
    }

    public void initSmallVersion() {
        try {
            smallVersion = new byte[4][];
            for (int i = 0; i < 4; i++) {
                File directory = new File("resources/image/" + (i + 1) + "/small/");
                File[] files = directory.listFiles();

                // Kiểm tra nếu không có file
                if (files == null || files.length == 0) {
                    logger.warn("No files found in directory: {}", directory.getPath());
                    continue;
                }

                // Tìm maxId và lưu kích thước file vào mảng
                OptionalInt maxIdOpt = Arrays.stream(files)
                        .mapToInt(file -> {
                            try {
                                String name = file.getName();
                                return extractId(name);
                            } catch (NumberFormatException e) {
                                logger.warn("Invalid file name format: {}", file.getName());
                                return -1; // Trả về -1 nếu có lỗi
                            }
                        })
                        .filter(id -> id >= 0) // Loại bỏ các ID không hợp lệ
                        .reduce(Integer::max);

                if (maxIdOpt.isPresent()) {
                    int maxId = maxIdOpt.getAsInt();
                    smallVersion[i] = new byte[maxId + 1];

                    int finalI = i;
                    Arrays.stream(files).forEach(file -> {
                        String name = file.getName();
                        try {
                            int id = extractId(name);
                            smallVersion[finalI][id] = (byte) (Files.size(file.toPath()) % 127);
                        } catch (IOException e) {
                            logger.error("Failed to read size for file: {}", file.getName(), e);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid file name format for size extraction: {}", file.getName());
                        }
                    });
                }
            }
        } catch (Exception ex) {
            logger.error("An error occurred while initializing small version", ex);
        }
    }

    public void initResVersion() {
        for (int i = 0; i < 4; i++) {
            File folder = new File("resources/data/" + (i + 1));

            if (!folder.exists() || !folder.isDirectory()) {
                logger.warn("Directory does not exist or is not a directory: {}", folder.getPath());
                resVersion[i] = 0; // Hoặc có thể gán giá trị mặc định khác
                continue; // Bỏ qua thư mục không hợp lệ
            }

            try {
                int ver = (int) Utils.getFolderSize(folder);
                resVersion[i] = ver;
            } catch (Exception e) {
                logger.error("Failed to get folder size for directory: {}", folder.getPath(), e);
                resVersion[i] = 0; // Gán giá trị mặc định trong trường hợp lỗi
            }
        }
    }




    // start server
    protected void start() {
        logger.debug("Start socket post=" + appConfig.getPort());
        try {
            server = new ServerSocket(appConfig.getPort());
            id = 0;
            start = true;
            Thread auto = new Thread(new AutoSaveData());
            auto.start();
//            BossManager.bornBoss();
//            MapManager mapManager = MapManager.getInstance();
//            mapManager.bornBroly();
//            mapManager.openBaseBabidi();
//            mapManager.openBlackDragonBall();
//            mapManager.openMartialArtsFestival();
//            mapManager.openMabuBossMap();
//            Thread threadMapManager = new Thread(mapManager);
//            threadMapManager.start();
            logger.debug("Start server Success!");
            while (start) {
                try {
                    Socket client = server.accept();
                    InetSocketAddress socketAddress = (InetSocketAddress) client.getRemoteSocketAddress();
                    String ip = socketAddress.getAddress().getHostAddress();
                    if (ip.equals("42.1.77.136") || ip.equals("117.5.33.234")) {
                        COUNT_SESSION_ON_IP = 100;
                    } else {
                        COUNT_SESSION_ON_IP = 3;
                    }
                    int currentSessionsForIp = ips.getOrDefault(ip, 0);
//                    if (currentSessionsForIp < COUNT_SESSION_ON_IP) {
//                        Session session = new Session(client, ip, ++id);
//                        SessionManager.addSession(session);
//                    } else {
//                        client.close();
//                    }
                } catch (IOException e) {
                    logger.error("failed!", e);
                }
            }
        } catch (Exception e) {
            logger.error("failed!", e);
        }
    }

    protected void stop() {
        if (start) {
            close();
            start = false;
        }
    }

    protected void close() {
        try {
            server.close();
            server = null;
//            Lucky.isRunning = false;
            MySQLConnect.close();
            System.gc();
            logger.debug("End socket");
        } catch (IOException e) {
            logger.error("failed!", e);
        }
    }



    // PRIVATE FUNC
    private int extractId(String name) {
        if (!name.contains("Small") || !name.endsWith(".png")) {
            throw new NumberFormatException("Invalid file name: " + name);
        }
        return Integer.parseInt(name.replace("Small", "").replace(".png", ""));
    }
}
