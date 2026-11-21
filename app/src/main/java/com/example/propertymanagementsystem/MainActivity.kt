package com.example.propertymanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.propertymanagementsystem.ui.theme.PropertyManagementSystemTheme
import com.example.propertymanagementsystem.ui.viewmodel.PropertyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PropertyManagementSystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PropertyDashboardScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDashboardScreen(
    viewModel: PropertyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val propertyCount by viewModel.propertyCountText.observeAsState("Total Properties: 0")
    val occupancySummary by viewModel.occupancySummaryText.observeAsState("Occupied: 0 | Vacant: 0")
    val totalRent by viewModel.totalMonthlyRentText.observeAsState("Total Monthly Rent: KSh 0.00")
    val allProperties by viewModel.allPropertiesText.observeAsState("No properties registered yet. Tap 'Add Property' to begin.")

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Property Management Portfolio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val sampleNames = listOf("Sunset Heights", "Green Valley", "Royal Apartments", "Metro Plaza", "Hilltop Villas")
                    val sampleLocations = listOf("Nairobi", "Mombasa", "Kisumu", "Nakuru", "Eldoret")
                    val sampleTypes = listOf("Residential", "Commercial")
                    val sampleRents = listOf("25000", "40000", "60000", "15000", "80000")
                    val samplePhones = listOf("0712345678", "0722334455", "0733445566", "0744556677", "0755667788")

                    val randomIndex = (0 until sampleNames.size).random()
                    val result = viewModel.validateAndAddProperty(
                        name = sampleNames[randomIndex],
                        location = sampleLocations.random(),
                        type = sampleTypes.random(),
                        rentInput = sampleRents.random(),
                        phone = samplePhones.random(),
                        isOccupied = (0..1).random() == 1
                    )
                    feedbackMessage = result.second
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Portfolio Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Count", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = propertyCount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Occupancy", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = occupancySummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Financial Overview", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = totalRent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            feedbackMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { feedbackMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Registered Properties",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = allProperties,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
