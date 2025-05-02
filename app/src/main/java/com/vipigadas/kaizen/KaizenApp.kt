package com.vipigadas.kaizen

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class with Hilt integration.
 * Required for Hilt dependency injection to work.
 */
@HiltAndroidApp
class KaizenApp : Application()