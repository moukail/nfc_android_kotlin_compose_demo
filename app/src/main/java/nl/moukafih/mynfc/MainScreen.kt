package nl.moukafih.mynfc

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import nl.moukafih.mynfc.model.TagInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context, viewModel: MyViewModel){

    val techList by viewModel.techList.collectAsState()
    val serialNumber by viewModel.serialNumber.collectAsState()
    val atqa by viewModel.atqa.collectAsState()
    val sak by viewModel.sak.collectAsState()

    val readingStatus by viewModel.readingStatus.collectAsState() // Collect the status


    //val manufacturer by viewModel.manufacturer.collectAsState()
    //val product by viewModel.product.collectAsState()

    var tagList by remember { mutableStateOf<List<TagInfo>>(emptyList()) }

    val manufacturer = remember { mutableStateOf("Fetching...") }
    val product = remember { mutableStateOf("Fetching...") }

    LaunchedEffect(atqa) {
        Log.d("MainScreen", "LaunchedEffect")

        tagList = parseTagInfo(context)

        val matchedTag = findTagInfo(atqa, sak, tagList)

        manufacturer.value = matchedTag?.manufacturer ?: ""
        product.value = matchedTag?.product ?: ""

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My App Title") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Tech List: $techList")
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Serial Number: $serialNumber")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ATAQ: $atqa")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "SAK: $sak")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Manufacturer: ${manufacturer.value}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Product: ${product.value}")
                Spacer(modifier = Modifier.height(8.dp))

                // Show reading status
                Text(text = "Status: $readingStatus", color = Color.Gray)
                if (readingStatus == "Reading...") {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.Blue)
                }
            }
        }
    )
}

fun readJsonFile(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

fun parseTagInfo(context: Context): List<TagInfo> {
    val json = readJsonFile(context, "tags.json")
    return Gson().fromJson(json, object : TypeToken<List<TagInfo>>() {}.type)
}

fun findTagInfo(atqa: String, sak: String, tagList: List<TagInfo>): TagInfo? {
    return tagList.find { it.atqa.equals(atqa, true) && it.sak.equals(sak, true) }
}