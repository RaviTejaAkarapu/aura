package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ListView
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler

class UserActivity : AppCompatActivity() {
    private val TAG = "UserActivity"

    // Cognito user objects
    private var user: CognitoUser? = null

    // User details
    private var username: String? = null

    private var attributesList: ListView? = null

    // To track changes to user details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        init()
    }

    internal var detailsHandler: GetDetailsHandler = object : GetDetailsHandler {
        override fun onSuccess(cognitoUserDetails: CognitoUserDetails) {
            AppHelper.setUserDetails(cognitoUserDetails)
        }

        override fun onFailure(exception: Exception) {
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Find which menu item was selected
        val menuItem = item.itemId
        // Do the task
        if (menuItem == R.id.nav_user_sign_out) {
            user?.signOut()
            exit()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        // Get the user name
        val extras = intent.extras
        username = AppHelper.getCurrUser()
        user = AppHelper.getPool().getUser(username)
        getDetails()
    }

    private fun exit() {
        val intent = Intent()
        if (username == null)
            username = ""
        intent.putExtra("name", username)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun getDetails() {
        AppHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler)
    }

    fun openPubSub(view:View) {
        startActivity(Intent(this, SendMessageActivity::class.java))
    }

}
