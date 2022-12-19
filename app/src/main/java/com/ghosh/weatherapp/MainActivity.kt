package com.ghosh.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ghosh.weatherapp.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    var weatherList= ArrayList<WeatherRVModel>()
    lateinit var weatherAdapter: WeatherAdapter
    lateinit var locationManager : LocationManager
    var permissionCode:Int= 1
    lateinit var cityName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //For not seeing the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        mainBinding=ActivityMainBinding.inflate(layoutInflater)
        val view= mainBinding.root
        setContentView(view)

        weatherAdapter= WeatherAdapter(this, weatherList )
        //mainBinding.idRVWeather.layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
        mainBinding.idRVWeather.adapter=weatherAdapter

        locationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION),permissionCode )
        }

        var location: Location?=null
        location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        //try { location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!! }
        //catch(e:java.lang.NullPointerException){ e.printStackTrace()}
        cityName= location?.let { getCityName(it.longitude, it.latitude) }.toString()

        //println(cityName)
        //Log.e("TAG",cityName)
        getWeatherInfo(cityName)

        mainBinding.idIVSearch.setOnClickListener( View.OnClickListener {

            val city: String= mainBinding.idEdtCity.text.toString()
            if(city.isEmpty())
            {
                Toast.makeText(this,"Please enter City Name", Toast.LENGTH_LONG).show()
            }
            else{
                //mainBinding.idEdtCity.setText(cityName)
                getWeatherInfo(city)

            }
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==permissionCode && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this@MainActivity,"Permissions Granted",Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(this@MainActivity,"Please grant the permissions",Toast.LENGTH_LONG).show()
            finish()
        }
    }



    fun getCityName(longitude: Double, latitude: Double ): String
    {
        var cityName: String="Not found"
        val gcd: Geocoder= Geocoder(baseContext, Locale.getDefault())
        try{
            val addresses: List<Address> = gcd.getFromLocation(latitude,longitude,10)

            for(address in addresses)
            {
                if(address!=null){
                    val city: String = address.locality
                    if(city!=null && !city.equals(""))
                    {
                        cityName=city
                    }
                    else
                    {
                        Log.d("TAG","City not found")
                        Toast.makeText(this,"City not found",Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
        catch(e: IOException)
        {
            e.printStackTrace()
        }
        return cityName
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun getWeatherInfo(cityName: String){
        val url: String="http://api.weatherapi.com/v1/forecast.json?key=7326e75d82f24fc0ac190644221512&q="+cityName+"&days=1&aqi=no&alerts=no"
        mainBinding.idTVCityName.setText(cityName)

        if(mainBinding.idTVCityName.text=="null") { mainBinding.idTVCityName.text="Home" }

        val requestQueue: RequestQueue = Volley.newRequestQueue(this@MainActivity)

        val jsonObjectRequest:JsonObjectRequest= JsonObjectRequest(Request.Method.GET,url,null,
            {response->
                mainBinding.idPBLoading.visibility= View.GONE
                mainBinding.idRLHome.visibility=View.VISIBLE

                weatherList.clear()


                try{
                    val temperature: String= response.getJSONObject("current").getString("temp_c")
                    mainBinding.idTvTemp.setText("$temperature°C")
                    val isDay: Int= response.getJSONObject("current").getInt("is_day")
                    val condition:String= response.getJSONObject("current").getJSONObject("condition").getString("text")
                    val conditionIcon:String= response.getJSONObject("current").getJSONObject("condition").getString("icon")
                    Picasso.get().load("http:"+conditionIcon).into(mainBinding.idTVIcon)
                    mainBinding.idTvCondition.setText(condition)
                    if(isDay==1)
                    {
                        Picasso.get().load("https://images.unsplash.com/photo-1546702954-503d7d305026?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80").into(mainBinding.idIVBack)
                    }
                    else
                    {
                        Picasso.get().load("https://images.unsplash.com/photo-1488866022504-f2584929ca5f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1162&q=80").into(mainBinding.idIVBack)
                    }

                    var forecastObj: JSONObject= response.getJSONObject("forecast")
                    val forecast0: JSONObject = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                    val hourArray: JSONArray= forecast0.getJSONArray("hour")

                    for(i in 0..hourArray.length())
                    {
                        val hourObj: JSONObject = hourArray.getJSONObject(i)
                        val time: String= hourObj.getString("time")
                        val temper:String= hourObj.getString("temp_c")
                        val img: String= hourObj.getJSONObject("condition").getString("icon")
                        val wind: String= hourObj.getString("wind_kph")

                        weatherList.add(WeatherRVModel(time, temper,img,wind))
                    }
                    weatherAdapter.notifyDataSetChanged()

                }catch(e: JSONException)
                {
                    e.printStackTrace()
                }

            },
            {
                Toast.makeText(this@MainActivity,"Please enter valid city name", Toast.LENGTH_LONG).show()
                })

        requestQueue.add(jsonObjectRequest)
    }
}