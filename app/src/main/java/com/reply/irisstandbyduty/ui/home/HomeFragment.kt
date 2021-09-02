package com.reply.irisstandbyduty.ui.home

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.databinding.FragmentHomeBinding
import com.reply.irisstandbyduty.domain.GoogleDriveConfig
import com.reply.irisstandbyduty.domain.ServiceListener
import com.reply.irisstandbyduty.domain.service.GoogleDriveService
import java.io.File

class HomeFragment : Fragment(), ServiceListener {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var googleDriveService: GoogleDriveService

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

        googleDriveService = GoogleDriveService(this, requireActivity())

        // Set this as the listener.
        googleDriveService.serviceListener = this

        // Change the state to logged-in if there is any logged-in account present.
        googleDriveService.checkLoginStatus()

        binding.login.setOnClickListener {
            googleDriveService.auth()
        }
        binding.start.setOnClickListener {
            googleDriveService.downloadFileWithId("273923671")
        }
        binding.createFile.setOnClickListener {
            googleDriveService.createFile()
        }
        binding.logout.setOnClickListener {
            googleDriveService.logout()
            state = ButtonState.LOGGED_OUT
            setButtons()
        }

        setButtons()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun loggedIn() {
        state = ButtonState.LOGGED_IN
        setButtons()
    }

    override fun fileDownloaded(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val apkURI = FileProvider.getUriForFile(
            requireContext(),
            requireActivity().applicationContext.packageName + ".provider",
            file)
        val uri = Uri.fromFile(file)
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        intent.setDataAndType(apkURI, mimeType)
        intent.flags = FLAG_GRANT_READ_URI_PERMISSION
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, R.string.not_open_file, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun cancelled() {
        Snackbar.make(binding.root, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
    }

    override fun handleError(exception: Exception) {
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