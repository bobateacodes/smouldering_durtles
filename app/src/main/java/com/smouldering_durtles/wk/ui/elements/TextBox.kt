package com.smouldering_durtles.wk.ui.elements

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smouldering_durtles.wk.ui.theme.Flash
import com.smouldering_durtles.wk.ui.theme.Ghost
import com.smouldering_durtles.wk.ui.theme.Input
import com.smouldering_durtles.wk.ui.theme.Primary
import com.smouldering_durtles.wk.ui.theme.PrimaryFocus
import com.smouldering_durtles.wk.ui.theme.Snow
import com.smouldering_durtles.wk.ui.theme.notoSans


@Composable
fun TextInput(
    placeholder: String,
//    clearFocus: Boolean = false,
//    onFocusClear: () -> Unit = {},
    modifier: Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember {
        FocusRequester()
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
        },
        placeholder = { Text(
            text = placeholder,
            fontFamily = notoSans,
            letterSpacing = 0.96.sp) },
        shape = RoundedCornerShape(10.dp),
        colors = run {
            TextFieldDefaults.colors(
                focusedContainerColor = Snow,
                unfocusedContainerColor = Snow,
                disabledContainerColor = Flash,
                focusedIndicatorColor = PrimaryFocus,
                unfocusedIndicatorColor = Ghost,
                focusedPlaceholderColor = Input,
                unfocusedPlaceholderColor = Input,
            )
        },
        interactionSource = interactionSource,
        modifier = Modifier
            .height(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = true,
                spotColor = if (isFocused) Primary else Input
            )
            .focusRequester(focusRequester)
            .then(modifier)
    )
}