package ru.kotlix.skinshowcase.designsystem.format

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun usdTwoDecimalsFormatter(): DecimalFormat {
    val f = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    f.roundingMode = RoundingMode.HALF_UP
    return f
}

/** Цена скина в USD: `$1,234.56` или `—` если нет. */
fun formatSkinPriceUsd(price: Double?): String {
    if (price == null) return "—"
    return "\$${usdTwoDecimalsFormatter().format(price)}"
}

/** То же для известного значения (фильтры, подписи диапазона). */
fun formatSkinPriceUsdAmount(usd: Double): String =
    "\$${usdTwoDecimalsFormatter().format(usd)}"
