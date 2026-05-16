# HW3: Consumer-Driven Contract Testing с Pact

## Выполнено

### 1. Настройка Pact Broker
- **docker-compose.yml**: подняты 3 сервиса:
  - `zookeeper:3.8` — для Service Discovery (HW2)
  - `postgres:16` — БД для Pact Broker
  - `pactfoundation/pact-broker:latest` — сам Pact Broker на порту **9292**
- Все сервисы запускаются через `docker compose up -d`

### 2. Consumer (rate-printer)
- **Зависимости**: `au.com.dius.pact.consumer:junit5:4.6.14`
- **Плагин публикации**: `au.com.dius.pact.provider:maven:4.6.14` с `pactBrokerUrl=http://localhost:9292`
- **RatePrinterPactTest**:
  - Использует **V4 HTTP DSL** (`expectsToReceiveHttpInteraction`)
  - Описывает контракт: `GET /api/rates/usdrub` → `{ "currency": "USDRUB", "rate": 80.0 }`
  - Для поля `rate` используется **type matcher** (`numberType`), чтобы провайдер мог возвращать любое число
  - Публикация контракта: `mvn pact:publish`

### 3. Provider (currency-rate-provider)
- **Зависимости**: `au.com.dius.pact.provider:junit5:4.6.14`
- **RateProviderPactVerificationTest**:
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)` — запускает реальное приложение
  - `@PactBroker(url = "http://localhost:9292")` — загружает контракты из Broker
  - `@MockBean CuratorFramework` и `@MockBean ServiceRegistrar` — моки для ZooKeeper (чтобы не было ошибок при старте)
  - `HttpTestTarget("localhost", port)` — цель для верификации
  - Результаты верификации публикуются обратно в Broker (`-Dpact.verifier.publishResults=true`)

### 4. End-to-End проверка
1. **Consumer test** → `mvn test` — зелёный
2. **Публикация контракта** → `mvn pact:publish` — `OK`
3. **Provider verification** → `mvn test -Dpact.verifier.publishResults=true` — `BUILD SUCCESS`

### 5. Файлы
| Файл | Назначение |
|---|---|
| `docker-compose.yml` | ZooKeeper + Postgres + Pact Broker |
| `rate-printer/.../RatePrinterPactTest.java` | Consumer contract test |
| `currency-rate-provider/.../RateProviderPactVerificationTest.java` | Provider verification test |
| `rate-printer/pom.xml` | Плагин публикации + зависимость consumer |
| `currency-rate-provider/pom.xml` | Зависимость provider |
| `rate-printer/src/test/resources/pact.publish.properties` | URL Pact Broker |

---

## Сборка и запуск

### Требования
- Java 17+
- Maven (или Maven Wrapper)
- Docker (для ZooKeeper, Postgres и Pact Broker)

### Запуск инфраструктуры
```bash
cd HW_Software_Engineering_and_Design_Principles_Part_2
docker compose up -d
```

Будут запущены 3 сервиса:
- **ZooKeeper** (порт 2181) — реестр сервисов (HW2)
- **Postgres** (порт 5432) — БД для Pact Broker
- **Pact Broker** (порт 9292) — хранилище контрактов

### Сборка сервисов
```bash
cd currency-rate-provider && ./mvnw clean package -DskipTests
cd rate-printer && ./mvnw clean package -DskipTests
```

### Consumer test + публикация контракта
```bash
cd rate-printer
./mvnw clean test pact:publish
```

Тест генерирует контракт и публикует его в Pact Broker (`http://localhost:9292`).

### Provider verification (верификация контракта)
```bash
cd currency-rate-provider
./mvnw clean test -Dpact.verifier.publishResults=true
```

Провайдер загружает контракты из Broker, запускает реальное приложение и проверяет, что его API соответствует ожиданиям consumer. Результат верификации публикуется обратно в Broker.

### Полный end-to-end сценарий
```bash
# 1. Поднять инфраструктуру
docker compose up -d

# 2. Consumer: тест + публикация
cd rate-printer && ./mvnw clean test pact:publish

# 3. Provider: верификация
cd ../currency-rate-provider && ./mvnw clean test -Dpact.verifier.publishResults=true
```

---

## Вывод
Consumer-Driven Contract Testing с Pact реализован: consumer описывает ожидания, provider их верифицирует. Контракты хранятся в Pact Broker, что позволяет отслеживать изменения и не допускать несовместимости между сервисами.
