package com.ryzix.regain.ui.screens

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzix.regain.ui.theme.BackgroundDark
import com.ryzix.regain.ui.theme.CardDark
import com.ryzix.regain.ui.theme.DividerColor
import com.ryzix.regain.ui.theme.GreenActive
import com.ryzix.regain.ui.theme.KeypadButton
import com.ryzix.regain.ui.theme.RegainRed
import com.ryzix.regain.ui.theme.TextMuted
import com.ryzix.regain.ui.theme.TextPrimary
import com.ryzix.regain.ui.theme.TextSecondary
import com.ryzix.regain.viewmodel.DialerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactEntry(val name: String, val number: String)
data class CallEntry(val name: String, val number: String, val type: Int, val date: Long)

@Composable
fun DialerScreen(
    onBack: () -> Unit,
    vm: DialerViewModel = viewModel()
) {
    val dialInput by vm.dialInput.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    var callPermGranted by remember { mutableStateOf(false) }
    var contactsPermGranted by remember { mutableStateOf(false) }
    var callLogPermGranted by remember { mutableStateOf(false) }

    val multiPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        callPermGranted = perms[Manifest.permission.CALL_PHONE] == true
        contactsPermGranted = perms[Manifest.permission.READ_CONTACTS] == true
        callLogPermGranted = perms[Manifest.permission.READ_CALL_LOG] == true
    }

    LaunchedEffect(Unit) {
        multiPermLauncher.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            )
        )
    }

    val contacts by produceState<List<ContactEntry>>(emptyList(), contactsPermGranted) {
        if (contactsPermGranted) {
            value = withContext(Dispatchers.IO) { loadContacts(context.contentResolver) }
        }
    }

    val callLog by produceState<List<CallEntry>>(emptyList(), callLogPermGranted) {
        if (callLogPermGranted) {
            value = withContext(Dispatchers.IO) { loadCallLog(context.contentResolver) }
        }
    }

    Scaffold(containerColor = BackgroundDark) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BACK",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.clickable { onBack() }
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Regain Dialer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(Modifier.weight(1f))
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BackgroundDark,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RegainRed
                    )
                },
                divider = {}
            ) {
                listOf("Keypad", "Contacts", "History").forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = {
                            selectedTab = i
                            vm.setSearchQuery("")
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTab == i) RegainRed else TextSecondary
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> KeypadTab(
                    dialInput = dialInput,
                    onDigit = { vm.appendDigit(it) },
                    onDelete = { vm.deleteDigit() },
                    onCall = {
                        if (dialInput.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$dialInput"))
                            context.startActivity(intent)
                        }
                    }
                )
                1 -> ContactsTab(
                    contacts = contacts,
                    searchQuery = searchQuery,
                    onSearchChange = { vm.setSearchQuery(it) },
                    onCall = { number ->
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                )
                2 -> HistoryTab(
                    callLog = callLog,
                    searchQuery = searchQuery,
                    onSearchChange = { vm.setSearchQuery(it) },
                    onCall = { number ->
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun KeypadTab(
    dialInput: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = dialInput.ifEmpty { " " },
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            ),
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
        keys.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    KeypadButton(digit = digit, onClick = { onDigit(digit) })
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GreenActive)
                    .clickable { onCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(24.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = RegainRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp).navigationBarsPadding())
    }
}

@Composable
private fun KeypadButton(digit: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(KeypadButton)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        placeholder = {
            Text("Search name or number...", color = TextMuted, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
        },
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DividerColor,
            unfocusedBorderColor = DividerColor,
            cursorColor = RegainRed,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark
        ),
        singleLine = true
    )
}

@Composable
private fun ContactsTab(
    contacts: List<ContactEntry>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCall: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SearchField(query = searchQuery, onQueryChange = onSearchChange)
        val filtered = contacts.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.number.contains(searchQuery)
        }
        LazyColumn {
            items(filtered) { contact ->
                ContactRow(name = contact.name, number = contact.number, onClick = { onCall(contact.number) })
            }
        }
    }
}

@Composable
private fun HistoryTab(
    callLog: List<CallEntry>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCall: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SearchField(query = searchQuery, onQueryChange = onSearchChange)
        val filtered = callLog.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.number.contains(searchQuery)
        }
        LazyColumn {
            items(filtered) { entry ->
                ContactRow(name = entry.name.ifEmpty { "Unknown Number" }, number = entry.number, onClick = { onCall(entry.number) })
            }
        }
    }
}

@Composable
private fun ContactRow(name: String, number: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = number,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor.copy(alpha = 0.3f))
    )
}

private fun loadContacts(resolver: ContentResolver): List<ContactEntry> {
    val list = mutableListOf<ContactEntry>()
    val cursor = resolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    ) ?: return list
    cursor.use {
        val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            list.add(ContactEntry(it.getString(nameIdx) ?: "", it.getString(numIdx) ?: ""))
        }
    }
    return list
}

private fun loadCallLog(resolver: ContentResolver): List<CallEntry> {
    val list = mutableListOf<CallEntry>()
    val cursor = resolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE
        ),
        null, null,
        CallLog.Calls.DATE + " DESC"
    ) ?: return list
    cursor.use {
        val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
        while (it.moveToNext()) {
            list.add(
                CallEntry(
                    name = it.getString(nameIdx) ?: "",
                    number = it.getString(numIdx) ?: "",
                    type = it.getInt(typeIdx),
                    date = it.getLong(dateIdx)
                )
            )
        }
    }
    return list
}
