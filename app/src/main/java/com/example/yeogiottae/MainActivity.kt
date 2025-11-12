package com.example.yeogiottae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.yeogiottae.ui.HomeScreen
import com.example.yeogiottae.viewmodel.AccommodationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AccommodationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.state.collectAsState()
            Surface {
                HomeScreen(
                    state = uiState,
                    onRetry = viewModel::refresh,
                    onBookClick = viewModel::bookAccommodation
                )
            }
        }
    }
}
