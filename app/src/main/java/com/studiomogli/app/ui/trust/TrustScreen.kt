package com.studiomogli.app.ui.trust

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.TextSecondary
import com.studiomogli.app.ui.theme.TrustGreen

@Composable
fun TrustScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = TrustGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Trust & Security",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "100% Malware-Free & Secured",
                    color = TrustGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Studio Mogli Anime is compiled directly from clean, verifiable source code via automated GitHub Actions builds. It contains zero adware, zero spyware, and zero background trackers.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NetflixSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TrustGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How We Technically Cannot Get Your Data",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TrustBullet(
                    title = "1. Android OS Sandbox & Minimal Manifest",
                    body = "Our AndroidManifest.xml declares ONLY 'android.permission.INTERNET'. The Android operating system physically blocks the app from accessing your Camera, Microphone, Contacts, SMS, Call Logs, GPS Location, or Local Files."
                )
                TrustBullet(
                    title = "2. No Login, No Account, No User Database",
                    body = "You never enter an email, phone number, or password. There is no backend server storing user profiles or watch history."
                )
                TrustBullet(
                    title = "3. Zero Analytics & Zero Ad SDKs",
                    body = "No Google Analytics, Facebook SDK, or third-party ad trackers are bundled in build.gradle.kts. Network calls only fetch anime metadata, video streams, and GitHub release checks."
                )
                TrustBullet(
                    title = "4. Reproducible GitHub Builds",
                    body = "Every APK is automatically packaged by GitHub Actions directly from the repository source, so anyone can inspect the code and permissions."
                )
            }
        }
    }
}

@Composable
private fun TrustBullet(title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = TrustGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = body, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
