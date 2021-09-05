package com.reply.irisstandbyduty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.reply.irisstandbyduty.databinding.FragmentHomeBinding
import com.reply.irisstandbyduty.domain.MonthParser
import com.reply.irisstandbyduty.domain.StandbyDutyCalendarParser
import com.reply.irisstandbyduty.domain.service.sheets.SheetsReader
import com.reply.irisstandbyduty.domain.usecase.LoadOnCallCalendarUseCase
import com.reply.irisstandbyduty.ui.HomeViewModelFactory
import com.reply.irisstandbyduty.ui.login.LoginState
import com.reply.irisstandbyduty.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import com.bumptech.glide.Glide


class HomeFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Create a storage reference from our app.
    var storageRef: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this, HomeViewModelFactory(
            loadOnCallCalendarUseCase = LoadOnCallCalendarUseCase(
                calendarParser = StandbyDutyCalendarParser(monthParser = MonthParser()),
                sheetsReader = SheetsReader(context = requireContext()),
                coroutineDispatcher = Dispatchers.IO
            )
        )).get(HomeViewModel::class.java)


        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        loginViewModel.checkAuthenticationStatus()
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
                            Glide.with(requireContext()).load(imageURL)
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