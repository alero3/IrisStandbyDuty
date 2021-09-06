package com.reply.irisstandbyduty.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.reply.irisstandbyduty.databinding.ActivityMainBinding
import timber.log.Timber
import android.view.Menu
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.view.MenuItem
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.snackbar.Snackbar
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.domain.AuthenticationManager
import com.reply.irisstandbyduty.domain.AuthenticationResultListener
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import com.reply.irisstandbyduty.ui.login.LoginState
import com.reply.irisstandbyduty.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    AuthenticationResultListener,
    AuthenticationManager {

    private lateinit var binding: ActivityMainBinding

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private var googleDriveAuthenticator: GoogleDriveAuthenticator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        googleDriveAuthenticator = GoogleDriveAuthenticator(this)
        // Set this as the listener.
        googleDriveAuthenticator?.authenticationResultListener = this

        binding.bottomNavigation.selectedItemId = R.id.bottom_nav_item_today
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_item_today -> {
                    navController.navigate(R.id.nav_home)
                }
                R.id.bottom_nav_item_schedule -> {
                    navController.navigate(R.id.nav_schedule)
                }
                R.id.bottom_nav_item_settings -> {

                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_profile -> {
                // Clicked Profile item.
                //addSomething()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onLoginSuccess(googleAccont: GoogleSignInAccount) {
        //Snackbar.make(binding.root, R.string.status_logged_in, Snackbar.LENGTH_LONG).show()
        loginViewModel.setAuthenticationStatus(
            LoginState.LoggedIn(googleAccont)
        )
    }

    override fun onLoginNotPerformed() {
        Snackbar.make(binding.root, R.string.status_not_logged_in, Snackbar.LENGTH_LONG).show()
        loginViewModel.setAuthenticationStatus(
            LoginState.NotLoggedIn
        )
    }

    override fun onLoginCancel() {
        Snackbar.make(binding.root, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
        loginViewModel.setAuthenticationStatus(
            LoginState.NotLoggedIn
        )
    }

    override fun onLoginError(exception: Exception) {
        Timber.e(exception, "error in login")
        val errorMessage = getString(R.string.status_error, exception.message)
        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
        loginViewModel.setAuthenticationStatus(
            LoginState.LoginError(exception)
        )
    }

    override fun login() {
        googleDriveAuthenticator?.auth()
    }

    override fun logout() {
        googleDriveAuthenticator?.logout()
    }

    override fun checkAuthenticationStatus() {
        googleDriveAuthenticator?.checkLoginStatus()
    }

}