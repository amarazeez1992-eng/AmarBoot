package com.personal.gridbot.amaros.intelligence.advanced

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Durable graph store for strategy/source/regime/outcome relationships. */
class AmarKnowledgeGraphRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun upsertNode(node: AmarGraphNode) {
        val current = snapshot().nodes.filterNot { it.id == node.id } + node
        save(current, snapshot().edges)
    }

    fun connect(from: String, to: String, relation: String, weight: Double = 1.0) {
        val graph = snapshot()
        if (graph.nodes.none { it.id == from } || graph.nodes.none { it.id == to }) return
        val edge = AmarGraphEdge(from, to, relation.trim(), weight.coerceIn(-1.0, 1.0))
        val edges = graph.edges.filterNot { it.from == from && it.to == to && it.relation == relation } + edge
        save(graph.nodes, edges.takeLast(5000))
    }

    fun snapshot(): AmarKnowledgeGraphSnapshot = runCatching {
        val nodesJson = JSONArray(prefs.getString(KEY_NODES, "[]"))
        val edgesJson = JSONArray(prefs.getString(KEY_EDGES, "[]"))
        val nodes = buildList { for (i in 0 until nodesJson.length()) nodesJson.optJSONObject(i)?.let { add(AmarGraphNode(it.optString("id"), it.optString("type"), it.optString("label"))) } }
        val edges = buildList { for (i in 0 until edgesJson.length()) edgesJson.optJSONObject(i)?.let { add(AmarGraphEdge(it.optString("from"), it.optString("to"), it.optString("relation"), it.optDouble("weight"))) } }
        AmarKnowledgeGraphSnapshot(nodes, edges)
    }.getOrDefault(AmarKnowledgeGraphSnapshot(emptyList(), emptyList()))

    private fun save(nodes: List<AmarGraphNode>, edges: List<AmarGraphEdge>) {
        val n = JSONArray().apply { nodes.forEach { put(JSONObject().put("id", it.id).put("type", it.type).put("label", it.label)) } }
        val e = JSONArray().apply { edges.forEach { put(JSONObject().put("from", it.from).put("to", it.to).put("relation", it.relation).put("weight", it.weight)) } }
        prefs.edit().putString(KEY_NODES, n.toString()).putString(KEY_EDGES, e.toString()).commit()
    }

    companion object {
        private const val PREFS = "amar_ai_knowledge_graph_v1"
        private const val KEY_NODES = "nodes"
        private const val KEY_EDGES = "edges"
    }
}
