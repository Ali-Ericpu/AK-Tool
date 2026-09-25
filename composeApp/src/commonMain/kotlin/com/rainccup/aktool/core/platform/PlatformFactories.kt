package com.rainccup.aktool.core.platform

expect fun createAppPaths(): AppPaths
expect fun createClipboard(): ClipboardPort
expect fun createFilePicker(): FilePicker
expect fun createPlatformTheme(): PlatformTheme

/** Uniform UI scale so dense desktop windows render the same content larger. */
expect fun platformUiScale(): Float
