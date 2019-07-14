package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler

class SignUpConfirm : AppCompatActivity() {
    private var username: EditText? = null
    private var confCode: EditText? = null

    private var confirm: Button? = null
    private var reqCode: TextView? = null
    private var userName: String? = null
    private var userDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_sign_up_confirm)

        init()
    }


    private fun init() {

        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("name")) {
                userName = extras.getString("name")
                username = findViewById(R.id.editTextConfirmUserId) as EditText
                username?.setText(userName)

                confCode = findViewById(R.id.editTextConfirmCode) as EditText
                confCode?.requestFocus()

                if (extras.containsKey("destination")) {
                    val dest = extras.getString("destination")
                    val delMed = extras.getString("deliveryMed")

                    val screenSubtext = findViewById(R.id.textViewConfirmSubtext_1) as TextView
                    if (dest != null && delMed != null && dest.length > 0 && delMed.length > 0) {
                        screenSubtext.text = "A confirmation code was sent to $dest via $delMed"
                    } else {
                        screenSubtext.text = "A confirmation code was sent"
                    }
                }
            } else {
                val screenSubtext = findViewById(R.id.textViewConfirmSubtext_1) as TextView
                screenSubtext.text = "Request for a confirmation code or confirm with the code you already have."
            }

        }

        username = findViewById(R.id.editTextConfirmUserId) as EditText
        confCode = findViewById(R.id.editTextConfirmCode) as EditText

        confirm = findViewById(R.id.confirm_button) as Button
        confirm?.setOnClickListener(View.OnClickListener { sendConfCode() })

        reqCode = findViewById(R.id.resend_confirm_req) as TextView
        reqCode?.setOnClickListener(View.OnClickListener { reqConfCode() })
    }

    private fun sendConfCode() {
        userName = username?.getText().toString()
        val confirmCode = confCode?.getText().toString()

        AppHelper.getPool().getUser(userName).confirmSignUpInBackground(confirmCode, true, confHandler)
    }

    private fun reqConfCode() {
        userName = username?.getText().toString()

        AppHelper.getPool().getUser(userName).resendConfirmationCodeInBackground(resendConfCodeHandler)
    }

    internal var confHandler: GenericHandler = object : GenericHandler {
        override fun onSuccess() {
            showDialogMessage("Success!", "$userName has been confirmed!", true)
        }

        override fun onFailure(exception: Exception) {
            var label = findViewById(R.id.textViewConfirmUserIdMessage) as TextView
            label.text = "Confirmation failed!"

            label = findViewById(R.id.textViewConfirmCodeMessage) as TextView
            label.text = "Confirmation failed!"

            showDialogMessage("Confirmation failed", AppHelper.formatException(exception), false)
        }
    }

    internal var resendConfCodeHandler: VerificationHandler = object : VerificationHandler {
        override fun onSuccess(cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails) {
            val mainTitle = findViewById(R.id.textViewConfirmTitle) as TextView
            mainTitle.text = "Confirm your account"
            confCode = findViewById(R.id.editTextConfirmCode) as EditText
            confCode?.requestFocus()
            showDialogMessage(
                "Confirmation code sent.",
                "Code sent to " + cognitoUserCodeDeliveryDetails.destination + " via " + cognitoUserCodeDeliveryDetails.deliveryMedium + ".",
                false
            )
        }

        override fun onFailure(exception: Exception) {
            val label = findViewById(R.id.textViewConfirmUserIdMessage) as TextView
            label.text = "Confirmation code resend failed"
            showDialogMessage("Confirmation code request has failed", AppHelper.formatException(exception), false)
        }
    }

    private fun showDialogMessage(title: String, body: String, exitActivity: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(body).setNeutralButton(
            "OK"
        ) { dialog, which ->
            try {
                userDialog?.dismiss()
                if (exitActivity) {
                    exit()
                }
            } catch (e: Exception) {
                exit()
            }
        }
        userDialog = builder.create()
        userDialog?.show()
    }

    private fun exit() {
        val intent = Intent()
        if (userName == null)
            userName = ""
        intent.putExtra("name", userName)
        setResult(RESULT_OK, intent)
        finish()
    }

}
