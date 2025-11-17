package com.example.yeogiottae.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yeogiottae.data.Accommodation
import com.example.yeogiottae.viewmodel.AccommodationUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AccommodationUiState,
    onRetry: () -> Unit,
    onBookClick: (Accommodation) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageEvents = remember { MutableSharedFlow<String>() }
    var showList by rememberSaveable { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { messageEvents.emit(it) }
    }

    LaunchedEffect(state.lastBooked) {
        state.lastBooked?.let { booked ->
            messageEvents.emit("${booked.name} 예약이 완료되었습니다!")
        }
    }

    LaunchedEffect(Unit) {
        messageEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let { coordinate ->
            val cameraPosition = CameraPosition.fromLatLngZoom(
                LatLng(coordinate.latitude, coordinate.longitude),
                15f
            )
            cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.currentLocation != null -> {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(
                        compassEnabled = false,
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false
                    )
                )
                CenterMarker(onClick = { showList = true })
            }

            state.isLoading -> LoadingIndicator()
            state.errorMessage != null -> ErrorState(state.errorMessage, onRetry)
            else -> EmptyState(onRetry)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isBooking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = "예약 진행중...")
                    }
                }
            }
        }
    }

    if (showList) {
        ModalBottomSheet(
            onDismissRequest = { showList = false },
            sheetState = sheetState
        ) {
            when {
                state.isLoading -> LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
                state.errorMessage != null -> ErrorState(state.errorMessage, onRetry)
                state.accommodations.isEmpty() -> EmptyState(onRetry)
                else -> AccommodationList(
                    accommodations = state.accommodations,
                    onBookClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CenterMarker(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .shadow(10.dp, CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "현재 위치 숙소 보기",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "주변 숙소를 찾지 못했어요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "인터넷 연결을 확인하거나 다시 시도해 주세요.",
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onRetry) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onRetry
        ) {
            Text(text = "다시 시도")
        }
    }
}

@Composable
private fun AccommodationList(
    accommodations: List<Accommodation>,
    onBookClick: (Accommodation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accommodations, key = { it.id }) { accommodation ->
            AccommodationCard(accommodation, onBookClick)
        }
    }
}

@Composable
private fun AccommodationCard(
    accommodation: Accommodation,
    onBookClick: (Accommodation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = accommodation.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = accommodation.address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = String.format("%.0f m", accommodation.distanceMeters),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "1박 ${formatCurrency(accommodation.pricePerNight)}원",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(onClick = { onBookClick(accommodation) }) {
                Text(text = "예약하기")
            }
        }
    }
}

private fun formatCurrency(amount: Int): String {
    return "%,d".format(amount)
}
