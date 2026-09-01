package com.mo.cashback.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [Bank::class, Category::class, CashbackEntry::class, Promo::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun categoryDao(): CategoryDao
    abstract fun entryDao(): CashbackEntryDao
    abstract fun promoDao(): PromoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }

        private fun build(context: Context): AppDatabase {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            lateinit var dbRef: AppDatabase
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "cashback.db",
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch { seed(dbRef) }
                    }
                })
                .build()
            dbRef = db
            return db
        }

        private suspend fun seed(db: AppDatabase) {
            db.bankDao().insertAll(catalogBanks)
            db.categoryDao().insertAll(seedCategories)
            db.promoDao().insertAll(seedPromos)
        }

        /**
         * v1 → v2: add the promos table (Кэшбэк+ tab) and fill it with the seed offers.
         * Seeding otherwise only runs in onCreate, so without this INSERT an upgraded
         * install would land on an empty Кэшбэк+ tab.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `promos` (" +
                        "`id` TEXT NOT NULL, `title` TEXT NOT NULL, `subtitle` TEXT NOT NULL, " +
                        "`benefits` TEXT NOT NULL, `url` TEXT NOT NULL, `accentHex` TEXT NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                seedPromos.forEach { p ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO promos (id, title, subtitle, benefits, url, accentHex, sortOrder) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        arrayOf(p.id, p.title, p.subtitle, p.benefits, p.url, p.accentHex, p.sortOrder),
                    )
                }
            }
        }

        /**
         * v2 → v3: no schema change — the version was bumped for a seed-data change only
         * (schemas/2.json and schemas/3.json are identical apart from version and hash).
         * The migration still has to exist, or Room cannot walk a v2 database up to v5.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = Unit
        }

        /** v4 → v5: insert Яндекс Банк, bump ОТП/Ozon/Юmoney sortOrder by 1. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE banks SET sortOrder = 14 WHERE id = 'otp'")
                db.execSQL("UPDATE banks SET sortOrder = 15 WHERE id = 'ozon'")
                db.execSQL("UPDATE banks SET sortOrder = 16 WHERE id = 'yumoney'")
                db.execSQL(
                    "INSERT OR IGNORE INTO banks (id, name, colorHex, isBuiltIn, isSelected, sortOrder) VALUES ('yandex', 'Яндекс Банк', '#FC3F1D', 1, 0, 13)"
                )
            }
        }

        /**
         * v3 → v4: add isBuiltIn / isSelected / sortOrder columns to the banks table.
         * Existing rows: mark as selected (preserve the user's setup); flag the 3 Ya.* labels
         * as custom (isBuiltIn = 0) since they were never real catalog banks.
         * Then insert the 11 new catalog banks at sortOrders 1..15.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE banks ADD COLUMN isBuiltIn INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE banks ADD COLUMN isSelected INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE banks ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE banks SET isBuiltIn = 0 WHERE id IN ('ya_weather', 'ya_tvprofi', 'ya_weather2')")
                // Preserve sortOrder for pre-existing real banks (positions in new catalog)
                db.execSQL("UPDATE banks SET sortOrder = 2 WHERE id = 'alfa'")
                db.execSQL("UPDATE banks SET sortOrder = 3 WHERE id = 'tbank'")
                db.execSQL("UPDATE banks SET sortOrder = 13 WHERE id = 'otp'")
                db.execSQL("UPDATE banks SET sortOrder = 14 WHERE id = 'ozon'")
                db.execSQL("UPDATE banks SET sortOrder = 100 WHERE id = 'ya_weather'")
                db.execSQL("UPDATE banks SET sortOrder = 101 WHERE id = 'ya_tvprofi'")
                db.execSQL("UPDATE banks SET sortOrder = 102 WHERE id = 'ya_weather2'")
                // Insert 11 new catalog banks (INSERT OR IGNORE — safe if they already exist)
                catalogBanks
                    .filter { it.id !in setOf("alfa", "tbank", "otp", "ozon") }
                    .forEach { b ->
                        db.execSQL(
                            "INSERT OR IGNORE INTO banks (id, name, colorHex, isBuiltIn, isSelected, sortOrder) VALUES (?, ?, ?, 1, 0, ?)",
                            arrayOf(b.id, b.name, b.colorHex, b.sortOrder),
                        )
                    }
            }
        }

        /** Curated catalog of 15 Russian banks with brand-correct colors. */
        val catalogBanks = listOf(
            Bank("sber",       "Сбер",            "#21A038", isBuiltIn = true, isSelected = false, sortOrder = 1),
            Bank("alfa",       "Альфа-Банк",      "#EF3124", isBuiltIn = true, isSelected = false, sortOrder = 2),
            Bank("tbank",      "Т-Банк",          "#FFDD2D", isBuiltIn = true, isSelected = false, sortOrder = 3),
            Bank("vtb",        "ВТБ",             "#002F87", isBuiltIn = true, isSelected = false, sortOrder = 4),
            Bank("gpb",        "Газпромбанк",     "#0079C2", isBuiltIn = true, isSelected = false, sortOrder = 5),
            Bank("raif",       "Райффайзен",      "#FFED00", isBuiltIn = true, isSelected = false, sortOrder = 6),
            Bank("otkritie",   "Открытие",        "#00A8E1", isBuiltIn = true, isSelected = false, sortOrder = 7),
            Bank("rshb",       "Россельхозбанк",  "#2A6E35", isBuiltIn = true, isSelected = false, sortOrder = 8),
            Bank("psb",        "Промсвязьбанк",   "#002554", isBuiltIn = true, isSelected = false, sortOrder = 9),
            Bank("sovcom",     "Совкомбанк",      "#D31E1E", isBuiltIn = true, isSelected = false, sortOrder = 10),
            Bank("mts",        "МТС-Банк",        "#E30613", isBuiltIn = true, isSelected = false, sortOrder = 11),
            Bank("pochta",     "Почта Банк",      "#BC237B", isBuiltIn = true, isSelected = false, sortOrder = 12),
            Bank("yandex",     "Яндекс Банк",     "#FC3F1D", isBuiltIn = true, isSelected = false, sortOrder = 13),
            Bank("otp",        "ОТП",             "#00FA80", isBuiltIn = true, isSelected = false, sortOrder = 14),
            Bank("ozon",       "Ozon",            "#005BFF", isBuiltIn = true, isSelected = false, sortOrder = 15),
            Bank("yumoney",    "Юmoney",          "#8B3FFD", isBuiltIn = true, isSelected = false, sortOrder = 16),
        )

        val seedCategories = listOf(
            Category("education",      "Образование",  "cat_education",      "🎓", true),
            Category("supermarket",    "Супермаркеты", "cat_supermarket",    "🛒", true),
            Category("all_purchases",  "Все покупки",  "cat_all_purchases",  "💳", true),
            Category("fastfood",       "Фастфуд",      "cat_fastfood",       "🍔", true),
            Category("restaurant",     "Рестораны",    "cat_restaurant",     "🍽", true),
            Category("books",          "Книги",        "cat_books",          "📚", true),
            Category("drugstore",      "Аптеки",       "cat_drugstore",      "💊", true),
            Category("sports_goods",   "Спорттовары",  "cat_sports_goods",   "⚽", true),
            Category("taxi",           "Такси",        "cat_taxi",           "🚕", true),
            Category("beauty",         "Красота",      "cat_beauty",         "💄", true),
            Category("transport",      "Транспорт",    "cat_transport",      "🚌", true),
            Category("entertainment",  "Развлечения",  "cat_entertainment",  "🎬", true),
            Category("home_repair",    "ДомРемонт",    "cat_home_repair",    "🔨", true),
            Category("train_tickets",  "Ждбилеты",     "cat_train_tickets",  "🚆", true),
            Category("clothes",        "Одежда",       "cat_clothes",        "👔", true),
        )

        val seedPromos = listOf(
            Promo(
                id = "promo_alfa",
                title = "Альфа-Банк",
                subtitle = "Бесплатное обслуживание + кэшбэк до 5%",
                benefits = "• Бесплатная карта при покупках от 10 000 ₽/мес\n• Кэшбэк до 5% в выбранных категориях\n• До 25% у партнёров\n• Снятие наличных без комиссии в любых банкоматах\n\nОфициальные условия и оформление — на сайте Альфа-Банка.",
                url = "https://alfabank.ru/get-money/debit-cards/alfa-card/",
                accentHex = "#EF3124",
                sortOrder = 1,
            ),
            Promo(
                id = "promo_tbank",
                title = "Т-Банк (Tinkoff Black)",
                subtitle = "До 30% кэшбэка и 16% на остаток",
                benefits = "• Кэшбэк до 30% у партнёров и до 15% в выбранных категориях\n• Процент на остаток до 16% годовых\n• Бесплатное обслуживание при тратах от 3 000 ₽/мес\n• Доставка карты курьером, без посещения отделения\n\nПодробные тарифы — на сайте Т-Банка.",
                url = "https://www.tbank.ru/cards/debit-cards/tinkoff-black/",
                accentHex = "#FFDD2D",
                sortOrder = 2,
            ),
            Promo(
                id = "promo_ozon",
                title = "Ozon Card",
                subtitle = "До 25% баллами на маркетплейсе",
                benefits = "• До 25% Ozon-баллов за покупки на Ozon\n• 1% баллов везде, где принимают Mastercard\n• Бесплатное обслуживание\n• До 12% годовых на остаток в Ozon-копилке\n\nПодробности и оформление — на сайте Ozon Финанс.",
                url = "https://finance.ozon.ru/cards",
                accentHex = "#005BFF",
                sortOrder = 3,
            ),
        )
    }
}
