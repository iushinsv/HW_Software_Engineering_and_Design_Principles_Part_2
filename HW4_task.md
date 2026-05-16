## Домашнее задание:
- Подключить к проекту Spring Actuator/Micrometer: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics
- И настроить Grafana Dashboard, чтобы в нем отображались все сервисы: client, service1, service2, zookeeper и их основные метрики jvm https://grafana.com/grafana/dashboards/4701-jvm-micrometer/

## ТЗ / Logs
1. На сервере:
a. Залогать запрос
b. Залогать ответ
2. На клиенте
a. Залогать запрос
b. Залогать ответ
3. Залогать при старте версию
Metrics
1. Метрики сервера
a. Кол-во запросов в секунду с разбивкой по клиентам
b. Кол-во 500-х ошибок
c. Время обработки запроса: среднее, медиана, n-% персентиль