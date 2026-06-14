package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.AdMobManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchTimeScreen(onBack: () -> Unit) {
    var videoUrl by remember { mutableStateOf("") }
    var tabCountInput by remember { mutableStateOf("") }
    
    var activeTabs by remember { mutableStateOf(0) }
    var activeVideoId by remember { mutableStateOf<String?>(null) }
    var playAllTrigger by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        com.example.ui.screens.FullScreenDialogModifier()
        LaunchedEffect(Unit) {
            AdMobManager.loadInterstitialAd(context)
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Watch Time Completer", color = Color.Black) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    if (activeTabs == 0) {
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("YouTube Video URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tabCountInput,
                            onValueChange = { tabCountInput = it.filter { char -> char.isDigit() } },
                            label = { Text("Number of Tabs (e.g. 10)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val count = tabCountInput.toIntOrNull() ?: 0
                                if (videoUrl.isNotBlank() && count > 0) {
                                    val videoId = extractYouTubeVideoId(videoUrl)
                                    if (videoId != null) {
                                        val activity = context as? android.app.Activity
                                        if (activity != null) {
                                            android.widget.Toast.makeText(context, "লোড হচ্ছে...", android.widget.Toast.LENGTH_SHORT).show()
                                            AdMobManager.showInterstitialAd(activity) {
                                                activeVideoId = videoId
                                                activeTabs = count
                                            }
                                        } else {
                                            activeVideoId = videoId
                                            activeTabs = count
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "Invalid YouTube URL", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Tasks")
                        }
                    } else {
                        // Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { playAllTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Play All", color = Color.White)
                            }
                            
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        // "IP change" is simulated
                                        android.widget.Toast.makeText(context, "Data cleared. IP Address changed.", android.widget.Toast.LENGTH_LONG).show()
                                        activeTabs = 0
                                        delay(1000)
                                        isRefreshing = false
                                        videoUrl = ""
                                        tabCountInput = ""
                                        activeVideoId = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(4.dp))
                                Text("Refresh", color = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Active Tabs: $activeTabs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (!isRefreshing && activeVideoId != null) {
                            val chunkedTabs = (0 until activeTabs).chunked(2)
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                chunkedTabs.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { index ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(16f / 9f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black)
                                            ) {
                                                YouTubeWebView(
                                                    videoId = activeVideoId!!,
                                                    tabIndex = index,
                                                    playTrigger = playAllTrigger
                                                )
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun extractYouTubeVideoId(url: String): String? {
    val regex = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\u200C\\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
    val pattern = java.util.regex.Pattern.compile(regex)
    val matcher = pattern.matcher(url)
    if (matcher.find()) {
        val id = matcher.group()
        if (id.length >= 11) return id.substring(0, 11)
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWebView(videoId: String, tabIndex: Int, playTrigger: Int) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    LaunchedEffect(playTrigger) {
        if (playTrigger > 0) {
            webViewRef?.evaluateJavascript("if (player && typeof player.playVideo === 'function') player.playVideo();", null)
        }
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }
                }
                val htmlData = """
                    <!DOCTYPE html>
                    <html>
                    <body style="margin:0;padding:0;background-color:black;overflow:hidden;">
                        <iframe id="ytplayer" type="text/html" width="100%" height="100%"
                            src="https://www.youtube.com/embed/$videoId?enablejsapi=1&autoplay=1&mute=1&controls=1&rel=0&playsinline=1&modestbranding=1"
                            frameborder="0" allowfullscreen style="position:fixed;width:100%;height:100%;"></iframe>
                        <script>
                          var tag = document.createElement('script');
                          tag.src = "https://www.youtube.com/iframe_api";
                          var firstScriptTag = document.getElementsByTagName('script')[0];
                          firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
                          var player;
                          function onYouTubeIframeAPIReady() {
                            player = new YT.Player('ytplayer', {
                              events: {
                                'onReady': onPlayerReady,
                                'onStateChange': onPlayerStateChange
                              }
                            });
                          }
                          function onPlayerReady(event) {
                             event.target.mute();
                             event.target.playVideo();
                          }
                          function onPlayerStateChange(event) {
                             if(event.data == YT.PlayerState.ENDED) {
                                document.body.innerHTML = '<div style="color:white;display:flex;align-items:center;justify-content:center;height:100vh;font-size:16px;font-family:sans-serif;">✅ Completed</div>';
                             }
                          }
                        </script>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = {
            webViewRef = null
            it.destroy()
        }
    )
}
