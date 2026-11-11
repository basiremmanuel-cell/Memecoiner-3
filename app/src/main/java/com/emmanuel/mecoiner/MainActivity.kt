package com.emmanuel.mecoiner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var results: LinearLayout
    private lateinit var whaleContract: EditText
    private lateinit var apiKey: EditText
    private lateinit var chain: Spinner
    private lateinit var threshold: EditText
    private var lastAlert = ""
    private val refresh = object : Runnable {
        override fun run() { loadMarkets(); handler.postDelayed(this, 10_000) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createChannel()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        }
        buildUi()
        handler.post(refresh)
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 26, 28, 30); setBackgroundColor(0xFF0A1326.toInt()) }
        fun label(s: String, size: Float = 15f): TextView = TextView(this).apply { text=s; textSize=size; setTextColor(0xFFEAF1FF.toInt()); setPadding(0,10,0,10) }
        page.addView(label("EMMANUEL NOVEMBER 2026", 13f))
        page.addView(label("MEMECOINER", 28f))
        page.addView(label("BYBIT LIVE SCANNER  •  ETHEREUM + BNB WHALE WATCH", 12f))
        status = label("Connecting to Bybit…")
        page.addView(status)
        val refreshButton = Button(this).apply { text="REFRESH NOW"; setOnClickListener { loadMarkets() } }
        page.addView(refreshButton)
        page.addView(label("LIVE MEMECOIN MARKETS", 18f))
        results = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        page.addView(results)
        page.addView(label("WHALE INTELLIGENCE SETUP", 18f))
        page.addView(label("Enter a token contract and an Etherscan-compatible API key to inspect recent token transfers. Use your own API key."))
        chain = Spinner(this).apply { adapter=ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Ethereum","BNB Chain")) }
        page.addView(chain)
        whaleContract = EditText(this).apply { hint="Token contract address"; setSingleLine(true); setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFF9EABC3.toInt()) }
        page.addView(whaleContract)
        apiKey = EditText(this).apply { hint="Etherscan / BscScan API key"; setSingleLine(true); setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFF9EABC3.toInt()) }
        page.addView(apiKey)
        threshold = EditText(this).apply { hint="Whale threshold (token units), e.g. 1000000"; setSingleLine(true); setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFF9EABC3.toInt()); setText("1000000") }
        page.addView(threshold)
        page.addView(Button(this).apply { text="CHECK RECENT WHALE TRANSFERS"; setOnClickListener { checkWhales() } })
        page.addView(label("IMPORTANT", 15f))
        page.addView(label("Research tool only. No trading, wallet connection, or guaranteed profit. Market data refreshes every 10 seconds while this screen is open. Whale checks are manual and depend on the selected explorer API, its plan limits, and token contract."))
        page.addView(label("APP VERSION 1.0.0  •  Upgradeable Android Studio project", 12f))
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun loadMarkets() {
        val request = Request.Builder().url("https://api.bybit.com/v5/market/tickers?category=spot").get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { runOnUiThread { status.text="Bybit connection issue: ${e.message}" } }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()
                try {
                    val root = JSONObject(body)
                    if (root.optInt("retCode", -1) != 0) throw Exception(root.optString("retMsg","API error"))
                    val list = root.getJSONObject("result").getJSONArray("list")
                    val picks = mutableListOf<JSONObject>()
                    val symbols = listOf("DOGEUSDT","SHIBUSDT","PEPEUSDT","BONKUSDT","FLOKIUSDT","WIFUSDT","BRETTUSDT","BOMEUSDT","1000SATSUSDT","NEIROUSDT","TURBOUSDT","MEMEUSDT")
                    for (i in 0 until list.length()) {
                        val item=list.getJSONObject(i)
                        if (symbols.contains(item.optString("symbol"))) picks.add(item)
                    }
                    picks.sortByDescending { it.optDouble("price24hPcnt") }
                    runOnUiThread {
                        status.text="LIVE • Bybit Spot • Updated ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())} • 10-sec refresh"
                        results.removeAllViews()
                        if (picks.isEmpty()) results.addView(TextView(this@MainActivity).apply { text="No configured meme symbols returned. Check connection or update watchlist."; setTextColor(-1) })
                        picks.forEach { item ->
                            val change=item.optDouble("price24hPcnt")*100
                            val line=TextView(this@MainActivity).apply {
                                text="${item.optString("symbol")}     ${item.optString("lastPrice")} USDT     24h ${DecimalFormat("0.00").format(change)}%\n24h turnover: ${item.optString("turnover24h")} USDT"
                                textSize=14f; setTextColor(if(change>=0) 0xFF35D6A4.toInt() else 0xFFFF7777.toInt()); setPadding(0,12,0,12)
                            }
                            results.addView(line)
                            val div=View(this@MainActivity).apply { setBackgroundColor(0xFF263650.toInt()); layoutParams=LinearLayout.LayoutParams(-1,1) }
                            results.addView(div)
                        }
                    }
                } catch (e: Exception) { runOnUiThread { status.text="Bybit response error: ${e.message}" } }
            }
        })
    }

    private fun checkWhales() {
        val contract=whaleContract.text.toString().trim()
        val key=apiKey.text.toString().trim()
        val min=threshold.text.toString().toDoubleOrNull() ?: 1_000_000.0
        if(contract.isBlank() || key.isBlank()) { Toast.makeText(this,"Enter token contract and explorer API key",Toast.LENGTH_LONG).show(); return }
        val base=if(chain.selectedItemPosition==0) "https://api.etherscan.io/v2/api?chainid=1" else "https://api.etherscan.io/v2/api?chainid=56"
        val url="$base&module=account&action=tokentx&contractaddress=$contract&page=1&offset=30&sort=desc&apikey=$key"
        val req=Request.Builder().url(url).get().build()
        status.text="Checking recent ${chain.selectedItem} token transfers…"
        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call:Call,e:IOException){runOnUiThread{status.text="Whale check failed: ${e.message}"}}
            override fun onResponse(call:Call,response:Response){
                val raw=response.body?.string().orEmpty()
                try{
                    val obj=JSONObject(raw)
                    if(obj.optString("status")!="1") throw Exception(obj.optString("message",obj.optString("result","Explorer API error")))
                    val arr=obj.getJSONArray("result")
                    var count=0
                    val details=StringBuilder()
                    for(i in 0 until arr.length()){
                        val tx=arr.getJSONObject(i)
                        val decimals=tx.optString("tokenDecimal").toIntOrNull() ?: 18
                        val amount=(tx.optString("value").toDoubleOrNull() ?: 0.0)/Math.pow(10.0,decimals.toDouble())
                        if(amount>=min){
                            count++
                            details.append("\n${tx.optString("tokenSymbol")} transfer: ${DecimalFormat("#,##0.####").format(amount)}\nFrom ${tx.optString("from")}\nTo ${tx.optString("to")}\nTx ${tx.optString("hash")}\n")
                        }
                    }
                    runOnUiThread{
                        status.text="Whale scan complete: $count transfers at or above ${DecimalFormat("#,##0.##").format(min)} token units."
                        if(count>0){ showNotification("Whale transfer alert","$count large transfer(s) detected on ${chain.selectedItem}."); Toast.makeText(this@MainActivity,details.toString(),Toast.LENGTH_LONG).show() }
                        else Toast.makeText(this@MainActivity,"No transfers met the threshold in the latest 30 records.",Toast.LENGTH_LONG).show()
                    }
                }catch(e:Exception){runOnUiThread{status.text="Whale scan error: ${e.message}"}}
            }
        })
    }

    private fun createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            val channel=NotificationChannel("whales","Whale alerts",NotificationManager.IMPORTANCE_DEFAULT).apply{description="Large token transfer alerts"}
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun showNotification(title:String,message:String){
        val notification=NotificationCompat.Builder(this,"whales").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1001,notification)
    }
    override fun onDestroy(){handler.removeCallbacks(refresh);super.onDestroy()}
}
