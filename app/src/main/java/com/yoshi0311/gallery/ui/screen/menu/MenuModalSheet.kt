package com.yoshi0311.gallery.ui.screen.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import com.yoshi0311.gallery.ui.navigation.FavoritesScreen
import com.yoshi0311.gallery.ui.navigation.LocationScreen
import com.yoshi0311.gallery.ui.navigation.RecentsScreen
import com.yoshi0311.gallery.ui.navigation.SettingsScreen
import com.yoshi0311.gallery.ui.navigation.TrashScreen
import com.yoshi0311.gallery.ui.navigation.VideosScreen

@Composable
fun MenuModalSheet(
    onDismiss: () -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // 투명 배경 영역 — 터치 시 닫힘
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            // 카드 본체 — 터치 이벤트 소비 (배경으로 전파 차단)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── 1행: 원형 아이콘 바로가기 4개 ─────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        QuickIconButton(
                            icon = Icons.Outlined.Videocam,
                            label = "동영상",
                            onClick = { onDismiss(); onNavigate(VideosScreen) },
                        )
                        QuickIconButton(
                            icon = Icons.Outlined.FavoriteBorder,
                            label = "즐겨찾기",
                            onClick = { onDismiss(); onNavigate(FavoritesScreen) },
                        )
                        QuickIconButton(
                            icon = Icons.Outlined.AccessTime,
                            label = "최근 항목",
                            onClick = { onDismiss(); onNavigate(RecentsScreen) },
                        )
                        QuickIconButton(
                            icon = Icons.Outlined.LocationOn,
                            label = "위치",
                            onClick = { onDismiss(); onNavigate(LocationScreen) },
                        )
                    }

                    HorizontalDivider()

                    // ── 2행: 공유 앨범 / 사진첩 정리 ─────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StadiumMenuButton(
                            icon = Icons.Outlined.PeopleOutline,
                            label = "공유 앨범",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                            },
                        )
                        StadiumMenuButton(
                            icon = Icons.Outlined.AutoFixHigh,
                            label = "사진첩 정리",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }

                    // ── 3행: 휴지통 / 설정 ────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StadiumMenuButton(
                            icon = Icons.Outlined.DeleteOutline,
                            label = "휴지통",
                            modifier = Modifier.weight(1f),
                            onClick = { onDismiss(); onNavigate(TrashScreen) },
                        )
                        StadiumMenuButton(
                            icon = Icons.Outlined.Settings,
                            label = "설정",
                            modifier = Modifier.weight(1f),
                            onClick = { onDismiss(); onNavigate(SettingsScreen) },
                        )
                    }

                    // ── 4행: 스튜디오로 이동 (전체 너비) ──────────────
                    StudioLinkButton(
                        onClick = {
                            // 삼성 갤러리 패키지 후보 목록 (기기에 따라 다를 수 있음)
                            val candidates = listOf(
                                "com.sec.android.gallery3d",
                                "com.samsung.android.app.gallery",
                            )
                            val launchIntent = candidates.firstNotNullOfOrNull { pkg ->
                                context.packageManager.getLaunchIntentForPackage(pkg)
                            }
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                                onDismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Samsung Studio 앱을 찾을 수 없습니다.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                    )
                }
            }
        }
    }
}

// ── 1행용: 원형 아이콘 + 라벨 ───────────────────────────────────
@Composable
private fun QuickIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── 2·3행용: 스타디움 버튼 (아이콘 좌 + 텍스트 우) ─────────────
@Composable
private fun StadiumMenuButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

// ── 4행용: 스튜디오 이동 링크 버튼 (전체 너비) ──────────────────
@Composable
private fun StudioLinkButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "스튜디오로 이동",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
