package com.rexosphere.money_splitter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rexosphere.money_splitter.ui.theme.customColors
import kotlin.math.roundToInt

// KMP-compatible number formatting
fun formatAmount(amount: Double): String {
    val rounded = (amount * 100).roundToInt() / 100.0
    val intPart = rounded.toLong()
    val decPart = ((rounded - intPart) * 100).roundToInt()
    return "$intPart.${decPart.toString().padStart(2, '0')}"
}

/**
 * Premium card with elevation and rounded corners
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * Balance card with gradient background
 */
@Composable
fun BalanceCard(
    title: String,
    amount: Double,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.customColors.gradientStart,
                            MaterialTheme.customColors.gradientEnd
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rs.${formatAmount(amount)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Avatar with initials
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val initials = name.split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercase() ?: "" }
        .joinToString("")
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Animated badge chip
 */
@Composable
fun AnimatedBadge(
    text: String,
    icon: String,
    isAppUser: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isAppUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isAppUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    AssistChip(
        onClick = {},
        label = { Text("$icon $text") },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Amount text with color based on positive/negative
 */
@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge
) {
    val color = when {
        amount > 0.01 -> MaterialTheme.customColors.positiveAmount
        amount < -0.01 -> MaterialTheme.customColors.negativeAmount
        else -> MaterialTheme.customColors.neutralAmount
    }
    
    val prefix = when {
        amount > 0 -> "+"
        amount < 0 -> ""
        else -> ""
    }
    
    Text(
        text = "${prefix}Rs.${formatAmount(kotlin.math.abs(amount))}",
        style = style,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

/**
 * Primary button with gradient background
 */
@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Section header
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        action?.invoke()
    }
}
