package br.edu.ifsp.hto.htoipdm.ui_components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * A [VisualTransformation] that formats a digit-only input string as a localized currency.
 *
 * This transformation:
 * - Interprets the input as a numeric value with an implicit decimal scale
 * - Formats it using [NumberFormat.getCurrencyInstance]
 * - Maintains correct cursor behavior via a custom [OffsetMapping]
 *
 * The input is expected to contain only digits (e.g., "1234"), which will be interpreted
 * as a scaled currency value:
 *
 * Example (scale = 2):
 * - "1234" -> "R$ 12,34" (pt-BR)
 * - "5" -> "R$ 0,05"
 *
 * Non-digit characters are not expected and may lead to fallback behavior.
 *
 * @param locale The [Locale] used for currency formatting (default is system locale).
 * @param scale Number of decimal places used to interpret the input (e.g., 2 for cents).
 */
class CurrencyVisualTransformation(
    private val locale: Locale = Locale.getDefault(),
    private val scale: Int = 2
) : VisualTransformation {

    private val formatter = NumberFormat.getCurrencyInstance(locale)

    override fun filter(text: AnnotatedString): TransformedText {

        val raw = text.text.ifEmpty { "0" }

        val number = try {
            BigDecimal(raw).movePointLeft(scale)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

        val formatted = formatter.format(number)

        val digitIndexes = formatted.mapIndexedNotNull { i, c ->
            if (c.isDigit()) i else null
        }

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset > digitIndexes.size) return formatted.length
                return digitIndexes[offset - 1] + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                val digitsBefore = digitIndexes.count { it < offset }
                return digitsBefore.coerceAtMost(raw.length)
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}