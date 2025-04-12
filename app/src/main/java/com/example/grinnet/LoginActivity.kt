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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


/**
 * Class that controls the authentication system for the users.
 *
 * @author github: dnabr04
 * @date 23/03/2025
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var googleIdTokenCredential: GoogleIdTokenCredential? = null
    private lateinit var request: GetCredentialRequest
//    private lateinit var signInRequest:

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


        // Configure the Google ID option
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.IdWebClient)) // ID de cliente web de tu servidor
            .build()


        // Creates the credentials request
        request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()






        loadFirebaseAuthenticator()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if( currentUser != null ){
            goToHome()
        } else {
            val credentialManagerClient = CredentialManager.create(this)

            lifecycleScope.launch {
                try {
                    val customCredential = credentialManagerClient.getCredential(this@LoginActivity, request)
                    val credential = customCredential.credential

                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    }
                } catch (e: NoCredentialException) {
                    e.printStackTrace()
                }
            }
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
            val idToken = googleIdTokenCredential?.idToken

            if( idToken != null ){
                val firebaseCredential = GoogleAuthProvider.getCredential( idToken, null )

                // Inicia sesión en Firebase con dicha credencial
                Firebase.auth.signInWithCredential( firebaseCredential )
                    .addOnCompleteListener { task ->
                        if ( task.isSuccessful ) {
                            // La autenticación fue exitosa
                            val user = Firebase.auth.currentUser
                            // Aquí puedes actualizar la UI o guardar información del usuario
                        } else {
                            // Hubo un error en la autenticación, maneja el error (por ejemplo, muestra un mensaje)
                        }
                    }
            }
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