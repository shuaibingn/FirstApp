package com.example.firstapp.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun ButtonTypesExample() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Button(onClick = { }) {
            Text("注册账号")  // 主要操作
        }

        ElevatedButton(onClick = { }) {
            Text("查看详情")  // 重要但非主要操作
        }

        FilledTonalButton(onClick = { }) {
            Text("添加到收藏")  // 次要功能
        }

        OutlinedButton(onClick = { }) {
            Text("稍后再说")  // 备选操作
        }

        TextButton(onClick = { }) {
            Text("跳过")  // 最低优先级操作
        }
    }
}
