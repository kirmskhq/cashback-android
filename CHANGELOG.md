# Changelog

All user-visible changes to **Кэшбэк**. Format loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

The pair to track:
- **versionName** — what humans see (`Settings → Apps → Кэшбэк → version`). Free-form, follow [SemVer](https://semver.org/): `MAJOR.MINOR.PATCH`.
- **versionCode** — monotonically-increasing integer. Android uses this for upgrade ordering; **always bump on a release**. Each version must have a strictly higher code than the previous one.

Both live in `app/build.gradle.kts → defaultConfig`.

---

## [1.2.1] — 2026-09-01  (versionCode 4)

### Fixed
- **Обновление приложения больше не может стереть данные.** Убран `fallbackToDestructiveMigration()` и добавлены недостающие миграции: `1 → 2` (создаёт таблицу `promos` и заполняет её сид-данными — иначе после апгрейда вкладка Кэшбэк+ осталась бы пустой) и `2 → 3` (no-op: версия поднималась под изменение сид-данных, схема не менялась). Цепочка миграций теперь полная, 1 → 5.
- **Напоминание переживает перезагрузку.** AlarmManager сбрасывает все отложенные будильники при рестарте устройства, а `ReminderReceiver` перевзводил себя только после срабатывания — то есть один ребут молча отменял напоминание навсегда. Добавлен `BootCompletedReceiver` на `BOOT_COMPLETED` и `MY_PACKAGE_REPLACED`.

### Added
- Юнит-тесты на расчёт даты напоминания (`Reminders.nextFireTimeMillis`): переход через границу месяца и года, короткий и високосный февраль. Запуск — `./gradlew testDebugUnitTest`.
- `.gitignore`. Из репозитория убраны артефакты сборки (`app/build/`, `.gradle/`, `app-debug.apk`, `*.log`) и машинно-зависимый `local.properties`.

## [1.2.0] — 2026-05-18  (versionCode 3)

### Added
- **Категории — сетка чипов** вместо списка. Цветные плитки сверху для категорий с активным кэшбэком в этом месяце (заливка цветом топ-банка, видно бренд + %), компактные серые чипы снизу для категорий без предложений. Помогает выбрать карту на кассе за 1–2 секунды.
- **Удаление записи на экране категории** — иконка корзины справа от каждой строки в ранжированном списке (зеркалит существующее на банк-детали).
- **Перенос предыдущего месяца** — на экране банка, когда в этом месяце ещё нет записей, а в прошлом были, показывается баннер «В прошлом месяце было: 🛒 5% 🍽 10% [Скопировать в этот месяц]». Тап → копирует все записи в текущий месяц одним нажатием.
- **Месячное напоминание** — локальное уведомление 30-го числа в 10:00: «🛎 Время выбрать кэшбэк». Запросит permission на уведомления при первом запуске (Android 13+).
- Яндекс Банк добавлен в каталог (брендовый красный `#FC3F1D`).

### Migrated
- v4 → v5: вставлен Яндекс Банк; bump sortOrder у ОТП, Ozon, Юmoney.

## [1.1.0] — 2026-05-17  (versionCode 2)

### Added
- Bank picker — Banks tab is now customizable. Curated catalog of 16 well-known Russian banks (Сбер, Альфа-Банк, Т-Банк, ВТБ, Газпромбанк, Райффайзен, Открытие, Россельхозбанк, Промсвязьбанк, Совкомбанк, МТС-Банк, Почта Банк, Яндекс Банк, ОТП, Ozon, Юmoney) with brand-correct colors.
- "Свой банк" — up to 3 custom banks with name + 12-color palette picker.
- Empty state on Banks tab for first-launch users.
- Pencil icon in Banks tab top-right to re-open the picker any time.
- **Кэшбэк+** tab — 4th bottom-nav tab for partner promo cards.
- Each promo card opens a detail screen with full T&C body + "Открыть на сайте банка" button (system browser via `Intent.ACTION_VIEW`).
- **Add cashback from Категория detail** — FAB on category detail mirrors the bank-detail FAB; category is locked, bank dropdown open.
- **"+N" pill** on Категории tab — shows how many additional banks offer the same category in the current month.
- **Smart sort** on Категории tab — categories with active cashback this month sort to the top by best %, empty ones fall to the bottom.

### Changed
- Built-in categories expanded 7 → 15 (added Образование, Книги, Спорттовары, Такси, Транспорт, Развлечения, ДомРемонт, Ждбилеты).
- Top tab renamed «По категориям» → **«Категории»**.
- Three Ya.* labels are now flagged as custom banks (no longer in catalog).
- Категории top-bank ranking now filters to selected banks only (banks you actually have).

### Migrated
- v3 → v4: added `isBuiltIn` / `isSelected` / `sortOrder` columns to `banks` table; existing rows preserved with `isSelected = 1`.
- v4 → v5: inserted Яндекс Банк; bumped ОТП / Ozon / Юmoney sortOrder.

## [1.0.0] — 2026-05-17  (versionCode 1)

### Added
- Initial release. Three tabs: Банки, По категориям, История.
- 7 seed banks (Альфа, Т-Банк, ОТП, Ozon + 3 Ya.* labels), 7 built-in categories.
- Add cashback dialog (bank locked) launchable from any bank detail.
- New custom category (name + emoji).
- Russian-only UI (locale forced in `MainActivity.attachBaseContext`).
- Room persistence; data survives kill + reopen.
- Sideload-able debug APK (no Play Store).
