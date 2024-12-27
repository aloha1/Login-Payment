package yunwen.exhibition.login_payment


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import yunwen.exhibition.login_payment.LoginActivity.Companion.TAG
import yunwen.exhibition.login_payment.ui.theme.LoginPaymentTheme


class LoginActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MyTag"
    }

    private var text: String = ""

    //private lateinit var auth: FirebaseAuth
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        // Handle the sign-in result here
        Log.d(TAG, "UI Auth res is: " + res.resultCode)
        Log.d(TAG, "UI Auth res is: " + res.idpResponse)
        text = res.resultCode.toString()
    }

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPaymentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    //Text(text = text)
                    ChatApp()
//                    Navigation(
//                        context = this@LoginActivity, modifier = Modifier.padding(innerPadding)
//                    )
                }
            }
        }
        initViews()
    }

    override fun onResume() {
        super.onResume()
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
//            AuthUI.IdpConfig.TwitterBuilder().build(),
        )

//        val auth = FirebaseAuth.getInstance().currentUser
        //if(logged out then it will launch sign-in intent)
        // Create and launch sign-in intent

        if (auth.currentUser != null) {
            // User is logged out
            Log.d(TAG, "onResume auth current user uid: " + auth.currentUser?.uid)
            //provide a button of FirebaseAuth.getInstance()

        } else {
            val signInIntent =
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                    .build()
            signInLauncher.launch(signInIntent)
            // User is logged in
        }
        //Log.d(TAG, "onResume auth current user uid: " + auth.currentUser?.uid)
    }

    private fun initViews() {
        val receiver = BatteryReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.BATTERY_CHANGED")
        this.registerReceiver(receiver, filter)
        createNotificationChannel(this)

    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

@Composable
fun ChatApp() {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()

    // Fetch messages from Firestore
    LaunchedEffect(Unit) {
        db.collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val fetchedMessages = snapshot.documents.map {
                        Message(
                            message = it.getString("message") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getTimestamp("timestamp")?.toDate().toString()
                        )
                    }
                    messages = fetchedMessages
                }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages.size) { index ->
                MessageItem(
                    message = messages[index],
                    isCurrentUser = messages[index].senderId == currentUser?.uid
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, CircleShape)
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.text.isNotBlank()) {
                        scope.launch {
                            val messageData = mapOf(
                                "message" to messageText.text,
                                "senderId" to currentUser?.uid,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )
                            db.collection("messages").add(messageData)
                            messageText = TextFieldValue("")
                        }
                    }
                }
            ) {
                Text(text = "Send")
            }
        }
    }
}

@Composable
fun Navigation(context: Context, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Login.route) {
        composable(Login.route) {
            LoginPage(context = context, modifier = modifier, navController = navController)
        }
        composable(Register.route) {
            Register(context = context, modifier = modifier, navController = navController)
        }
    }
}

@Composable
fun LoginPage(
    context: Context, modifier: Modifier = Modifier, navController: NavController
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") })
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()

        )
        Button(onClick = {
            if (email.value.trim() == "" || password.value.trim() == "") {
                Toast.makeText(context, "Email or password can not be empty.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                auth.signInWithEmailAndPassword(email.value.trim(), password.value.trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login successful, navigate to the next screen
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            // Handle login failure
                            Toast.makeText(
                                context, "Please check username or password.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

        }) {
            Text("Login")
        }
        Button(onClick = { navController.navigate(Register.route) }) {
            Text("Register")
        }
    }
}

@Composable
fun Register(context: Context, modifier: Modifier = Modifier, navController: NavController) {
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
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp), text = "Email"
        )
        TextField(modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
            value = email,
            onValueChange = {
                email = it
            })
        Text(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp), text = "Password"
        )
        TextField(modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
            value = password,
            onValueChange = {
                password = it
            })

        Button(modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth(),
            onClick = {
                if (email.trim() == "" || password.trim() == "") {
                    Toast.makeText(
                        context, "Email or password can not be empty.", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                Log.d(TAG, "user: $user")

                                Toast.makeText(
                                    context,
                                    "Authentication Successful.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                //updateUI(user)
                                //redirect to login page
                                navController.navigate(Login.route)
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

            }) {
            Text(
                text = "Register", textAlign = TextAlign.Center
            )
        }
    }
}