package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.theme.*

data class ImagePreset(val name: String, val url: String)

val DEFAULT_PRESETS = listOf(
    ImagePreset("Mindful Nature", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"),
    ImagePreset("Modern Workspace", "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"),
    ImagePreset("Minimalist Architecture", "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"),
    ImagePreset("Deep Space", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80"),
    ImagePreset("Golden Horizon", "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=600&auto=format&fit=crop&q=80"),
    ImagePreset("Cyber Neon", "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600&auto=format&fit=crop&q=80")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerDialog(
    title: String = "Customize Picture",
    currentImageUrl: String? = null,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentImageUrl ?: "") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Local/Gallery, 1: Presets, 2: Web URL

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageSelected(it.toString())
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("image_picker_dialog"),
        containerColor = SolidSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Photo Picker",
                    tint = Secondary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Selection Row with solid background and high contrast
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF282B37),
                    contentColor = Secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Device",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Secondary else OnSurfaceVariant
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 0) Secondary else OnSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Presets",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Secondary else OnSurfaceVariant
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Collections,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) Secondary else OnSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Link",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) Secondary else OnSurfaceVariant
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 2) Secondary else OnSurfaceVariant
                            )
                        }
                    )
                }

                when (selectedTab) {
                    0 -> { // Local Device Photo Picker
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select any picture from your device gallery or files.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = BaseDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("choose_local_photo_button")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = BaseDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Browse Device Photos", fontWeight = FontWeight.Bold, color = BaseDark)
                            }
                        }
                    }

                    1 -> { // Preset Wallpapers
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Choose a high-resolution aesthetic preset:",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                items(DEFAULT_PRESETS) { preset ->
                                    Box(
                                        modifier = Modifier
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, Secondary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                onImageSelected(preset.url)
                                                onDismiss()
                                            }
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = preset.url),
                                            contentDescription = preset.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f))
                                                .padding(6.dp),
                                             contentAlignment = Alignment.BottomStart
                                        ) {
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Web Image Link Input
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                label = { Text("Image URL", color = OnSurfaceVariant) },
                                placeholder = { Text("https://example.com/photo.jpg", color = OnSurfaceVariant.copy(alpha = 0.6f)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("image_url_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface,
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (urlInput.isNotBlank()) {
                                        onImageSelected(urlInput.trim())
                                        onDismiss()
                                    }
                                },
                                enabled = urlInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = BaseDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_url_button")
                            ) {
                                Text("Apply URL", fontWeight = FontWeight.Bold, color = BaseDark)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        }
    )
}
