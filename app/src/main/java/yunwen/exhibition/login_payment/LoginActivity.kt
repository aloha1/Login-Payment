package yunwen.exhibition.login_payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import yunwen.exhibition.login_payment.ui.theme.LoginPaymentTheme

class LoginActivity: AppCompatActivity() {

    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPaymentTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginPage(
                        context = this@LoginActivity,
                        modifier = Modifier.padding(innerPadding))
                    //Onboarding(modifier = Modifier.padding(innerPadding))
                }
            }
        }
        initViews()
        // ... (initialize UI elements)
        val auth = FirebaseAuth.getInstance()

//        signInButton.setOnClickListener {
//            val email = emailEditText.text.toString()
//            val password = passwordEditText.text.toString()
//
//            auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                // Sign-in successful, navigate to the next screen
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                // Handle sign-in failure
//                Toast.makeText(this, "Sign-in failed.", Toast.LENGTH_SHORT).show()
//            }
//        }

    }


    private fun initViews() {

    }
}

@Composable
fun LoginPage(context: Context,
              modifier: Modifier = Modifier) {
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
            onValueChange
        = { email.value = it },
        label = { Text("Email") }
        )
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()

        )
        Button(onClick = {
//            val emailAddress = email.value.trim()
//            val password = password.value.trim()

            auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to the next screen
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        // Handle login failure
                        Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Login")
        }
    }
}

@Composable
fun Onboarding(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
//        Image(
//            painter = painterResource(id = R.drawable.logo),
//            contentDescription = "Title",
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally)
//                .height(100.dp)
//                .width(240.dp),
//        )
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
            text = "First name"
        )
        TextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            value = firstName,
            onValueChange = {
                firstName = it
            }
        )
        Text(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            text = "Last name"
        )
        TextField(
            modifier = Modifier.padding(horizontal = 32.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            value = lastName,
            onValueChange = {
                lastName = it
            }
        )
        Text(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            text = "Email"
        )
        TextField(
            modifier = Modifier.padding(horizontal = 32.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            value = email,
            onValueChange = {
                email = it
            }
        )
        Button(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
            onClick = {
                //onClick(firstName, lastName, email, navController)
            }
        ){
            Text(
                text = "Register",
                textAlign = TextAlign.Center
            )
        }
    }
}