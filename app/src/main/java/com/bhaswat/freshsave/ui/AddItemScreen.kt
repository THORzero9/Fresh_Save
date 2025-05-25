package com.bhaswat.freshsave.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.bhaswat.freshsave.utils.ITEM_ADDED_REFRESH_KEY_HOME

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = koinViewModel() // Using Koin to get the ViewModel
) {
    var itemName by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1.0") }
    var itemUnit by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Date?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Item") } ,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack , contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize() ,
            horizontalAlignment = Alignment.CenterHorizontally ,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = itemName ,
                onValueChange = { itemName = it } ,
                label = { Text("Item Name") } ,
                modifier = Modifier.fillMaxWidth() ,
                singleLine = true ,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            OutlinedTextField(
                value = itemCategory ,
                onValueChange = { itemCategory = it } ,
                label = { Text("Category (e.g., Fruits, Dairy)") } ,
                modifier = Modifier.fillMaxWidth() ,
                singleLine = true ,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Row(Modifier.fillMaxWidth() , verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = itemQuantity ,
                    onValueChange = { itemQuantity = it } ,
                    label = { Text("Quantity") } ,
                    modifier = Modifier.weight(1f) ,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal) ,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = itemUnit ,
                    onValueChange = { itemUnit = it } ,
                    label = { Text("Unit (e.g., kg, pcs)") } ,
                    modifier = Modifier.weight(1f) ,
                    singleLine = true ,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }

            OutlinedTextField(
                value = expiryDate?.let {
                    SimpleDateFormat(
                        "dd/MM/yyyy" ,
                        Locale.getDefault()
                    ).format(it)
                } ?: "Select Expiry Date" ,
                onValueChange = { /* Read-only, changed by dialog */ } ,
                label = { Text("Expiry Date") } ,
                modifier = Modifier.fillMaxWidth() ,
                readOnly = true ,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange , contentDescription = "Select Date")
                    }
                }
            )

            if (showDatePicker) {
                val calendar = Calendar.getInstance()
                expiryDate?.let { calendar.time = it }

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = calendar.timeInMillis ,
                    yearRange = (Calendar.getInstance().get(Calendar.YEAR))..(Calendar.getInstance()
                        .get(Calendar.YEAR) + 10)
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false } ,
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let {
                                expiryDate = Date(it)
                            }
                        }) {
                            Text("OK")
                        }
                    } ,
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState) // This is androidx.compose.material3.DatePicker
                }
            }
                Spacer(Modifier.weight(1f)) // Push button to bottom

                Button(
                    onClick = {
                        val quantityDouble = itemQuantity.toDoubleOrNull() ?: 1.0
                        if (itemName.isNotBlank() && itemCategory.isNotBlank()) {
                            val newItem = InventoryItem(
                                name = itemName,
                                category = itemCategory,
                                quantity = quantityDouble,
                                unit = itemUnit.ifBlank { null },
                                expiryDate = expiryDate
                            )
                            homeViewModel.addItem(newItem)
                            navController.previousBackStackEntry?.savedStateHandle?.set(ITEM_ADDED_REFRESH_KEY_HOME, true)
                            navController.popBackStack()
                        } else {
                            // TODO: Show error message for required fields
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Item")
                }
        }
    }
}