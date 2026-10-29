package com.munireport.app.core.designsystem.colors

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// MuniReport civic palette
//
// Muntinlupa blue is the primary action colour, red is reserved for alerts and
// destructive actions, yellow is a highlight/attention colour, and green is
// used only for resolved or approved states.
// ---------------------------------------------------------------------------

val MuniBlue50 = Color(0xFFEEF3FF)
val MuniBlue100 = Color(0xFFDCE7FF)
val MuniBlue200 = Color(0xFFB9CEFF)
val MuniBlue500 = Color(0xFF2E5FBE)
val MuniBlue600 = Color(0xFF244E9F)
val MuniBlue700 = Color(0xFF1B3B7A)
val MuniBlue800 = Color(0xFF142E60)
val MuniBlue900 = Color(0xFF0E234A)

val MuniRed50 = Color(0xFFFFEEF1)
val MuniRed100 = Color(0xFFFFD9E0)
val MuniRed600 = Color(0xFFC8102E)
val MuniRed700 = Color(0xFFA30C25)

val MuniYellow50 = Color(0xFFFFFAE5)
val MuniYellow100 = Color(0xFFFFF2BF)
val MuniYellow400 = Color(0xFFF7CE46)
val MuniYellow500 = Color(0xFFF5C518)
val MuniYellow700 = Color(0xFF8A6410)

val MuniGreen50 = Color(0xFFE9F7EF)
val MuniGreen100 = Color(0xFFD3EFDF)
val MuniGreen600 = Color(0xFF277A4C)
val MuniGreen700 = Color(0xFF1D5D3A)

val MuniNavy = Color(0xFF10213F)
val MuniNavySoft = Color(0xFF1A2747)

val MuniCanvas = Color(0xFFF4F6FA)
val MuniSurface = Color(0xFFFFFFFF)
val MuniSurfaceMuted = Color(0xFFF8F9FC)
val MuniBorder = Color(0xFFE1E6EF)
val MuniBorderStrong = Color(0xFFC8D1E0)
val MuniInk = Color(0xFF162033)
val MuniInkMuted = Color(0xFF5B6678)
val MuniInkFaint = Color(0xFF8A95A7)

// Screen-guide tokens. The guide uses a civic blue action colour, a warm cream
// notice, and a thin rose rule around the fields and sections that matter most.
val MuniActionBlue = Color(0xFF2B5AAA)
val MuniHeroBlue = Color(0xFF2A55A4)
val MuniHeroBlueDeep = Color(0xFF244B94)
val MuniGuideBorder = Color(0xFFD4828C)
val MuniGuideBorderStrong = Color(0xFFB84758)
val MuniFieldPink = Color(0xFFFDF2F4)
val MuniFieldPinkStrong = Color(0xFFFBE9EC)
val MuniCream = Color(0xFFFCEFD2)
val MuniCreamStrong = Color(0xFFF8E3B6)
val MuniWarmIcon = Color(0xFF7B5B22)
val MuniCardPink = Color(0xFFFCF4F5)
val MuniPillGray = Color(0xFFF1F3F7)
val MuniLineSoft = Color(0xFFE4E8F0)

// Dark surfaces and supporting tints.
val MuniDarkCanvas = Color(0xFF0C1322)
val MuniDarkSurface = Color(0xFF131D31)
val MuniDarkSurfaceMuted = Color(0xFF1A2740)
val MuniDarkBorder = Color(0xFF2B3A56)
val MuniDarkInk = Color(0xFFF2F5FB)
val MuniDarkInkMuted = Color(0xFFB5C0D4)

// Status colours. Each status also gets a container/tint and is paired with an
// icon or text label in the UI, so meaning never depends on colour alone.
val StatusPendingFg = Color(0xFF745100)
val StatusPendingBg = Color(0xFFFFF1C4)
val StatusReviewFg = Color(0xFF1F4D9E)
val StatusReviewBg = Color(0xFFE3ECFF)
val StatusProgressFg = Color(0xFF3B3F9E)
val StatusProgressBg = Color(0xFFE5E7FF)
val StatusResolvedFg = Color(0xFF1D6A41)
val StatusResolvedBg = Color(0xFFDDF3E6)
val StatusClosedFg = Color(0xFF596477)
val StatusClosedBg = Color(0xFFEDF0F5)
val StatusUrgentFg = Color(0xFF9B1028)
val StatusUrgentBg = Color(0xFFFFE0E5)

// Legacy aliases retained so existing screens continue to compile while the
// redesign migrates them to the civic palette above.
val CivicGreen900 = MuniBlue900
val CivicGreen800 = MuniBlue800
val CivicGreen700 = MuniBlue600
val CivicGreen600 = MuniBlue500
val CivicGreen200 = MuniBlue200
val CivicGreen100 = MuniBlue100
val CivicGreen50 = MuniBlue50

val Clay700 = MuniYellow700
val Clay500 = MuniYellow500
val Clay100 = MuniYellow100

val Sand50 = MuniCanvas
val Sand100 = MuniSurfaceMuted
val Sand200 = MuniBorder
val Sand300 = MuniBorderStrong

val Ink900 = MuniInk
val Ink700 = MuniInkMuted
val Ink500 = Color(0xFF6F7B8D)
val Ink300 = MuniInkFaint

val Night900 = MuniDarkCanvas
val Night800 = MuniDarkSurface
val Night700 = MuniDarkSurfaceMuted
val Night600 = MuniDarkBorder
val Night200 = MuniBlue200

val Amber600 = StatusPendingFg
val Blue600 = MuniBlue600
val Indigo600 = StatusProgressFg
val Green600 = MuniGreen600
val Red600 = MuniRed600

// Brand aliases used by the sign-in reference and the tricolour city stripe.
val MuniBlue = Color(0xFF244E9F)
val MuniRed = Color(0xFFC8102E)
val MuniYellow = Color(0xFFF5C518)

// Dedicated login palette. The sign-in reference keeps its own light shell.
val MuniLoginCanvas = Color(0xFFF4F5FA)
val MuniLoginSurface = Color(0xFFFFFFFF)
val MuniLoginFrameSurface = Color(0xFFFCFCFE)
val MuniLoginInk = Color(0xFF222333)
val MuniLoginInkMuted = Color(0xFF687083)
val MuniLoginCardBorder = Color(0xFFE7E8ED)
val MuniLoginErrorSurface = Color(0xFFFCEEF1)
val MuniLoginNoticeIcon = Color(0xFF7A5A1E)

val MuniNoticeYellow = Color(0xFFFFF0C7)
val MuniInputPink = Color(0xFFFBF1F3)
val MuniBrandPink = Color(0xFFF2E8ED)
