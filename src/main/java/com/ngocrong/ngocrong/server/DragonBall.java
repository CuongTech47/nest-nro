package com.ngocrong.ngocrong.server;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonBall {
    private static final Logger logger = LoggerFactory.getLogger(DragonBall.class);

    @Getter
    private static final DragonBall instance = new DragonBall();

    @Getter
    private Server server;

    private DragonBall() {
        // Khởi tạo server trong constructor
        server = new Server();
    }

    public void start() {
        try {
            logger.debug("Starting server...");
            addShutdownHook();
            server.init();
            System.gc(); // Gọi GC nếu cần thiết
            server.start();
            logger.info("Server started successfully.");
        } catch (Exception ex) {
            logger.error("Error while starting server", ex);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Shutting down server...");
            server.stop();
        }));
    }
}
