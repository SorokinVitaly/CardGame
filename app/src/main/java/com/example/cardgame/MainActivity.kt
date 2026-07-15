package com.example.cardgame

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
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
                        .weight(1f)
                ) {
                    Bank(
                        bankChips = state.bankChips,
                        modifier = Modifier.constrainAs(createRef()) { centerTo(parent) }
                    )

                    val refs = state.players.indices.map { createRef() }
                    val portrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
                    if (portrait) {
                        createVerticalChain(
                            refs[2], refs[1],
                            chainStyle = ChainStyle.Packed(bias = 0.45f)
                        )
                        createVerticalChain(
                            refs[4], refs[5],
                            chainStyle = ChainStyle.Packed(bias = 0.45f)
                        )
                    } else {
                        createHorizontalChain(
                            refs[1], horizontalSpacer(), refs[0],
                            chainStyle = ChainStyle.Packed
                        )
                        createHorizontalChain(
                            refs[3], horizontalSpacer(), refs[4],
                            chainStyle = ChainStyle.Packed
                        )
                    }

                    state.players.forEachIndexed { index, player ->
                        val isCardsOpen = when {
                            index == 0 -> true
                            !player.isInGame -> false
                            else -> state.isCardsOpen
                        }
                        fun onCardClick(card: Card) {
                            if (state.isDrawEnabled && index == 0) {
                                viewModel.onCardClick(card)
                            }
                        }
                        Player(
                            playerData = player,
                            isCardsOpen = isCardsOpen,
                            onCardClick = ::onCardClick,
                            modifier = Modifier.constrainAs(
                                refs[index],
                                playerConstraint(index, portrait)
                            )
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
                    AppButton("Deal next", viewModel::onDealNext)
                }
                if (state.isResetAvailable) {
                    AppButton("Reset game", viewModel::onResetGame)
                }
            }
        } else {
            CircularProgressIndicator(
                color = Color.Yellow,
                modifier = Modifier.size(40.dp)
            )
        }
    }

    private fun playerConstraint(index: Int, portrait: Boolean): ConstrainScope.() -> Unit = {
        val margin = 2.dp
        if (portrait) {
            when (index) {
                0 -> {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom, margin = margin)
                }
                1, 2 -> {
                    absoluteLeft.linkTo(parent.absoluteLeft, margin = margin)
                }
                3 -> {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top, margin = margin)
                }
                4, 5 -> {
                    absoluteRight.linkTo(parent.absoluteRight, margin = margin)
                }
                else -> throw IllegalArgumentException("Invalid player index: $index")
            }
        } else {
            when (index) {
                0, 1 -> {
                    bottom.linkTo(parent.bottom, margin = margin)
                }
                2 -> {
                    centerVerticallyTo(parent)
                    absoluteLeft.linkTo(parent.absoluteLeft, margin = margin)
                }
                3, 4 -> {
                    top.linkTo(parent.top, margin = margin)
                }
                5 -> {
                    centerVerticallyTo(parent)
                    absoluteRight.linkTo(parent.absoluteRight, margin = margin)
                }
                else -> throw IllegalArgumentException("Invalid player index: $index")
            }
        }
    }
}