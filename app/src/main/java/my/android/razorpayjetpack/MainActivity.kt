package my.android.razorpayjetpack

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import my.android.razorpayjetpack.ui.theme.RazorPayJetpackTheme
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultListener {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RazorPayJetpackTheme {
                val amountState = remember { mutableStateOf("") }

                Scaffold(topBar = {
                    TopAppBar(
                        colors = topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = { Text("PaymentGateway Integration") }
                    )
                }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        AmountInputField(amountState)
                        PayButton(amountState, isLoading)
                    }
                }
            }
        }
    }

    @Composable
    private fun AmountInputField(amountState: MutableState<String>) {
        BasicTextField(
            value = amountState.value,
            onValueChange = { amountState.value = it },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (amountState.value.isEmpty()) {
                        Text(
                            text = "Enter amount",
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }

    @Composable
    private fun PayButton(amountState: MutableState<String>, isLoading: MutableState<Boolean>) {
        Button(
            onClick = {
                isLoading.value = true
                handlePaymentButtonClick(amountState, isLoading)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !isLoading.value
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = "Pay", color = Color.White)
            }
        }
    }

    private fun handlePaymentButtonClick(
        amountState: MutableState<String>,
        isLoading: MutableState<Boolean>
    ) {
        val amountText = amountState.value.trim()

        when {
            amountText.isEmpty() -> {
                showToast("Please enter an amount")
                isLoading.value = false
                return
            }

            amountText.toIntOrNull()?.let { it <= 0 } ?: true -> {
                showToast("Please enter a valid amount")
                isLoading.value = false
                return
            }

            else -> {
                startPayment(amountText.toInt(), isLoading)
                amountState.value = ""
            }
        }
    }

    private fun startPayment(amount: Int, isLoading: MutableState<Boolean>) {
        val checkout = Checkout()
        checkout.setKeyID("YOUR_API_KEY") // Replace with your actual API key

        try {
            val options = createPaymentOptions(amount)
            checkout.open(this, options)
        } catch (e: Exception) {
            showToast("Error in payment: ${e.message}")
            Log.e("MainActivity", "Error starting payment", e)
            isLoading.value = false
        }
    }

    private fun createPaymentOptions(amount: Int): JSONObject {
        return JSONObject().apply {
            put("name", "RazorPay Integration")
            put("description", "Funding Charges")
            put("theme.color", "#3399cc")
            put("currency", "INR")
            put("amount", (amount * 100).toString())
            put("prefill", JSONObject().apply {
                put("email", "sample123@example.com")
                put("contact", "3523289239")
            })
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        isLoading.value = false // Set isLoading to false on success
        showToast("Payment Successful")
        Log.d("#DEBUG", "Payment Successful: $razorpayPaymentId")
    }

    override fun onPaymentError(code: Int, response: String?) {
        isLoading.value = false // Set isLoading to false on error
        showToast("Payment Not Successful")
        Log.e("#DEBUG", "Payment error $code: $response")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        val isLoading: MutableState<Boolean> = mutableStateOf(false)
    }
}
