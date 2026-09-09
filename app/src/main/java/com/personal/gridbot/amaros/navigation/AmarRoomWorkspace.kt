package com.personal.gridbot.amaros.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * مساحة داخلية موحدة لكل غرفة: غرفة → أقسام → صفحة/لوحة.
 * المحتوى التفصيلي يضاف لاحقاً كملفات مستقلة دون تغيير هذا الإطار.
 */
@Composable
fun AmarRoomWorkspace(
    room: AmarRoom,
    onSectionSelected: (AmarRoomSection) -> Unit = {}
) {
    val definition = AmarRoomCatalog.definition(room)
    var selectedId by remember(room) {
        mutableStateOf(definition.sections.firstOrNull()?.id.orEmpty())
    }
    val selected = definition.sections.firstOrNull { it.id == selectedId }
        ?: definition.sections.firstOrNull()

    Row(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .width(190.dp)
                .fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "أقسام الغرفة",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "اختر القسم لفتح صفحته",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(definition.sections, key = { it.id }) { section ->
                        val active = section.id == selectedId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedId = section.id
                                    onSectionSelected(section)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (active) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 9.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(section.icon)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    section.titleAr,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        definition.room.titleAr,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        definition.subtitleAr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    if (selected != null) {
                        Text(
                            "${selected.icon}  ${selected.titleAr}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            selected.descriptionAr,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("مساحة الصفحة جاهزة", style = MaterialTheme.typography.titleMedium)
                                Text("سيتم وضع البطاقات والأزرار والأدوات الخاصة بهذا القسم هنا كعناصر مستقلة قابلة للتعديل.")
                                Text("الوضع الحالي: تجريبي — لا يوجد اتصال MT5.")
                            }
                        }
                    }
                }
            }
        }
    }
}
