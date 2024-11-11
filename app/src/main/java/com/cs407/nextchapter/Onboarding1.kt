package com.cs407.nextchapter

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class Onboarding1Fragment : Fragment(R.layout.activity_onboarding1) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigate to the next fragment
        view.findViewById<Button>(R.id.btnNext2).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Onboarding2Fragment())
                .commit()
        }
    }
}
