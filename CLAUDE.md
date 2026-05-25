# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Собрать WAR-архив
./mvnw clean package

# Собрать и запустить тесты
./mvnw test

# Запустить только один тест
./mvnw test -Dtest=HitDetectorTest#testOriginIsHit

# Запустить тесты и закоммитить отчёты в git (профиль report)
./mvnw test -Preport

# Поднять PostgreSQL через Docker
docker compose up -d
```

Приложение деплоится на **WildFly 36** как exploded WAR через IntelliJ IDEA. Контекстный путь — `/` (задан в `jboss-web.xml`). Datasource JNDI: `java:/PostgresDS` — настроен в WildFly.

БД PostgreSQL: `appdb`, пользователь `appuser`, пароль `mysecretpassword`, порт `5432` (см. `docker-compose.yaml`).

WildFly расположен в `C:\Users\devat\Downloads\wildfly-36.0.1.Final\wildfly-36.0.1.Final`. HTTP-порт: 3001, management-порт: 3002.

## Архитектура

Jakarta EE 11 / JSF 4.1 (PrimeFaces 15) веб-приложение с CDI и JPA (Hibernate 7).

**Слои:**
- `point/` — CDI-бины (`XBean`, `YBean`, `RBean`) с `@SessionScoped`: хранят текущие значения координат и радиуса, содержат JSF-валидаторы
- `bean/ResultBean` — `@SessionScoped` CDI-бин, точка входа для JSF; управляет списком попаданий сессии, вызывает `HitService`
- `service/HitService` — `@ApplicationScoped`, `@Transactional`; создаёт `Hit`-сущности, делегирует детектирование и сохранение
- `service/HitDetector` — `@ApplicationScoped`; реализует геометрическую логику попадания (форма «Бэтмен»), нормирует координаты по `r`
- `repository/HitRepository` — JPA-репозиторий с `EntityManager`, реализует `Repository<Hit>`
- `entity/Hit` — JPA-сущность, таблица `hit`; хранит x, y, r, статус попадания, время выполнения, время запроса, идентификатор сессии пользователя
- `dto/HitDTO` — передаёт x/y/r от бина к сервису
- `util/Messages` — загружает строки из `messages.properties` через `ResourceBundle`
- `error/ErrorView` — показывает сообщения об ошибках через JSF и редиректит на `error.xhtml`
- `jmx/` — JMX MBeans (см. ниже)

**Страницы:** `index.xhtml` (редирект/приветствие), `main.xhtml` (основной интерфейс), `error.xhtml` (страница ошибки).

## JMX MBeans (задание лабораторной)

Реализованы в пакете `se.ifmo.lab3web.jmx`:

| Класс | Интерфейс | Описание |
|---|---|---|
| `PointStats` | `PointStatsMBean` | Счётчики `TotalPoints` / `HitsInArea`; отправляет JMX-уведомление `point.out_of_bounds` при выходе точки за пределы canvas |
| `ClickInterval` | `ClickIntervalMBean` | `ClickCount` и `AverageIntervalMs` — средний интервал между кликами |
| `JmxRegistrar` | — | CDI `@ApplicationScoped`; регистрирует оба MBean в `PlatformMBeanServer` при старте (`@PostConstruct`), снимает при остановке (`@PreDestroy`) |

**ObjectName'ы в JMX:** `se.ifmo.lab3web:type=PointStats` и `se.ifmo.lab3web:type=ClickInterval`.

**Интеграция:** `HitService.createHit()` вызывает `jmxRegistrar.getClickInterval().recordClick()` и `jmxRegistrar.getPointStats().recordPoint(x, y, r, hitStatus)`.

**Формула out-of-bounds:** canvas 400×400, pixelR интерполируется линейно (140px при R=1, 60px при R=4). Максимальная отображаемая координата = `30R / (25 - 4R)`. Уведомление отправляется если `|x| > maxCoord || |y| > maxCoord`.

**Важно для JMX Standard MBean:** имя класса-реализации должно совпадать с `<X>` в интерфейсе `<X>MBean`. Поэтому `PointStats` (не `PointStatsImpl`) реализует `PointStatsMBean`. Файлы `PointStatsImpl.java` и `ClickIntervalImpl.java` оставлены пустыми (только package-declaration).

## Подключение JConsole к WildFly

Нельзя использовать стандартный `-Dcom.sun.management.jmxremote` — он запускает JMX-агент до инициализации JBoss LogManager и ломает WildFly.

Правильный способ — нативный JMX-коннектор WildFly с `jboss-client.jar`:

```bat
# Создать management-пользователя (один раз)
C:\Users\devat\Downloads\wildfly-36.0.1.Final\wildfly-36.0.1.Final\bin\add-user.bat
# Тип: Management User, логин: admin1, пароль: <заданный>

# Запустить JConsole с jboss-client.jar
"C:\Users\devat\.sdkman\candidates\java\25.0.1-amzn\bin\jconsole.exe" -J-Djava.class.path="C:\Users\devat\Downloads\wildfly-36.0.1.Final\wildfly-36.0.1.Final\bin\client\jboss-client.jar"
```

Подключение: Remote Process → `service:jmx:remote+http://localhost:3002`, username `admin1`.

## Важные детали

- Идентификатор пользователя = `HttpSession.getId()`: данные сессии изолированы, при перезапуске сервера история теряется
- `HitDetector` не зависит от CDI-контейнера — тестируется напрямую через `new HitDetector()` (см. `HitDetectorTest`)
- Сообщения об ошибках и валидации — в `src/main/resources/messages.properties` (Unicode escape). Русский перевод — в `messages_ru.properties`, конвертируется плагином `native2ascii-maven-plugin` при сборке
- `build.properties` читается плагином `properties-maven-plugin` на фазе `initialize` — не удалять
- PrimeFaces тема: `saga` (задана в `web.xml`)
- Все `catch (Throwable e)` в `ResultBean` логируют реальный стектрейс через `java.util.logging.Logger` перед показом ошибки пользователю

## Статус лабораторной работы

**Задание 1** ✅ Реализованы два JMX MBean (`PointStats`, `ClickInterval`).

**Задание 2** ✅ Мониторинг через JConsole проведён: сняты показания MBean'ов, определены имена потоков.

**Задание 3** — мониторинг и профилирование через **VisualVM** (в работе):
- Снять график изменения показаний MBean'ов с течением времени
- Определить класс, объекты которого занимают наибольший объём памяти JVM; определить пользовательский класс в экземплярах которого находятся эти объекты

**Задание 4** — локализация и устранение проблем с производительностью через VisualVM + профилировщик IntelliJ (не начато):
- Описание выявленной проблемы
- Описание путей устранения
- Подробное описание алгоритма с скриншотами
