package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.SubjectEntity

fun getSubjectIcon(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "code" -> Icons.Default.Code
        "terminal" -> Icons.Default.Terminal
        "database", "db" -> Icons.Default.Storage
        "network", "web" -> Icons.Default.Language
        "math", "calc" -> Icons.Default.Functions
        "science" -> Icons.Default.Science
        "data" -> Icons.Default.DataObject
        "book" -> Icons.Default.MenuBook
        else -> Icons.Default.Book
    }
}

val SubjectPresetColors = listOf(
    0xFF4F46E5, // Indigo
    0xFF059669, // Emerald
    0xFFD97706, // Amber
    0xFF7C3AED, // Violet
    0xFFDC2626, // Red
    0xFF0284C7, // Cyan
    0xFFDB2777, // Pink
    0xFF475569  // Slate
)

val SubjectPresetIcons = listOf(
    "code" to "Coding / DSA",
    "terminal" to "OS / Systems",
    "database" to "DBMS / SQL",
    "network" to "Networks / Web",
    "math" to "Math / Theory",
    "science" to "Science",
    "book" to "Reading / General"
)

@Composable
fun SubjectBadge(
    subject: SubjectEntity?,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val color = subject?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val name = subject?.name ?: "Unknown Subject"
    val icon = getSubjectIcon(subject?.iconKey ?: "book")

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
