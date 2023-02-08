package com.example.weathercompose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weathercompose.data.WeatherModel
import com.example.weathercompose.ui.theme.BlueLight

@Composable
fun Loading(text: String) {
    Text(text = text, fontSize = 25.sp, color = Color.White)
}

@Composable
fun Error(text: String) {
    Text(text = text, fontSize = 25.sp, color = Color.Red)
}

@Composable
fun MainList(list: List<WeatherModel>, currentDay: MutableState<WeatherModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(list) { _, item ->
            ListItem(item, currentDay)
        }
    }
}

@Composable
fun ListItem(item: WeatherModel, currentDay: MutableState<WeatherModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp)
            .clickable {
                if (item.hours.isNotEmpty())
                    currentDay.value = item
            },
        backgroundColor = BlueLight,
        elevation = 0.dp,
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Text(
                    text = item.time,
                    color = Color.White,
                    textDecoration = TextDecoration.Underline
                )
                Text(text = item.condition, color = Color.White, fontSize = 11.sp)
            }
            Text(
                text = if (item.currentTemp.isNotEmpty()) "${item.currentTemp.toFloat().toInt()}°С"
                else "${item.maxTemp.toFloat().toInt()}/${item.minTemp.toFloat().toInt()}°С",
                color = Color.White,
                style = TextStyle(fontSize = 25.sp)
            )
            AsyncImage(
                modifier = Modifier
                    .size(35.dp)
                    .padding(top = 3.dp, end = 8.dp),
                model = "https://${item.icon}",
                contentDescription = ""
            )
        }
    }
}

@Composable
fun dialogSearch(stateDialog: MutableState<Boolean>, onSubmit: (String) -> Unit) {
    val dialogText = remember {
        mutableStateOf("")
    }
    AlertDialog(onDismissRequest = {
        //функция запускаетя когда происходит нажатие за экраном
        stateDialog.value = false
    }, confirmButton = {
        TextButton(onClick = {
            onSubmit(dialogText.value)
            stateDialog.value = false
        }) {
            Text(text = "OK")
        }
    }, dismissButton = {
        TextButton(onClick = {
            stateDialog.value = false
        }) {
            Text(text = "Cancel")
        }
    }, title = {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Введите название города:")
            TextField(value = dialogText.value, onValueChange = {
                dialogText.value = it
            })
        }
    })
}