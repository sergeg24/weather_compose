package com.example.weathercompose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weathercompose.`object`.DialogManager
import com.example.weathercompose.data.WeatherModel
import com.example.weathercompose.screens.MainCard
import com.example.weathercompose.screens.TabLayout
import com.example.weathercompose.screens.dialogSearch
import com.example.weathercompose.ui.theme.WeatherComposeTheme
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject

const val API_KEY = "e06f13b7dd5e4f8ebf081520230801"

class MainActivity : ComponentActivity() {

    private lateinit var pLauncher: ActivityResultLauncher<String>
    open lateinit var daysList: MutableState<List<WeatherModel>>
    open lateinit var currentDay: MutableState<WeatherModel>
    open lateinit var stateLoading: MutableState<Boolean>
    open lateinit var stateDialog: MutableState<Boolean>
    open lateinit var stateErrors: MutableState<MutableList<String>>
    private var openDialog: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val isPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isPermission) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            WeatherComposeTheme {

                daysList = remember {
                    mutableStateOf(listOf())
                }
                currentDay = remember {
                    mutableStateOf(WeatherModel("", "", "0", "0", "", "0", "0", ""))
                }
                stateLoading = remember {
                    mutableStateOf(true)
                }
                stateDialog = remember {
                    mutableStateOf(false)
                }
                stateErrors = remember {
                    mutableStateOf(ArrayList())
                }

                if (stateDialog.value) {
                    dialogSearch(stateDialog, onSubmit = {
                        if (it.isNotEmpty()) {
                            getData(it)
                        }
                    })
                }

                Image(
                    painter = painterResource(id = R.drawable.weather),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.6f),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier.padding(5.dp)
                ) {
                    MainCard(currentDay, stateErrors, stateLoading, onClickSync = {
                        checkLocation()
                    }, onClickSearch = {
                        stateDialog.value = true
                    })
                    TabLayout(daysList, currentDay, stateLoading)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun checkLocation(context: Context = this) {
        if (!gpsEnabled(context)) {
            if (openDialog) return
            DialogManager.locationSettingsDialog(context, object : DialogManager.Listener {
                override fun onClick() {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
            openDialog = true
        } else {
            getLocation(context)
        }
    }

    private fun gpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(context: Context = this) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || !gpsEnabled(context)
        ) {
            return
        }

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
            .addOnCompleteListener {
                getData("${it.result.latitude},${it.result.longitude}", context)
            }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {

        }
    }

    /**
     * Получение API данных с сервера
     */
    private fun getData(
        city: String,
        context: Context = this,
    ) {
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY&q=$city&days=3&aqi=no&alerts=no&lang=ru"
        val queue = Volley.newRequestQueue(context)
        stateLoading.value = true
        val strRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val resp = String(response.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
                val list = getWeatherByDay(resp)
                currentDay.value = list[0]
                daysList.value = list
                stateLoading.value = false
            },
            {
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(strRequest)
    }
}

/**
 * Получение информации по дням
 */
private fun getWeatherByDay(response: String): List<WeatherModel> {

    if (response.isEmpty()) return listOf()

    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val current = mainObject.getJSONObject("current")
    val location = mainObject.getJSONObject("location")
    val city = location.getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        val day = item.getJSONObject("day")
        val dayCondition = day.getJSONObject("condition")
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                dayCondition.getString("text"),
                dayCondition.getString("icon"),
                day.getString("maxtemp_c"),
                day.getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }

    list[0] = list[0].copy(
        time = current.getString("last_updated"),
        currentTemp = current.getString("temp_c"),
        condition = current.getJSONObject("condition").getString("text"),
    )
    return list
}