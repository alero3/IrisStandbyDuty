package com.reply.irisstandbyduty.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.reply.irisstandbyduty.databinding.FragmentHomeBinding
import com.reply.irisstandbyduty.ui.login.LoginState
import com.reply.irisstandbyduty.ui.login.LoginViewModel
import timber.log.Timber
import com.bumptech.glide.Glide
import com.reply.irisstandbyduty.domain.AuthenticationManager
import dagger.hilt.android.AndroidEntryPoint
import java.lang.RuntimeException


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Create a storage reference from our app.
    var storageRef: StorageReference = FirebaseStorage.getInstance().reference

    private var authenticationManager: AuthenticationManager? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is AuthenticationManager) {
            authenticationManager = context
        } else {
            throw RuntimeException("$context must implement ${AuthenticationManager::class.java.simpleName}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        authenticationManager?.checkAuthenticationStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObserver() {
        homeViewModel.uiState.observe(viewLifecycleOwner, Observer {
            Timber.d("State update: $it")

            when (it) {
                is UiState.OnCallEmployeeAvailable -> {
                    hideLoader()
                    binding.onCallEmployeeTextView.text = it.employee.name

                    val pictureLocation = storageRef.child("${it.employee.profilePictureUrl}.png")
                    pictureLocation.downloadUrl
                        .addOnSuccessListener { uri ->
                            val imageURL = uri.toString()
                            Glide.with(requireContext())
                                .load(imageURL)
                                .circleCrop()
                                .into(binding.profilePictureImageView)
                        }.addOnFailureListener { e ->
                            // Handle any errors
                            Timber.e(e, "Error in image loading")
                        }
                }
                is UiState.Loading -> {
                    showLoader()
                }
                is UiState.Error -> {
                    hideLoader()

                }
            }
        })

        loginViewModel.loginState.observe(requireActivity(), Observer {
            when (it) {
                LoginState.Loading -> {
                    showLoader()
                }
                is LoginState.LoggedIn -> {
                    homeViewModel.loadCurrentOnCallEmployee(it.googleAccount)
                }
                is LoginState.LoginError -> {

                }
                LoginState.NotLoggedIn -> {

                }
            }
        })

    }

    private fun showLoader() {
        binding.layoutSuccess.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.layoutSuccess.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }



}