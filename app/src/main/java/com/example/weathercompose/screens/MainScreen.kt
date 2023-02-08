package com.example.weathercompose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weathercompose.R
import com.example.weathercompose.data.WeatherModel
import com.example.weathercompose.ui.theme.BlueLight
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(
    currentDay: MutableState<WeatherModel>,
    stateErrors: MutableState<MutableList<String>>,
    stateLoading: MutableState<Boolean>,
    onClickSync: () -> Unit,
    onClickSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(bottom = 5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f),
            backgroundColor = BlueLight,
            elevation = 1.dp,
            shape = RoundedCornerShape(10.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time,
                        style = TextStyle(fontSize = 15.sp),
                        color = Color.White
                    )
                    AsyncImage(
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 3.dp, end = 8.dp),
                        model = "https:${currentDay.value.icon}",
                        contentDescription = ""
                    )
                }
                Text(
                    text = currentDay.value.city,
                    style = TextStyle(fontSize = 55.sp),
                    color = Color.White
                )
                if (stateErrors.value.isNotEmpty()) {
                    for (error in stateErrors.value) {
                        Error(error)
                    }
                } else if (stateLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (currentDay.value.currentTemp.isNotEmpty()) "${
                            currentDay.value.currentTemp.toFloat().toInt()
                        }°С" else "${
                            currentDay.value.maxTemp.toFloat().toInt()
                        }°С/${currentDay.value.minTemp.toFloat().toInt()}°С",
                        style = TextStyle(fontSize = (if (currentDay.value.currentTemp.isNotEmpty()) 65 else 55).sp),
                        color = Color.White
                    )
                }
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            onClickSearch.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }

                    if (currentDay.value.currentTemp.isNotEmpty()) {
                        Text(
                            text = "${
                                currentDay.value.maxTemp.toFloat().toInt()
                            }°С/${currentDay.value.minTemp.toFloat().toInt()}°С",
                            style = TextStyle(fontSize = 16.sp),
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            onClickSync.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>,
    stateLoading: MutableState<Boolean>
) {

    val tabList = listOf("ЧАСЫ", "ДНИ")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .alpha(0.7f)
    ) {
        TabRow(
            selectedTabIndex = 0,
            indicator = { pos ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, pos)
                )
            },
            backgroundColor = BlueLight,
            contentColor = Color.White
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = text)
                    }
                )
            }
        }

        if (stateLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = BlueLight
            )
        }

        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }

            MainList(list, currentDay)
        }
    }
}

/**
 * Получение информации по часам
 */
private fun getWeatherByHours(hours: String): List<WeatherModel> {

    if (hours.isEmpty()) return listOf()

    val list = ArrayList<WeatherModel>()
    val hoursArray = JSONArray(hours)

    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        val dayCondition = item.getJSONObject("condition")
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString(),
                dayCondition.getString("text"),
                dayCondition.getString("icon"),
                "0",
                "0",
                ""
            )
        )
    }

    return list;
}