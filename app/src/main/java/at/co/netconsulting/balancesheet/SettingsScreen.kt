package at.co.netconsulting.balancesheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
        },
        floatingActionButton = {
            // In Material 3, we need to handle the disabled state manually
            val containerColor = if (uiState.isChanged)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant

            val contentColor = if (uiState.isChanged)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)

            FloatingActionButton(
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
                containerColor = containerColor,
                contentColor = contentColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Save"
                )
            }
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
            // IP Address
            SettingsField(
                label = stringResource(R.string.textViewIpAddress),
                value = uiState.ipAddress,
                onValueChange = viewModel::onIpAddressChanged,
                hint = "IP address",
                keyboardType = KeyboardType.Text
            )

            // Port
            SettingsField(
                label = stringResource(R.string.textViewPort),
                value = uiState.port,
                onValueChange = viewModel::onPortChanged,
                hint = "8080",
                keyboardType = KeyboardType.Number
            )

            // Persons
            SettingsField(
                label = stringResource(R.string.textViewPerson),
                value = uiState.persons,
                onValueChange = viewModel::onPersonsChanged,
                hint = "Person1 Person2",
                keyboardType = KeyboardType.Text
            )

            // Food Budget
            SettingsField(
                label = stringResource(R.string.textViewMoneyLeftForFoodJustForPerson),
                value = uiState.foodBudget,
                onValueChange = viewModel::onFoodBudgetChanged,
                hint = "How much money for food/month",
                keyboardType = KeyboardType.Decimal
            )

            // Default Position
            SettingsField(
                label = stringResource(R.string.textViewDefaultPosition),
                value = uiState.defaultPosition,
                onValueChange = viewModel::onDefaultPositionChanged,
                hint = "Position",
                keyboardType = KeyboardType.Text
            )

            // Default Location
            SettingsField(
                label = stringResource(R.string.textViewDefaultLocation),
                value = uiState.defaultLocation,
                onValueChange = viewModel::onDefaultLocationChanged,
                hint = "Location",
                keyboardType = KeyboardType.Text
            )

            // Default Currency
            SettingsField(
                label = "Default Currency",
                value = uiState.defaultCurrency,
                onValueChange = viewModel::onDefaultCurrencyChanged,
                hint = "EUR",
                keyboardType = KeyboardType.Text
            )
        }
    }
}

@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(hint) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}