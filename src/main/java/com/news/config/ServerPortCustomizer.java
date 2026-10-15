package com.news.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Tự động phát hiện và xử lý xung đột cổng khi chạy cục bộ.
 * Nếu cổng 8080 mặc định đang bị ứng dụng khác (ví dụ: dịch vụ sao lưu MiniTool) chiếm dụng,
 * hệ thống sẽ tự động chuyển sang cổng 8081 để ứng dụng khởi động thành công mà không bị crash.
 * Trên máy không bị xung đột, ứng dụng vẫn chạy trên cổng 8080 như thiết kế.
 */
@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger logger = LoggerFactory.getLogger(ServerPortCustomizer.class);

    @Value("${server.port:8080}")
    private int configuredPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (isPortOccupied(configuredPort)) {
            int alternativePort = 8081;
            while (isPortOccupied(alternativePort) && alternativePort < 8095) {
                alternativePort++;
            }
            logger.warn("⚠️ Cổng {} đang bị ứng dụng khác chiếm dụng trên hệ điều hành! Tự động chuyển ứng dụng sang cổng: {}",
                    configuredPort, alternativePort);
            factory.setPort(alternativePort);
        }
    }

    private boolean isPortOccupied(int port) {
        if (port <= 0) return false;
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress("127.0.0.1", port));
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
