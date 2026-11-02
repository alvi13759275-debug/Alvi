package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.model.LanguageParadigm
import com.example.knowledge.FormatCategory
import com.example.knowledge.FormatItem
import com.example.ui.theme.*

@Composable
fun KnowledgeCatalogView(
    query: String,
    selectedCategory: FormatCategory?,
    selectedParadigm: LanguageParadigm?,
    items: List<FormatItem>,
    selectedDetail: FormatItem?,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (FormatCategory?) -> Unit,
    onParadigmSelect: (LanguageParadigm?) -> Unit,
    onSelectDetail: (FormatItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("knowledge_catalog_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search 700+ formats, extensions (.dex, .kt, .wasm)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanPrimary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("knowledge_search_input")
            )
        }

        // Category Filter Chips
        item {
            Text("DOMAINS & CATEGORIES", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All Domains") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary
                        )
                    )
                }
                items(FormatCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onCategorySelect(cat) },
                        label = { Text("${cat.displayName} (${cat.countLabel})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = CyanPrimary
                        )
                    )
                }
            }
        }

        // Results Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECOGNIZED SPECIFICATIONS (${items.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "700+ Ecosystem KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark
                )
            }
        }

        // Format Cards
        items(items) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDetail(item) }
                    .testTag("format_item_${item.name.replace(" ", "_")}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item.extensions.forEach { ext ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DarkBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = ext,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 11.sp,
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Model: ${item.executionModel}",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                        item.magicSignature?.let { sig ->
                            Text(
                                text = "Magic: $sig",
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                color = AmberAccent
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { onSelectDetail(null) },
            title = {
                Text(detail.name, color = CyanPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(detail.description, style = MaterialTheme.typography.bodyMedium)

                    Text("Extensions: ${detail.extensions.joinToString(", ")}", fontFamily = JetBrainsMono, fontSize = 12.sp, color = CyanPrimary)
                    Text("Category: ${detail.category.displayName}", style = MaterialTheme.typography.bodySmall)
                    Text("Paradigm: ${detail.paradigm.displayName}", style = MaterialTheme.typography.bodySmall)
                    Text("Execution: ${detail.executionModel}", style = MaterialTheme.typography.bodySmall)

                    detail.magicSignature?.let {
                        Text("Binary Magic Bytes: $it", fontFamily = JetBrainsMono, fontSize = 12.sp, color = AmberAccent)
                    }

                    Text("Canonical Code Sample:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = detail.sampleSnippet,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = EmeraldSuccess
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onSelectDetail(null) }) {
                    Text("Close", color = CyanPrimary)
                }
            }
        )
    }
}
