package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Class that controls the authentication system for the users.
 *
 * @author github: dnabr04
 * @date 23/03/2025
 */
class LoginActivity : AppCompatActivity() {

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

        loadFirebaseAuthenticator()
    }

    /**
     * Log in or sign up with firebase authenticator
     */
    fun loadFirebaseAuthenticator(){
        val buttonSignup = findViewById<Button>( R.id.buttonSignup )
        val buttonLogin = findViewById<Button>( R.id.buttonLogin )
        val emailInput = findViewById<EditText>( R.id.emailInput )
        val passwordInput = findViewById<EditText>( R.id.passwordInput )

        buttonSignup.setOnClickListener {

            if( emailInput.text.isNotEmpty() && passwordInput.text.isNotEmpty() ){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword( emailInput.text.toString(), passwordInput.text.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){
                            goToHome()
                        } else {

                        }
                    }
            }
        }

        buttonLogin.setOnClickListener {

            if( emailInput.text.isNotEmpty() && passwordInput.text.isNotEmpty() ){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword( emailInput.text.toString(), passwordInput.text.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){
                            goToHome()
                            it.result.user
                        } else {

                        }
                    }
            }
        }
    }

    /**
     * Launch the main activity.
     */
    fun goToHome(){
        val intent = Intent( this, MainActivity::class.java )
        startActivity( intent )
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