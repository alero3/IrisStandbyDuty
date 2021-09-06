package com.reply.irisstandbyduty.ui.calendar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.reply.irisstandbyduty.ui.login.LoginState
import com.reply.irisstandbyduty.ui.login.LoginViewModel
import timber.log.Timber
import com.reply.irisstandbyduty.databinding.FragmentCalendarBinding
import com.reply.irisstandbyduty.domain.AuthenticationManager
import com.reply.irisstandbyduty.ui.calendar.adapter.CalendarAdapter
import com.reply.irisstandbyduty.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import java.lang.RuntimeException


@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()

    private var _binding: FragmentCalendarBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        authenticationManager?.checkAuthenticationStatus()

        binding.recyclerviewSchedule.adapter = CalendarAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObserver() {
        calendarViewModel.uiState.observe(viewLifecycleOwner, Observer {
            Timber.d("State update: $it")

            when (it) {
                is UiState.ShowCalendar -> {
                    hideLoader()

                    it.shifts.firstOrNull()?.date?.let { firstDate ->
                        val monthName = DateUtils.getMonthName(requireContext(), firstDate)
                        val year = DateUtils.getYear(requireContext(), firstDate)
                        binding.headerTextView.text = "$monthName $year"
                    }
                    binding.headerTextView.text
                    (binding.recyclerviewSchedule.adapter as CalendarAdapter)
                        .submitList(it.shifts)
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
                    calendarViewModel.loadCalendar(it.googleAccount)
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