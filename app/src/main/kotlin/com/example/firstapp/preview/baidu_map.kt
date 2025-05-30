package com.example.firstapp.preview

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstapp.compose.BaiduLocationTracker
import com.example.firstapp.compose.BaiduMapConfig
import com.example.firstapp.compose.BaiduMapDisplay
import com.example.firstapp.compose.LocationState

@Composable
fun LocationMapScreen() {
    val context = LocalContext.current

    // 定位状态
    var currentState by remember { mutableStateOf<LocationState>(LocationState.Loading) }

    // 记录当前位置
    var latitude by remember { mutableStateOf(39.915) }  // 默认北京坐标
    var longitude by remember { mutableStateOf(116.404) }

    // 是否显示位置详情
    var showLocationDetails by remember { mutableStateOf(true) }

    // 界面布局
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 使用封装的定位跟踪器组件
        BaiduLocationTracker(
            minTimeMillis = 3000,
            onStateChanged = { state ->
                currentState = state
                if (state is LocationState.Success) {
                    latitude = state.location.latitude
                    longitude = state.location.longitude

                    // 显示成功定位提示（仅在首次定位成功时）
                    if (currentState is LocationState.Loading) {
                        Toast.makeText(context, "定位成功", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) { state ->
            // 根据定位状态渲染UI
            when (state) {
                is LocationState.Loading -> {
                    // 加载状态UI
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "正在获取位置...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is LocationState.Success -> {
                    // 定位成功，显示地图
                    // 2. 使用封装的地图组件显示位置
                    BaiduMapDisplay(
                        latitude = state.location.latitude,
                        longitude = state.location.longitude,
                        config = BaiduMapConfig(
                            zoomLevel = 18f,
                            enableZoomControls = true,
                            trackUserLocation = true
                        ),
                        onMapReady = { /* 地图就绪后可以进行额外设置 */ },
                        onMapClick = { latLng ->
                            // 点击地图时收起位置信息面板
                            showLocationDetails = false
                        }
                    )
                }

                is LocationState.Error -> {
                    // 错误状态UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "错误",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "定位失败：${state.errorMessage}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            currentState = LocationState.Loading
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "重试",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重试")
                        }
                    }
                }

                is LocationState.PermissionDenied -> {
                    // 权限拒绝状态UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "权限拒绝",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "需要定位权限才能使用此功能",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "请在系统设置中开启本应用的定位权限",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = {
                            // 重新尝试请求权限的逻辑
                            currentState = LocationState.Loading
                        }) {
                            Text("重新请求权限")
                        }
                    }
                }
            }
        }

        // 3. 浮动按钮 - 重新定位
        if (currentState is LocationState.Success) {
            FloatingActionButton(
                onClick = {
                    currentState = LocationState.Loading
                    showLocationDetails = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "重新定位",
                    tint = Color.White
                )
            }
        }

        // 4. 位置信息面板
        AnimatedVisibility(
            visible = currentState is LocationState.Success && showLocationDetails,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            if (currentState is LocationState.Success) {
                val locationData = (currentState as LocationState.Success).location
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.95f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "当前位置信息",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { showLocationDetails = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )

                        locationData.address?.let {
                            LocationInfoRow("地址", it)
                        }

                        LocationInfoRow(
                            "经纬度",
                            "${String.format("%.6f", locationData.latitude)}, ${String.format("%.6f", locationData.longitude)}"
                        )

                        LocationInfoRow("精度", "${locationData.accuracy}米")

                        locationData.city?.let {
                            LocationInfoRow("城市", it)
                        }

                        LocationInfoRow("时间", locationData.time)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

//@Composable
//fun MapScreen() {
//    BaiduMapView(
//        modifier = Modifier.fillMaxSize(),
//        center = LatLng(31.2304, 121.4737), // 上海
//        zoom = 14f,
//        enableLocation = true,
//    )
//}

//@Composable
//fun BaiduMapWithLocationScreen() {
//    var currentState by remember { mutableStateOf<LocationState>(LocationState.Loading) }
//
//    var latitude by remember { mutableDoubleStateOf(39.915) }  // 默认北京坐标
//    var longitude by remember { mutableDoubleStateOf(116.404) }
//
//    Column(Modifier.fillMaxSize()) {
//        BaiduMapView(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth(),
//            center = LatLng(lat ?: 31.2304, lng ?: 121.4737), // 默认上海
//            zoom = 14f,
//            enableLocation = true,
//        )
//
//        Text(
//            text = "地址: $addr",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}
