package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import java.util.*

class MainActivity : AppCompatActivity() {

    private var inUsername: EditText? = null
    private var inPassword: EditText? = null

    private var username: String? = null
    private var password: String? = null

    private var waitDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppHelper.init(applicationContext)
        initApp()
        findCurrent()
    }

    private fun findCurrent() {
        val user = AppHelper.getPool().currentUser
        username = user.userId
        if (username != null) {
            AppHelper.setUser(username)
            inUsername?.setText(user.userId)
            user.getSessionInBackground(authenticationHandler)
        }
    }

    private fun initApp() {
        inUsername = findViewById(R.id.editTextUserId)
        inPassword = findViewById(R.id.editTextUserPassword)
    }

    fun signUp(view: View) {
        signUpNewUser()
    }

    private fun signUpNewUser() {
        val registerActivity = Intent(this, RegisterUserActivity::class.java)
        startActivity(registerActivity)
    }

    var authenticationHandler: AuthenticationHandler = object : AuthenticationHandler {
        override fun authenticationChallenge(continuation: ChallengeContinuation?) {
        }

        override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
        }

        override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice) {
            Log.d("MAINACTIVITY", " -- Auth Success")
            AppHelper.setCurrSession(cognitoUserSession)
            AppHelper.newDevice(device)
            closeWaitDialog()
            launchUser()
        }

        override fun getAuthenticationDetails(
            authenticationContinuation: AuthenticationContinuation,
            username: String
        ) {
            closeWaitDialog()
            Locale.setDefault(Locale.US)
            getUserAuthentication(authenticationContinuation, username)
        }

        override fun onFailure(e: Exception) {
            closeWaitDialog()
//            Toast.makeText(this@MainActivity, "Sign-in failed", Toast.LENGTH_SHORT).show()
            return
        }
    }

    fun signInUser(view: View) {
        username = inUsername?.getText().toString();
        password = inPassword?.getText().toString();
        if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(this, "Enter all the details", Toast.LENGTH_SHORT).show()
            return

        } else {
            AppHelper.setUser(username);

            showWaitDialog("Signing in...");
            Log.d("MAINACTIVITY", "signing in with username = $username password =$password")
            AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
            startActivity(Intent(this, UserActivity::class.java))
            finish()
        }
    }

    private fun showWaitDialog(message: String) {
        closeWaitDialog()
        waitDialog = ProgressDialog(this)
        waitDialog?.setTitle(message)
        waitDialog?.show()
    }

    private fun closeWaitDialog() {
        try {
            waitDialog?.dismiss()
        } catch (e: Exception) {
            //
        }

    }

    private fun getUserAuthentication(continuation: AuthenticationContinuation, username: String?) {
        if (username != null) {
            this.username = username
            AppHelper.setUser(username)
        }
        val authenticationDetails = AuthenticationDetails(this.username, password, null)
        continuation.setAuthenticationDetails(authenticationDetails)
        continuation.continueTask()
    }

    private fun launchUser() {
        val userActivity = Intent(this, UserActivity::class.java)
        userActivity.putExtra("name", username)
        startActivity(userActivity)
        Log.d("MAINACTIVITY", "starting user activity with username ${username}")
        finish()
    }

}
