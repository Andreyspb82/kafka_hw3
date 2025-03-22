package ru.yandex.hw3_task2.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PrometheusHttpServer {

    private static final Logger log = LoggerFactory.getLogger(PrometheusHttpServer.class.getName());
    private static volatile PrometheusHttpServer instance;
    private final Map<String, Queue<String>> metrics = new ConcurrentHashMap<>();
    private Server server;

    private final int port;
    private final String path;

    public static PrometheusHttpServer getInstance(final String path, final int port) {
        if (instance == null) {
            synchronized (PrometheusHttpServer.class) {
                if (instance == null) {
                    instance = new PrometheusHttpServer(path, port);
                    instance.start();
                }
            }
        }
        return instance;
    }

    private PrometheusHttpServer(String path, int port) {
        this.port = port;
        this.path = path;
    }

    private void start() {
        var runnerThread = new Thread(() -> {
            server = new Server(port);
            ServerConnector serverConnector = new ServerConnector(server);
            server.addConnector(serverConnector);
            var handler = new MetricsServletHandler(new MetricsServlet(metrics), path);
            server.setHandler(handler);
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        runnerThread.setDaemon(false);
        runnerThread.start();
    }

    public void stop() throws Exception {
        if (server.isRunning()) {
            server.stop();
        }
    }

    public void addMetric(String name, String prometheusData) {
        log.info("Add metric '{}: {}'", name, prometheusData);
        var q = metrics.computeIfAbsent(name, k -> new ConcurrentLinkedQueue<>());
        q.add(prometheusData);
    }
}
