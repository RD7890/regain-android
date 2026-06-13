package com.ryzix.regain.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ryzix.regain.R

val RegainFont = FontFamily(
    Font(R.font.regain_bold, FontWeight.Normal),
    Font(R.font.regain_bold, FontWeight.Medium),
    Font(R.font.regain_bold, FontWeight.SemiBold),
    Font(R.font.regain_bold, FontWeight.Bold),
    Font(R.font.regain_bold, FontWeight.ExtraBold),
    Font(R.font.regain_bold, FontWeight.Black),
    Font(R.font.regain_bold, FontWeight.Light),
    Font(R.font.regain_bold, FontWeight.Thin),
)

val RegainTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1).sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = RegainFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    )
)
