package ru.yandex.hw3_task2.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

public class MetricsServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MetricsServlet.class);

    private final Map<String, Queue<String>> buffer;

    public MetricsServlet(Map<String, Queue<String>> buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        buffer.values().forEach(q -> {
            var value = q.poll();
            if (value != null) {
                sb.append(value);
                sb.append("\n");
            }
        });
        var responseText = sb.toString();
        response.setContentType("text/plain");
        response.getWriter().write(responseText);
        log.info("Response: {}", responseText);
    }

}
