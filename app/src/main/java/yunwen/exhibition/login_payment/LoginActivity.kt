package yunwen.exhibition.login_payment


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import yunwen.exhibition.login_payment.ui.theme.LoginPaymentTheme


class LoginActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MyTag"
    }

    //private late init var auth: FirebaseAuth
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        // Handle the sign-in result here
        Log.d(TAG, "UI Auth res is: " + res.resultCode)
        Log.d(TAG, "UI Auth res is: " + res.idpResponse)
        //text = res.resultCode.toString()
    }

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPaymentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
