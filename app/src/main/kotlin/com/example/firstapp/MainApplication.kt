package com.example.firstapp

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 必须调用，否则地图无法使用
        SDKInitializer.setAgreePrivacy(this, true)

        // 初始化百度地图 SDK
        SDKInitializer.initialize(this)

        // 设置坐标类型：BD09LL（百度经纬度）或 GCJ02（国测局）
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}