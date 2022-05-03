package com.lft.hear4me.ui.login

import android.app.Activity
import android.content.Intent
import android.content.Intent.EXTRA_STREAM
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lft.hear4me.*
import com.lft.hear4me.databinding.ActivityHomeBinding
import com.lft.hear4me.databinding.ActivityLoginBinding
import com.lft.hear4me.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val register = binding.registro
        val loading = binding.loading
        val returnMain: Button = findViewById<Button>(R.id.retornaMain)
        register.isEnabled = true

        var loginViewModel: LoginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        if(verify_logged_user()){
            Toast.makeText(
                applicationContext,
                "Usuário já está logado, para acessar outra conta, faça loggout",
                Toast.LENGTH_LONG
            ).show()
            val sendintent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(sendintent)
        }else{
            loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
                val loginState = it ?: return@Observer

                // disable login button unless both username / password is valid
                login.isEnabled = loginState.isDataValid

                if (loginState.usernameError != null) {
                    username.error = getString(loginState.usernameError)
                }
                if (loginState.passwordError != null) {
                    password.error = getString(loginState.passwordError)
                }
            })

            loginViewModel.loginResult.observe(this@LoginActivity, Observer {
                val loginResult = it ?: return@Observer

                loading.visibility = View.GONE
                if (loginResult.error != null) {
                    showLoginFailed(loginResult.error)
                }
                if (loginResult.success != null) {
                    updateUiWithUser(loginResult.success)
                }
                setResult(Activity.RESULT_OK)

                //Complete and destroy login activity once successful
                finish()
            })

            username.afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            password.apply {
                afterTextChanged {
                    loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                    )
                }

                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE ->
                            loginViewModel.login(
                                username.text.toString(),
                                password.text.toString()
                            )
                    }
                    false
                }

                login.setOnClickListener {
                    loading.visibility = View.VISIBLE
                    loginViewModel.login(username.text.toString(), password.text.toString())

                }
            }

            returnMain.setOnClickListener(View.OnClickListener {
                val intentResponse = Intent(this, HomeActivity::class.java)
                startActivity(intentResponse)
            })

            register.setOnClickListener(View.OnClickListener {
                val intentResponse = Intent(this, RegisterActivity::class.java)
                startActivity(intentResponse)
            })
        }


    }

    fun verify_logged_user(): Boolean {

        var auth = Firebase.auth
        val user = auth.currentUser
        return user != null
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
