package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (role) {
        UserRole.ADMIN -> Pair(Color(0xFFFEE2E2), EduRoseDanger)
        UserRole.MODERATOR -> Pair(Color(0xFFCFFAFE), EduCyanModerator)
        UserRole.ADVISOR -> Pair(Color(0xFFEDE9FE), EduVioletAdvisor)
        UserRole.MEMBER -> Pair(Color(0xFFDBEAFE), EduBlueLight)
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = role.displayName.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
