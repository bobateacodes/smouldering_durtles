package com.smouldering_durtles.wk.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smouldering_durtles.wk.ui.theme.Cloud
import com.smouldering_durtles.wk.ui.theme.Dark
import com.smouldering_durtles.wk.ui.theme.Primary
import com.smouldering_durtles.wk.ui.theme.Snow
import com.smouldering_durtles.wk.R
import com.smouldering_durtles.wk.ui.theme.imprima
import com.smouldering_durtles.wk.ui.theme.notoSans
import androidx.compose.ui.text.TextStyle
import com.smouldering_durtles.wk.ui.elements.GradientButton
import com.smouldering_durtles.wk.ui.elements.GradientIconButton
import com.smouldering_durtles.wk.ui.elements.SecondaryButton
import com.smouldering_durtles.wk.ui.elements.TextInput
import com.smouldering_durtles.wk.ui.elements.TransparentButton
import com.smouldering_durtles.wk.ui.theme.Ghost
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun OnboardingScreen () {
    val header = painterResource(id = R.drawable.onboarding)
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Box {
        var clearFocus by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .clickable { clearFocus = true },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4F)
                        .scale(1.1F)
                ) {
                    Image(
                        painter = header,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(0.dp, (-24).dp)
                    )
                }
                LargeTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    title = {},
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
        ) {}
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Snow,
            ),
            shape = RoundedCornerShape(32.dp,32.dp,0.dp,0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65F)
                .align(BottomCenter)
        ) {
           OnBoardingScreens()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreens() {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            pageCount = 3,
            state = pagerState
        ) { page ->
            when (page) {
                0 -> FirstPage()
                1 -> SecondPage()
                2 -> ThirdPage()
            }
        }
        Row(
            Modifier
                .padding(24.dp, 24.dp)
                .height(50.dp)
                .fillMaxWidth()
                .align(BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < 2) {
                TransparentButton(
                    text = "Skip",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    modifier = Modifier.width(48.dp))
            } else {
                TransparentButton(
                    text = "Back",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    modifier = Modifier.width(48.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Primary else Ghost
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
            if (pagerState.currentPage < 2) {
                GradientIconButton(
                    vector = Icons.Filled.ArrowForward,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    })
            } else { Spacer(modifier = Modifier.width(48.dp)) }
        }
    }
}

@Composable
fun FirstPage() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Welcome!",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = imprima,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight,
                letterSpacing = 0.96.sp,
                color = Dark,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(0.dp, 32.dp, 0.dp, 24.dp)
                .fillMaxWidth()
        )
        LazyColumn(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(32.dp)
        ) {
            item {
                Text(
                    text = "Welcome to Smouldering Durtles v1.0.7!\n\n" +
                            "Smouldering Durtles is an app for WaniKani, the kanji learning service created by Tofugu. \n\n" +
                            "To learn more about WaniKani, tap the 'About WaniKani' button below, otherwise swipe left or tap the arrow button to proceed with the next step in the setup process.",
                    fontSize = 16.sp,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    color = Cloud,
                    textAlign = TextAlign.Center,
                )
            }
            //val aboutWK = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wanikani.com/")) }
            item {
                val localUriHandler = LocalUriHandler.current
                SecondaryButton(
                    text = "About WaniKani",
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        localUriHandler.openUri("https://www.tofugu.com/japanese-learning-resources-database/wanikani/")
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SecondPage() {
    Surface ( modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Let's get setup!",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = imprima,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight,
                letterSpacing = 0.96.sp,
                color = Dark,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(0.dp, 32.dp, 0.dp, 24.dp)
                .fillMaxWidth()
        )
        LazyColumn(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(32.dp)
        ) {
            item {
                Text(
                    text = "In order to use this app, you must have an active WaniKani account and a valid API token.\n\n" +
                            "To create a token, go to the ‘Settings’ page within WaniKani. Click ‘Generate a new token’ and when prompted, give the token all permissions.",
                    fontSize = 16.sp,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    color = Cloud,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                val localUriHandler = LocalUriHandler.current
                SecondaryButton(
                    text = "Go to API Settings",
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        localUriHandler.openUri("https://www.wanikani.com/settings/personal_access_tokens")
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ThirdPage() {
    Surface ( modifier = Modifier.fillMaxSize() ) {
        Text(
            text = "Enter API Token",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = imprima,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight,
                letterSpacing = 0.96.sp,
                color = Dark,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(0.dp, 32.dp, 0.dp, 0.dp)
                .fillMaxWidth()
        )
        LazyColumn(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(32.dp)
        ) {
            item {
                Text(
                    text = "In order to use this app, you must have an active WaniKani account and a valid API token.\n\n" +
                            "After creating a new token, copy it and enter it in the input below.",
                    fontSize = 16.sp,
                    fontFamily = notoSans,
                    fontWeight = FontWeight.Normal,
                    color = Cloud,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                TextInput(
                    placeholder = "x0x0x0x0-x0x0-x0x0-x0x0-x0x0x0x0x0x0",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                GradientButton(
                    text = "Finish",
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

