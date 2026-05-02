package com.wulala.demo01.data

sealed interface CharSubState {
    data object Idle : CharSubState
    data object Discovering : CharSubState
    data object Subscribing : CharSubState
    data object Ready : CharSubState
    data class Error(val e: Throwable) : CharSubState
}