package com.rainccup.aktool.core.platform

expect fun createAppPaths(): AppPaths
expect fun createMessenger(): Messenger
expect fun createClipboard(): ClipboardPort
expect fun createFilePicker(): FilePicker
expect fun createPlatformTheme(): PlatformTheme
