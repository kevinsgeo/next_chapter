package com.cs407.nextchapter

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.widget.Toast

class UploadBookFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upload_book, container, false)

        // Initialize views and set click listeners if needed
        val uploadPhotoButton: Button = view.findViewById(R.id.upload_photo_button)
        val scanIsbnButton: Button = view.findViewById(R.id.scan_isbn_button)
        val closeButton: ImageButton = view.findViewById(R.id.close_button)
        val mapButton: ImageButton = view.findViewById(R.id.mapicon)

        uploadPhotoButton.setOnClickListener {
            Toast.makeText(requireContext(), "Upload Photo Button Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement upload photo functionality
        }

        scanIsbnButton.setOnClickListener {
            Toast.makeText(requireContext(), "Scan ISBN Button Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement scan ISBN functionality
        }
        closeButton.setOnClickListener{
            Toast.makeText(requireContext(), "Close Button Clicked", Toast.LENGTH_SHORT).show()
        }
        mapButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment())
                .commit()

        }

        return view
    }
}
