package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Class that controls the authentication system for the users.
 *
 * @author github: dnabr04
 * @date 23/03/2025
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    /**
     * Executed when the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth

        loadFirebaseAuthenticator()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if( currentUser != null ){
            goToHome()
        }
    }

    /**
     * Log in or sign up with firebase authenticator
     */
    fun loadFirebaseAuthenticator(){
        val buttonSignup = findViewById<Button>( R.id.buttonSignup )
        val buttonLogin = findViewById<Button>( R.id.buttonLogin )
        val buttonGoogle = findViewById<Button>( R.id.buttonGoogle )
        val email = findViewById<EditText>( R.id.emailInput ).text
        val password = findViewById<EditText>( R.id.passwordInput ).text

        buttonSignup.setOnClickListener {

            if( email.isNotEmpty() && password.isNotEmpty() ){
                auth.createUserWithEmailAndPassword( email.toString(), password.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){
                            goToHome()
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        buttonLogin.setOnClickListener {

            if( email.isNotEmpty() && password.isNotEmpty() ){
                auth.signInWithEmailAndPassword( email.toString(), password.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){
                            goToHome()
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        buttonGoogle.setOnClickListener {
//            GoogleIdTokenCredential.createFrom(credential.data)
//            googleIdTokenCredential.idToken
        }
    }

    /**
     * Launch the main activity.
     */
    private fun goToHome(){
        val intent = Intent( this, MainActivity::class.java )
        startActivity( intent )
    }

    /**
     * Launches an error alert dialog. Saying that
     * an error occurred while authenticating.
     */
    private fun showAlert(){
        val alertBuilder = AlertDialog.Builder( this )
        alertBuilder.setTitle( getString( R.string.alertErrorTitle ) )
        alertBuilder.setMessage( getString( R.string.alertErrorMessage ) )
        alertBuilder.setPositiveButton( "", null )
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }

    /**
     * Save the user token in an encrypted shared preferences file to keep the user session open.
     * Even after closing the application
     */
    fun saveUser( user: FirebaseUser ){
        //Create the encryption  key
        val masterKey = MasterKey.Builder( this )
            .setKeyScheme( MasterKey.KeyScheme.AES256_GCM ).build()

        //Create the encrypted shared preferences file.
        val filePreferences = EncryptedSharedPreferences.create(
            this,
            "user_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        //Edit the preferences file
//        val editPreferences = filePreferences.edit()
//        editPreferences.putString( "refresh_token", "" )
//        editPreferences.apply()
    }
}