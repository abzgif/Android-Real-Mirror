package com.realmirror.app.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Result of a GitHub release update check.
 */
sealed class UpdateResult {
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseUrl: String,
        val releaseNotes: String
    ) : UpdateResult()

    data object UpToDate : UpdateResult()

    data class Error(val message: String) : UpdateResult()
}

/**
 * Checks for app updates using the GitHub Releases API.
 *
 * Calls the public endpoint (no API key needed):
 *   GET https://api.github.com/repos/{owner}/{repo}/releases/latest
 *
 * Rate limit: 60 requests/hour per IP (more than enough for manual taps).
 */
object UpdateChecker {

    private const val GITHUB_API_URL =
        "https://api.github.com/repos/abzgif/Android-Real-Mirror/releases/latest"

    /**
     * Check whether a newer release exists on GitHub.
     *
     * @param currentVersionName The installed app's versionName (e.g. "1.0").
     * @return [UpdateResult] indicating whether an update is available, up-to-date, or an error.
     */
    suspend fun checkForUpdate(currentVersionName: String): UpdateResult =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "Android-Real-Mirror")
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext UpdateResult.Error(
                        "GitHub API returned HTTP $responseCode"
                    )
                }

                val responseBody = BufferedReader(
                    InputStreamReader(connection.inputStream)
                ).use { it.readText() }

                connection.disconnect()

                val json = JSONObject(responseBody)
                val tagName = json.optString("tag_name", "")
                val htmlUrl = json.optString("html_url", "")
                val body = json.optString("body", "")

                if (tagName.isBlank()) {
                    return@withContext UpdateResult.Error("No release tag found")
                }

                val latestVersion = normalizeVersion(tagName)
                val currentVersion = normalizeVersion(currentVersionName)

                if (isNewerVersion(latestVersion, currentVersion)) {
                    UpdateResult.UpdateAvailable(
                        latestVersion = tagName,
                        releaseUrl = htmlUrl,
                        releaseNotes = body.take(200) // Truncate for display
                    )
                } else {
                    UpdateResult.UpToDate
                }
            } catch (e: java.net.UnknownHostException) {
                UpdateResult.Error("No internet connection. Please check your network and try again.")
            } catch (e: Exception) {
                UpdateResult.Error(
                    e.localizedMessage ?: "Failed to check for updates"
                )
            }
        }

    /**
     * Strip leading "v" or "V" and trim whitespace from a version string.
     * e.g. "v1.2.3" → "1.2.3", "1.0" → "1.0"
     */
    private fun normalizeVersion(version: String): String {
        return version.trim().removePrefix("v").removePrefix("V")
    }

    /**
     * Compare two dot-separated version strings segment by segment.
     * Returns true if [latest] is strictly newer than [current].
     *
     * Examples:
     *   isNewerVersion("1.1", "1.0")   → true
     *   isNewerVersion("2.0", "1.9.9") → true
     *   isNewerVersion("1.0", "1.0")   → false
     */
    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false // Equal
    }
}
