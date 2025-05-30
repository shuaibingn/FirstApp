package com.example.firstapp.compose

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * 百度定位结果数据类
 */
data class BaiduLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val address: String?,
    val city: String?,
    val district: String?,
    val time: String,
    val errorCode: Int = 0,
    val errorMsg: String? = null,
    val isSuccess: Boolean = errorCode == 0
)

/**
 * 定位状态数据类
 */
sealed class LocationState {
    object Loading : LocationState()
    data class Success(val location: BaiduLocationData) : LocationState()
    data class Error(val errorCode: Int, val errorMessage: String) : LocationState()
    object PermissionDenied : LocationState()
}

/**
 * 封装的持续定位组件, 处理权限请求和返回定位信息
 *
 * @param minTimeMillis 最小定位更新时间间隔(毫秒)
 * @param onStateChanged 定位状态变化回调
 * @param content 子组件内容，接收当前定位状态作为参数
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BaiduLocationTracker(
    minTimeMillis: Long = 2000,
    onStateChanged: (LocationState) -> Unit = {},
    content: @Composable (LocationState) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // 定义所需权限
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 请求权限
    val permissionsState = rememberMultiplePermissionsState(permissions)

    // 定位状态
    var locationState by remember { mutableStateOf<LocationState>(LocationState.Loading) }

    // 请求权限监听
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 判断权限状态
    val hasLocationPermission = permissionsState.permissions.all { it.status.isGranted }

    // 更新状态并通知回调
    fun updateState(newState: LocationState) {
        locationState = newState
        onStateChanged(newState)
    }

    // 创建和监听定位客户端
    if (hasLocationPermission) {
        val locationClient = remember {
            try {
                createLocationClient(context, minTimeMillis) { bdLocation ->
                    if (bdLocation.locType == BDLocation.TypeGpsLocation
                        || bdLocation.locType == BDLocation.TypeNetWorkLocation
                        || bdLocation.locType == BDLocation.TypeOffLineLocation) {

                        val locationData = BaiduLocationData(
                            latitude = bdLocation.latitude,
                            longitude = bdLocation.longitude,
                            accuracy = bdLocation.radius,
                            address = bdLocation.addrStr,
                            city = bdLocation.city,
                            district = bdLocation.district,
                            time = bdLocation.time,
                            errorCode = 0,
                            errorMsg = null,
                            isSuccess = true
                        )
                        updateState(LocationState.Success(locationData))
                    } else {
                        // 定位失败
                        updateState(LocationState.Error(
                            bdLocation.locType,
                            bdLocation.locTypeDescription ?: "Unknown error"
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("BaiduLocationTracker", "初始化定位客户端失败", e)
                updateState(LocationState.Error(-1, e.message ?: "初始化定位客户端失败"))
                null
            }
        }

        // 管理定位客户端生命周期
        DisposableEffect(locationClient) {
            locationClient?.start()

            onDispose {
                try {
                    locationClient?.stop()
                } catch (e: Exception) {
                    Log.e("BaiduLocationTracker", "停止定位客户端失败", e)
                }
            }
        }
    } else {
        // 权限被拒绝
        updateState(LocationState.PermissionDenied)
    }

    // 渲染内容
    content(locationState)
}

/**
 * 创建百度定位客户端
 */
private fun createLocationClient(
    context: Context,
    minTimeMillis: Long,
    callback: (BDLocation) -> Unit
): LocationClient? {
    try {
        LocationClient.setAgreePrivacy(true)
        val client = LocationClient(context.applicationContext)

        // 配置定位选项
        val option = LocationClientOption().apply {
            // 设置高精度定位模式
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            // 是否需要地址信息
//            isNeedAddress = true
            setIsNeedAddress(true)
            // 打开GPS
            openGps = true
//            isOpenGps = true
            // 设置定位时间间隔
            setScanSpan(minTimeMillis.toInt())
            // 设置坐标系类型
            setCoorType("bd09ll")
            // 设置产品线名称，用于记录日志
            prodName = "BaiduLocationSDK"
            // 设置需要详细的POI信息
            isNeedNewVersionRgc = true
            // 设置需要设备方向
            setNeedDeviceDirect(true)
//            isNeedDeviceDirect = true
            // 设置定位请求超时时间
            isLocationNotify = true
            // 设置是否当GPS有效时按照1S/1次频率输出GPS结果
            isLocationNotify = true
        }

        // 设置配置
        client.locOption = option

        // 注册定位监听器
        client.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                location?.let { callback(it) }
            }
        })

        return client
    } catch (e: Exception) {
        Log.e("BaiduLocationTracker", "创建定位客户端失败", e)
        return null
    }
}