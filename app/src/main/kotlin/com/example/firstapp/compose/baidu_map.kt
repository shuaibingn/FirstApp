package com.example.firstapp.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng


@Composable
fun BaiduMapView(
    modifier: Modifier = Modifier,
    center: LatLng = LatLng(39.915, 116.404), // 默认北京天安门
    zoom: Float = 15f,
    enableLocation: Boolean = true,
    onMapReady: ((BaiduMap) -> Unit)? = null // 可选：地图加载完成后回调
) {
    val context = LocalContext.current

    // 持久化 MapView 实例，避免重复创建
    val mapView = rememberMapViewWithLifecycle(context)

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val baiduMap = view.map

            if (enableLocation) {
                baiduMap.isMyLocationEnabled = true

                // 示例：伪造一个定位点（实际使用时应结合定位 SDK）
                val locationData = MyLocationData.Builder()
                    .latitude(center.latitude)
                    .longitude(center.longitude)
                    .build()
                baiduMap.setMyLocationData(locationData)
                baiduMap.setMyLocationConfiguration(
                    MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL,
                        true,
                        null
                    )
                )
            }

            // 设置地图初始状态
            val update = MapStatusUpdateFactory.newLatLngZoom(center, zoom)
            baiduMap.setMapStatus(update)

            onMapReady?.invoke(baiduMap)
        }
    )
}

@Composable
fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        MapView(context)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}


//@Composable
//fun BaiduMap() {
//    val context = LocalContext.current
//    val mapView = remember {
//        MapView(context)
//    }
//
//    AndroidView(
//        factory = { mapView },
//        modifier = Modifier.fillMaxSize(),
//        update = {
//            val baiduMap = it.map
//            baiduMap.isMyLocationEnabled = true
//            val center = LatLng(39.915, 116.404)
//            val update = MapStatusUpdateFactory.newLatLngZoom(center, 15f)
//            baiduMap.setMapStatus(update)
//        }
//    )
//    AndroidView(
//        factory = { context ->
//            MapView(context)
//        },
//        modifier = Modifier.fillMaxSize()
//    )
//}