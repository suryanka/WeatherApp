package com.ghosh.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ghosh.weatherapp.databinding.WeatherRvItemBinding
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class WeatherAdapter(val context: Context, val weatherList: ArrayList<WeatherRVModel>):
    RecyclerView.Adapter<WeatherAdapter.weatherViewHolder>(){

    inner class weatherViewHolder(val adapterBinding: WeatherRvItemBinding ) : RecyclerView.ViewHolder(adapterBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): weatherViewHolder {
        val binding = WeatherRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return weatherViewHolder(binding)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: weatherViewHolder, position: Int) {
        holder.adapterBinding.idTvTemperature.setText(weatherList[position].temperature+"°C")
        Picasso.get().load("http:"+weatherList[position].icon).into(holder.adapterBinding.idTVCond)
        holder.adapterBinding.idTvWindSpeed.setText(weatherList[position].windspeed+"Km/hr")

        val input: SimpleDateFormat= SimpleDateFormat("yyyy-MM-dd hh:mm")
        val output: SimpleDateFormat= SimpleDateFormat("hh:mm aa")

        try{
            val t: Date  = input . parse (weatherList[position].time) as Date
            holder.adapterBinding.idTvTime.setText(output.format(t))
        }catch(e: ParseException)
        {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }
}