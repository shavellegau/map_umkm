package com.example.map_umkm.utils

import com.example.map_umkm.R

data class TierInfo(
    val tierName: String,
    val tierIndex: Int, 
    val maxXp: Int,
    val iconRes: Int
)

object TierCalculator {
    
    fun calculateTier(xp: Int): TierInfo {
        return when {
            xp < 100 -> TierInfo("Bronze", 0, 100, R.drawable.ic_tier_bronze)
            xp < 250 -> TierInfo("Silver", 1, 250, R.drawable.ic_tier_silver)
            xp < 400 -> TierInfo("Gold", 2, 400, R.drawable.ic_tier_gold)
            xp < 600 -> TierInfo("Platinum", 3, 600, R.drawable.ic_tier_platinum)
            else -> TierInfo("Diamond", 4, 1000, R.drawable.ic_tier_diamond)
        }
    }
}