package com.example.retrofitforecaster

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.math.RoundingMode


data class Main(
    val temp: Double,
    val feels_like: String? = null,
    val temp_min: String? = null,
    val temp_max: String? = null,
    val pressure: String? = null,
    val sea_level: String? = null,
    val grnd_level: String? = null,
    val humidity: String? = null,
    val temp_kf: String? = null,
)

data class Weather(
    val id: String? = null,
    val main: String? = null,
    val description: String? = null,
    val icon: String? = null,
)

data class Clouds(
    val all: String? = null,
)

data class Wind(
    val speed: String? = null,
    val deg: String? = null,
    val gust: String? = null,
)

data class Rain(
    val h3: String? = null,
)

data class Sys(
    val pod: String? = null,
)

data class Coord(
    val lat: String? = null,
    val lon: String? = null,
)

data class City(
    val id: String? = null,
    val name: String? = null,
    val coord: Coord? = null,
    val country: String? = null,
    val population: String? = null,
    val timezone: String? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
)

data class List(
    val dt: String? = null,
    val main: Main,
    val weather: Array<Weather>? = null,
    val clouds: Clouds? = null,
    val wind: Wind? = null,
    val visibility: String? = null,
    val pop: String? = null,
    val rain: Rain? = null,
    val sys: Sys? = null,
    val dt_txt: String? = null,
)

data class ApiWeather(
    val cod: String? = null,
    val message: String? = null,
    val cnt: String? = null,
    val list: Array<List>,
    val city: City? = null,
)

interface RetrofitApi {
    @GET("/data/2.5/forecast?lat=54.2021736&lon=30.2964015&appid=6c3a585c22ab1639999bc5af792abacd")
    suspend fun getWeather(): ApiWeather
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.r_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val productApi = retrofit.create(RetrofitApi::class.java)
        CoroutineScope(Dispatchers.IO).launch{
            val weather = productApi.getWeather()
            runOnUiThread{
                recyclerView.adapter = ListAdapter(this@MainActivity, weather.list)
            }
        }
    }
}

class ListAdapter(private val context: Context, private val list: Array<List>) : RecyclerView.Adapter<ListAdapter.Holder>(){
    private val TYPE_HOT = 1
    private val TYPE_COLD = 2
    class Holder(view: View): RecyclerView.ViewHolder(view){
        val dateTime: TextView = view.findViewById(R.id.txt_date)
        val temperature: TextView = view.findViewById(R.id.txt_C)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if(viewType == TYPE_HOT) {
            val view = LayoutInflater.from(context).inflate(R.layout.view_holder_hot, parent, false)
            return Holder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.view_holder_cold, parent, false)
            return Holder(view)
        }
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun getItemViewType(position: Int): Int {
        return if (list.get(position).main.temp.minus(272.15) <= 0) {
            TYPE_COLD
        } else {
            TYPE_HOT
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val data = list[position]

        holder.dateTime.text = data.dt_txt
        var temp = data.main.temp.minus(272.15)
        holder.temperature.text = (temp.toBigDecimal().setScale(2, RoundingMode.UP)).toString()
    }
}