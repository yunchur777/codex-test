package com.example.yeogiottae.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yeogiottae.data.Accommodation
import com.example.yeogiottae.viewmodel.AccommodationUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    state: AccommodationUiState,
    onRetry: () -> Unit,
    onBookClick: (Accommodation) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageEvents = remember { MutableSharedFlow<String>() }

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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingIndicator()
            state.accommodations.isEmpty() && state.errorMessage == null -> EmptyState(onRetry)
            state.accommodations.isNotEmpty() -> AccommodationList(
                accommodations = state.accommodations,
                onBookClick = onBookClick
            )
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
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
private fun AccommodationList(
    accommodations: List<Accommodation>,
    onBookClick: (Accommodation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
