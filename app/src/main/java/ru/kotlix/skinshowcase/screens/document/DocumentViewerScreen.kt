package ru.kotlix.skinshowcase.screens.document

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kotlix.skinshowcase.R
import ru.kotlix.skinshowcase.mock.MockDocuments
import java.io.File
import java.io.FileOutputStream

private sealed class DocumentLoadState {
    data object Loading : DocumentLoadState()
    data class Success(val bitmaps: List<Bitmap>) : DocumentLoadState()
    data object Error : DocumentLoadState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    documentId: String,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetPath = MockDocuments.getDocumentAssetPath(documentId)
    var loadState by remember(assetPath) { mutableStateOf<DocumentLoadState>(DocumentLoadState.Loading) }

    LaunchedEffect(assetPath) {
        if (assetPath != null) {
            val bitmaps = withContext(Dispatchers.IO) {
                loadPdfPagesFromAssets(context.assets.open(assetPath))
            }
            loadState = if (bitmaps.isNotEmpty()) DocumentLoadState.Success(bitmaps)
            else DocumentLoadState.Error
        }
    }

    val title = when (documentId) {
        MockDocuments.DOC_AGREEMENT -> stringResource(R.string.profile_user_agreement)
        MockDocuments.DOC_INSTRUCTIONS -> stringResource(R.string.profile_instructions)
        else -> stringResource(R.string.profile_documents)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1C1B1F),
                titleContentColor = Color.White
            )
        )
        when {
            assetPath == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_documents),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            loadState is DocumentLoadState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            loadState is DocumentLoadState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_documents),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            loadState is DocumentLoadState.Success -> {
                val bitmaps = (loadState as DocumentLoadState.Success).bitmaps
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .verticalScroll(scrollState)
                ) {
                    bitmaps.forEach { bitmap ->
                        androidx.compose.foundation.Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Копирует PDF из InputStream во временный файл и рендерит страницы через [PdfRenderer].
 * Возвращает список bitmap'ов (по одному на страницу). Закрывает ресурсы.
 */
private fun loadPdfPagesFromAssets(inputStream: java.io.InputStream): List<Bitmap> {
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    return try {
        val file = File.createTempFile("pdf", ".pdf").apply { deleteOnExit() }
        FileOutputStream(file).use { out ->
            inputStream.use { it.copyTo(out) }
        }
        pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount
        val bitmaps = mutableListOf<Bitmap>()
        val scale = 2f
        for (i in 0 until pageCount) {
            renderer.openPage(i).use { page ->
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
            }
        }
        bitmaps
    } catch (_: Exception) {
        emptyList()
    } finally {
        renderer?.close()
        pfd?.close()
        inputStream.close()
    }
}
