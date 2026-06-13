package com.ryzix.regain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ryzix.regain.navigation.RegainNavGraph
import com.ryzix.regain.ui.theme.RegainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegainTheme {
                RegainNavGraph()
            }
        }
    }
}
