package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler

class UserActivity : AppCompatActivity() {
    private val TAG = "UserActivity"

    private var user: CognitoUser? = null
    private var username: String? = null
    private var attributesList: ListView? = null

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
        menuInflater.inflate(R.menu.activity_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuItem = item.itemId
        if (menuItem == R.id.nav_user_sign_out) {
            user?.signOut()
            exit()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init() {
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

    fun openPubSub(view: View) {
        startActivity(Intent(this, SendMessageActivity::class.java))
    }

}
