package yunwen.exhibition.login_payment

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONObject
import yunwen.exhibition.login_payment.ui.theme.LoginPaymentTheme
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val clientID =
        "AQYQm8ytpreKDB3pUl7FzEYUl2C14d-6irBzLButt7_8qiZDY1LOT4lj4VeRtO1uYjCSv8pfUynmZLEZ"
    private val secretID =
        "EDwS3qjwzpKdISzXtMWiHV8vF0qSeaJuXMhesB4AprZWv8KPbEiYC0u5elOHGdmtiVBwnPfdktuwa3v2"
    private val returnUrl = "yunwen.exhibition.login_payment://returnUrl"
    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        AndroidNetworking.initialize(applicationContext)

        fetchAccessToken()
        setContent {
            LoginPaymentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        onClick = { startOrder() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
        if (intent.data!!.getQueryParameter("opType") == "payment") {
            captureOrder(orderid)
        } else if (intent.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "5.00")
                    })
                })
            })
//            put("payment_source", JSONObject().apply {
//                put("paypal", JSONObject().apply {
//                    put("experience_context", JSONObject().apply {
//                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
//                        put("brand_name", "SH Developer")
//                        put("locale", "en-US")
//                        put("landing_page", "LOGIN")
//                        put("shipping_preference", "NO_SHIPPING")
//                        put("user_action", "PAY_NOW")
//                        put("return_url", returnUrl)
//                        put("cancel_url", "https://example.com/cancelUrl")
//                    })
//                })
//            })
        }

        Log.d(TAG, "orderRequestJson: $orderRequestJson")
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : $response")
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun handlerOrderID(orderID: String) {
        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@MainActivity, config, returnUrl)
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                Log.d(TAG, "onPayPalWebSuccess: $result")
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d(TAG, "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled: ")
            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, "accessToken: $accessToken")

                    Toast.makeText(this@MainActivity, "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()

//                    binding.startOrderBtn.visibility = View.VISIBLE
                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    Toast.makeText(this@MainActivity, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : $response")
                    Toast.makeText(this@MainActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ANError) {
                    // Handle the error
                    Log.e(TAG, "Capture Error : " + error.errorDetail)
                }
            })
    }

    companion object {
        const val TAG = "MyTag"
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onClick: ()->Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Button(
            onClick = onClick
        ) {
            Text(text = "Paypal")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LoginPaymentTheme {
        Greeting("Android", onClick = {})
    }
}