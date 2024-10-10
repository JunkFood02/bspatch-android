package io.github.junkfood02.bsdiff

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

@Composable
fun App(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPatchUri by remember { mutableStateOf<Uri?>(null) }
    var file: File? by remember { mutableStateOf(null) }
    var patchFile: File? by remember { mutableStateOf(null) }
    var patchedFile by remember { mutableStateOf<File?>(null) }
    var checksum by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedFileUri = uri },
    )


    val launcherPatch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedPatchUri = uri },
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedFileUri) {
        if (selectedFileUri != null) {
            file = copyFileToInternalStorage(context, selectedFileUri!!).getOrNull()
        }
    }

    LaunchedEffect(selectedPatchUri) {
        if (selectedPatchUri != null) {
            patchFile = copyFileToInternalStorage(context, selectedPatchUri!!).getOrNull()
        }
    }

    Scaffold() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { launcher.launch(arrayOf("*/*")) }) { Text("Select File") }
            Button(onClick = { launcherPatch.launch(arrayOf("*/*")) }) { Text("Select Patch") }
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    patchedFile = patchFile(file!!, patchFile!!)
                }
            }
            ) { Text(text = "PATCH!") }
            Button(onClick = {
                scope.launch(Dispatchers.Default) {
                    checksum = patchedFile?.let { it1 -> calculateChecksum(it1) }.toString()
                }
            }) {
                Text("Calculate the checksum of patched file")
            }

            file?.let { it1 -> Text(it1.path) }
            patchFile?.let { it1 -> Text(it1.path) }
            patchedFile?.let { it1 -> Text(it1.path) }

            Text(checksum)
        }
    }
}

fun patchFile(fileToPatch: File, patch: File): File {
    val patchedFile: File = File(fileToPatch.parentFile, fileToPatch.name + ".patched")
    if (patchedFile.exists()) {
        patchedFile.delete()
    }
    LibBSPatch.init()
    LibBSPatch.patch(fileToPatch, patchedFile, patch)
    return patchedFile
}

fun copyFileToInternalStorage(context: Context, uri: Uri): Result<File> {
    return runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(context, uri) ?: "selected_file"
        val outputFile = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(outputFile)

        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }

        outputFile
    }

}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val columnIndex =
                it.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
            return it.getString(columnIndex)
        }
    }
    return null
}


fun calculateChecksum(file: File): String? {
    return try {
        val inputStream = FileInputStream(file)
        val digest = MessageDigest.getInstance("SHA-256")

        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }

        val checksumBytes = digest.digest()
        checksumBytes.fold("") { str, it -> str + "%02x".format(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
