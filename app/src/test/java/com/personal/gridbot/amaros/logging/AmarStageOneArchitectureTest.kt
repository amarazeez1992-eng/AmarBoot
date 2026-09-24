package com.personal.gridbot.amaros.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AmarStageOneArchitectureTest {

    private val root: Path = Paths.get(
        System.getProperty("user.dir"),
        "app", "src", "main", "java"
    )

    @Test
    fun criticalBoundaries_areIsolated_andAcyclic() {
        val files = mapOf(
            "agent" to root.resolve("com/personal/gridbot/amaros/agent"),
            "security" to root.resolve("com/personal/gridbot/amaros/security"),
            "network" to root.resolve("com/personal/gridbot/network")
        ).mapValues { (_, path) -> kotlinFiles(path) }

        val imports = files.mapValues { (_, paths) ->
            paths.flatMap { it.readLines() }
                .filter { it.trimStart().startsWith("import ") }
        }

        val agentImportsNetwork = imports.getValue("agent").any {
            it.contains("com.personal.gridbot.network.")
        }
        val securityImportsAgent = imports.getValue("security").any {
            it.contains("com.personal.gridbot.amaros.agent.")
        }

        assertFalse("agent/ must not import network/", agentImportsNetwork)
        assertFalse("security/ must not import agent/", securityImportsAgent)

        val graph = imports.mapValues { (_, lines) ->
            lines.mapNotNull { line ->
                criticalBoundaryFromImport(line)
            }.distinct()
        }

        assertTrue(
            "critical architecture boundaries must be acyclic: $graph",
            graph.values.none { edges -> hasCycle(graph, edges) }
        )
    }

    private fun kotlinFiles(directory: Path): List<Path> {
        if (!Files.exists(directory)) return emptyList()
        val result = mutableListOf<Path>()
        Files.walk(directory).use { stream ->
            stream.forEach { path ->
                if (Files.isRegularFile(path) && path.toString().endsWith(".kt")) {
                    result.add(path)
                }
            }
        }
        return result
    }

    private fun criticalBoundaryFromImport(line: String): String? {
        val name = line.substringAfter("import ").trim()
        return when {
            name.startsWith("com.personal.gridbot.amaros.agent.") -> "agent"
            name.startsWith("com.personal.gridbot.amaros.security.") -> "security"
            name.startsWith("com.personal.gridbot.network.") -> "network"
            else -> null
        }
    }

    private fun hasCycle(graph: Map<String, List<String>>, startEdges: List<String>): Boolean {
        fun visit(node: String, visiting: Set<String>, visited: Set<String>): Boolean {
            if (node in visiting) return true
            if (node in visited) return false
            return graph.getValue(node).any { next ->
                visit(next, visiting + node, visited)
            }
        }
        return startEdges.any { start ->
            visit(start, emptySet(), emptySet())
        }
    }
}
