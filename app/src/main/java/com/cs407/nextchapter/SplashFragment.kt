package com.cs407.nextchapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Launch a coroutine to delay for 5 seconds
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000) // 5000 milliseconds = 5 seconds

            // Navigate to the next fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Onboarding0Fragment())
                .commit()
        }
    }
}
