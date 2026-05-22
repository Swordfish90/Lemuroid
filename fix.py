import sys

filename = r'c:\Users\irem\OneDrive\Belgeler\Projects\VibeCode\Lemuroid\lemuroid-app\src\main\java\com\swordfish\lemuroid\app\mobile\feature\settings\general\SettingsScreen.kt'

with open(filename, 'r', encoding='utf-8') as f:
    content = f.read()

old_str = '''        LocalSaveSyncSettings(
            state = state,
            onChangeFolder = { viewModel.changeLocalSaveSyncFolder() },
            onSyncNow = { viewModel.syncLocalSaveSyncFolderManually() }
        )'''

new_str = '''        val viewContext = androidx.compose.ui.platform.LocalContext.current
        LocalSaveSyncSettings(
            state = state,
            onChangeFolder = { viewModel.changeLocalSaveSyncFolder(viewContext) },
            onSyncNow = { viewModel.syncLocalSaveSyncFolderManually(viewContext) }
        )'''

content = content.replace(old_str, new_str)

with open(filename, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)

print("Done")
