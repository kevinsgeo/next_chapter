package com.cs407.next_chapter

import androidx.annotation.DrawableRes
import com.cs407.next_chapter.R

sealed class OnboardingModel(
    @DrawableRes val image: Int,
    val title: String,
    val description: String,
) {

    data object FirstPage : OnboardingModel(
        image = R.drawable.splash1,
        title = "Swap Books with People Nearby",
        description = "Exchange books with people in your area and discover new reads"
    )

    data object SecondPage : OnboardingModel(
        image = R.drawable.splash2,
        title = "Chat with Fellow Book Swappers",
        description = "Communicate directly with others to arrange book swaps easily"
    )

    data object ThirdPages : OnboardingModel(
        image = R.drawable.splash3,
        title = "Enable Location to Discover Nearby Books",
        description = "Turn on your location to find books available for swap around you"
    )
}