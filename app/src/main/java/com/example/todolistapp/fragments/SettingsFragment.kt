package com.example.todolistapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.todolistapp.R
import com.example.todolistapp.LoginActivity
import android.widget.Button
import com.google.android.material.card.MaterialCardView

class SettingsFragment : Fragment() {
    private lateinit var accountSettingsCard: MaterialCardView
    private lateinit var aboutAppCard: MaterialCardView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        accountSettingsCard = view.findViewById(R.id.accountSettingsCard)
        aboutAppCard = view.findViewById(R.id.aboutAppCard)
        logoutButton = view.findViewById(R.id.logoutButton)

        setupClickListeners()


        return view
    }

    private fun setupClickListeners() {
        aboutAppCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutAppFragment())
                .addToBackStack(null)
                .commit()
        }

        accountSettingsCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AccountSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            requireActivity().getSharedPreferences("TodoApp", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            // Navigate to LoginActivity
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}