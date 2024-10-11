package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdHelper(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/1033173712"

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d("InterstitialAdHelper", "Ad Loaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("InterstitialAdHelper", "Ad failed to load: ${adError.message}")
                interstitialAd = null
            }
        })
    }

    // Show the Interstitial Ad and trigger a callback after the ad is dismissed
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialAdHelper", "Ad was dismissed.")
                    interstitialAd = null
                    loadAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    Log.e("InterstitialAdHelper", "Ad failed to show: ${p0?.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdHelper", "The interstitial ad wasn't ready yet.")
            onAdDismissed()
        }
    }
}
