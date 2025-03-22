# Kafka Самостоятельная работа №3
## Задание 3. Получение лога вывода Depezium PostgrsConnector

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
    "class": "io.debezium.connector.postgresql.PostgresConnector",
    "type": "source",
    "version": "3.0.8.Final"
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
#### Следующим шагом будет создание таблцы **users** в БД и заполнение её тестовыми данными (делал с помощью DBeaver):
```
CREATE TABLE users (
 id int PRIMARY KEY,
 name varchar(255),
 private_info VARCHAR  
);
```
```
INSERT INTO users (id, name, private_info)
VALUES (1, 'Alex', 'Alex@email.com'),
        (2, 'Dian', 'Dian@email.com'),
        (3, 'Xenia', 'Xenia@email.com'); 
```
#### После того как таблица создана настраиваем коннектор "io.debezium.connector.postgresql.PostgresConnector", для этого в терминале вводим следующую команду (параметры считываются из файла *"connector-truncate.json"*:
```
 curl -X PUT -H 'Content-Type: application/json' --data @connector-truncate.json http://localhost:8083/connectors/pg-connector/config | jq
```
#### Коннектор выполняет снимок таблицв *"Users"* и отправляет данные в топик *"customers.public.users"*

#### Статус коннектора можно проверить следующей командой:
```
curl http://localhost:8083/connectors/pg-connector/status | jq
```
#### Должен вернутся ответ слудующего вида:
```
{
  "name": "pg-connector",
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
### 2) Результат задания:
#### Логи при создании коннектора:
```
[2025-03-22 18:00:42,248] INFO Creating connector pg-connector of type io.debezium.connector.postgresql.PostgresConnector (org.apache.kafka.connect.runtime.Worker)
[2025-03-22 18:00:42,248] INFO SourceConnectorConfig values: 
config.action.reload = restart
connector.class = io.debezium.connector.postgresql.PostgresConnector
errors.log.enable = false
errors.log.include.messages = false
errors.retry.delay.max.ms = 60000
errors.retry.timeout = 0
errors.tolerance = none
exactly.once.support = requested
header.converter = null
key.converter = null
name = pg-connector
offsets.storage.topic = null
predicates = []
tasks.max = 1
topic.creation.groups = []
transaction.boundary = poll
transaction.boundary.interval.ms = null
transforms = [unwrap]
value.converter = null
 (org.apache.kafka.connect.runtime.SourceConnectorConfig)
[2025-03-22 18:00:42,250] INFO EnrichedConnectorConfig values: 
config.action.reload = restart
connector.class = io.debezium.connector.postgresql.PostgresConnector
errors.log.enable = false
errors.log.include.messages = false
errors.retry.delay.max.ms = 60000
errors.retry.timeout = 0
errors.tolerance = none
exactly.once.support = requested
header.converter = null
key.converter = null
name = pg-connector
offsets.storage.topic = null
predicates = []
tasks.max = 1
topic.creation.groups = []
transaction.boundary = poll
transaction.boundary.interval.ms = null
transforms = [unwrap]
transforms.unwrap.add.fields = []
transforms.unwrap.add.fields.prefix = __
transforms.unwrap.add.headers = []
transforms.unwrap.add.headers.prefix = __
transforms.unwrap.delete.handling.mode = rewrite
transforms.unwrap.drop.fields.from.key = false
transforms.unwrap.drop.fields.header.name = null
transforms.unwrap.drop.fields.keep.schema.compatible = true
transforms.unwrap.drop.tombstones = false
transforms.unwrap.negate = false
transforms.unwrap.predicate = null
transforms.unwrap.route.by.field = 
transforms.unwrap.type = class io.debezium.transforms.ExtractNewRecordState
value.converter = null
 (org.apache.kafka.connect.runtime.ConnectorConfig$EnrichedConnectorConfig)
[2025-03-22 18:00:42,253] INFO EnrichedSourceConnectorConfig values: 
config.action.reload = restart
connector.class = io.debezium.connector.postgresql.PostgresConnector
errors.log.enable = false
errors.log.include.messages = false
errors.retry.delay.max.ms = 60000
errors.retry.timeout = 0
errors.tolerance = none
exactly.once.support = requested
header.converter = null
key.converter = null
name = pg-connector
offsets.storage.topic = null
predicates = []
tasks.max = 1
topic.creation.default.exclude = []
topic.creation.default.include = [.*]
topic.creation.default.partitions = -1
topic.creation.default.replication.factor = -1
topic.creation.groups = []
transaction.boundary = poll
transaction.boundary.interval.ms = null
transforms = [unwrap]
value.converter = null
 (org.apache.kafka.connect.runtime.SourceConnectorConfig$EnrichedSourceConnectorConfig)
[2025-03-22 18:00:42,254] INFO EnrichedConnectorConfig values: 
config.action.reload = restart
connector.class = io.debezium.connector.postgresql.PostgresConnector
errors.log.enable = false
errors.log.include.messages = false
errors.retry.delay.max.ms = 60000
errors.retry.timeout = 0
errors.tolerance = none
exactly.once.support = requested
header.converter = null
key.converter = null
name = pg-connector
offsets.storage.topic = null
predicates = []
tasks.max = 1
topic.creation.default.exclude = []
topic.creation.default.include = [.*]
topic.creation.default.partitions = -1
topic.creation.default.replication.factor = -1
topic.creation.groups = []
transaction.boundary = poll
transaction.boundary.interval.ms = null
transforms = [unwrap]
transforms.unwrap.add.fields = []
transforms.unwrap.add.fields.prefix = __
transforms.unwrap.add.headers = []
transforms.unwrap.add.headers.prefix = __
transforms.unwrap.delete.handling.mode = rewrite
transforms.unwrap.drop.fields.from.key = false
transforms.unwrap.drop.fields.header.name = null
transforms.unwrap.drop.fields.keep.schema.compatible = true
transforms.unwrap.drop.tombstones = false
transforms.unwrap.negate = false
transforms.unwrap.predicate = null
transforms.unwrap.route.by.field = 
transforms.unwrap.type = class io.debezium.transforms.ExtractNewRecordState
value.converter = null
 (org.apache.kafka.connect.runtime.ConnectorConfig$EnrichedConnectorConfig)
[2025-03-22 18:00:42,260] INFO Instantiated connector pg-connector with version 3.0.8.Final of type class io.debezium.connector.postgresql.PostgresConnector (org.apache.kafka.connect.runtime.Worker)
[2025-03-22 18:00:42,260] INFO Finished creating connector pg-connector (org.apache.kafka.connect.runtime.Worker)
[2025-03-22 18:00:42,261] INFO [Worker clientId=connect-localhost:8083, groupId=kafka-connect] Finished starting connectors and tasks (org.apache.kafka.connect.runtime.distributed.DistributedHerder)
```
