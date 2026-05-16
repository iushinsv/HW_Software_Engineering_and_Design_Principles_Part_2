# HW2. Service Registry и балансировка нагрузки с ZooKeeper

## Общее описание

В рамках домашнего задания №2 в существующую систему из двух микросервисов (Currency Rate Provider и Rate Printer) добавлен слой **service discovery** на базе **Apache ZooKeeper**. Реализована автоматическая регистрация провайдеров при запуске и динамическая балансировка запросов со стороны клиента.

Система демонстрирует принципы централизованного реестра сервисов (Service Registry), клиентской балансировки нагрузки (Client-Side Load Balancing) и отказоустойчивости при наличии нескольких инстансов одного сервиса.

---

## ZooKeeper как Service Registry

**ZooKeeper** — распределённый координационный сервис, используемый в данной работе в качестве центрального реестра (Service Registry). Каждый экземпляр сервиса при запуске регистрирует себя в ZooKeeper, а при остановке — дерегистрируется. Клиентский сервис запрашивает у ZooKeeper список активных инстансов и выбирает один для выполнения запроса.

- **Развёртывание:** ZooKeeper 3.8 запущен в Docker-контейнере
- **Порт:** 2181 (стандартный порт ZooKeeper)
- **Путь в ZooKeeper:** `/services/currency-rate-provider`

---

## Сервис 1: Currency Rate Provider (с регистрацией в ZooKeeper)

### Изменения относительно HW1

Добавлена автоматическая регистрация в ZooKeeper при запуске приложения и дерегистрация при остановке.

### Новые компоненты

**CuratorConfig.java** — конфигурация клиента ZooKeeper на базе библиотеки Apache Curator:

- Подключение к ZooKeeper по адресу `localhost:2181`
- Стратегия повторных попыток: ExponentialBackoffRetry (3 попытки с задержкой 1с, экспоненциально растущей)
- `CuratorFramework` — высокоуровневый клиент ZooKeeper, предоставляемый как Spring Bean

**ServiceRegistrar.java** — компонент, отвечающий за регистрацию сервиса в реестре:

- При старте (через `@PostConstruct`): создаёт в ZooKeeper узел `/services/currency-rate-provider` с информацией о хосте (`localhost`) и порте (берётся из `server.port`)
- При остановке (через `@PreDestroy`): закрывает соединение, что автоматически удаляет узел из ZooKeeper
- Использует `ServiceDiscovery<Void>` из библиотеки curator-x-discovery для удобной работы с экземплярами сервисов
- Порт каждого инстанса определяется через `@Value("${server.port}")` — это позволяет запускать несколько копий провайдера на разных портах

---

## Сервис 2: Rate Printer (с балансировкой)

### Изменения относительно HW1

Вместо использования жёстко заданного URL (`http://localhost:8080/api/rates/usdrub`) сервис теперь получает список активных инстансов из ZooKeeper и выбирает один случайным образом.

### Новые компоненты

**CuratorConfig.java** (аналогичный первому сервису) — настройка клиента ZooKeeper для получения списка зарегистрированных сервисов.

### Обновлённая логика RatePrinterService

1. При старте — подключение к ZooKeeper через `ServiceDiscovery<Void>`
2. В бесконечном цикле (каждые 5 секунд):
   - Запрос всех активных инстансов `currency-rate-provider` из ZooKeeper
   - Если инстансы есть: **случайный выбор** (Random Load Balancing) одного из списка
   - Формирование URL на основе адреса и порта выбранного инстанса
   - HTTP GET-запрос, логирование результата с указанием, какой именно инстанс обработал запрос
   - Если инстансов нет — логирование предупреждения

### Пример вывода

```
USDRUB: 79.33 (from localhost:8082)
USDRUB: 81.95 (from localhost:8080)
USDRUB: 80.12 (from localhost:8080)
USDRUB: 78.54 (from localhost:8082)
```

Из вывода видно, что запросы распределяются между двумя инстансами провайдера (8080 и 8082).

---

## Балансировка нагрузки

**Стратегия:** Random (случайный выбор)

При каждом запросе клиент случайным образом выбирает один из доступных инстансов сервиса. Преимущества:

- Простота реализации
- Не требует хранения состояния (stateful) на стороне клиента
- При большом количестве запросов нагрузка распределяется равномерно

При падении одного из инстансов ZooKeeper автоматически исключает его из списка (по истечении таймаута сессии), и клиент перестаёт отправлять запросы на недоступный сервер.

---

## Организация репозитория

```
HW_Software_Engineering_and_Design_Principles_Part_2/
├── currency-rate-provider/          # Сервис 1 (провайдер курса)
│   ├── pom.xml                      # + curator-x-discovery
│   └── src/main/java/com/example/
│       └── currency_rate_provider/
│           ├── CurrencyRateProviderApplication.java
│           ├── config/
│           │   ├── CuratorConfig.java          # [NEW] Подключение к ZooKeeper
│           │   └── ServiceRegistrar.java       # [NEW] Регистрация в ZooKeeper
│           ├── controller/
│           │   └── RateController.java
│           └── model/
│               └── RateResponse.java
├── rate-printer/                    # Сервис 2 (печатник курса)
│   ├── pom.xml                      # + curator-x-discovery
│   └── src/main/java/com/example/
│       └── rate_printer/
│           ├── RatePrinterApplication.java
│           ├── config/
│           │   ├── CuratorConfig.java          # [NEW] Подключение к ZooKeeper
│           │   └── RestTemplateConfig.java
│           └── service/
│               └── RatePrinterService.java     # [UPD] Балансировка через ZooKeeper
└── HW2_task_report.md               # [NEW] Документация HW2
```

---

## Принципы и подходы

1. **Service Registry (Реестр сервисов)** — ZooKeeper выступает как единая точка учёта всех доступных инстансов сервиса.
2. **Client-Side Service Discovery** — клиент сам запрашивает список доступных инстансов и выбирает, к кому обратиться (без промежуточного балансировщика).
3. **Client-Side Load Balancing** — равномерное распределение нагрузки между инстансами на стороне клиента.
4. **Graceful Registration/Deregistration** — сервисы регистрируются при старте и автоматически удаляются из реестра при штатной или аварийной остановке.
5. **High Availability (Отказоустойчивость)** — при запуске нескольких экземпляров провайдера клиент продолжает работать, даже если один из инстансов выходит из строя.
6. **Централизованная конфигурация** — данные об адресе ZooKeeper (`localhost:2181`) заданы в конфигурационных классах и легко изменяются.

---

## Сборка и запуск

### Требования
- Java 17+
- Maven (или Maven Wrapper)
- Docker (для ZooKeeper)

### Запуск ZooKeeper
```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.8
```

### Сборка сервисов
```bash
cd currency-rate-provider && ./mvnw clean package -DskipTests
cd rate-printer && ./mvnw clean package -DskipTests
```

### Запуск нескольких инстансов провайдера
```bash
# Терминал 1 — провайдер на порту 8080
cd currency-rate-provider && java -jar target/currency-rate-provider-0.0.1-SNAPSHOT.jar

# Терминал 2 — провайдер на порту 8082
cd currency-rate-provider && java -jar target/currency-rate-provider-0.0.1-SNAPSHOT.jar --server.port=8082
```

### Запуск принтера
```bash
# Терминал 3 — принтер на порту 8081
cd rate-printer && java -jar target/rate-printer-0.0.1-SNAPSHOT.jar
```

### Проверка балансировки
```bash
# Прямой запрос к любому инстансу
curl http://localhost:8080/api/rates/usdrub
curl http://localhost:8082/api/rates/usdrub