package com.example.cardgame

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.example.cardgame.ui.theme.MyApplicationTheme


enum class PlayerType {
    IT_ME,
    ACTIVE,
    NOT_ACTIVE
}

data class Player(
    val name: String,
    val type: PlayerType,
    val cards: List<Card> = emptyList(),
    val chips: Int = 0
)

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val randomCard = deckPokerWithJokers.random()
                    //val randomCard = deckPokerWithJokers[10]
                    Log.e(null, randomCard.assetName)
                    Box(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            border = BorderStroke(width = 1.dp, color = Color.Black),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(88.dp).width(63.dp)
                        ) {
                            val imageLoader = (LocalContext.current.applicationContext as App).imageLoader
                            AsyncImage(
                                model = randomCard.assetName,
                                imageLoader = imageLoader,
                                contentDescription = "Jack of Clubs",
                            )
                        }
                    }
                }
            }
        }
    }
}

data class CardData(val name: String)

@Composable
fun Player(
    name: String,
    cards: List<CardData>,
    score: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium
        )

        Box {
            cards.forEachIndexed { index, card ->
                Box(modifier = Modifier.padding(start = (index * 20).dp)) {
                    //Card(card)
                }
            }
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}