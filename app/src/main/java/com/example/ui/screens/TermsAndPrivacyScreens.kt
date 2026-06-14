package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyDocumentScreen(
    title: String,
    onBack: () -> Unit,
    englishContent: String,
    bengaliContent: String
) {
    var isBengali by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { isBengali = !isBengali }) {
                        Icon(Icons.Filled.Language, contentDescription = "Language", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBengali) "English" else "বাংলা")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isBengali) bengaliContent else englishContent,
                        color = Color(0xFF333333),
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

val termsEnglish = """
Terms and Conditions

Welcome to CircleBiz! By using our application, you agree to these terms:

1. General
- You must comply with all applicable laws and regulations.
- Accounts are strictly personal and cannot be shared.

2. Earning Tasks
- Daily Spin: Users can spin the wheel to earn rewards based on chance, up to the daily limit.
- Scratch Card: Scratch areas to reveal rewards. Abuse of this feature will result in a ban.
- Watch Time & Ads: Ads must be viewed genuinely. Use of ad blockers or automated scripts is strictly prohibited.
- Job completion: Tasks such as typing jobs or micro-jobs must be completed authentically.

3. Refer and Earn
- You will earn a bonus when someone registers using your valid referral code.
- Fake accounts created to gain referral bonuses will lead to permanent suspension.

4. Withdrawals & Wallet
- Minimum withdrawal limits apply based on the chosen payment method (e.g., bKash, Nagad, Mobile Recharge).
- Payouts may take up to 24-72 hours to process.

5. Account Suspension
- Any violation of these terms will lead to immediate account termination without prior notice.
""".trimIndent()

val termsBengali = """
শর্তাবলী

CircleBiz-এ আপনাকে স্বাগতম! আমাদের অ্যাপ ব্যবহার করার মাধ্যমে আপনি নিম্নলিখিত শর্তাবলীতে সম্মত হচ্ছেন:

১. সাধারণ
- আপনাকে অবশ্যই প্রযোজ্য সকল আইন ও প্রবিধান মেনে চলতে হবে।
- অ্যাকাউন্ট কঠোরভাবে ব্যক্তিগত এবং শেয়ার করা যাবে না।

২. ইনকামের কাজসমূহ
- ডেইলি স্পিন: ব্যবহারকারীরা দৈনিক সীমার উপর ভিত্তি করে স্পিন করে পুরস্কার জিততে পারেন।
- স্ক্র্যাচ কার্ড: পুরস্কার পেতে কার্ড স্ক্র্যাচ করুন। এই ফিচারের অপব্যবহার করলে অ্যাকাউন্ট ব্যান করা হবে।
- ওয়াচ টাইম এবং অ্যাডস: অ্যাডগুলো সঠিকভাবে দেখতে হবে। অ্যাড ব্লকার বা অটোমেটেড স্ক্রিপ্ট ব্যবহার সম্পূর্ণ নিষিদ্ধ।
- জব কমপ্লিশন: টাইপিং জব বা মাইক্রো-জবগুলো অবশ্যই সততার সাথে সম্পন্ন করতে হবে।

৩. রেফার এবং আর্ন
- আপনার বৈধ রেফারেল কোড ব্যবহার করে কেউ নিবন্ধন করলে আপনি বোনাস পাবেন।
- রেফারেল বোনাস পাওয়ার জন্য ফেইক অ্যাকাউন্ট তৈরি করলে অ্যাকাউন্ট স্থায়ীভাবে সাসপেন্ড করা হবে।

৪. উইথড্র ও ওয়ালেট
- পেমেন্ট মেথড অনুযায়ী (যেমন: বিকাশ, নগদ, মোবাইল রিচার্জ) সর্বনিম্ন উইথড্র সীমা প্রযোজ্য।
- পেমেন্ট প্রসেস হতে ২৪-৭২ ঘণ্টা পর্যন্ত সময় লাগতে পারে।

৫. অ্যাকাউন্ট সাসপেনশন
- এই শর্তাবলীর যেকোনো লঙ্ঘন পূর্ব ঘোষণা ছাড়াই অ্যাকাউন্ট বাতিলের কারণ হবে।
""".trimIndent()

val privacyEnglish = """
Privacy Policy

Your privacy is important to CircleBiz. This policy outlines how we handle your data.

1. Information We Collect
- Profile Data: We collect your name, mobile number, and profile picture (if provided) during registration.
- Device & Usage Data: We collect information about your interactions with ads, completed tasks, and spins to calculate rewards.

2. How We Use Your Data
- To manage your wallet balance and process withdrawals.
- To verify task completions (e.g., job posts, micro-jobs, watch time).
- To detect fraud and prevent abuse of our reward systems (like fake referrals or bot usage).

3. Data Sharing
- We do not sell your personal information to third parties.
- We may share generalized data with advertising partners (like AdMob) to serve relevant ads.

4. Data Security
- We use industry-standard encryption to protect your account and wallet details.
- However, no internet transmission is 100% secure. You are responsible for keeping your password/OTP safe.

5. Changes to Policy
- We reserve the right to modify this privacy policy at any time. Significant changes will be notified within the app.
""".trimIndent()

val privacyBengali = """
গোপনীয়তা নীতি

CircleBiz-এর কাছে আপনার গোপনীয়তা গুরুত্বপূর্ণ। আমরা কীভাবে আপনার ডেটা পরিচালনা করি তা এই নীতিতে বর্ণনা করা হয়েছে।

১. আমরা যা সংগ্রহ করি
- প্রোফাইল ডেটা: রেজিস্ট্রেশনের সময় আমরা আপনার নাম, মোবাইল নম্বর এবং প্রোফাইল ছবি সংগ্রহ করি।
- ডিভাইস এবং ইউসেজ ডেটা: আপনার অ্যাড দেখা, সম্পূর্ণ করা কাজ এবং স্পিন বিষয়ক তথ্য আমরা সংগ্রহ করি রিওয়ার্ড হিসাব করার জন্য।

২. আপনার ডেটার ব্যবহার
- আপনার ওয়ালেট ব্যালেন্স পরিচালনা এবং উইথড্র প্রসেস করার জন্য।
- টাস্ক কমপ্লিশন যাচাই করার জন্য (যেমন: জব পোস্ট, মাইক্রো-জব, ওয়াচ টাইম)।
- ফ্রড ডিটেক্ট করতে এবং আমাদের রিওয়ার্ড সিস্টেমের অপব্যবহার রোধ করতে (যেমন ফেক রেফারেল বা বট ব্যবহার)।

৩. ডেটা শেয়ারিং
- আমরা তৃতীয় পক্ষের কাছে আপনার ব্যক্তিগত তথ্য বিক্রি করি না।
- আপনাকে প্রাসঙ্গিক অ্যাড দেখানোর জন্য আমরা অ্যাডভার্টাইজিং পার্টনারদের (যেমন AdMob) সাথে সাধারণ ডেটা শেয়ার করতে পারি।

৪. ডেটা নিরাপত্তা
- আমরা আপনার অ্যাকাউন্ট এবং ওয়ালেটের বিবরণ সুরক্ষিত রাখতে আধুনিক এনক্রিপশন ব্যবহার করি।
- তবে, ইন্টারনেটের কোনো ট্রান্সমিশনই ১০০% নিরাপদ নয়। আপনার পাসওয়ার্ড/ওটিপি সুরক্ষিত রাখার দায়িত্ব আপনার।

৫. নীতি পরিবর্তন
- যেকোনো সময় এই গোপনীয়তা নীতি পরিবর্তন করার অধিকার আমরা রাখি। গুরুত্বপূর্ণ পরিবর্তনগুলো অ্যাপের মাধ্যমে জানিয়ে দেওয়া হবে।
""".trimIndent()

@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    PolicyDocumentScreen(
        title = "Terms & Conditions",
        onBack = onBack,
        englishContent = termsEnglish,
        bengaliContent = termsBengali
    )
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    PolicyDocumentScreen(
        title = "Privacy Policy",
        onBack = onBack,
        englishContent = privacyEnglish,
        bengaliContent = privacyBengali
    )
}
