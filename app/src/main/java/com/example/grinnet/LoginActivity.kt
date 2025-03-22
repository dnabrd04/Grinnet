package com.example.grinnet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

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

        //loadFirebaseAuthenticator()
    }

    /*fun loadFirebaseAuthenticator(){
        val buttonSignup = findViewById<Button>( R.id.buttonSignup )
        val emailInput = findViewById<EditText>( R.id.emailInput )
        val passwordInput = findViewById<EditText>( R.id.passwordInput )

        buttonSignup.setOnClickListener {

            if( emailInput.text.isNotEmpty() && passwordInput.text.isNotEmpty() ){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword( emailInput.text.toString(), passwordInput.text.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){

                        } else {

                        }
                    }
            }
        }
    }

    fun goToHome(){

    }*/
}