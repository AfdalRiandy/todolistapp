package com.example.todolistapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.todolistapp.database.DatabaseHelper
import com.example.todolistapp.database.User
import com.example.todolistapp.databinding.FragmentAccountSettingsBinding

class AccountSettingsFragment : Fragment() {
    private lateinit var binding: FragmentAccountSettingsBinding
    private lateinit var dbHelper: DatabaseHelper
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Load current user data
        loadUserData()

        // Set up click listeners
        setupClickListeners()
    }

    private fun loadUserData() {
        // Get current user ID from SharedPreferences
        val sharedPrefs = requireActivity().getSharedPreferences("TodoApp", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("USER_ID", -1)

        // Get user data from database
        currentUser = dbHelper.getUserById(userId)

        // Display user data
        currentUser?.let { user ->
            binding.usernameInput.setText(user.username)
            binding.emailText.text = "Email: ${user.email}"
        }
    }

    private fun setupClickListeners() {
        binding.updateProfileButton.setOnClickListener {
            updateProfile()
        }

        binding.updatePasswordButton.setOnClickListener {
            updatePassword()
        }
    }

    private fun updateProfile() {
        val newUsername = binding.usernameInput.text.toString().trim()

        if (newUsername.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
            return
        }

        currentUser?.let { user ->
            user.username = newUsername
            val result = dbHelper.updateUserProfile(user)

            if (result > 0) {
                showToast("Profile updated successfully")
            } else {
                showToast("Failed to update profile")
            }
        }
    }

    private fun updatePassword() {
        val currentPassword = binding.currentPasswordInput.text.toString()
        val newPassword = binding.newPasswordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill all password fields")
            return
        }

        if (newPassword != confirmPassword) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
            return
        }

        currentUser?.let { user ->
            if (user.password != currentPassword) {
                binding.currentPasswordLayout.error = "Current password is incorrect"
                return
            }

            user.password = newPassword
            val result = dbHelper.updateUserPassword(user)

            if (result > 0) {
                showToast("Password updated successfully")
                clearPasswordFields()
            } else {
                showToast("Failed to update password")
            }
        }
    }

    private fun clearPasswordFields() {
        binding.currentPasswordInput.text?.clear()
        binding.newPasswordInput.text?.clear()
        binding.confirmPasswordInput.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}