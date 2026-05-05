package com.lockedbrowser.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Bookmark(val title: String, val url: String)

object BookmarkManager {

    private const val PREFS_NAME = "bookmarks_prefs"
    private const val KEY_BOOKMARKS = "bookmarks_json"

    fun getAll(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BOOKMARKS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Bookmark>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(Bookmark(obj.getString("title"), obj.getString("url")))
        }
        return list
    }

    fun add(context: Context, bookmark: Bookmark) {
        val list = getAll(context).toMutableList()
        if (list.none { it.url == bookmark.url }) {
            list.add(0, bookmark)
            save(context, list)
        }
    }

    fun remove(context: Context, bookmark: Bookmark) {
        val list = getAll(context).toMutableList()
        list.removeAll { it.url == bookmark.url }
        save(context, list)
    }

    private fun save(context: Context, list: List<Bookmark>) {
        val array = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }
}
