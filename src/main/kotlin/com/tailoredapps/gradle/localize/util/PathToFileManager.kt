package com.tailoredapps.gradle.localize.util

import org.gradle.api.Project
import java.io.File

class PathToFileManager(private val project: Project) {
    fun pathToFile(path: String): File = File(project.projectDir, path)
}
