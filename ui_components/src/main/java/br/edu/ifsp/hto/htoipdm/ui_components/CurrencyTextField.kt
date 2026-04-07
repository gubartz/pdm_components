package br.edu.ifsp.hto.htoipdm.ui_components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * A [Composable] text field specialized for currency input.
 *
 * This component stores the value internally as a digit-only string while displaying
 * it formatted as currency using a [CurrencyVisualTransformation].
 *
 * It ensures:
 * - Only numeric input is accepted
 * - Proper cursor positioning during edits
 * - Conversion between raw digits and [BigDecimal] currency values
 *
 * @param value The current monetary value represented as [BigDecimal].
 * @param onValueChange Callback invoked whenever the value changes,
 * providing the updated [BigDecimal].
 * @param modifier Optional [Modifier] for styling and layout.
 * @param locale The [Locale] used for currency formatting (default is system locale).
 * @param scale Number of decimal places (e.g., 2 for cents).
 * @param label Optional label composable displayed inside the text field.
 */
@Composable
fun CurrencyTextField(
    value: BigDecimal,
    onValueChange: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.getDefault(),
    scale: Int = 2,
    label: @Composable (() -> Unit)? = null
) {
    var internalDigits by remember {
        mutableStateOf(value.toDigits(scale))
    }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(internalDigits))
    }

    LaunchedEffect(value) {
        val newDigits = value.toDigits(scale)
        if (newDigits != internalDigits) {
            internalDigits = newDigits
            textFieldValue = TextFieldValue(
                text = newDigits,
                selection = TextRange(newDigits.length)
            )
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->

            val digitsOnly = newValue.text.filter { it.isDigit() }
            val safeDigits = digitsOnly.ifEmpty { "0" }

            val normalized = safeDigits.trimStart('0').ifEmpty { "0" }

            val newCursor = calculateNewCursorPosition(
                old = textFieldValue,
                new = newValue,
                filtered = normalized
            )

            internalDigits = normalized

            textFieldValue = TextFieldValue(
                text = normalized,
                selection = TextRange(newCursor.coerceAtLeast(1))
            )

            onValueChange(normalized.toBigDecimalCurrency(scale))
        },
        visualTransformation = CurrencyVisualTransformation(locale, scale),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        label = label,
        modifier = modifier
    )
}

/**
 * Calculates the new cursor position after text transformation.
 *
 * This function preserves cursor consistency when non-digit characters are removed
 * and the internal representation changes.
 *
 * It works by:
 * - Counting how many digits existed before the cursor in the old value
 * - Counting how many digits exist before the cursor in the new value
 * - Adjusting the cursor position based on the difference
 *
 * @param old Previous [TextFieldValue].
 * @param new New [TextFieldValue] after user input.
 * @param filtered The filtered string containing only valid digits.
 *
 * @return The corrected cursor position within the filtered text.
 */
fun calculateNewCursorPosition(
    old: TextFieldValue,
    new: TextFieldValue,
    filtered: String
): Int {
    val oldDigitsBeforeCursor = old.text
        .take(old.selection.start)
        .count { it.isDigit() }

    val newDigitsBeforeCursor = new.text
        .take(new.selection.start)
        .count { it.isDigit() }

    val diff = newDigitsBeforeCursor - oldDigitsBeforeCursor

    return (old.selection.start + diff)
        .coerceIn(0, filtered.length)
}

/**
 * Converts a digit-only string into a [BigDecimal] representing a currency value.
 *
 * The string is interpreted as an integer value and then scaled down
 * using [BigDecimal.movePointLeft].
 *
 * Example (scale = 2):
 * - "1234" -> 12.34
 * - "5" -> 0.05
 *
 * @param scale Number of decimal places.
 *
 * @return A [BigDecimal] representing the currency value.
 */
fun String.toBigDecimalCurrency(scale: Int = 2): BigDecimal {
    val digits = this.ifEmpty { "0" }
    return BigDecimal(digits).movePointLeft(scale)
}

/**
 * Converts a [BigDecimal] currency value into a digit-only string.
 *
 * The decimal point is shifted to the right according to the given scale
 * and the value is truncated (no rounding up).
 *
 * Example (scale = 2):
 * - 12.34 -> "1234"
 * - 0.05 -> "5"
 *
 * @param scale Number of decimal places.
 *
 * @return A string containing only digits representing the scaled value.
 */
fun BigDecimal.toDigits(scale: Int = 2): String {
    return this
        .movePointRight(scale)
        .setScale(0, RoundingMode.DOWN)
        .toPlainString()
}

/**
 * Formats a [BigDecimal] value as a localized currency string.
 *
 * This uses [NumberFormat.getCurrencyInstance] with the provided [Locale].
 *
 * Example:
 * - Locale.US -> "$12.34"
 * - Locale("pt", "BR") -> "R$ 12,34"
 *
 * @param locale The locale used for formatting (default is system locale).
 *
 * @return A properly formatted currency string.
 */
fun BigDecimal.formatCurrency(
    locale: Locale = Locale.getDefault()
): String {
    val formatter = NumberFormat.getCurrencyInstance(locale)
    return formatter.format(this)
}