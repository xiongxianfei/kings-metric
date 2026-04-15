package com.kingsmetric.release

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal fun resolveRepositoryRoot(): Path {
    val start = Paths.get("").toAbsolutePath().normalize()
    return generateSequence(start) { current -> current.parent }
        .firstOrNull { candidate ->
            Files.exists(candidate.resolve("settings.gradle.kts"))
        }
        ?: error("Could not locate repository root from $start")
}
