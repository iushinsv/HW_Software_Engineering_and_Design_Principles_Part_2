# Домашнее задание 2:
- Подключить zookeeper как service registry в проект, чтобы producer-ы автоматически в нем регистрировались при старте, а consumer-ы автоматически узнавали сколько есть instance-ов и делали между ними балансировку.
- Почитать: https://www.oreilly.com/library/view/distributed-systems-observability/9781492033431/ch04.html

```
Chapter 4. The Three Pillars of Observability
Logs, metrics, and traces are often known as the three pillars of observability. While plainly having access to logs, metrics, and traces doesn’t necessarily make systems more observable, these are powerful tools that, if understood well, can unlock the ability to build better systems.

Event Logs
An event log is an immutable, timestamped record of discrete events that happened over time. Event logs in general come in three forms but are fundamentally the same: a timestamp and a payload of some context. The three forms are:

Plaintext
A log record might be free-form text. This is also the most common format of logs.

Structured
Much evangelized and advocated for in recent days. Typically, these logs are emitted in the JSON format.

Binary
Think logs in the Protobuf format, MySQL binlogs used for replication and point-in-time recovery, systemd journal logs, the pflog format used by the BSD firewall pf that often serves as a frontend to tcpdump.

Debugging rare or infrequent pathologies of systems often entails debugging at a very fine level of granularity. Event logs, in particular, shine when it comes to providing valuable insight along with ample context into the long tail that averages and percentiles don’t surface. As such, event logs are especially helpful for uncovering emergent and unpredictable behaviors exhibited by components of a distributed system.

Failures in complex distributed systems rarely arise because of one specific ...
```