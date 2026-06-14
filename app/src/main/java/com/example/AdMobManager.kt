package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    private var rewardedAd: RewardedAd? = null
    private var spinRewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-4288324218526190/8832383188",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdMob", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "Ad was loaded.")
                    rewardedAd = ad
                }
            })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (rewardedAd != null) {
            var rewardEarned = false
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Ad was dismissed.")
                    rewardedAd = null
                    loadRewardedAd(activity) // Preload next ad
                    if (rewardEarned) {
                        onRewardEarned()
                    } else {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdMob", "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdMob", "Ad showed fullscreen content.")
                }
            }

            rewardedAd?.show(activity) { rewardItem ->
                Log.d("AdMob", "User earned the reward.")
                rewardEarned = true
            }
        } else {
            Log.d("AdMob", "The rewarded ad wasn't ready yet.")
            onAdDismissed() // Fallback if ad isn't loaded
        }
    }

    fun loadSpinRewardedAd(context: Context) {
        if (spinRewardedAd != null) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-4288324218526190/1290229653",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdMob", "Spin Ad failed to load: ${adError.message}")
                    spinRewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "Spin Ad was loaded.")
                    spinRewardedAd = ad
                }
            })
    }

    fun showSpinRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        if (spinRewardedAd != null) {
            var rewardEarned = false
            spinRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Spin Ad was dismissed.")
                    spinRewardedAd = null
                    loadSpinRewardedAd(activity) // Preload next spin ad
                    if (rewardEarned) {
                        onRewardEarned()
                    } else {
                        onAdDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdMob", "Spin Ad failed to show: ${adError.message}")
                    spinRewardedAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdMob", "Spin Ad showed fullscreen content.")
                }
            }

            spinRewardedAd?.show(activity) { rewardItem ->
                Log.d("AdMob", "User earned spin reward.")
                rewardEarned = true
            }
        } else {
            Log.d("AdMob", "The spin rewarded ad wasn't ready yet.")
            onAdDismissed() // Fallback if ad isn't loaded
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-4288324218526190/1290229653",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdMob", "Interstitial Ad failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdMob", "Interstitial Ad was loaded.")
                    interstitialAd = ad
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Interstitial Ad was dismissed.")
                    interstitialAd = null
                    loadInterstitialAd(activity) // Preload next
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdMob", "Interstitial Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("AdMob", "The interstitial ad wasn't ready yet.")
            onAdDismissed()
        }
    }
}
