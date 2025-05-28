package com.bhaswat.freshsave.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bhaswat.freshsave.model.InventoryItem
import com.bhaswat.freshsave.utils.ITEM_ADDED_REFRESH_KEY_HOME
import com.bhaswat.freshsave.utils.PREDEFINED_CATEGORIES
import com.bhaswat.freshsave.utils.PREDEFINED_UNITS
import com.bhaswat.freshsave.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    navController: NavController ,
    homeViewModel: HomeViewModel , // Directly pass the ViewModel instance
    itemId: String? = null
) {
    val isEditMode = itemId != null
    val screenTitle = if (isEditMode) "Edit Item" else "Add New Item"
    val buttonText = if (isEditMode) "Update Item" else "Save Item"

    var itemName by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Date?>(null) }
    var existingItemId by remember { mutableStateOf<String?>(null) } // To store the ID of the item being edited

    var itemNameError by remember { mutableStateOf<String?>(null) }
    var itemCategoryError by remember { mutableStateOf<String?>(null) }
    var itemQuantityError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current // Context for Toast

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val existingCategories by homeViewModel.itemCategories.collectAsState() // From HomeViewModel
    val allCategorySuggestions = remember(existingCategories) {
        (PREDEFINED_CATEGORIES + existingCategories).distinct().sorted()
    }
    // Observe the item to edit from ViewModel
    val itemToEdit by homeViewModel.itemToEdit.collectAsState()

    // Load item data if in edit mode
    LaunchedEffect(itemId) {
        if (isEditMode) {
            Log.d("AddItemScreen" , "Edit mode: Loading item with ID - $itemId")
            homeViewModel.loadItemForEditing(itemId)
        } else {
            Log.d("AddItemScreen" , "Add mode: Clearing item to edit.")
            homeViewModel.clearItemToEdit() // Clear if not in edit mode or itemId is null
        }
    }

    // Populate fields when itemToEdit is available
    LaunchedEffect(itemToEdit , isEditMode) {
        if (isEditMode && itemToEdit != null) {
            Log.d("AddItemScreen" , "Populating fields for item: ${itemToEdit!!.name}")
            existingItemId = itemToEdit!!.id // Store the original ID
            itemName = itemToEdit!!.name
            itemCategory = itemToEdit!!.category
            itemQuantity = itemToEdit!!.quantity.toString() // Convert Double to String
            itemUnit = itemToEdit!!.unit ?: ""
            expiryDate = itemToEdit!!.expiryDate
        } else if (!isEditMode) {
            // Reset fields if switching from edit mode to add mode (e.g. due to nav args changing)
            // or if itemToEdit becomes null unexpectedly in edit mode.
            Log.d(
                "AddItemScreen" ,
                "Not in edit mode or itemToEdit is null, ensuring fields are clear for add."
            )
            existingItemId = null
            itemName = ""
            itemCategory = ""
            itemQuantity = "1.0" // Default for add mode
            itemUnit = ""
            expiryDate = null
        }
    }


    // Clear itemToEdit when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("AddItemScreen" , "Disposing AddItemScreen, clearing itemToEdit from ViewModel.")
            homeViewModel.clearItemToEdit()
        }
    }


    fun validateInputs(): Boolean {
        itemNameError = if (itemName.isBlank()) "Item name cannot be empty" else null
        itemCategoryError = if (itemCategory.isBlank()) "Category cannot be empty" else null

        val quantityDouble = itemQuantity.toDoubleOrNull()
        itemQuantityError = if (itemQuantity.isBlank()) {
            "Quantity cannot be empty"
        } else if (quantityDouble == null) {
            "Invalid quantity format"
        } else if (quantityDouble <= 0) {
            "Quantity must be positive"
        } else {
            null
        }
        return itemNameError == null && itemCategoryError == null && itemQuantityError == null
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing slightly
        ) {
            OutlinedTextField(
                value = itemName ,
                onValueChange = {
                    itemName = it
                    itemNameError = null
                } ,
                label = { Text("Item Name") } ,
                modifier = Modifier.fillMaxWidth() ,
                singleLine = true ,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) ,
                isError = itemNameError != null ,
                supportingText = {
                    if (itemNameError != null) {
                        Text(itemNameError!! , color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded ,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded } ,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = itemCategory ,
                    onValueChange = {
                        itemCategory = it // Allow typing to filter or add new
                        itemCategoryError = null
                        // Optionally, filter suggestions here if desired:
                        // categoryDropdownExpanded = true // Keep open while typing
                    } ,
                    label = { Text("Category") } ,
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable) // Essential for ExposedDropdownMenuBox
                        .fillMaxWidth() ,
                    readOnly = false , // Set true to disable typing and force selection
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) } ,
                    singleLine = true ,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words) ,
                    isError = itemCategoryError != null ,
                    supportingText = {
                        if (itemCategoryError != null) {
                            Text(itemCategoryError!! , color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded ,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    allCategorySuggestions.filter { // Filter suggestions based on typed text
                        it.contains(itemCategory , ignoreCase = true) || itemCategory.isBlank()
                    }.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) } ,
                            onClick = {
                                itemCategory = selectionOption
                                categoryDropdownExpanded = false
                                itemCategoryError = null
                            }
                        )
                    }
                    if (allCategorySuggestions.none {
                            it.equals(
                                itemCategory ,
                                ignoreCase = true
                            )
                        } && itemCategory.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text("Add \"$itemCategory\" as new category") } ,
                            onClick = {
                                // itemCategory is already set by typing
                                categoryDropdownExpanded = false
                                itemCategoryError = null
                            }
                        )
                    }
                }
            }


            Row(Modifier.fillMaxWidth() , verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = itemQuantity ,
                    onValueChange = {
                        itemQuantity = it
                        itemQuantityError = null
                    } ,
                    label = { Text("Quantity") } ,
                    modifier = Modifier.weight(1f) ,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal) ,
                    singleLine = true ,
                    isError = itemQuantityError != null ,
                    supportingText = {
                        if (itemQuantityError != null) {
                            Text(itemQuantityError!! , color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Unit Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded ,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded } ,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = itemUnit ,
                            onValueChange = { itemUnit = it } , // Allow typing for custom units
                            label = { Text("Unit") } ,
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryEditable) // Essential
                                .fillMaxWidth() ,
                            readOnly = false , // Set true to disable typing
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) } ,
                            singleLine = true ,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded ,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            PREDEFINED_UNITS.filter { // Filter suggestions
                                it.contains(itemUnit , ignoreCase = true) || itemUnit.isBlank()
                            }.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) } ,
                                    onClick = {
                                        itemUnit = selectionOption
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                            // Optionally, allow adding a new typed unit explicitly
                            if (PREDEFINED_UNITS.none {
                                    it.equals(
                                        itemUnit ,
                                        ignoreCase = true
                                    )
                                } && itemUnit.isNotBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Use \"$itemUnit\"") } ,
                                    onClick = {
                                        // itemUnit is already set by typing
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                val initialSelectedMillis = expiryDate?.time ?: Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY , 0); set(Calendar.MINUTE , 0); set(
                    Calendar.SECOND ,
                    0
                ); set(Calendar.MILLISECOND , 0)
                }.timeInMillis

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialSelectedMillis ,
                    yearRange = (Calendar.getInstance().get(Calendar.YEAR))..(Calendar.getInstance()
                        .get(Calendar.YEAR) + 10)
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false } ,
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let {
                                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                cal.timeInMillis = it
                                cal.set(Calendar.HOUR_OF_DAY , 0); cal.set(
                                Calendar.MINUTE ,
                                0
                            ); cal.set(Calendar.SECOND , 0); cal.set(Calendar.MILLISECOND , 0)
                                expiryDate = cal.time
                            }
                        }) { Text("OK") }
                    } ,
                    dismissButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                        }) { Text("Cancel") }
                    }
                ) { DatePicker(state = datePickerState) }
            }
            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (validateInputs()) {
                        val currentItemName = itemName.trim()
                        val currentItemCategory = itemCategory.trim()
                        val currentQuantity =
                            itemQuantity.toDoubleOrNull() ?: 1.0 // Already validated
                        val currentUnit = itemUnit.trim().ifBlank { null }
                        if (isEditMode && existingItemId != null) {
                            val updatedItem = InventoryItem(
                                id = existingItemId!! , // Use the stored original ID
                                name = currentItemName ,
                                category = currentItemCategory ,
                                quantity = currentQuantity ,
                                unit = currentUnit ,
                                expiryDate = expiryDate ,
                                isFavorite = itemToEdit?.isFavorite
                                    ?: false // Preserve favorite status
                            )
                            homeViewModel.updateItem(updatedItem)
                            Log.d(
                                "AddItemScreen" ,
                                "Update button clicked for item ID: $existingItemId"
                            )
                        } else {

                            //val quantityDouble = itemQuantity.toDoubleOrNull() ?: 1.0
                            val newItem = InventoryItem(
                                name = currentItemName ,
                                category = currentItemCategory ,
                                quantity = currentQuantity ,
                                unit = currentUnit ,
                                expiryDate = expiryDate
                            )
                            homeViewModel.addItem(newItem)
                            Log.d("AddItemScreen" , "Save button clicked for new item.")
                        }
                        //Signal Homescreen to refresh
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            ITEM_ADDED_REFRESH_KEY_HOME ,
                            true
                        )
                        navController.popBackStack()
                    } else {
                        Toast.makeText(
                            context ,
                            "Please correct the errors above." ,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } ,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Item") }
        }
    }
}