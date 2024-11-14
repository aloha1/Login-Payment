package yunwen.exhibition.login_payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import yunwen.exhibition.login_payment.ui.theme.LoginPaymentTheme

class LoginActivity: AppCompatActivity() {

    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPaymentTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Navigation(
                        context = this@LoginActivity,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        initViews()
    }


    private fun initViews() {}
}

@Composable
fun Navigation(context: Context, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Login.route){
        composable(Login.route) {
            LoginPage(context = context, modifier = modifier, navController = navController)
        }
        composable(Register.route) {
            Register(context = context, modifier = modifier)
        }
    }
}

@Composable
fun LoginPage(
    context: Context,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,

    horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") }
        )
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()

        )
        Button(onClick = {
            if (email.value.trim() == "" || password.value.trim() == ""){
                Toast.makeText(context, "Email or password can not be empty.", Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login successful, navigate to the next screen
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            // Handle login failure
                            Toast.makeText(context, "Please check username or password.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

        }) {
            Text("Login")
        }
        Button(
            onClick = { navController.navigate("register") }
        ) {
            Text("Register")
        }
    }
}

@Composable
fun Register(context: Context, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        var password by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        val auth = FirebaseAuth.getInstance()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .background(color = Color.DarkGray)
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Yellow,
            text = "Let's get to know you"
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 54.dp)
                .wrapContentHeight(),
            text = "Personal information"
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 4.dp)
                .wrapContentHeight(),
            text = "Information is for demo only"
        )
        Text(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            text = "Email"
        )
        TextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            value = email,
            onValueChange = {
                email = it
            }
        )
        Text(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            text = "Password"
        )
        TextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            value = password,
            onValueChange = {
                password = it
            }
        )

        Button(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
            onClick = {
                if (email.trim() == "" || password.trim() == ""){
                    Toast.makeText(context, "Email or password can not be empty.", Toast.LENGTH_SHORT).show()
                }else{
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("MYTAG", "createUserWithEmail:success")
                                val user = auth.currentUser
                                Log.d("MYTAG", "auth: $auth")
                                Log.d("MYTAG", "user: $user")
                                Toast.makeText(
                                    context,
                                    "Authentication Successful.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                //updateUI(user)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    context,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                //updateUI(null)
                            }
                        }
                }

            }
        ){
            Text(
                text = "Register",
                textAlign = TextAlign.Center
            )
        }
    }
}