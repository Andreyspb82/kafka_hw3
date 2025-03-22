# Kafka Самостоятельная работа №3
## Задание 1. Оптимизация параметров для повышения пропускной способности JDBC Source Connector

### 1) Запуск проекта и настрока коннектора:
#### Запускаем проект командой:

```
docker compose up -d
```

#### После запуска проекта, проверяме успешность запуска коннектора командой:
```
curl localhost:8083/connector-plugins | jq
```
#### Должен вернуться ответ следующего вида:
```
[
  {
    "class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "type": "sink",
    "version": "10.8.0"
  },
  {
    "class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
    "type": "sink",
    "version": "7.7.1-ccs"
  },
  {
    "class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "type": "source",
    "version": "10.8.0"
  },
  {
    "class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
    "type": "source",
    "version": "7.7.1-ccs"
  },
  {
    "class": "org.apache.kafka.connect.mirror.MirrorCheckpointConnector",
    "type": "source",
    "version": "7.7.1-ccs"
  },
  {
    "class": "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector",
    "type": "source",
    "version": "7.7.1-ccs"
  },
  {
    "class": "org.apache.kafka.connect.mirror.MirrorSourceConnector",
    "type": "source",
    "version": "7.7.1-ccs"
  }
]
```
#### Следующим шагом будет создание таблцы **users** в БД (делал с помощью DBeaver):
```
CREATE TABLE users (
id int PRIMARY KEY,
name varchar(255),
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
private_info VARCHAR 
); 
```
#### После того как таблица создана настраиваем коннектор JdbcSourceConnector, для этого в терминале вводим следующую команду:
```
curl -X PUT \
-H "Content-Type: application/json" \
--data '{
"connector.class":"io.confluent.connect.jdbc.JdbcSourceConnector",
"tasks.max":"1",
"connection.url":"jdbc:postgresql://postgres:5432/customers?user=postgres-user&password=postgres-pw&useSSL=false",
"connection.attempts":"5",
"connection.backoff.ms":"50000",
"mode":"timestamp",
"timestamp.column.name":"updated_at",
"topic.prefix":"postgresql-jdbc-bulk-",
"table.whitelist": "users",
"poll.interval.ms": "200",
"batch.max.rows": 100,
"producer.override.linger.ms": 1000,
"producer.override.batch.size": 500,
"transforms":"MaskField",
"transforms.MaskField.type":"org.apache.kafka.connect.transforms.MaskField$Value",
"transforms.MaskField.fields":"private_info",
"transforms.MaskField.replacement":"CENSORED"
}' \
http://localhost:8083/connectors/postgres-source/config | jq
```
#### Чтобы проверить успешность настройки коннекторв, в консоли вводим следующую команду:
```
curl http://localhost:8083/connectors/postgres-source/status | jq
```
#### Должен вернутся ответ слудующего вида:
```
{
  "name": "postgres-source",
  "connector": {
    "state": "RUNNING",
    "worker_id": "localhost:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "localhost:8083"
    }
  ],
  "type": "source"
}
```
#### Следующим шагом будет заполненеие таблицы тестовыми данным (9 000 000 значений):
```
INSERT INTO users (id, name, private_info)
SELECT
   i,
  'Name_' || i || '_' || substring('abcdefghijklmnopqrstuvwxyz', (random() * 26)::integer + 1, 1),
 'Private_info_' || i || '_' || substring('abcdefghijklmnopqrstuvwxyz', (random() * 26)::integer + 1, 1)
FROM
   generate_series(1, 9000000) AS i; 
```
### 2) Изменение параметров коннектора и результат:
#### С помощью графиков в Grafana измеряем параметры захвата и передачи данных из БД в топик kafka.
#### Перезапуская проект и изменяя следующие параметы коннектора:
* **batch.size**
* **linger.ms**
* **compression.type**
* **buffer.memory**
* **batch.max.rows**
* **poll.interval.ms**
#### Получил следующие значения параметра **Source Record Write Rate (K ops/sec)**:

|Эксперимент|batch.size|linger.ms|compression.type|buffer.memory|Source Record Write Rate (K ops/sec) (измерил)|Record Size Average (B) (измерил)|batch.max.rows|poll.interval.ms|
|-----------|----------|---------|----------------|-------------|----------------------------------------------|---------------------------------|--------------|----------------|
|1          |500       |1000     |none            | 33554432    |5,47                                          |528                              |100           |200             |
|2          |6000      |0        |snappy          | 33554432    |27,5                                          |528                              |10            |200             |
|3          |6000      |500      |snappy          | 33554432    |34,1                                          |528                              |10            |200             |
|4          |6000      |1000     |snappy          | 33554432    |58,5                                          |528                              |10            |200             |
|5          |60000     |1000     |snappy          | 33554432    |137,0                                         |528                              |100           |200             |
|6          |600000    |10000    |snappy          | 33554432    |160,0                                         |528                              |1000          |200             |
|7          |600000    |10000    |none            | 33554432    |161,0                                         |528                              |1000          |200             |
|8          |600000    |10000    |gzip            | 33554432    |136,0                                         |528                              |1000          |200             |
|9          |6000000   |10000    |snappy          | 33554432    |152,0                                         |528                              |10000         |200             |
#### В эксперементах 6 и 7 самое высокое значение считываемых записей.
