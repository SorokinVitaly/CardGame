package com.example.cardgame

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder


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
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            MainScreen(state)
        }
    }

    @Composable
    fun MainScreen(state: ScreenState) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.ShowToast -> {
                        Toast.makeText(
                            context,
                            event.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        Surface(
            color = colorResource(R.color.backGround),
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(12f)
                ) {
                    Bank(
                        bankChips = state.bankChips,
                        modifier = Modifier.constrainAs(createRef()) { centerTo(parent) }
                    )
                    state.players.forEachIndexed { i, player ->
                        Player(
                            playerData = player,
                            isCardsOpen = if (i == 0) true else state.isCardsOpen,
                            modifier = Modifier.constrainAs(
                                createRef(),
                                playerConstraint(i)
                            )
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ActionBar(state)
                }
            }
        }
    }

    @Composable
    fun ActionBar(state: ScreenState) {
        if (state.isActionAvailable) {
            if (state.actionsAvailable.isNotEmpty()) {
                state.actionsAvailable.forEach { action ->
                    AppButton(action.name) { viewModel.onAction(action) }
                }
            } else {
                if (state.isDealAvailable) {
                    AppButton("Deal next", viewModel::onDialNext)
                }
                if (state.isResetAvailable) {
                    AppButton("Reset game", viewModel::onResetGame)
                }
            }
        } else {
            CircularProgressIndicator(
                color = Color.Yellow,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun AppButton(text: String, onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(2.dp, Color.Yellow),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.Yellow
        )
    }
}

@Composable
fun ChipIcon() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.poker_chip),
        tint = Color.Red,
        contentDescription = "Poker chip"
    )
}

@Composable
fun Card(
    card: Card,
    isCardsOpen: Boolean,
    modifier: Modifier = Modifier
) {
    val assetName = if (isCardsOpen) card.faceAssetName else card.backAssetName
    Card(
        border = BorderStroke(width = 1.dp, color = Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .height(cardHeight)
            .width(cardWidth)
    ) {
        val imageLoader = (LocalContext.current.applicationContext as App).imageLoader
        AsyncImage(
            model = assetName,
            imageLoader = imageLoader,
            contentDescription = "Card",
        )
    }
}

@Composable
fun Player(
    playerData: PlayerData,
    isCardsOpen: Boolean,
    modifier: Modifier = Modifier
) {
    if (!playerData.isActive) {
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = playerData.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Box {
            playerData.cards.forEachIndexed { index, card ->
                Box(modifier = Modifier.padding(start = (index * 20).dp)) {
                    Card(card, isCardsOpen)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ChipIcon()
            Text(
                text = playerData.chips.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }

        Text(
            text = playerData.footerText,
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray
        )
    }
}

@Composable
fun Bank(
    bankChips: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Bank",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChipIcon()
            Text(
                text = bankChips.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

private fun playerConstraint(index: Int): ConstrainScope.() -> Unit = {
    when (index) {
        0 -> {
            centerHorizontallyTo(parent)
            bottom.linkTo(parent.bottom, margin = 5.dp)
        }

        1 -> {
            centerVerticallyTo(parent)
            absoluteLeft.linkTo(parent.absoluteLeft, margin = 5.dp)
        }

        2 -> {
            centerHorizontallyTo(parent)
            top.linkTo(parent.top, margin = 5.dp)
        }

        3 -> {
            centerVerticallyTo(parent)
            absoluteRight.linkTo(parent.absoluteRight, margin = 5.dp)
        }

        else -> throw IllegalArgumentException("Invalid player index: $index")
    }
}

val cardHeight = 88.dp
val cardWidth = 63.dp