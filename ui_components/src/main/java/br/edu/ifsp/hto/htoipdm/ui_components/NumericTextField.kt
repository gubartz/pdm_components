package br.edu.ifsp.hto.htoipdm.ui_components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.text.DecimalFormatSymbols

/**
 * A text field specialized for numeric input with decimal support.
 *
 * This composable restricts user input to digits and a single decimal separator,
 * based on the current locale. It ensures that only valid numeric values are
 * propagated through [onValueChange], while still allowing intermediate typing states.
 *
 * Key behaviors:
 * - Accepts only digits (`0-9`) and one decimal separator.
 * - Uses the device's locale to determine the decimal separator (e.g., `,` or `.`).
 * - Prevents multiple decimal separators.
 * - Allows intermediate states such as:
 *   - Empty input
 *   - Trailing decimal separator (e.g., "12," while typing)
 * - Internally normalizes the value (replacing the locale separator with `.`)
 *   to validate numeric conversion using [String.toDoubleOrNull].
 *
 * @param modifier Optional [Modifier] for layout and styling.
 * @param value The current text value displayed in the field.
 * @param onValueChange Callback invoked when the input changes with a valid numeric value.
 * @param label Optional composable displayed as the label inside the [TextField].
 * This allows full customization (e.g., [Text], icons, or custom layouts).
 *
 * @sample
 * ```
 * var amount by remember { mutableStateOf("") }
 *
 * NumericTextField(
 *     value = amount,
 *     onValueChange = { amount = it },
 *     label = { Text("Valor") }
 * )
 * ```
 *
 * @sample
 * ```
 * NumericTextField(
 *     value = amount,
 *     onValueChange = { amount = it },
 *     label = {
 *         Row {
 *             Icon(Icons.Default.AttachMoney, contentDescription = null)
 *             Text("Valor")
 *         }
 *     }
 * )
 * ```
 *
 * @note This composable does not apply number formatting (e.g., grouping separators,
 * currency formatting). It only constrains and validates input.
 * Formatting should be handled externally if needed.
 *
 * @see TextField
 * @see KeyboardOptions
 * @see KeyboardType.Decimal
 */
@Composable
fun NumericTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: (@Composable () -> Unit)? = null,
) {
    val decimalSeparator = remember {
        DecimalFormatSymbols.getInstance().decimalSeparator
    }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == decimalSeparator }

            val result = buildString {
                var hasSeparator = false

                for (c in filtered) {
                    if (c.isDigit()) {
                        append(c)
                    } else if (c == decimalSeparator && !hasSeparator) {
                        append(c)
                        hasSeparator = true
                    }
                }
            }

            val normalized = result.replace(decimalSeparator, '.')
            3
            if (
                result.isEmpty() ||
                result.last() == decimalSeparator ||
                normalized.toDoubleOrNull() != null
            ) {
                onValueChange(result)
            }
        },
        label = label,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        modifier = modifier
    )
}