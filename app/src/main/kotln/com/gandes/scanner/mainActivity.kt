package com.gandes.scanner

// ═══════════════════════════════════════════════════════════
// IMPORTS
// ═══════════════════════════════════════════════════════════
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ═══════════════════════════════════════════════════════════
// SECTION 1 — THEME
// ═══════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color(0xFF00181C),
    background = Color(0xFF0F1115),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF161A22),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2630),
    onSurfaceVariant = Color(0xFFB0B7C3),
    outline = Color(0xFF3A424D),
    error = Color(0xFFEF5350),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE5EAF0),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFCBD5E0),
    error = Color(0xFFDC2626),
)

@Composable
fun GandesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

// ═══════════════════════════════════════════════════════════
// SECTION 2 — MODEL
// ═══════════════════════════════════════════════════════════
data class ScannedDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pageCount: Int,
    val pdfUri: Uri? = null,
    val pageUris: List<Uri> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════
// SECTION 3 — UTILS
// ═══════════════════════════════════════════════════════════
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun formatDateTime(millis: Long): String {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return fmt.format(Date(millis))
}

fun defaultScanName(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd HH.mm", Locale.US)
    return "Scan ${fmt.format(Date())}"
}

// ═══════════════════════════════════════════════════════════
// SECTION 4 — COMPONENTS
// ═══════════════════════════════════════════════════════════
@Composable
fun GandesCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun GandesButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Description
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SuccessBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 5 — SCANNER (ML Kit)
// ═══════════════════════════════════════════════════════════
@Composable
fun ScannerScreen(
    autoStart: Boolean = true,
    onCancel: () -> Unit,
    onSuccess: (pages: List<Uri>, pdfUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pages = scanResult?.pages?.map { it.imageUri } ?: emptyList()
            val pdf = scanResult?.pdf?.uri
            if (pages.isNotEmpty()) onSuccess(pages, pdf) else onCancel()
        } else {
            onCancel()
        }
    }

    fun startScan() {
        error = null
        loading = true
        val activity = context.findActivity()
        if (activity == null) {
            error = "Activity tidak ditemukan"
            loading = false
            return
        }

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(50)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                loading = false
            }
            .addOnFailureListener { e ->
                error = "Gagal buka scanner: ${e.message}"
                loading = false
            }
    }

    LaunchedEffect(autoStart) { if (autoStart) startScan() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Membuka scanner…")
        }
        if (error != null) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            Text(
                error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { startScan() }) { Text("Coba Lagi") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCancel) { Text("Kembali") }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 6 — SCREENS
// ═══════════════════════════════════════════════════════════
enum class Screen { HOME, SCANNER }

@Composable
fun HomeContent(
    documents: List<ScannedDocument>,
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        GandesCard {
            Column {
                Text(
                    "Scan Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Auto-detect tepi, auto-crop, langsung PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                GandesButton(
                    text = "Scan Dokumen",
                    icon = Icons.Filled.Add,
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Dokumen (${documents.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        if (documents.isEmpty()) {
            EmptyState(
                title = "Belum ada dokumen",
                message = "Tap Scan Dokumen untuk memulai."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(documents, key = { it.id }) { doc ->
                    GandesCard {
                        Column {
                            Text(
                                doc.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${doc.pageCount} halaman • ${formatDateTime(doc.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (doc.pdfUri != null) {
                                Spacer(Modifier.height(8.dp))
                                SuccessBanner("PDF siap")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 7 — APP ROOT
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GandesApp() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    val documents = remember { mutableStateListOf<ScannedDocument>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gandes Scanner", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        when (screen) {
            Screen.HOME -> HomeContent(
                documents = documents,
                modifier = Modifier.padding(padding),
                onScanClick = { screen = Screen.SCANNER }
            )
            Screen.SCANNER -> ScannerScreen(
                autoStart = true,
                onCancel = { screen = Screen.HOME },
                onSuccess = { pages, pdf ->
                    documents.add(
                        0,
                        ScannedDocument(
                            name = defaultScanName(),
                            pageCount = pages.size,
                            pdfUri = pdf,
                            pageUris = pages
                        )
                    )
                    screen = Screen.HOME
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 8 — MAIN ACTIVITY
// ═══════════════════════════════════════════════════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GandesTheme(darkTheme = true) {
                GandesApp()
            }
        }
    }
}
