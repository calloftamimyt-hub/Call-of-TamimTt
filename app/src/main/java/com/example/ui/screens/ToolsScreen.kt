package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class AppTool(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val tagline: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onBack: (() -> Unit)? = null) {
    var selectedTool by remember { mutableStateOf<AppTool?>(null) }
    
    val toolsList = listOf(
        AppTool("World Clock", Icons.Filled.Language, Color(0xFFFF9100), "Real-time world timezones & smooth visual calendar"),
        AppTool("Calculator", Icons.Filled.Calculate, Color(0xFFE91E63), "Modern math solver with iOS curved circular keys"),
        AppTool("Password Gen", Icons.Filled.Password, Color(0xFF9C27B0), "Ultra-secure randomized offline credential key encoder"),
        AppTool("Unit Conv", Icons.Filled.SyncAlt, Color(0xFF673AB7), "Quick conversions for dynamic physical metric scopes"),
        AppTool("Word Counter", Icons.Filled.TextFormat, Color(0xFF3F51B5), "Complete text statistics analyzing paragraphs & reading metrics"),
        AppTool("Rand Number", Icons.Filled.Casino, Color(0xFF2196F3), "Weighted randomized range digit generation engine"),
        AppTool("Quick Note", Icons.Filled.NoteAlt, Color(0xFF03A9F4), "Safe memory text drafting console for quick reference"),
        AppTool("BMI Calc", Icons.Filled.FitnessCenter, Color(0xFF00BCD4), "Instantly compute Body Mass Index with fitness guidance"),
        AppTool("Stopwatch", Icons.Filled.Timer, Color(0xFF009688), "High-precision split lap runner counting down to milliseconds"),
        AppTool("Base64 Code", Icons.Filled.Code, Color(0xFF4CAF50), "Format, encode, or decode binary UTF text blocks in a tap"),
        AppTool("Age Calc", Icons.Filled.CalendarMonth, Color(0xFF8BC34A), "Exact birth analytics showing years, minutes & birth date charts")
    )

    AnimatedContent(
        targetState = selectedTool,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "ToolTransition"
    ) { activeTool ->
        if (activeTool != null) {
            when (activeTool.title) {
                "World Clock" -> WorldClockScreen(tool = activeTool, onBack = { selectedTool = null })
                "Calculator" -> CalculatorScreen(tool = activeTool, onBack = { selectedTool = null })
                "Password Gen" -> PasswordGeneratorScreen(tool = activeTool, onBack = { selectedTool = null })
                "Unit Conv" -> UnitConverterScreen(tool = activeTool, onBack = { selectedTool = null })
                "Word Counter" -> WordCounterScreen(tool = activeTool, onBack = { selectedTool = null })
                "Rand Number" -> RandomNumberScreen(tool = activeTool, onBack = { selectedTool = null })
                "Quick Note" -> QuickNoteScreen(tool = activeTool, onBack = { selectedTool = null })
                "BMI Calc" -> BMICalcScreen(tool = activeTool, onBack = { selectedTool = null })
                "Stopwatch" -> StopwatchScreen(tool = activeTool, onBack = { selectedTool = null })
                "Base64 Code" -> Base64Screen(tool = activeTool, onBack = { selectedTool = null })
                "Age Calc" -> AgeCalcScreen(tool = activeTool, onBack = { selectedTool = null })
                else -> GenericToolScreen(tool = activeTool, onBack = { selectedTool = null })
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Utility Tools", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                        navigationIcon = {
                            if (onBack != null) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Premium Header Banner Card - iPhone/Squircle Rounded Curves
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(28.dp), // iPhone highly round/curved aesthetic
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF000000), Color(0xFF1E1E1E))
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Smart Utility Suite ⚡",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ultra-fast, beautiful secure offline tools built to assist your daily workflow in comfort.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        Text(
                            "Essential Free Tools", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                        )
                    }

                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(560.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(toolsList) { tool ->
                                ToolItemCard(tool = tool) {
                                    selectedTool = tool
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// iPhone Circular/Highly Rounded Elements
@Composable
fun ToolItemCard(tool: AppTool, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(tool.color.copy(alpha = 0.12f), CircleShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tool.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tool.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// Helper container for full-screen tools with curved iPhone-like cards & buttons
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenToolPage(
    tool: AppTool,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

// ==========================================
// 1. WORLD CLOCK & CALENDAR SCREEN (REAL-TIME!)
// ==========================================
@Composable
fun WorldClockScreen(tool: AppTool, onBack: () -> Unit) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Updates local states every second to secure absolute real-time updating
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val dacSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Asia/Dhaka") }
    val dacDateSdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Asia/Dhaka") }
    
    val timeZones = listOf(
        Triple("New York", "America/New_York", "🇺🇸 USA"),
        Triple("London", "Europe/London", "🇬🇧 UK"),
        Triple("Riyadh", "Asia/Riyadh", "🇸🇦 KSA"),
        Triple("Tokyo", "Asia/Tokyo", "🇯🇵 Japan"),
        Triple("Sydney", "Australia/Sydney", "🇦🇺 Australia")
    )

    FullScreenToolPage(tool = tool, onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Local Running Clock
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LOCAL TIME (DHAKA)",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dacSdf.format(Date(currentTimeMillis)),
                            color = tool.color,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dacDateSdf.format(Date(currentTimeMillis)),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Timezone sliders
            item {
                Text("Global Timezones 🌍", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(timeZones) { (name, tzId, nation) ->
                val tzSdf = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone(tzId) }
                val dateSecSdf = SimpleDateFormat("EE, dd MMMM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone(tzId) }
                val timeStr = tzSdf.format(Date(currentTimeMillis))
                val dateStr = dateSecSdf.format(Date(currentTimeMillis))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(nation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(timeStr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Real-time World Calendar Panel
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("World Calendar 🗓️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val cal = Calendar.getInstance()
                val todayDay = cal.get(Calendar.DAY_OF_MONTH)
                val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                // Get month label
                val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                
                // Find day offsets (First day of month)
                val tempCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday ... 7 = Saturday
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = monthLabel.uppercase(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Days of week
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysList = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                            daysList.forEach { d ->
                                Text(
                                    text = d,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Calendar Grid Days
                        // Map days list. Saturday corresponds to 7 in standard JVM calendar. Offset accordingly
                        // Standard JVM days of week: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
                        // Align grid starting with Sat (7), Sun (1), Mon (2) ...
                        val startIndex = when (firstDayOfWeek) {
                            Calendar.SATURDAY -> 0
                            Calendar.SUNDAY -> 1
                            Calendar.MONDAY -> 2
                            Calendar.TUESDAY -> 3
                            Calendar.WEDNESDAY -> 4
                            Calendar.THURSDAY -> 5
                            Calendar.FRIDAY -> 6
                            else -> 0
                        }

                        val gridItems = mutableListOf<String>()
                        for (i in 0 until startIndex) {
                            gridItems.add("")
                        }
                        for (i in 1..totalDays) {
                            gridItems.add(i.toString())
                        }
                        
                        val rows = gridItems.chunked(7)
                        rows.forEach { rowDays ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                rowDays.forEach { dayText ->
                                    val isToday = dayText == todayDay.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (isToday) tool.color else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayText.isNotBlank()) {
                                            Text(
                                                text = dayText,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                                // Fill remaining slots in last row to prevent asymmetrical space
                                if (rowDays.size < 7) {
                                    for (j in 0 until (7 - rowDays.size)) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

// ==========================================
// 2. IPHONE STYLE CALCULATOR SCREEN
// ==========================================
@Composable
fun CalculatorScreen(tool: AppTool, onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    
    // Simple state parser equations
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var activeOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextClick by remember { mutableStateOf(false) }

    fun handleInput(symbol: String) {
        when {
            symbol in "0123456789." -> {
                if (display == "0" || resetOnNextClick) {
                    display = if (symbol == ".") "0." else symbol
                    resetOnNextClick = false
                } else {
                    if (symbol == "." && display.contains(".")) return
                    display += symbol
                }
            }
            symbol in listOf("+", "-", "×", "÷") -> {
                operand1 = display.toDoubleOrNull()
                activeOp = symbol
                expressionHistory = "${display} ${symbol}"
                resetOnNextClick = true
            }
            symbol == "C" -> {
                display = "0"
                expressionHistory = ""
                operand1 = null
                activeOp = null
            }
            symbol == "±" -> {
                val value = display.toDoubleOrNull() ?: 0.0
                display = (value * -1).let {
                    if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                }
            }
            symbol == "%" -> {
                val value = display.toDoubleOrNull() ?: 0.0
                display = (value / 100.0).toString()
            }
            symbol == "=" -> {
                val op1 = operand1
                val op2 = display.toDoubleOrNull()
                val currentOp = activeOp
                if (op1 != null && op2 != null && currentOp != null) {
                    val resultVal = when (currentOp) {
                        "+" -> op1 + op2
                        "-" -> op1 - op2
                        "×" -> op1 * op2
                        "÷" -> if (op2 == 0.0) Double.NaN else op1 / op2
                        else -> op2
                    }
                    display = if (resultVal.isNaN()) {
                        "Error"
                    } else if (resultVal % 1 == 0.0) {
                        resultVal.toLong().toString()
                    } else {
                        String.format(Locale.US, "%.5f", resultVal).trimEnd('0').trimEnd('.')
                    }
                    expressionHistory = "$op1 $currentOp $op2 ="
                    operand1 = null
                    activeOp = null
                    resetOnNextClick = true
                }
            }
        }
    }

    val buttons = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Board
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                if (expressionHistory.isNotBlank()) {
                    Text(
                        text = expressionHistory,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = display,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 64.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Keypad (Curved Round Buttons matching iOS premium aesthetics)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { btn ->
                            val isAction = btn in listOf("÷", "×", "-", "+", "=")
                            val isAux = btn in listOf("C", "±", "%")
                            
                            val containerCol = when {
                                isAction -> Color(0xFFFF9F0A)
                                isAux -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            
                            val textCol = when {
                                isAction -> Color.White
                                isAux -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(if (btn == "0") 2f else 1f)
                                    .aspectRatio(if (btn == "0") 2.1f else 1f)
                                    .clip(RoundedCornerShape(32.dp)) // Super curved organic button shape
                                    .background(containerCol)
                                    .clickable { handleInput(btn) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textCol
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ==========================================
// 3. SECURE PASSWORD GENERATOR SCREEN
// ==========================================
@Composable
fun PasswordGeneratorScreen(tool: AppTool, onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var password by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(16f) }
    
    // Toggles
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeDigits by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(false) }

    fun generatePass() {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        var pool = ""
        if (includeUpper) pool += uppercase
        if (includeLower) pool += lowercase
        if (includeDigits) pool += digits
        if (includeSymbols) pool += symbols
        
        if (pool.isEmpty()) {
            password = "Check options first!"
            return
        }
        
        password = (1..length.toInt()).map { pool.random() }.joinToString("")
    }

    LaunchedEffect(Unit) {
        generatePass()
    }

    // Evaluates password strength
    val strength = remember(password) {
        val len = password.length
        if (password.contains("Check") || password.isBlank()) "None"
        else if (len < 10) "Weak 🔴"
        else if (len < 14) "Medium 🟡"
        else if (len < 18) "Strong 🟢"
        else "Super Secure 👑"
    }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = password,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = tool.color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Strength: $strength", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Clipboard Actions
            Button(
                onClick = {
                    if (password.isNotBlank() && !password.contains("Check")) {
                        clipboardManager.setText(AnnotatedString(password))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Password", fontWeight = FontWeight.Bold)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("PASSWORD CONFIGURATION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("Length: ${length.toInt()}", fontWeight = FontWeight.Medium)
                    Slider(
                        value = length,
                        onValueChange = { length = it; generatePass() },
                        valueRange = 8f..32f,
                        steps = 24
                    )
                    
                    ListItem(
                        headlineContent = { Text("Uppercase letters (A-Z)") },
                        trailingContent = { Switch(checked = includeUpper, onCheckedChange = { includeUpper = it; generatePass() }) }
                    )
                    ListItem(
                        headlineContent = { Text("Lowercase letters (a-z)") },
                        trailingContent = { Switch(checked = includeLower, onCheckedChange = { includeLower = it; generatePass() }) }
                    )
                    ListItem(
                        headlineContent = { Text("Numeric Digits (0-9)") },
                        trailingContent = { Switch(checked = includeDigits, onCheckedChange = { includeDigits = it; generatePass() }) }
                    )
                    ListItem(
                        headlineContent = { Text("Special Symbols (!@#$)") },
                        trailingContent = { Switch(checked = includeSymbols, onCheckedChange = { includeSymbols = it; generatePass() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { generatePass() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tool.color)
            ) {
                Text("Re-generate Key", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ==========================================
// 4. UNIT CONVERTER SCREEN
// ==========================================
@Composable
fun UnitConverterScreen(tool: AppTool, onBack: () -> Unit) {
    var rawInput by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("Length") } // Length, Mass, Temp

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Length", "Mass", "Temp").forEach { category ->
                    val isSel = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isSel) tool.color else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedCategory = category },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            OutlinedTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                label = { Text("Input Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            )

            Text("CONVERTERS SUMMARY", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

            val inputNum = rawInput.toDoubleOrNull() ?: 1.0
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedCategory) {
                        "Length" -> {
                            // Meters to Kms, Miles, Feet
                            val kms = inputNum / 1000.0
                            val miles = inputNum * 0.000621371
                            val feet = inputNum * 3.28084
                            ConverterRow("Meters", inputNum, "Kilometers", kms)
                            ConverterRow("Meters", inputNum, "Miles", miles)
                            ConverterRow("Meters", inputNum, "Feet", feet)
                        }
                        "Mass" -> {
                            // Kgs to Pounds, Ounces, Grams
                            val lbs = inputNum * 2.20462
                            val ozs = inputNum * 35.274
                            val grams = inputNum * 1000.0
                            ConverterRow("Kilograms", inputNum, "Pounds", lbs)
                            ConverterRow("Kilograms", inputNum, "Ounces", ozs)
                            ConverterRow("Kilograms", inputNum, "Grams", grams)
                        }
                        "Temp" -> {
                            // Celcius to Fahrenheit, Kelvin
                            val fahr = (inputNum * 9/5) + 32
                            val kelvin = inputNum + 273.15
                            ConverterRow("Celcius", inputNum, "Fahrenheit", fahr)
                            ConverterRow("Celcius", inputNum, "Kelvin", kelvin)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConverterRow(fromUnit: String, fromVal: Double, toUnit: String, toVal: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("$fromVal $fromUnit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(toUnit, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = String.format(Locale.US, "%.4f", toVal).trimEnd('0').trimEnd('.'),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ==========================================
// 5. WORD COUNTER SCREEN
// ==========================================
@Composable
fun WordCounterScreen(tool: AppTool, onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    
    val words = if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    val chars = text.length
    val charactersNoSpace = text.replace(" ", "").replace("\n", "").length
    val paragraphs = if (text.isBlank()) 0 else text.split(Regex("\n+")).filter { it.isNotBlank() }.size
    val estReadTime = (words / 200.0).let { if (it < 1) "1 min" else "${it.toInt()} min" }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Words", words.toString(), Modifier.weight(1f))
                    StatCard("Chars", chars.toString(), Modifier.weight(1f))
                    StatCard("Read Time", estReadTime, Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Alternative Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paragraphs:")
                            Text(paragraphs.toString(), fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Chars (no space):")
                            Text(charactersNoSpace.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Write or paste your article text here...") }
                )
            }

            item {
                Button(
                    onClick = { text = "" },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Text", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==========================================
// 6. RANDOM NUMBER GENERATOR SCREEN
// ==========================================
@Composable
fun RandomNumberScreen(tool: AppTool, onBack: () -> Unit) {
    var minVal by remember { mutableStateOf("1") }
    var maxVal by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf<Int?>(null) }
    var logHistory by remember { mutableStateOf(listOf<Int>()) }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RESULT", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result?.toString() ?: "-",
                        color = tool.color,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minVal,
                    onValueChange = { minVal = it },
                    label = { Text("Minimum") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                OutlinedTextField(
                    value = maxVal,
                    onValueChange = { maxVal = it },
                    label = { Text("Maximum") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Button(
                onClick = {
                    val min = minVal.toIntOrNull() ?: 1
                    val max = maxVal.toIntOrNull() ?: 100
                    if (min <= max) {
                        val num = Random.nextInt(min, max + 1)
                        result = num
                        logHistory = (listOf(num) + logHistory).take(10)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tool.color)
            ) {
                Text("Generate Random Number", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Text("Session Logs History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(logHistory) { num ->
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (logHistory.isEmpty()) {
                    item {
                        Text("No logs yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. PERSISTENT QUICK NOTE SCREEN
// ==========================================
@Composable
fun QuickNoteScreen(tool: AppTool, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("utility_notes", Context.MODE_PRIVATE) }
    var currentNoteText by remember { mutableStateOf(prefs.getString("stored_note", "") ?: "") }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Simple draft console that automatically saves your thoughts in offline device storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = currentNoteText,
                onValueChange = {
                    currentNoteText = it
                    prefs.edit().putString("stored_note", it).apply()
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text("Draft/write notes here. It will auto-save on change...") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        currentNoteText = ""
                        prefs.edit().putString("stored_note", "").apply()
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Note", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Exit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 8. BMI CALCULATOR SCREEN
// ==========================================
@Composable
fun BMICalcScreen(tool: AppTool, onBack: () -> Unit) {
    var heightCm by remember { mutableStateOf(170f) }
    var weightKg by remember { mutableStateOf(65f) }

    val bmi = remember(heightCm, weightKg) {
        val hMeters = heightCm / 100.0
        weightKg / (hMeters * hMeters)
    }

    val (categoryStr, colorVal) = when {
        bmi < 18.5 -> Pair("UNDERWEIGHT 🔵", Color(0xFF2196F3))
        bmi < 24.9 -> Pair("NORMAL WEIGHT 🟢", Color(0xFF4CAF50))
        bmi < 29.9 -> Pair("OVERWEIGHT 🟡", Color(0xFFFFC107))
        else -> Pair("OBESE 🛑", Color(0xFFF44336))
    }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colorVal.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("YOUR BODY MASS INDEX (BMI)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = colorVal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", bmi),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorVal
                    )
                    Text(
                        text = categoryStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorVal
                    )
                }
            }

            Text("MEASUREMENTS ADJUSTMENT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Height: ${heightCm.toInt()} cm", fontWeight = FontWeight.Bold)
                    Slider(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        valueRange = 100f..220f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Weight: ${weightKg.toInt()} kg", fontWeight = FontWeight.Bold)
                    Slider(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        valueRange = 30f..150f
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorVal)
            ) {
                Text("Back to Tools", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ==========================================
// 9. TIMER STOPWATCH SCREEN
// ==========================================
@Composable
fun StopwatchScreen(tool: AppTool, onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var lapsList by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val start = System.currentTimeMillis() - elapsedMs
            while (isRunning) {
                elapsedMs = System.currentTimeMillis() - start
                delay(30L)
            }
        }
    }

    val displaySdf = remember(elapsedMs) {
        val mins = (elapsedMs / 60000) % 60
        val secs = (elapsedMs / 1000) % 60
        val hunds = (elapsedMs / 10) % 100
        String.format(Locale.US, "%02d:%02d.%02d", mins, secs, hunds)
    }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier.padding(42.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displaySdf,
                        color = tool.color,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                        } else {
                            isRunning = true
                        }
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                ) {
                    Text(if (isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        if (isRunning) {
                            lapsList = listOf("Lap ${lapsList.size + 1}: $displaySdf") + lapsList
                        } else {
                            elapsedMs = 0L
                            lapsList = emptyList()
                        }
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(if (isRunning) "Lap" else "Reset", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Text("Laps Split Timeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lapsList) { lap ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(lap, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }
                if (lapsList.isEmpty()) {
                    item {
                        Text("No laps logged yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. BASE64 ENCODER DECODER SCREEN
// ==========================================
@Composable
fun Base64Screen(tool: AppTool, onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var isEncode by remember { mutableStateOf(true) }
    var rawText by remember { mutableStateOf("") }
    var convertedText by remember { mutableStateOf("") }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isEncode) tool.color else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isEncode = true; convertedText = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Encode text", fontWeight = FontWeight.Bold, color = if (isEncode) Color.White else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (!isEncode) tool.color else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isEncode = false; convertedText = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Decode text", fontWeight = FontWeight.Bold, color = if (!isEncode) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(24.dp),
                label = { Text("Original text Input") }
            )

            Button(
                onClick = {
                    try {
                        if (isEncode) {
                            convertedText = Base64.getEncoder().encodeToString(rawText.toByteArray())
                        } else {
                            convertedText = String(Base64.getDecoder().decode(rawText))
                        }
                    } catch (e: Exception) {
                        convertedText = "Error formatting/decoding text block"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tool.color)
            ) {
                Text(if (isEncode) "Encode to Base64" else "Decode from Base64", fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedTextField(
                value = convertedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(24.dp),
                label = { Text("Processed Output") }
            )

            Button(
                onClick = {
                    if (convertedText.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(convertedText))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Output Result", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 11. AGE CALCULATOR SCREEN
// ==========================================
@Composable
fun AgeCalcScreen(tool: AppTool, onBack: () -> Unit) {
    var rawBirthYear by remember { mutableStateOf("2000") }
    var rawBirthMonth by remember { mutableStateOf("1") }
    var rawBirthDay by remember { mutableStateOf("1") }

    var calculatedYMD by remember { mutableStateOf<String?>(null) }
    var totalDaysLived by remember { mutableStateOf("") }
    var countdownNextBirthday by remember { mutableStateOf("") }

    fun calculate() {
        try {
            val y = rawBirthYear.toIntOrNull() ?: 2000
            val m = (rawBirthMonth.toIntOrNull() ?: 1) - 1 // Calendar is 0-indexed month
            val d = rawBirthDay.toIntOrNull() ?: 1

            val birthCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            val today = Calendar.getInstance()

            if (birthCal.after(today)) {
                calculatedYMD = "Birth date cannot be in the future!"
                return
            }

            // Years, Months, Days logic
            var diffYears = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            var diffMonths = today.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
            var diffDays = today.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

            if (diffDays < 0) {
                diffMonths--
                tempCalendarForMonthOffset(today, birthCal)
                val daysInPrevMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
                diffDays += daysInPrevMonth
            }

            if (diffMonths < 0) {
                diffYears--
                diffMonths += 12
            }

            calculatedYMD = "$diffYears Years, $diffMonths Months, and $diffDays Days"

            val diffMillis = today.timeInMillis - birthCal.timeInMillis
            val daysLived = diffMillis / (1000 * 60 * 60 * 24)
            totalDaysLived = "$daysLived Total Days Lived"

            // Next birthday countdown
            val nextBday = Calendar.getInstance().apply {
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            if (nextBday.before(today) || nextBday.equals(today)) {
                nextBday.add(Calendar.YEAR, 1)
            }
            val diffBdayMillis = nextBday.timeInMillis - today.timeInMillis
            val daysToBday = diffBdayMillis / (1000 * 60 * 60 * 24)
            countdownNextBirthday = "$daysToBday Days remaining until next birthday"

        } catch (e: Exception) {
            calculatedYMD = "Error parsing dates."
        }
    }

    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rawBirthDay,
                    onValueChange = { rawBirthDay = it },
                    label = { Text("Day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                OutlinedTextField(
                    value = rawBirthMonth,
                    onValueChange = { rawBirthMonth = it },
                    label = { Text("Month") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                OutlinedTextField(
                    value = rawBirthYear,
                    onValueChange = { rawBirthYear = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Button(
                onClick = { calculate() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tool.color)
            ) {
                Text("Calculate Age Metrics", fontWeight = FontWeight.Bold, color = Color.White)
            }

            if (calculatedYMD != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("AGE SUMMARY", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text(calculatedYMD!!, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Divider()
                        Text(totalDaysLived, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                        Text(countdownNextBirthday, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

private fun tempCalendarForMonthOffset(today: Calendar, birthCal: Calendar) {
    today.add(Calendar.MONTH, -1)
}


// ==========================================
// GENERIC FALLBACK SCREEN FOR COMING SOON TOOLS
// ==========================================
@Composable
fun GenericToolScreen(tool: AppTool, onBack: () -> Unit) {
    FullScreenToolPage(tool = tool, onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(tool.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(tool.tagline, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Tool content is getting optimized for offline loading.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp)) {
                Text("Back to Utility Tools", fontWeight = FontWeight.Bold)
            }
        }
    }
}
