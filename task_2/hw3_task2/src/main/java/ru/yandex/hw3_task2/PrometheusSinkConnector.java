package ru.yandex.hw3_task2;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PrometheusSinkConnector extends SinkConnector {

    private static final Logger log = LoggerFactory.getLogger(PrometheusSinkConnector.class);

    public static final String PROMETHEUS_LISTENER_PATH = "prometheus.listener.path";
    public static final String PROMETHEUS_LISTENER_PORT = "prometheus.listener.port";

    private Map<String, String> props;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        this.props = props;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return PrometheusSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {

        log.info("maxTasks: {}", maxTasks);
        log.info("Prometheus metrics path: {}", props.get(PROMETHEUS_LISTENER_PATH));
        log.info("Prometheus metrics port: {}", props.get(PROMETHEUS_LISTENER_PORT));

        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            configs.add(Map.of(
                    PROMETHEUS_LISTENER_PATH, props.get(PROMETHEUS_LISTENER_PATH),
                    PROMETHEUS_LISTENER_PORT, props.get(PROMETHEUS_LISTENER_PORT)
            ));
        }
        return configs;
    }

    @Override
    public void stop() {
        // Завершение работы, если требуется
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

}
