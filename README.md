# MS_Authorization

## Описание

MS_Authorization — это серверная реализация на Java с использованием Netty и HTTP/3 (QUIC), предназначенная для авторизации пользователей и управления токенами доступа. Проект использует Hibernate для работы с базой данных MySQL.

## Основные компоненты
- **Netty HTTP/3 Server**: Высокопроизводительный сервер, реализующий протокол HTTP/3 поверх QUIC.
- **Hibernate ORM**: Для взаимодействия с MySQL и хранения информации о пользователях и refresh-токенах.
- **JWT**: Для генерации и проверки токенов авторизации.
- **Обработчики маршрутов**: Реализованы в пакете `ms.netty.server.handlers`.

## Структура проекта
- `src/main/java/ms/netty/server/` — серверная логика, обработчики, маршрутизация
- `src/main/java/ms/netty/server/Hibernate/` — сущности Hibernate (например, UsersDefault, RefreshTokens)
- `src/main/java/ms/netty/client/` — клиентские примеры и обработчики

## Запуск сервера
1. Убедитесь, что MySQL сервер запущен и создана база данных `ms_authorization`.
2. Установите зависимости через Maven:
   ```bash
   mvn clean install
   ```
3. Запустите сервер:
   ```bash
   java -cp target/classes ms.netty.server.Http3ServerExample
   ```
   По умолчанию сервер стартует на порту, заданном в классе `Http3ServerExample`.

## Конфигурация
Параметры подключения к БД и другие настройки задаются в классе `Http3ServerExample` через Hibernate `Configuration`.

## Основные классы
- `Http3ServerExample` — точка входа, настройка сервера и Hibernate
- `handlers/` — обработчики маршрутов API
- `Hibernate/UsersDefault.java` — сущность пользователя
- `Hibernate/RefreshTokens.java` — сущность refresh-токена

## Пример запроса
Для взаимодействия с сервером используйте HTTP/3 клиент (например, curl с поддержкой QUIC или собственный клиент из проекта).

## Требования
- Java 11+
- Maven
- MySQL 8+

## Лицензия
MIT License

