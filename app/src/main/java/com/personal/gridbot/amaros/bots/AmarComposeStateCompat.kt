package com.personal.gridbot.amaros.bots

import androidx.compose.runtime.MutableIntState

/** Compatibility bridge for legacy Bot-Lab screens that use mutableIntStateOf without importing it. */
fun mutableIntStateOf(initialValue: Int): MutableIntState =
    androidx.compose.runtime.mutableIntStateOf(initialValue)
