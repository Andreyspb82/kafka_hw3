package ru.yandex.hw3_task2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.hw3_task2.server.PrometheusHttpServer;


import java.util.Collection;
import java.util.Map;

import static ru.yandex.hw3_task2.PrometheusSinkConnector.PROMETHEUS_LISTENER_PATH;
import static ru.yandex.hw3_task2.PrometheusSinkConnector.PROMETHEUS_LISTENER_PORT;


public class PrometheusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(PrometheusSinkTask.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private PrometheusHttpServer httpServer;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        String path = props.get(PROMETHEUS_LISTENER_PATH);
        int port = Integer.parseInt(props.get(PROMETHEUS_LISTENER_PORT));

        log.info("TASK Prometheus listener port: {}", port);
        log.info("TASK Prometheus listener path: {}", path);

        try {
            httpServer = PrometheusHttpServer.getInstance(path, port);
        } catch (Exception e) {
            throw new RuntimeException("Error starting PrometheusHttpServer", e);
        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            try {
                JsonNode root = MAPPER.readTree(record.value().toString());
                root.fields().forEachRemaining(entry -> {
                    JsonNode metric = entry.getValue();
                    String name = metric.get("Name").asText();
                    String type = metric.get("Type").asText();
                    String description = metric.get("Description").asText();
                    double value = metric.get("Value").asDouble();

                    String prometheusData = String.format(
                            "# HELP %s %s\n# TYPE %s %s\n%s %f\n",
                            name, description, name, type, name, value
                    );
                    log.info("Prometheus Data: {}", prometheusData);
                    httpServer.addMetric(name, prometheusData);
                });
            } catch (Exception e) {
                log.error("Failed to parse record", e);
            }
        }
    }

    @Override
    public void stop() {
    }
}
