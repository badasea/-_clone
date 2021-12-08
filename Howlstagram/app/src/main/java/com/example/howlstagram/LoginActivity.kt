package com.example.howlstagram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var callbackManager : CallbackManager? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()

        google_sign_in_button.setOnClickListener {
            googleLogin()
        }

        facebook_login_button.setOnClickListener {
            facebookLogin()
        }

        email_login_button.setOnClickListener {
            emailLogin()
        }
    }
    fun moveMainPage(user : FirebaseUser?) {
        if(user != null) {
            Toast.makeText(this, getString(R.string.signin_complete),
            Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun googleLogin() {
        progress_bar.visibility = View.VISIBLE
        var signIntent = googleSignInClient?.signInIntent
        startActivityForResult(signIntent, GOOGLE_LOGIN_CODE)
    }

    fun facebookLogin() {
        progress_bar.visibility = View.VISIBLE

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFaccbookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                progress_bar.visibility = View.GONE
            }

            override fun onError(error: FacebookException) {
                progress_bar.visibility = View.GONE
            }
        })
    }

    fun handleFaccbookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                }
            }
    }

    fun createAndLoginEmail() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),
        password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.signup_complete), Toast.LENGTH_SHORT).show()
                    moveMainPage(auth?.currentUser)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                } else {
                    signinEmail()
                }
            }
    }

    fun emailLogin() {
        if (email_edittext.text.toString().isNullOrEmpty() ||
                password_edittext.text.toString().isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.signout_fail_null), Toast.LENGTH_SHORT).show()
        } else {
            progress_bar.visibility = View.VISIBLE
            createAndLoginEmail()
        }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),
        password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode,resultCode,data)

        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result!!.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                progress_bar.visibility = View.GONE
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                }
            }
    }
    override fun onStart() {
        super.onStart()

        moveMainPage(auth?.currentUser)
    }
}