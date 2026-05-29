package com.example.cardgame

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.cardgame.ui.theme.MyApplicationTheme


class App : Application() {
    val imageLoader by lazy {
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationResourceManager.init(this)
    }
}

val cardHeight = 88.dp
val cardWidth = 63.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel by viewModels<MainViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                Scaffold(
                    containerColor = Color.Cyan,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Player(state.players[0])
                    }
                    LaunchedEffect(0) {
                        viewModel.dealingCards()
                    }
                }
            }
        }
    }
}

@Composable
fun Card(
    card: Card,
    modifier: Modifier = Modifier
) {
    Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.height(cardHeight).width(cardWidth)
    ) {
        val imageLoader = (LocalContext.current.applicationContext as App).imageLoader
        AsyncImage(
            model = card.assetName,
            imageLoader = imageLoader,
            contentDescription = "Jack of Clubs",
        )
    }
}

@Composable
fun Player(
    playerData: PlayerData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(Color.Green),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = playerData.name,
            style = MaterialTheme.typography.titleMedium
        )

        Box {
            if (playerData.cards.isEmpty()) {
                Spacer(modifier = Modifier.height(cardHeight))
            } else {
                playerData.cards.forEachIndexed { index, card ->
                    Box(modifier = Modifier.padding(start = (index * 20).dp)) {
                        Card(card)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.poker_chip),
                tint = Color.Red,
                contentDescription = "Poker chip"
            )
            Text(
                text = playerData.chips.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}