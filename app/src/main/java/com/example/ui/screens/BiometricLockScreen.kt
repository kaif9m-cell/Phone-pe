package com.example.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.security.BiometricAuthHelper
import com.example.security.BiometricCapability
import com.example.security.findFragmentActivity
import com.example.ui.theme.PulseCrimsonFail
import com.example.ui.theme.PulseDarkBg
import com.example.ui.theme.PulseDarkCard
import com.example.ui.theme.PulseElectricViolet
import com.example.ui.theme.PulseHeroBottom
import com.example.ui.theme.PulseHeroTop
import com.example.ui.theme.PulseOutlineDark
import com.example.ui.theme.PulseTealSuccess
import com.example.ui.theme.PulseViolet

@Composable
fun BiometricLockScreen(
    userName: String,
    phoneNumber: String,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var capability by remember { mutableStateOf(BiometricAuthHelper.checkCapability(context)) }
    var statusError by remember { mutableStateOf<String?>(null) }
    var showPinFallback by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    fun launchBiometricPrompt() {
        capability = BiometricAuthHelper.checkCapability(context)
        if (activity == null) {
            statusError = "Activity context unavailable. Use App PIN (1234)."
            showPinFallback = true
            return
        }
        if (capability == BiometricCapability.NONE_ENROLLED || capability == BiometricCapability.NO_HARDWARE) {
            statusError = capability.statusLabel
            showPinFallback = true
            return
        }

        statusError = null
        BiometricAuthHelper.showBiometricPrompt(
            activity = activity,
            title = "Unlock PhonePe",
            subtitle = "Verify fingerprint, face, or device lock to access your UPI accounts",
            negativeButtonText = "Use App PIN",
            allowDeviceCredential = true,
            confirmationRequired = false,
            onSuccess = {
                onUnlocked()
            },
            onError = { errorCode, errString ->
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    showPinFallback = true
                } else {
                    statusError = errString
                }
            },
            onFailed = {
                statusError = "Biometric not recognized. Try again."
            }
        )
    }

    // Automatically prompt on lock screen mount when biometrics/device credential are configured
    LaunchedEffect(Unit) {
        if (capability == BiometricCapability.READY || capability == BiometricCapability.DEVICE_CREDENTIAL_ONLY) {
            launchBiometricPrompt()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PulseHeroTop, PulseHeroBottom, PulseDarkBg)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Identity Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 18.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.phonepe_launcher_icon),
                    contentDescription = "PhonePe Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PhonePe is Locked",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$userName • $phoneNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = PulseTealSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = capability.statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Center Biometric Sensor Callout OR Fallback PIN Keypad
            if (!showPinFallback) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(PulseViolet.copy(alpha = 0.22f))
                            .border(2.dp, PulseElectricViolet.copy(alpha = 0.75f), CircleShape)
                            .clickable { launchBiometricPrompt() }
                            .testTag("biometric_unlock_circle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Tap to unlock with biometrics",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Text(
                        text = if (capability == BiometricCapability.READY || capability == BiometricCapability.DEVICE_CREDENTIAL_ONLY) {
                            "Tap the sensor icon to verify with fingerprint, face, or device screen lock"
                        } else {
                            "No biometric or screen lock enrolled on this device. Enroll in Settings or unlock with App PIN (1234)."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    if (statusError != null) {
                        Surface(
                            color = PulseCrimsonFail.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = statusError!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFCA5A5),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = PulseDarkCard,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseOutlineDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Enter App PIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Default PIN: 1234",
                            style = MaterialTheme.typography.labelMedium,
                            color = PulseTealSuccess
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            repeat(4) { idx ->
                                val filled = idx < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) PulseViolet else PulseDarkBg)
                                        .border(
                                            1.5.dp,
                                            if (filled) PulseElectricViolet else PulseOutlineDark,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        AnimatedVisibility(visible = pinError != null) {
                            Text(
                                text = pinError.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = PulseCrimsonFail,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("BIO", "0", "BACK")
                        )
                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                when (key) {
                                                    "BIO" -> {
                                                        showPinFallback = false
                                                        launchBiometricPrompt()
                                                    }
                                                    "BACK" -> {
                                                        if (enteredPin.isNotEmpty()) {
                                                            enteredPin = enteredPin.dropLast(1)
                                                            pinError = null
                                                        }
                                                    }
                                                    else -> {
                                                        if (enteredPin.length < 4) {
                                                            val next = enteredPin + key
                                                            enteredPin = next
                                                            pinError = null
                                                            if (next.length == 4) {
                                                                if (next == "1234") {
                                                                    onUnlocked()
                                                                } else {
                                                                    enteredPin = ""
                                                                    pinError = "Incorrect PIN. Use 1234"
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .testTag("lock_pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (key) {
                                            "BACK" -> Icon(
                                                Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = Color.White
                                            )
                                            "BIO" -> Icon(
                                                Icons.Filled.Fingerprint,
                                                contentDescription = "Use Biometrics",
                                                tint = Color(0xFFC084FC)
                                            )
                                            else -> Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (capability == BiometricCapability.NONE_ENROLLED) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(BiometricAuthHelper.buildEnrollIntent())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enroll Fingerprint / Face in Settings")
                    }
                } else if (!showPinFallback) {
                    Button(
                        onClick = { launchBiometricPrompt() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("unlock_with_biometrics_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PulseViolet)
                    ) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock with Biometrics", color = Color.White)
                    }
                }

                TextButton(
                    onClick = { showPinFallback = !showPinFallback },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_lock_pin_fallback")
                ) {
                    Text(
                        text = if (showPinFallback) "Back to Biometric Prompt" else "Unlock with App PIN",
                        color = Color(0xFFC084FC)
                    )
                }
            }
        }
    }
}
