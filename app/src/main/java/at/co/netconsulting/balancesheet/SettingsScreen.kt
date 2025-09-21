package at.co.netconsulting.balancesheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.co.netconsulting.balancesheet.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onSaveSettings: (String, String, String, String, String, String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Server Configuration Section
            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // IP Address
            SettingsField(
                label = stringResource(R.string.textViewIpAddress),
                value = uiState.ipAddress,
                onValueChange = viewModel::onIpAddressChanged,
                hint = "balancesheet.duckdns.org",
                keyboardType = KeyboardType.Text,
                isRequired = true,
                supportingText = "Server IP address or domain name",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Port
            SettingsField(
                label = stringResource(R.string.textViewPort),
                value = uiState.port,
                onValueChange = viewModel::onPortChanged,
                hint = "8080",
                keyboardType = KeyboardType.Number,
                isRequired = true,
                supportingText = "Server port number (typically 8080)",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Settings Section
            Text(
                text = "Personal Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Persons
            SettingsField(
                label = stringResource(R.string.textViewPerson),
                value = uiState.persons,
                onValueChange = viewModel::onPersonsChanged,
                hint = "Julia Bernd Max",
                keyboardType = KeyboardType.Text,
                supportingText = "Space-separated list of person names",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Food Budget
            SettingsField(
                label = stringResource(R.string.textViewMoneyLeftForFoodJustForPerson),
                value = uiState.foodBudget,
                onValueChange = viewModel::onFoodBudgetChanged,
                hint = "300.00",
                keyboardType = KeyboardType.Decimal,
                supportingText = "Monthly food budget amount",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Default Values Section
            Text(
                text = "Default Values",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Default Position
            SettingsField(
                label = stringResource(R.string.textViewDefaultPosition),
                value = uiState.defaultPosition,
                onValueChange = viewModel::onDefaultPositionChanged,
                hint = "Food",
                keyboardType = KeyboardType.Text,
                supportingText = "Default category for new entries",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Default Location
            SettingsField(
                label = stringResource(R.string.textViewDefaultLocation),
                value = uiState.defaultLocation,
                onValueChange = viewModel::onDefaultLocationChanged,
                hint = "Hollgasse_1_54",
                keyboardType = KeyboardType.Text,
                supportingText = "Default location for new entries",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Default Currency
            SettingsField(
                label = "Default Currency",
                value = uiState.defaultCurrency,
                onValueChange = viewModel::onDefaultCurrencyChanged,
                hint = "EUR",
                keyboardType = KeyboardType.Text,
                supportingText = "Currency code (EUR, USD, etc.)",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Custom Positions Management
            CustomValuesSection(
                title = "Manage Positions",
                values = uiState.customPositions,
                onAddValue = viewModel::addCustomPosition,
                onRemoveValue = viewModel::removeCustomPosition
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Locations Management
            CustomValuesSection(
                title = "Manage Locations",
                values = uiState.customLocations,
                onAddValue = viewModel::addCustomLocation,
                onRemoveValue = viewModel::removeCustomLocation
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    if (uiState.isChanged) {
                        onSaveSettings(
                            uiState.ipAddress,
                            uiState.port,
                            uiState.persons,
                            uiState.foodBudget,
                            uiState.defaultPosition,
                            uiState.defaultLocation,
                            uiState.defaultCurrency
                        )
                        viewModel.resetChangedFlag()
                    }
                },
                enabled = uiState.isChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Save Settings")
            }

            // Add some bottom padding to ensure the button is fully visible
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    supportingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val isError = isRequired && value.isBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            value.isNotEmpty() -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Label with optional required indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = leadingIcon,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Supporting text or error message
            if (supportingText != null || isError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        isError -> "This field is required"
                        supportingText != null -> supportingText
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CustomValuesSection(
    title: String,
    values: List<String>,
    onAddValue: (String) -> Unit,
    onRemoveValue: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var newValue by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Add new value section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        placeholder = { Text("Enter new $title") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newValue.isNotBlank()) {
                                onAddValue(newValue.trim())
                                newValue = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of custom values
                if (values.isNotEmpty()) {
                    Text(
                        text = "Custom $title:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    values.forEach { value ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = value,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onRemoveValue(value) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove $value"
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No custom $title added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    )
}