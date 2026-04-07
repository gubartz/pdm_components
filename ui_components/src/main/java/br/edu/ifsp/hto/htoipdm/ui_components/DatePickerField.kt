package br.edu.ifsp.hto.htoipdm.ui_components

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * A read-only date input field that opens a [DatePickerDialog] when clicked,
 * allowing the user to select a date.
 *
 * This composable displays the selected date formatted according to the
 * device's locale, always enforcing leading zeros for day and month.
 *
 * Internally, it uses a disabled [OutlinedTextField] to present the value
 * and a Material 3 [DatePickerDialog] for date selection.
 *
 * @param label The label displayed inside the text field.
 * @param value The currently selected date. If null, the field is shown empty.
 * @param onValueChange Callback triggered when the user selects a new date.
 * The selected [LocalDate] is returned.
 * @param modifier Optional [Modifier] for styling and layout customization.
 * @param maxDate Optional maximum selectable date. If provided, users will
 * only be able to select dates up to (and including) this value.
 *
 * @sample
 * ```
 * var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
 *
 * DataPickerField(
 *     label = "Data de Nascimento",
 *     value = selectedDate,
 *     onValueChange = { selectedDate = it },
 *     maxDate = LocalDate.now()
 * )
 * ```
 *
 * @see DatePickerDialog
 * @see DatePicker
 * @see OutlinedTextField
 */
@Composable
fun DataPickerField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    maxDate: LocalDate? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyyyy")
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli(),
        selectableDates = maxDate?.let {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= maxDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }
            }
        } ?: DatePickerDefaults.AllDates
    )

    Box(
        modifier = modifier
            .clickable { showDialog = true }
    ) {

        OutlinedTextField(
            value = value?.let {
                sdf.format(
                    java.util.Date.from(
                        it.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                    )
                )
            } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onValueChange(date)
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}