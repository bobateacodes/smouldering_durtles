package com.smouldering_durtles.wk.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smouldering_durtles.wk.ui.theme.Charcoal
import com.smouldering_durtles.wk.ui.theme.ChinaRose
import com.smouldering_durtles.wk.ui.theme.Flash
import com.smouldering_durtles.wk.ui.theme.Primary
import com.smouldering_durtles.wk.ui.theme.PrimaryDark
import com.smouldering_durtles.wk.ui.theme.Snow
import com.smouldering_durtles.wk.ui.theme.imprima


@Composable
fun GradientButton(text: String, modifier: Modifier, onClick: () -> Unit = { }) {
    val gradient = Brush.horizontalGradient(listOf(PrimaryDark, ChinaRose, Primary))
    Button(
        modifier = Modifier
            .height(48.dp)
            .then(modifier),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = { onClick() },
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .fillMaxWidth()
                .fillMaxHeight()
                .background(gradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Snow,
                fontSize = 16.sp,
                fontFamily = imprima,
                textAlign = TextAlign.Center,
                letterSpacing = 0.575.sp,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier, onClick: () -> Unit = { }) {
    Button(
        modifier = Modifier
            .height(48.dp)
            .then(modifier),
        colors = ButtonDefaults.buttonColors(Flash),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = { onClick() },
    ) {
        Text(
            text = text,
            color = Charcoal,
            fontSize = 16.sp,
            fontFamily = imprima,
            textAlign = TextAlign.Center,
            letterSpacing = 0.575.sp,
        )
    }
}

@Composable
fun TransparentButton(text: String, onClick: () -> Unit, modifier: Modifier) {
    TextButton(
        colors = ButtonDefaults.buttonColors(
            contentColor = Primary,
            containerColor = Color.Transparent,
        ),
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.then(modifier)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = imprima,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontSynthesis = FontSynthesis.Weight,
                letterSpacing = 0.575.sp
            )
        )
    }
}

@Composable
fun GradientIconButton(vector: ImageVector, onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(PrimaryDark, ChinaRose, Primary))
    IconButton( onClick = onClick ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(gradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = vector,
                contentDescription = "Continue",
                tint = Snow,
            )
        }
    }
}
