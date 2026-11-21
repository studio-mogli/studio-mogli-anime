package com.studiomogli.app.ui.credits

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studiomogli.app.data.AppConfig
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixRed
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.TextSecondary

@Composable
fun CreditsScreen() {
    val context = LocalContext.current

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
        Text(
            text = "Credits",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = NetflixSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Studio Mogli",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Official Creator & Publisher of Studio Mogli Anime (v${AppConfig.CURRENT_VERSION}).",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.STUDIO_MOGLI_URL)))
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit studiomogli.com", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Refer & Earn 10% Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Refer & Earn 10%",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Invite friends to Studio Mogli and earn a 10% reward on referrals.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.REFER_AND_EARN_URL)))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refer and Earn (10%)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = NetflixSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Powered By Open APIs",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("• Metadata: AniList GraphQL API (graphql.anilist.co)", color = TextSecondary, fontSize = 13.sp)
                Text("• Multi-Source Providers: Aniwatch-API, Consumet, Anify", color = TextSecondary, fontSize = 13.sp)
                Text("• Playback Engine: AndroidX Media3 (ExoPlayer HLS)", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
