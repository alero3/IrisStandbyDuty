package com.reply.irisstandbyduty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.reply.irisstandbyduty.databinding.ActivityMainBinding
import timber.log.Timber
import android.view.Menu
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.snackbar.Snackbar
import com.reply.irisstandbyduty.domain.AuthenticationListener
import com.reply.irisstandbyduty.domain.service.GoogleDriveAuthenticator
import com.reply.irisstandbyduty.ui.LoginViewModelFactory
import com.reply.irisstandbyduty.ui.login.LoginState
import com.reply.irisstandbyduty.ui.login.LoginViewModel


class MainActivity : AppCompatActivity(), AuthenticationListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        val googleDriveAuthenticator = GoogleDriveAuthenticator(this)
        // Set this as the listener.
        googleDriveAuthenticator.authenticationListener = this

        // Create ViewModel.
        loginViewModel = ViewModelProvider(
            this, LoginViewModelFactory(
                googleDriveAuthenticator
            )
        ).get(LoginViewModel::class.java)

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
        Snackbar.make(binding.root, R.string.status_logged_in, Snackbar.LENGTH_LONG).show()
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

    fun login() {
    }

    fun logout() {
    }

}