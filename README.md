# Кэшбэк · Cashback

Android-приложение, которое помнит, какая карта в этом месяце даёт лучший кэшбэк в какой категории.
*An Android app that remembers which card gives the best cashback in which category this month.*

**[🇷🇺 Русский](#-русский) · [🇬🇧 English](#-english)**

---

## 🇷🇺 Русский

Банки каждый месяц заново раздают повышенный кэшбэк по категориям, и к моменту, когда вы стоите у кассы, никто уже не помнит, какой картой платить за продукты. Приложение решает ровно эту задачу: один раз в месяц вы записываете выбранные категории, а дальше за одну-две секунды видите, какую карту доставать.

Работает полностью офлайн: нет аккаунтов, нет сети, нет аналитики. Все данные лежат в локальной базе Room на устройстве.

### Что умеет

- **Банки** — свой список карт из каталога 16 российских банков (Сбер, Альфа-Банк, Т-Банк, ВТБ, Газпромбанк, Райффайзен, Открытие, Россельхозбанк, Промсвязьбанк, Совкомбанк, МТС-Банк, Почта Банк, Яндекс Банк, ОТП, Ozon, Юmoney) с фирменными цветами, плюс до 3 своих банков с палитрой на 12 цветов.
- **Категории** — сетка чипов: сверху цветные плитки категорий с активным кэшбэком (заливка цветом лучшего банка, видно бренд и процент), снизу — серые чипы без предложений. Пилюля «+N» показывает, сколько ещё банков дают ту же категорию.
- **История** — все записи по месяцам, с переключением вперёд-назад.
- **Кэшбэк+** — подборка промо-условий по картам; карточка открывает условия и ведёт на сайт банка.
- **Перенос предыдущего месяца** — если в этом месяце записей ещё нет, а в прошлом были, одна кнопка копирует их целиком.
- **Напоминание** — локальное уведомление 30-го числа в 10:00: банки уже опубликовали категории на следующий месяц.

15 встроенных категорий (Супермаркеты, Все покупки, Фастфуд, Рестораны, Аптеки, Одежда, Образование, Книги, Спорттовары, Такси, Транспорт, Развлечения, ДомРемонт, Ждбилеты, Красота); свои добавляются с любым эмодзи.

### Стек

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.10.01) · Material 3 · Navigation Compose · Room 2.6.1 + KSP · AGP 8.7.0 · Gradle 8.10.2 · JDK 17.

`minSdk 26` · `targetSdk 35` · `applicationId com.mo.cashback` · версия 1.2.0 (versionCode 3).

Архитектура — однослойная MVVM: экраны на Compose, по `ViewModel` на экран, единый `CashbackRepository` поверх четырёх DAO. База — синглтон, репозиторий живёт в `CashbackApplication`; DI-фреймворка нет.

### Сборка

Нужен Android SDK и JDK 17. Путь к SDK — в `local.properties` (`sdk.dir=...`).

```bash
./gradlew assembleDebug
```

APK окажется в `app/build/outputs/apk/debug/app-debug.apk` — приложение ставится сайдлоадом, в Google Play его нет.

### Данные

Room, схема версии 5, экспорт в `app/schemas/`. Четыре таблицы:

| Таблица | Что хранит |
|---|---|
| `banks` | каталожные и свои банки: имя, цвет, выбран ли, порядок |
| `categories` | встроенные и пользовательские категории (имя, эмодзи) |
| `entries` | процент кэшбэка: банк × категория × год × месяц (уникальный индекс) |
| `promos` | карточки вкладки Кэшбэк+ |

Удаление банка или категории каскадом удаляет связанные записи. Заполнение каталога происходит один раз при создании базы, новые банки доезжают миграциями (`MIGRATION_3_4`, `MIGRATION_4_5`).

### Структура

```
app/src/main/java/com/mo/cashback/
├── data/        сущности Room, DAO, база, сид-данные, миграции
├── repo/        CashbackRepository — единственная точка доступа к данным
├── ui/
│   ├── banks/       список банков, детали банка, пикер банков
│   ├── categories/  сетка категорий и детали категории
│   ├── history/     все записи по месяцам
│   ├── promos/      вкладка Кэшбэк+ и детали промо
│   ├── dialog/      добавление записи, свой банк, новая категория
│   ├── component/   общие элементы (топ-бар, хелперы)
│   ├── navigation/  AppNav — нижние вкладки и маршруты
│   └── theme/       цвета, типографика, тема Material 3
└── util/        Reminders — ежемесячное напоминание
mockups/index.html   HTML-макеты всех экранов
CHANGELOG.md         журнал изменений по версиям
```

### Язык интерфейса

Интерфейс только на русском: локаль принудительно выставляется в `MainActivity.attachBaseContext`, системный язык устройства игнорируется.

### Замечания по репозиторию

В репозиторий попали артефакты сборки (`app/build/`, `.gradle/`, `app-debug.apk`, `.build*.log`) — `.gitignore` пока нет. При клонировании их можно смело удалять, на сборку они не влияют.

---

## 🇬🇧 English

Russian banks reshuffle their elevated-cashback categories every month, and by the time you are at the checkout nobody remembers which card to pay for groceries with. This app solves exactly that: once a month you write down the categories you picked, and from then on it takes a second or two to see which card to pull out.

Fully offline: no accounts, no network, no analytics. Everything lives in a local Room database on the device.

### Features

- **Banks** — your own card list, built from a curated catalog of 16 Russian banks (Sber, Alfa-Bank, T-Bank, VTB, Gazprombank, Raiffeisen, Otkritie, Rosselkhozbank, Promsvyazbank, Sovcombank, MTS-Bank, Pochta Bank, Yandex Bank, OTP, Ozon, YuMoney) with brand-correct colors, plus up to 3 custom banks with a 12-color palette.
- **Categories** — a chip grid: colored tiles on top for categories with active cashback this month (filled with the best bank's brand color, showing brand and percentage), gray chips below for categories with no offers. A "+N" pill shows how many other banks offer the same category.
- **History** — every entry by month, with back/forward navigation.
- **Cashback+** — a set of promotional card offers; each card opens the terms and links out to the bank's site.
- **Carry forward** — if this month is still empty but last month was not, one tap copies the whole set over.
- **Reminder** — a local notification on the 30th at 10:00, when banks publish next month's categories.

15 built-in categories (Supermarkets, All purchases, Fast food, Restaurants, Pharmacies, Clothing, Education, Books, Sporting goods, Taxi, Transport, Entertainment, Home repair, Rail tickets, Beauty); custom ones take any emoji.

### Stack

Kotlin 2.0.21 · Jetpack Compose (BOM 2024.10.01) · Material 3 · Navigation Compose · Room 2.6.1 + KSP · AGP 8.7.0 · Gradle 8.10.2 · JDK 17.

`minSdk 26` · `targetSdk 35` · `applicationId com.mo.cashback` · version 1.2.0 (versionCode 3).

Architecture is flat MVVM: Compose screens, one `ViewModel` per screen, a single `CashbackRepository` over four DAOs. The database is a singleton and the repository lives on `CashbackApplication`; there is no DI framework.

### Build

Requires the Android SDK and JDK 17. Point `local.properties` at your SDK (`sdk.dir=...`).

```bash
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`. The app is sideloaded — it is not on Google Play.

### Data

Room, schema version 5, exported to `app/schemas/`. Four tables:

| Table | Holds |
|---|---|
| `banks` | catalog and custom banks: name, color, selected flag, sort order |
| `categories` | built-in and user-created categories (name, emoji) |
| `entries` | the cashback percentage: bank × category × year × month (unique index) |
| `promos` | cards for the Cashback+ tab |

Deleting a bank or a category cascades to its entries. The catalog is seeded once on database creation; new banks arrive through migrations (`MIGRATION_3_4`, `MIGRATION_4_5`).

### Layout

```
app/src/main/java/com/mo/cashback/
├── data/        Room entities, DAOs, database, seed data, migrations
├── repo/        CashbackRepository — the single data access point
├── ui/
│   ├── banks/       bank list, bank detail, bank picker
│   ├── categories/  category grid and category detail
│   ├── history/     all entries by month
│   ├── promos/      Cashback+ tab and promo detail
│   ├── dialog/      add entry, custom bank, new category
│   ├── component/   shared pieces (top bar, helpers)
│   ├── navigation/  AppNav — bottom tabs and routes
│   └── theme/       colors, typography, Material 3 theme
└── util/        Reminders — the monthly notification
mockups/index.html   HTML mockups of every screen
CHANGELOG.md         per-version change log
```

### UI language

The interface is Russian only: the locale is forced in `MainActivity.attachBaseContext`, ignoring the device language.

### Repository notes

Build artifacts are currently tracked in git (`app/build/`, `.gradle/`, `app-debug.apk`, `.build*.log`) — there is no `.gitignore` yet. They can be deleted after cloning; the build does not depend on them.
