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

Приложение деплоится на **WildFly** (JBoss) как WAR. Контекстный путь — `/` (задан в `jboss-web.xml`). Datasource JNDI: `java:/PostgresDS` — должен быть настроен в WildFly перед деплоем.

БД PostgreSQL: `appdb`, пользователь `appuser`, пароль `mysecretpassword`, порт `5432` (см. `docker-compose.yaml`).

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
- `error/ErrorView` — показывает сообщения об ошибках через JSF

**Страницы:** `index.xhtml` (редирект/приветствие), `main.xhtml` (основной интерфейс), `error.xhtml` (страница ошибки).

## Важные детали

- Идентификатор пользователя = `HttpSession.getId()`: данные сессии изолированы, при перезапуске сервера история теряется
- `HitDetector` не зависит от CDI-контейнера — тестируется напрямую через `new HitDetector()` (см. `HitDetectorTest`)
- Сообщения об ошибках и валидации — в `src/main/resources/messages.properties` (Unicode escape). Русский перевод — в `messages_ru.properties`, конвертируется плагином `native2ascii-maven-plugin` при сборке
- `build.properties` читается плагином `properties-maven-plugin` на фазе `initialize` — не удалять
- PrimeFaces тема: `saga` (задана в `web.xml`)
