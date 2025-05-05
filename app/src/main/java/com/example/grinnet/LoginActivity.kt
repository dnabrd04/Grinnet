package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.grinnet.data.UserRequest
import com.example.grinnet.data.UserResponse
import com.example.grinnet.utils.SessionManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    private lateinit var buttonSignup: Button
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoogle: Button
    private lateinit var email: EditText
    private lateinit var password: EditText

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

        initializeUIElements()
        configureGoogleSignIn()
        loadFirebaseAuthenticator()
    }

    /**
     * Initialize all the user interface elements
     * that will be used throughout the activity.
     */
    private fun initializeUIElements() {
        buttonSignup = findViewById( R.id.buttonSignup )
        buttonLogin = findViewById( R.id.buttonLogin )
        buttonGoogle = findViewById( R.id.buttonGoogle )
        email = findViewById( R.id.emailInput )
        password = findViewById( R.id.passwordInput )
    }

    private fun configureGoogleSignIn() {
        // Configure the Google ID option
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.IdWebClient)) // ID de cliente web de tu servidor
            .build()

        // Creates the credentials request
        request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if( currentUser != null ){
            goToHome()
        } /*else {
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
        }*/
    }

    /**
     * Load all application access methods
     */
    private fun loadFirebaseAuthenticator(){
        val email = email.text
        val password = password.text

        buttonSignup.setOnClickListener {

            if( email.isNotEmpty() && password.isNotEmpty() ){
                auth.createUserWithEmailAndPassword( email.toString(), password.toString() )
                    .addOnCompleteListener{

                        if( it.isSuccessful ){
                            createUser(auth.currentUser!!.uid)
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
                            val call = getApiUser(auth.currentUser!!.uid)
                            call.enqueue(object: Callback<UserResponse>{
                                override fun onResponse(
                                    call: Call<UserResponse>,
                                    response: Response<UserResponse>
                                ) {
                                    if(response.isSuccessful) {
                                        val responseUser = response.body()

                                        if (responseUser != null) {
                                            SessionManager.saveUserId(
                                                this@LoginActivity,
                                                responseUser.idUser ?: -1L
                                            )
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                }

                            })
                            goToHome()
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        buttonGoogle.setOnClickListener {
            launchGoogleSignIn()
        }
    }

    /**
     * Launch Google Sign-In flow
     */
    private fun launchGoogleSignIn() {
        val credentialManagerClient = CredentialManager.create(this)

        lifecycleScope.launch {
            try {
                val result = credentialManagerClient.getCredential(
                    this@LoginActivity,
                    request
                )
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    googleIdTokenCredential?.idToken?.let { token ->
                        signInWithGoogle(token)
                    }
                } else {
                    Log.d("", "Usuario inesperado.")
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Error en Google Sign-In: ${e.message}")

                val errorMessage = when (e) {
                    is androidx.credentials.exceptions.NoCredentialException ->
                        "No se encontraron cuentas de Google. Por favor, agrega una cuenta de Google a tu dispositivo."
                    else -> e.localizedMessage ?: getString(R.string.alertErrorMessage)
                }
                Log.e("LoginActivity", "Error en Google Sign-In: $errorMessage")

            }
        }
    }

    /**
     * Sign in with Google token
     */
    private fun signInWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser
                    if (user != null) {
                        // Verificar si el usuario existe en nuestra API
                        val call = getApiUser(user.uid)
                        call.enqueue(object: Callback<UserResponse> {
                            override fun onResponse(
                                call: Call<UserResponse>,
                                response: Response<UserResponse>
                            ) {
                                if(response.isSuccessful) {
                                    val responseUser = response.body()

                                    if(responseUser == null) {
                                        createUser(user.uid)
                                    } else {
                                        SessionManager.saveUserId(this@LoginActivity, responseUser.idUser ?: -1L)
                                    }
                                } else if(response.code() == 404) {
                                    createUser(user.uid)
                                }
                            }

                            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                                Log.d("", t.message!!)
                            }
                        })
                    }
                    goToHome()
                } else {
//                    showAlert(task.exception?.message)
                }
            }
    }

    /**
     * Launch the main activity.
     */
    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Makes a post call to create the user.
     */
    private fun createUser(firebaseId: String) {
        val user = UserRequest(null, "", "diego", "private", firebaseId, "diego", "Programador")
        Log.d("", "Creando usuario $user")
        val call = ApiClient.userService.createUser(user)
        call.enqueue(object : Callback<UserRequest> {
            override fun onResponse(
                call: Call<UserRequest>,
                response: Response<UserRequest>
            ) {
                if (response.isSuccessful) {
                    SessionManager.saveUserId(this@LoginActivity, response.body()?.idUser ?: -1L)
                    Log.d("CreateUser", "Usuario creado correctamente: ${response.body()}")
                } else {
                    Log.e("CreateUser", "Error al crear usuario: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserRequest>, t: Throwable) {
                Log.e("CreateUser", "Fallo en la creación del usuario: ${t.message}")
            }
        })
    }

    private fun getApiUser(firebaseId: String): Call<UserResponse> {
        return ApiClient.userService.getUserByFirebaseId(firebaseId)
    }

    /**
     * Launches an error alert dialog. Saying that
     * an error occurred while authenticating.
     */
    private fun showAlert() {
        val alertBuilder = AlertDialog.Builder( this )
        alertBuilder.setTitle( getString( R.string.alertErrorTitle ) )
        alertBuilder.setMessage( getString( R.string.alertErrorMessage ) )
        alertBuilder.setPositiveButton( "", null )
        val alertDialog = alertBuilder.create()
        alertDialog.show()
    }
}