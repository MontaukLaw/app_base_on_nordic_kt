package com.wulala.demo01.data

data class HrFilterParams(
    val medianWindow: Int = 5,
    val hpHz: Float = 0.7f,
    val lpHz: Float = 6f,
    val fsHz: Float = 100f,
    val historySize: Int = 300
)