package com.example.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler

class RegisterUserActivity : AppCompatActivity() {

    private var userAttributes: CognitoUserAttributes? = null
    private var userpasswordInput: String? = null
    private val TAG = "SignUp"

    private var username: EditText? = null
    private var password: EditText? = null
    private var givenName: EditText? = null
    private val familyName: EditText? = null
    private var email: EditText? = null
    private var phone: EditText? = null

    private var signUp: Button? = null
    private var userDialog: AlertDialog? = null
    private var waitDialog: ProgressDialog? = null
    private var usernameInput: String? = null
    private var userPasswd: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val extras = intent.extras
        if (extras != null) {
            // get back to main screen
            val value = extras.getString("TODO")
            if (value == "exit") {
                onBackPressed()
            }
        }

        init()
    }

    internal var signUpHandler: SignUpHandler = object : SignUpHandler {
        override fun onSuccess(
            user: CognitoUser, signUpConfirmationState: Boolean,
            cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails
        ) {
            // Check signUpConfirmationState to see if the user is already confirmed
            closeWaitDialog()
            val regState = signUpConfirmationState
            if (signUpConfirmationState) {
                // User is already confirmed
                Toast.makeText(
                    this@RegisterUserActivity,
                    "Sign up successful!\", \"$usernameInput has been Confirmed",
                    Toast.LENGTH_SHORT
                )

            } else {
                // User is not confirmed
//                confirmSignUp(cognitoUserCodeDeliveryDetails)
            }
        }

        override fun onFailure(exception: Exception) {
            closeWaitDialog()

            Toast.makeText(this@RegisterUserActivity, "Sign up failed", Toast.LENGTH_SHORT)
        }
    }

    private fun confirmSignUp(cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails) {
        val intent = Intent(this, SignUpConfirm::class.java)
        intent.putExtra("source", "signup")
        intent.putExtra("name", usernameInput)
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.destination)
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.deliveryMedium)
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.attributeName)
        startActivityForResult(intent, 10)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                var name: String? = null
                if (data!!.hasExtra("name")) {
                    name = data.getStringExtra("name")
                }
                exit(name, userPasswd)
            }
        }
    }


    private fun confirmUser() {
        val confirmActivity = Intent(this, UserActivity::class.java)
        confirmActivity.putExtra("source", "main")
        startActivity(confirmActivity)

    }

    private fun init() {
        phone = this.findViewById(R.id.editTextRegPhone)

        password = findViewById(R.id.editTextRegUserPassword);

        phone = findViewById(R.id.editTextRegPhone);

        signUp = findViewById(R.id.signUp) as Button
        signUp?.setOnClickListener(View.OnClickListener {
            // Read user data and register
            userAttributes = CognitoUserAttributes()

            usernameInput = phone?.getText().toString()

            userpasswordInput = password?.getText().toString()
            userPasswd = userpasswordInput

            showWaitDialog("Signing up...")

            AppHelper.getPool()
                .signUpInBackground(usernameInput, userpasswordInput, userAttributes, null, signUpHandler)

            confirmUser()
        })

    }

    private fun showWaitDialog(message: String) {
        closeWaitDialog()
        waitDialog = ProgressDialog(this)
        waitDialog!!.setTitle(message)
        waitDialog!!.show()
    }

    private fun closeWaitDialog() {
        try {
            waitDialog?.dismiss()
        } catch (e: Exception) {
            //
        }

    }

    private fun exit(uname: String) {
        exit(uname, null)
    }

    private fun exit(uname: String?, password: String?) {
        var uname = uname
        var password = password
        val intent = Intent()
        if (uname == null) {
            uname = ""
        }
        if (password == null) {
            password = ""
        }
        intent.putExtra("name", uname)
        intent.putExtra("password", password)
        setResult(RESULT_OK, intent)
        finish()
    }


}
