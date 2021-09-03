package com.reply.irisstandbyduty.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.databinding.FragmentHomeBinding
import com.reply.irisstandbyduty.domain.ServiceListener
import com.reply.irisstandbyduty.domain.service.sheets.ReadSheetCallback
import com.reply.irisstandbyduty.domain.service.sheets.SheetsService

class HomeFragment : Fragment(), ServiceListener {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sheetsService: SheetsService

    private var state = ButtonState.LOGGED_OUT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sheetsService = SheetsService(this)

        // Set this as the listener.
        sheetsService.serviceListener = this

        // Change the state to logged-in if there is any logged-in account present.
        sheetsService.checkLoginStatus()

        binding.login.setOnClickListener {
            sheetsService.auth()
        }
        binding.start.setOnClickListener {
            sheetsService.readSheet(
                id = "1s4igidc3c3z5u6fNSWc7Dwsyz1lObUC3KptXaDYH38A",
                range = "A1:AF16",
                sheetName = null,
                callback = object : ReadSheetCallback {
                    override fun onSuccess(data: ArrayList<*>) {
                        //TODO start parsing
                    }

                    override fun onFailure(exception: java.lang.Exception) {
                    }

                    override fun onCancel() {

                    }
                }
            )
        }

        binding.logout.setOnClickListener {
            sheetsService.logout()
            state = ButtonState.LOGGED_OUT
            setButtons()
        }

        setButtons()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLoginSuccess() {
        state = ButtonState.LOGGED_IN
        setButtons()
    }

    override fun onLoginCancel() {
        Snackbar.make(binding.root, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
    }

    override fun onLoginError(exception: Exception) {
        Log.e("HomeFragment", "error in login", exception)
        val errorMessage = getString(R.string.status_error, exception.message)
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun setButtons() {
        when (state) {
            ButtonState.LOGGED_OUT -> {
                binding.status.text = getString(R.string.status_logged_out)
                binding.start.isEnabled = false
                binding.logout.isEnabled = false
                binding.login.isEnabled = true
            }

            else -> {
                binding.status.text = getString(R.string.status_logged_in)
                binding.start.isEnabled = true
                binding.logout.isEnabled = true
                binding.login.isEnabled = false
            }
        }
    }

}