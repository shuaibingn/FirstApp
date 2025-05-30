package com.example.firstapp.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.model.LatLng


@Composable
fun BaiduMap() {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = {
            val baiduMap = it.map
            baiduMap.isMyLocationEnabled = true
            val center = LatLng(39.915, 116.404)
            val update = MapStatusUpdateFactory.newLatLngZoom(center, 15f)
            baiduMap.setMapStatus(update)
        }
    )
//    AndroidView(
//        factory = { context ->
//            MapView(context)
//        },
//        modifier = Modifier.fillMaxSize()
//    )
}