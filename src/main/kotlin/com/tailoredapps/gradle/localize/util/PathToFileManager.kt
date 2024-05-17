package com.tailoredapps.gradle.localize.util

import java.io.File
import org.gradle.api.Project

class PathToFileManager(private val project: Project) {
    fun pathToFile(path: String): File = File(project.projectDir, path)
}
