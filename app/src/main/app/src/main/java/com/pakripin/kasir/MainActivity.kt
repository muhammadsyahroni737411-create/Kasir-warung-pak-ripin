package com.pakripin.kasir

import android.app.Activity
import android.os.Bundle
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import java.text.NumberFormat
import java.util.Locale

private fun rupiah(n: Double): String =
    NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .format(n)

class KasirDB(a: Activity) :
    SQLiteOpenHelper(a, "pak_ripin.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE menu(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                harga REAL NOT NULL,
                hpp REAL NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE bahan(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                stok REAL NOT NULL,
                satuan TEXT NOT NULL,
                modal REAL NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE resep(
                menu_id INTEGER,
                bahan_id INTEGER,
                jumlah REAL NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE penjualan(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                waktu TEXT NOT NULL,
                total REAL NOT NULL,
                hpp REAL NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE item_jual(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jual_id INTEGER,
                nama TEXT,
                jumlah INTEGER,
                harga REAL,
                hpp REAL
            )
        """)
        db.execSQL("""
            CREATE TABLE pengeluaran(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT,
                jumlah REAL,
                waktu TEXT
            )
        """)

        db.execSQL(
            "INSERT INTO menu(nama,harga,hpp) VALUES('Bakso Biasa',12000,8500)"
        )
        db.execSQL(
            "INSERT INTO menu(nama,harga,hpp) VALUES('Bakso Spesial',18000,12000)"
        )
        db.execSQL(
            "INSERT INTO menu(nama,harga,hpp) VALUES('Bakso Urat',16000,10500)"
        )
        db.execSQL(
            "INSERT INTO menu(nama,harga,hpp) VALUES('Es Teh',4000,1000)"
        )

        db.execSQL(
            "INSERT INTO bahan(nama,stok,satuan,modal) VALUES('Bakso',200,'butir',1500)"
        )
        db.execSQL(
            "INSERT INTO bahan(nama,stok,satuan,modal) VALUES('Mi',5000,'gram',25)"
        )
        db.execSQL(
            "INSERT INTO bahan(nama,stok,satuan,modal) VALUES('Kaldu',50,'porsi',1500)"
        )

        db.execSQL(
            "INSERT INTO resep VALUES(1,1,3)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(1,2,100)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(1,3,1)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(2,1,5)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(2,2,120)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(2,3,1)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(3,1,4)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(3,2,100)"
        )
        db.execSQL(
            "INSERT INTO resep VALUES(3,3,1)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {}
}

class MainActivity : Activity() {
    private lateinit var db: KasirDB
    private lateinit var area: LinearLayout
    private val keranjang = linkedMapOf<Int, Int>()
    private var halaman = "Kasir"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = KasirDB(this)
        tampilkan()
    }

    private fun tulisan(s: String, size: Float = 16f): TextView {
        return TextView(this).apply {
            text = s
            textSize = size
            setTextColor(Color.rgb(35, 45, 38))
            setPadding(12, 12, 12, 12)
        }
    }

    private fun tombol(s: String, aksi: () -> Unit): Button {
        return Button(this).apply {
            text = s
            setOnClickListener { aksi() }
        }
    }

    private fun tampilkan() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val judul = TextView(this).apply {
            text = "BAKSO DAN WARUNG PAK RIPIN"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(23, 107, 69))
            setPadding(12, 22, 12, 22)
        }
        root.addView(judul)

        val navigasi = LinearLayout(this)
        listOf("Kasir", "Stok", "Laporan").forEach { nama ->
            navigasi.addView(tombol(nama) {
                halaman = nama
                tampilkan()
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(navigasi)

        area = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 24)
        }
        val scroll = ScrollView(this)
        scroll.addView(area)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        when (halaman) {
            "Kasir" -> kasir()
            "Stok" -> stok()
            else -> laporan()
        }
        setContentView(root)
    }

    private fun kasir() {
        area.addView(tulisan("Pilih menu", 21f))

        db.readableDatabase.rawQuery(
            "SELECT id,nama,harga FROM menu ORDER BY id", null
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                val nama = c.getString(1)
                val harga = c.getDouble(2)

                area.addView(tombol("$nama - ${rupiah(harga)}  +") {
                    keranjang[id] = (keranjang[id] ?: 0) + 1
                    tampilkan()
                })
            }
        }

        area.addView(tulisan("Keranjang", 20f))
        var total = 0.0
        keranjang.toMap().forEach { (id, jumlah) ->
            db.readableDatabase.rawQuery(
                "SELECT nama,harga FROM menu WHERE id=?",
                arrayOf(id.toString())
            ).use { c ->
                if (c.moveToFirst()) {
                    val nama = c.getString(0)
                    val harga = c.getDouble(1)
                    total += harga * jumlah
                    area.addView(tulisan(
                        "$nama x $jumlah = ${rupiah(harga * jumlah)}"
                    ))
                    area.addView(tombol("Hapus $nama") {
                        keranjang.remove(id)
                        tampilkan()
                    })
                }
            }
        }

        area.addView(tulisan("TOTAL: ${rupiah(total)}", 22f))
        area.addView(tombol("SIMPAN PENJUALAN TUNAI") {
            simpanPenjualan()
        })
        area.addView(tombol("Kosongkan keranjang") {
            keranjang.clear()
            tampilkan()
        })
    }

    private fun simpanPenjualan() {
        if (keranjang.isEmpty()) {
            Toast.makeText(
                this, "Keranjang masih kosong", Toast.LENGTH_SHORT
            ).show()
            return
        }

        val database = db.writableDatabase
        var total = 0.0
        var totalHpp = 0.0

        try {
            database.beginTransaction()

            val items = mutableListOf<Triple<String, Int, Pair<Double, Double>>>()
            keranjang.forEach { (id, qty) ->
                database.rawQuery(
                    "SELECT nama,harga,hpp FROM menu WHERE id=?",
                    arrayOf(id.toString())
                ).use { c ->
                    if (!c.moveToFirst()) error("Menu tidak ditemukan")
                    val nama = c.getString(0)
                    val harga = c.getDouble(1)
                    val hpp = c.getDouble(2)
                    items.add(Triple(nama, qty, harga to hpp))
                    total += harga * qty
                    totalHpp += hpp * qty
                }
            }

            val waktu = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.US
            ).format(java.util.Date())

            val jual = ContentValues().apply {
                put("waktu", waktu)
                put("total", total)
                put("hpp", totalHpp)
            }
            val idJual = database.insertOrThrow(
                "penjualan", null, jual
            )

            items.forEach { item ->
                val v = ContentValues().apply {
                    put("jual_id", idJual)
                    put("nama", item.first)
                    put("jumlah", item.second)
                    put("harga", item.third.first)
                    put("hpp", item.third.second)
                }
                database.insertOrThrow("item_jual", null, v)
            }

            database.setTransactionSuccessful()
            keranjang.clear()
            Toast.makeText(
                this, "Penjualan tersimpan: ${rupiah(total)}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this, "Gagal menyimpan: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            database.endTransaction()
        }
        tampilkan()
    }

    private fun stok() {
        area.addView(tulisan("Stok bahan baku", 21f))
        db.readableDatabase.rawQuery(
            "SELECT id,nama,stok,satuan,modal FROM bahan ORDER BY nama",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                val nama = c.getString(1)
                val jumlah = c.getDouble(2)
                val satuan = c.getString(3)
                val modal = c.getDouble(4)

                area.addView(tulisan(
                    "$nama: $jumlah $satuan\nModal: ${rupiah(modal)} per $satuan"
                ))
                area.addView(tombol("Tambah stok $nama") {
                    val v = EditText(this)
                    v.hint = "Jumlah tambahan"
                    v.inputType = 8194

                    android.app.AlertDialog.Builder(this)
                        .setTitle("Tambah stok $nama")
                        .setView(v)
                        .setPositiveButton("Simpan") { _, _ ->
                            val tambahan = v.text.toString().toDoubleOrNull()
                            if (tambahan != null && tambahan > 0) {
                                db.writableDatabase.execSQL(
                                    "UPDATE bahan SET stok=stok+? WHERE id=?",
                                    arrayOf(tambahan, id)
                                )
                                tampilkan()
                            } else {
                                Toast.makeText(
                                    this, "Jumlah tidak valid",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                })
            }
        }
    }

    private fun laporan() {
        var omzet = 0.0
        var hpp = 0.0
        var biaya = 0.0

        db.readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(total),0),COALESCE(SUM(hpp),0) FROM penjualan",
            null
        ).use { c ->
            if (c.moveToFirst()) {
                omzet = c.getDouble(0)
                hpp = c.getDouble(1)
            }
        }

        db.readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(jumlah),0) FROM pengeluaran",
            null
        ).use { c ->
            if (c.moveToFirst()) biaya = c.getDouble(0)
        }

        area.addView(tulisan("Laporan Keuangan", 22f))
        area.addView(tulisan("Omzet: ${rupiah(omzet)}"))
        area.addView(tulisan("HPP: ${rupiah(hpp)}"))
        area.addView(tulisan("Laba kotor: ${rupiah(omzet - hpp)}"))
        area.addView(tulisan("Pengeluaran: ${rupiah(biaya)}"))
        area.addView(tulisan(
            "Estimasi laba: ${rupiah(omzet - hpp - biaya)}", 19f
        ))

        area.addView(tulisan("Catat pengeluaran", 20f))
        val nama = EditText(this).apply {
            hint = "Nama pengeluaran"
        }
        val jumlah = EditText(this).apply {
            hint = "Nominal rupiah"
            inputType = 8194
        }
        area.addView(nama)
        area.addView(jumlah)

        area.addView(tombol("Simpan pengeluaran") {
            val n = nama.text.toString().trim()
            val a = jumlah.text.toString().toDoubleOrNull()
            if (n.isEmpty() || a == null || a <= 0) {
                Toast.makeText(
                    this, "Isi nama dan nominal dengan benar",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val v = ContentValues().apply {
                    put("nama", n)
                    put("jumlah", a)
                    put("waktu", java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.US
                    ).format(java.util.Date()))
                }
                db.writableDatabase.insertOrThrow(
                    "pengeluaran", null, v
                )
                tampilkan()
            }
        })
    }
}
