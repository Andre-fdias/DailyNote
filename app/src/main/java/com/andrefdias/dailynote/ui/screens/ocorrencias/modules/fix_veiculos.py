import os
import re

file_path = r'C:\Users\andre_we17otv\AndroidStudioProjects\DailyNotes\app\src\main\java\com\andrefdias\dailynote\ui\screens\ocorrencias\modules\VeiculosModuleView.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

missing_code = '''
        val file = java.io.File(context.cacheDir, "veiculo_.jpg")
        file.parentFile?.mkdirs()
        file.createNewFile() // Important: file must exist for some providers to not crash
        return androidx.core.content.FileProvider.getUriForFile(context, "com.andrefdias.dailynote.fileprovider", file)
    }

    // Launchers
    val cameraLauncherCrlv = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUriString != null) {
            fotoCrlvUriString = tempCameraUriString
        }
    }
    
    val cameraLauncherVeiculo = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempCameraUriString?.let { fotosVeiculoUrisStrings = fotosVeiculoUrisStrings + it }
        }
    }
    
    val galleryLauncherVeiculo = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isNotEmpty()) {
            fotosVeiculoUrisStrings = fotosVeiculoUrisStrings + uris.map { it.toString() }
        }
    }

    // Modals State
    var showAnoModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showCorModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
'''

content = content.replace('fun createTempUri(): Uri {\n\n    if (isAdding) {', 'fun createTempUri(): Uri {' + missing_code + '\n    if (isAdding) {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
