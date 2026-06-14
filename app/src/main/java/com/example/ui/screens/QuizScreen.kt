package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AdMobManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class QuizQuestion(
    val questionBangla: String,
    val questionEnglish: String,
    val optionsBangla: List<String>,
    val optionsEnglish: List<String>,
    val correctOptionIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val questions = remember {
        listOf(
            QuizQuestion(
                questionBangla = "ইসলামের প্রথম রোকন বা স্তম্ভ কোনটি?",
                questionEnglish = "What is the first pillar of Islam?",
                optionsBangla = listOf("সালাত", "যাকাত", "কালেমা শাহাদাহ্", "হজ্জ"),
                optionsEnglish = listOf("Salat", "Zakat", "Shahada", "Hajj"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "কোন নবী আল্লাহর নির্দেশে কাবা শরীফ পুনর্নির্মাণ করেছিলেন?",
                questionEnglish = "Which Prophet rebuilt the Kaaba?",
                optionsBangla = listOf("হযরত মুহাম্মদ (সা.)", "হযরত ইব্রাহিম (আ.) ও হযরত ইসমাইল (আ.)", "হযরত মূসা (আ.)", "হযরত ঈসা (আ.)"),
                optionsEnglish = listOf("Prophet Muhammad (SAW)", "Prophet Ibrahim (AS) & Ismail (AS)", "Prophet Musa (AS)", "Prophet Isa (AS)"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                questionBangla = "কোন পবিত্র মাসে আল-কুরআন অবতীর্ণ হয়েছিল?",
                questionEnglish = "In which month was the Quran revealed?",
                optionsBangla = listOf("মুহাররাম", "রজব", "জিলহজ্জ", "রমজান"),
                optionsEnglish = listOf("Muharram", "Rajab", "Dhul-Hijjah", "Ramadan"),
                correctOptionIndex = 3
            ),
            QuizQuestion(
                questionBangla = "দিনে ও রাতে মোট কত ওয়াক্ত ফরজ নামাজ রয়েছে?",
                questionEnglish = "How many obligatory prayers are there in a day?",
                optionsBangla = listOf("তিন ওয়াক্ত", "চার ওয়াক্ত", "পাঁচ ওয়াক্ত", "ছয় ওয়াক্ত"),
                optionsEnglish = listOf("Three", "Four", "Five", "Six"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "ইসলামের প্রধান ও পবিত্র কিতাবের নাম কী?",
                questionEnglish = "What is the holy book of Islam?",
                optionsBangla = listOf("তাওরাত", "যবুর", "ইঞ্জিল", "আল-কুরআন"),
                optionsEnglish = listOf("Torah", "Zabur", "Injeel", "Quran"),
                correctOptionIndex = 3
            ),
            QuizQuestion(
                questionBangla = "সর্বপ্রথম কোন ব্যক্তি ইসলাম গ্রহণ করেন?",
                questionEnglish = "Who was the first person to accept Islam?",
                optionsBangla = listOf("হযরত আবু বকর (রা.)", "হযরত ওমর (রা.)", "হযরত খাদিজা (রা.)", "হযরত আলী (রা.)"),
                optionsEnglish = listOf("Hazrat Abu Bakr (RA)", "Hazrat Umar (RA)", "Hazrat Khadijah (RA)", "Hazrat Ali (RA)"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "সর্বপ্রথম আযান কে দিয়েছিলেন?",
                questionEnglish = "Who was the first person to call the Adhan (call to prayer)?",
                optionsBangla = listOf("হযরত বিলাল (রা.)", "হযরত সালমান ফারসী (রা.)", "হযরত হামযা (রা.)", "হযরত আবু বকর (রা.)"),
                optionsEnglish = listOf("Hazrat Bilal (RA)", "Hazrat Salman Farsi (RA)", "Hazrat Hamza (RA)", "Hazrat Abu Bakr (RA)"),
                correctOptionIndex = 0
            ),
            QuizQuestion(
                questionBangla = "ইসলামের প্রথম যুদ্ধ বা যুদ্ধক্ষেত্র কোনটি?",
                questionEnglish = "What was the first major battle of Islam?",
                optionsBangla = listOf("উহুদের যুদ্ধ", "বদরের যুদ্ধ", "খন্দকের যুদ্ধ", "খায়বারের যুদ্ধ"),
                optionsEnglish = listOf("Battle of Uhud", "Battle of Badr", "Battle of the Trench", "Battle of Khyber"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                questionBangla = "কুরআন মজিদে মোট কতটি সূরা আছে?",
                questionEnglish = "How many Surahs are there in the Holy Quran?",
                optionsBangla = listOf("১১০টি", "১১২টি", "১১৪টি", "১১৬টি"),
                optionsEnglish = listOf("110", "112", "114", "116"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "ইসলামের প্রথম খলীফা কে ছিলেন?",
                questionEnglish = "Who was the first Caliph of Islam?",
                optionsBangla = listOf("হযরত ওমর (রা.)", "হযরত আলী (রা.)", "হযরত ওসমান (রা.)", "হযরত আবু বকর (রা.)"),
                optionsEnglish = listOf("Hazrat Umar (RA)", "Hazrat Ali (RA)", "Hazrat Uthman (RA)", "Hazrat Abu Bakr (RA)"),
                correctOptionIndex = 3
            ),
            QuizQuestion(
                questionBangla = "পবিত্র কুরআন কোন রাতে নাযিল হওয়া শুরু হয়?",
                questionEnglish = "On which night did the revelation of the Quran begin?",
                optionsBangla = listOf("শবে বরাত", "শবে কদর", "শবে মেরাজ", "ঈদের রাতে"),
                optionsEnglish = listOf("Shab-e-Barat", "Lailat-al-Qadr", "Shab-e-Miraj", "Night of Eid"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                questionBangla = "হজ্জ ইসলামের কততম রোকন বা স্তম্ভ?",
                questionEnglish = "Hajj is which pillar of Islam?",
                optionsBangla = listOf("৩য়", "৪র্থ", "৫ম", "২য়"),
                optionsEnglish = listOf("3rd", "4th", "5th", "2nd"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "কোন সূরাকে কুরআনের হৃদয় বা হার্ট বলা হয়?",
                questionEnglish = "Which Surah is known as the Heart of the Quran?",
                optionsBangla = listOf("সূরা আল-ফাতিহা", "সূরা আর-রহমান", "সূরা ইয়াসিন", "সূরা আল-ইখলাস"),
                optionsEnglish = listOf("Surah Al-Fatihah", "Surah Ar-Rahman", "Surah Yaseen", "Surah Al-Ikhlas"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "মক্কায় অবতীর্ণ সূরাগুলোকে কী বলা হয়?",
                questionEnglish = "What are the Surahs revealed in Makkah called?",
                optionsBangla = listOf("মৌখিক সূরা", "মাদানী সূরা", "মাক্কী সূরা", "মাক্কী আয়াত"),
                optionsEnglish = listOf("Oral Surahs", "Madani Surahs", "Makki Surahs", "Makki Ayat"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "সালাতুত তাসবীহ নামাজে কতবার তাসবীহ পড়তে হয়?",
                questionEnglish = "How many times is Tasbih recited in Salat-ut-Tasbih prayer?",
                optionsBangla = listOf("১০০ বার", "২০০ বার", "৩০০ বার", "৪০০ বার"),
                optionsEnglish = listOf("100 times", "200 times", "300 times", "400 times"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "তাকওয়া শব্দের সাধারণ বা আভিধানিক অর্থ কী?",
                questionEnglish = "What is the general meaning of 'Taqwa'?",
                optionsBangla = listOf("আল্লাহভীতি বা সংগ্রহ", "দান করা", "নামাজ পড়া", "ज्ञान লাভ"),
                optionsEnglish = listOf("God-consciousness", "Charity", "Praying", "Seeking knowledge"),
                correctOptionIndex = 0
            ),
            QuizQuestion(
                questionBangla = "ইসলামের ২য় খলীফা কে ছিলেন?",
                questionEnglish = "Who was the second Caliph of Islam?",
                optionsBangla = listOf("হযরত আবু বকর (রা.)", "হযরত ইউসুফ (আ.)", "হযরত ওমর ইবনুল খাত্তাব (রা.)", "হযরত ওসমান (রা.)"),
                optionsEnglish = listOf("Hazrat Abu Bakr (RA)", "Prophet Yusuf (AS)", "Hazrat Umar bin Al-Khattab (RA)", "Hazrat Uthman (RA)"),
                correctOptionIndex = 2
            ),
            QuizQuestion(
                questionBangla = "কুরআন মজিদের সবচেয়ে বড় বা দীর্ঘতম সূরা কোনটি?",
                questionEnglish = "Which is the longest Surah in the Holy Quran?",
                optionsBangla = listOf("সূরা আল-ইমরান", "সূরা আল-বাকারা", "সূরা আন-নিসা", "সূরা আল-মায়েদা"),
                optionsEnglish = listOf("Surah Al-Imran", "Surah Al-Baqarah", "Surah An-Nisa", "Surah Al-Ma'idah"),
                correctOptionIndex = 1
            ),
            QuizQuestion(
                questionBangla = "কোন নবীকে আবুল বাশার বা মানবজাতির পিতা বলা হয়?",
                questionEnglish = "Which Prophet is called Abul Bashar (Father of Mankind)?",
                optionsBangla = listOf("হযরত আদম (আ.)", "হযরত নূহ (আ.)", "হযরত ইব্রাহিম (আ.)", "হযরত শীষ (আ.)"),
                optionsEnglish = listOf("Prophet Adam (AS)", "Prophet Nuh (AS)", "Prophet Ibrahim (AS)", "Prophet Sheeth (AS)"),
                correctOptionIndex = 0
            ),
            QuizQuestion(
                questionBangla = "আল্লাহ তায়ালার গুণবাচক বা পবিত্র নাম কয়টি?",
                questionEnglish = "How many beautiful names (Asma-ul-Husna) does Allah have?",
                optionsBangla = listOf("৮৮টি", "৯৯টি", "১০১টি", "১১০টি"),
                optionsEnglish = listOf("88", "99", "101", "110"),
                correctOptionIndex = 1
            )
        )
    }

    val activeQuestions = remember {
        questions.shuffled().take(5)
    }

    var isEnglish by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var isCheckingAnswer by remember { mutableStateOf(false) }

    // Dynamic Admin Settings loaded from Firestore settings/quiz_settings
    var rewardAmount by remember { mutableStateOf(2.50) }
    var breakDuration by remember { mutableStateOf(25) }
    var lastQuizTime by remember { mutableStateOf(0L) }
    var isLoadingSettings by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val currentUserUid = UserSession.getUid(context)

    // Listen to quiz settings in real-time independently of user authentication timing
    LaunchedEffect(Unit) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("settings").document("quiz_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("QuizScreen", "Settings listen failed", error)
                    android.widget.Toast.makeText(context, "কুইজ সেটিংস আপডেট ব্যর্থ বা অনুমতি নেই: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    isLoadingSettings = false
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawReward = snapshot.get("reward_amount")
                    rewardAmount = when (rawReward) {
                        is Number -> rawReward.toDouble()
                        is String -> rawReward.toDoubleOrNull() ?: 2.50
                        else -> snapshot.getDouble("reward_amount") ?: 2.50
                    }
                    
                    val rawBreak = snapshot.get("break_duration")
                    breakDuration = when (rawBreak) {
                        is Number -> rawBreak.toInt()
                        is String -> rawBreak.toIntOrNull() ?: 25
                        else -> snapshot.getLong("break_duration")?.toInt() ?: 25
                    }
                }
                isLoadingSettings = false
            }
    }

    // Listen to current user quiz progress/time limits
    LaunchedEffect(currentUserUid) {
        if (currentUserUid.isNotBlank()) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.w("QuizScreen", "User listen failed: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        lastQuizTime = when (val value = snapshot.get("lastQuizTime")) {
                            is Number -> value.toLong()
                            is String -> value.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                    }
                }
        }
    }

    // Live countdown updates in seconds
    var remainingSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(lastQuizTime, breakDuration) {
        while (true) {
            val totalBreakMs = breakDuration.toLong() * 60L * 1000L
            val elapsedMs = System.currentTimeMillis() - lastQuizTime
            val diffMs = totalBreakMs - elapsedMs
            if (diffMs > 0) {
                remainingSeconds = diffMs / 1000
            } else {
                remainingSeconds = 0
            }
            delay(1000)
        }
    }

    if (showRewardDialog) {
        Dialog(onDismissRequest = onBack) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEnglish) "Congratulations!" else "অভিনন্দন!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isEnglish) "You got $score out of 5 correct." else "আপনি ৫টির মধ্যে $score টি প্রশ্নের সঠিক উত্তর দিয়েছেন।",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isEnglish) "Reward Earned: ৳${String.format("%.2f", rewardAmount)}" else "অর্জিত পুরস্কার: ৳${String.format("%.2f", rewardAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isEnglish) "Claim & Go Back" else "পুরস্কার নিন ও ফিরে যান")
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        com.example.ui.screens.FullScreenDialogModifier()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(if (isEnglish) "Islamic Quiz" else "ইসলামিক কুইজ", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        actions = {
                            IconButton(onClick = { isEnglish = !isEnglish }) {
                                Icon(
                                    imageVector = if (isEnglish) Icons.Default.Language else Icons.Default.Translate,
                                    contentDescription = "Toggle Language",
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White,
                            titleContentColor = Color.Black
                        )
                    )
                }
            ) { paddingValues ->
                if (isLoadingSettings) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (remainingSeconds > 0) {
                    // RENDERING PREMIUM BREAK ACTIVE SCREEN WITH SECONDS ACCURACY countdown
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    val timeText = "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Break Time",
                                    modifier = Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isEnglish) "Quiz is on Rest!" else "কুইজের ব্রেক টাইম চলছে!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isEnglish) 
                                        "You have played 5 questions. Please wait for the break timer to expire before restarting."
                                        else "আপনি ৫টি কুইজ খেলেছেন। দয়া করে ব্রেক টাইম শেষ হওয়া পর্যন্ত অপেক্ষা করুন এবং আবারও খেলুন।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = timeText,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isEnglish) "REMAINING TIME" else "অবশিষ্ট সময়",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(if (isEnglish) "Go Back" else "ফিরে যান")
                        }
                    }
                } else if (currentQuestionIndex < 5 && currentQuestionIndex < activeQuestions.size) {
                    val currentQuestion = activeQuestions[currentQuestionIndex]
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1) / 5.0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = if (isEnglish) "Question ${currentQuestionIndex + 1} of 5" else "প্রশ্ন ৫ এর মধ্যে ${currentQuestionIndex + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (isEnglish) currentQuestion.questionEnglish else currentQuestion.questionBangla,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val activeOptions = if (isEnglish) currentQuestion.optionsEnglish else currentQuestion.optionsBangla
                        
                        activeOptions.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            
                            // Highlighting correctness as Red or Green using light themed layouts
                            val backgroundColor = if (isCheckingAnswer) {
                                if (index == currentQuestion.correctOptionIndex) {
                                    Color(0xFFE8F5E9) // Soft light green
                                } else if (isSelected && selectedOptionIndex != currentQuestion.correctOptionIndex) {
                                    Color(0xFFFFEBEE) // Soft light red
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            } else {
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            }
                            
                            val textColor = if (isCheckingAnswer) {
                                if (index == currentQuestion.correctOptionIndex) {
                                    Color(0xFF2E7D32) // Solid dark green
                                } else if (isSelected && selectedOptionIndex != currentQuestion.correctOptionIndex) {
                                    Color(0xFFC62828) // Solid dark red
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            } else {
                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            val borderStrokeValue = if (isCheckingAnswer) {
                                if (index == currentQuestion.correctOptionIndex) {
                                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
                                } else if (isSelected && selectedOptionIndex != currentQuestion.correctOptionIndex) {
                                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF44336))
                                } else {
                                    null
                                }
                            } else {
                                if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                shape = RoundedCornerShape(12.dp),
                                border = borderStrokeValue,
                                onClick = {
                                    if (!isCheckingAnswer) {
                                        selectedOptionIndex = index
                                    }
                                }
                            ) {
                                Text(
                                    text = optionText,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        
                        // Compact custom raised Spacer (moves submit buttons significantly higher up)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null && !isCheckingAnswer) {
                                    isCheckingAnswer = true
                                    coroutineScope.launch {
                                        if (selectedOptionIndex == currentQuestion.correctOptionIndex) {
                                            score++
                                        }
                                        delay(1500) // Show response feedback colors
                                        
                                        currentQuestionIndex++
                                        selectedOptionIndex = null
                                        isCheckingAnswer = false
                                        
                                        if (currentQuestionIndex >= 5) {
                                            // Handle dynamically credited rewards on Firebase Firestore
                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            val uDocRef = db.collection("users").document(currentUserUid)
                                            db.runTransaction { tx ->
                                                val userSnap = tx.get(uDocRef)
                                                val currentBalance = when (val v = userSnap.get("balance")) {
                                                    is Number -> v.toDouble()
                                                    is String -> v.toDoubleOrNull() ?: 0.0
                                                    else -> 0.0
                                                }
                                                val currentEarnings = when (val v = userSnap.get("earnings")) {
                                                    is Number -> v.toDouble()
                                                    is String -> v.toDoubleOrNull() ?: 0.0
                                                    else -> 0.0
                                                }
                                                tx.update(uDocRef, "balance", currentBalance + rewardAmount)
                                                tx.update(uDocRef, "earnings", currentEarnings + rewardAmount)
                                                tx.update(uDocRef, "lastQuizTime", System.currentTimeMillis())
                                                tx.update(uDocRef, "lastAdCategoryTaskTime", System.currentTimeMillis())
                                                
                                                val nDoc = db.collection("notifications").document()
                                                val notifyData = hashMapOf(
                                                    "id" to nDoc.id,
                                                    "userId" to currentUserUid,
                                                    "title" to "Quiz Reward Earned 🏆",
                                                    "message" to "অভিনন্দন! আপনি কুইজের ৫টি প্রশ্নের সঠিক উত্তর দিয়ে ৳${String.format("%.2f", rewardAmount)} বোনাস পেয়েছেন।",
                                                    "type" to "SUCCESS",
                                                    "isRead" to false,
                                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                                )
                                                tx.set(nDoc, notifyData)
                                            }.addOnCompleteListener { taskResult ->
                                                showRewardDialog = true
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedOptionIndex != null && !isCheckingAnswer
                        ) {
                            Text(
                                text = if (currentQuestionIndex == 4) 
                                    (if (isEnglish) "Submit Quiz" else "কুইজ সাবমিট করুন")
                                    else (if (isEnglish) "Next Question" else "পরবর্তী প্রশ্ন"), 
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
