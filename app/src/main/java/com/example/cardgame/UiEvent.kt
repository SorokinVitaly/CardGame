package com.example.cardgame

interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
}