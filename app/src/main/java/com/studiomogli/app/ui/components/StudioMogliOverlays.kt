package com.studiomogli.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.studiomogli.app.data.AppConfig
import com.studiomogli.app.data.model.GitHubUpdateInfo
import com.studiomogli.app.ui.theme.NetflixBlack
import com.studiomogli.app.ui.theme.NetflixRed
import com.studiomogli.app.ui.theme.NetflixSurface
import com.studiomogli.app.ui.theme.TextSecondary
import com.studiomogli.app.ui.theme.TrustGreen

/**
 * Top-right floating Studio Mogli button + hidden-until-available GitHub Update button.
 * Completely hidden when isFullScreenPlayer == true.
 */
@Composable
fun StudioMogliTopRightControls(
    isFullScreenPlayer: Boolean,
    updateInfo: GitHubUpdateInfo?,
    onOpenPopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isFullScreenPlayer) return

    val context = LocalContext.current

    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // GitHub Update button stays completely hidden until a new release exists
        if (updateInfo != null) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrustGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = "Update Available",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Update ${updateInfo.versionTag}",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top-right corner Studio Mogli button to open the Welcome / Trust / Refer popup
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NetflixRed)
                .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { onOpenPopup() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SM",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * First-time launch popup (and re-accessible anytime from the top-right SM button).
 */
@Composable
fun StudioMogliWelcomePopup(
    onDismiss: () -> Unit,
    onOpenTrustPage: () -> Unit,
    onOpenCreditsPage: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NetflixSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = TrustGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Welcome to Studio Mogli Anime",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "100% Malware-Free • Zero Data Collection",
                            color = TrustGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "This app only requests basic Internet permission. It is technically unable to read your files, contacts, camera, microphone, or location.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Refer & Earn 10% Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.REFER_AND_EARN_URL))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refer & Earn 10%", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenTrustPage()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = TrustGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trust Page", color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenCreditsPage()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Credits", color = Color.White, fontSize = 12.sp)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Continue to App", color = TextSecondary)
                }
            }
        }
    }
}
