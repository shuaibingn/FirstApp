package com.example.firstapp.compose

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng

/**
 * 百度地图配置数据类
 */
data class BaiduMapConfig(
    val zoomLevel: Float = 19f,
    val enableZoomControls: Boolean = true,
    val enableScaleControl: Boolean = true,
    val enableRotateGestures: Boolean = true,
    val enableScrollGestures: Boolean = true,
    val enableOverlookingGestures: Boolean = true,
    val markerIcon: Int? = null,
    val trackUserLocation: Boolean = true,
)

/**
 * 封装的百度地图显示组件
 *
 * @param latitude 纬度
 * @param longitude 经度
 * @param config 地图配置
 * @param onMapReady 地图就绪回调
 * @param onMapLoaded 地图加载完成回调
 * @param onMapClick 地图点击回调
 * @param markerContent 自定义标记内容
 */
@Composable
fun BaiduMapDisplay(
    latitude: Double,
    longitude: Double,
    config: BaiduMapConfig = BaiduMapConfig(),
    onMapReady: (BaiduMap) -> Unit = {},
    onMapLoaded: () -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    markerContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 确保百度地图SDK只初始化一次
    DisposableEffect(Unit) {
        try {
            SDKInitializer.setAgreePrivacy(context, true)
            SDKInitializer.initialize(context.applicationContext)
            SDKInitializer.setCoordType(CoordType.BD09LL)
        } catch (e: Exception) {
            Log.e("BaiduMapDisplay", "初始化百度地图SDK失败", e)
        }

        onDispose {}
    }

    // 创建地图视图
    val mapView = remember {
        MapView(context).apply {
            showZoomControls(config.enableZoomControls)
        }
    }

    // 管理地图生命周期
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(context)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 地图操作
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            val map = view.map

            // 配置地图
            map.apply {
                // 设置地图类型
                mapType = BaiduMap.MAP_TYPE_NORMAL

                // 启用/禁用手势
//                settings.apply {
//                    isZoomGesturesEnabled = config.enableZoomControls
//                    isScrollGesturesEnabled = config.enableScrollGestures
//                    isOverlookingGesturesEnabled = config.enableOverlookingGestures
//                    isRotateGesturesEnabled = config.enableRotateGestures
//                }

                // 设置缩放级别
                setMapStatus(MapStatusUpdateFactory.zoomTo(config.zoomLevel))

                // 设置地图加载完成监听
                setOnMapLoadedCallback {
                    onMapLoaded()
                }

                // 设置地图点击监听
//                setOnMapClickListener { latLng ->
//                    onMapClick(latLng)
//                }
//                setOnMapClickListener(onMapClick = onMapClick())}
            }

            // 创建位置点
            val position = LatLng(latitude, longitude)

            // 移动到当前位置
            val builder = MapStatus.Builder()
            builder.target(position).zoom(config.zoomLevel)
            map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))

            // 清除所有覆盖物
            map.clear()

            // 添加标记点
            val markerOptions = MarkerOptions().apply {
                position(position)
                animateType(MarkerOptions.MarkerAnimateType.drop)

                // 使用自定义图标或默认图标
                config.markerIcon?.let { iconResource ->
                    val bitmap = BitmapFactory.decodeResource(context.resources, iconResource)
                    val bdBitmap = BitmapDescriptorFactory.fromBitmap(bitmap)
                    icon(bdBitmap)
                } ?: run {
                    icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation))
                }
            }

            // 添加标记到地图上
            map.addOverlay(markerOptions)

            // 回调地图对象
            onMapReady(map)
        }
    )

    // 渲染自定义标记内容
    markerContent?.let {
        Box(modifier = Modifier.fillMaxSize()) {
            it()
        }
    }
}

// MapView扩展函数用于生命周期管理
private fun MapView.onCreate(context: Context) {
    // 创建时需要执行的代码
}

private fun MapView.onStart() {
    // 启动时需要执行的代码
}

private fun MapView.onResume() {
    this.onResume()
}

private fun MapView.onPause() {
    this.onPause()
}

private fun MapView.onStop() {
    // 停止时需要执行的代码
}

private fun MapView.onDestroy() {
    this.onDestroy()
}