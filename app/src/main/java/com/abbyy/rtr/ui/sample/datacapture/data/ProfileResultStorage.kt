// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.edit
import com.abbyy.mobile.rtr.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class ProfileField(val id: String, val value: String)

data class ProfileCaptureResult(
    val profile: Profile,
    val image: Bitmap?,
    val fields: List<ProfileField>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileCaptureResult

        if (profile != other.profile) return false
        if (image != other.image) return false
        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profile.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + fields.hashCode()
        return result
    }
}

class ProfileResultStorage(applicationContext: Application) {
    companion object {
        private const val PROFILE_KEY = "profile_key"
        private const val FIELDS_KEY = "fields_key"
        private const val FIELD_ID_KEY = "id_key"
        private const val FIELD_VALUE_KEY = "value_key"
        private const val TEXT_LANGUAGES_KEY = "text_languages_key"
        private const val BUSINESS_CARD_LANGUAGES_KEY = "business_card_languages_key"
        private const val IBAN_LANGUAGES_KEY = "iban_languages_key"
        private const val INVOICE_LANGUAGES_KEY = "languages_key"

        private const val LANGUAGES_SEPARATOR = ", "
    }

    private val imageFile = File(applicationContext.filesDir, "image.jpg")
    private val sharedPreferences =
        applicationContext.getSharedPreferences("result", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    suspend fun store(result: ProfileCaptureResult?) = mutex.withLock {
        if (result == null) {
            deleteImage()
            deletePreferences()
        } else {
            storeImage(result.image)
            storeProfile(result.profile)
            storeFields(result.fields)
        }
    }

    suspend fun load(): ProfileCaptureResult? = mutex.withLock {
        val profile = loadProfile() ?: return@withLock null
        val fields = loadFieldsInternal() ?: return@withLock null
        ProfileCaptureResult(
            profile = Profile.valueOf(profile),
            fields = fields,
            image = loadImage()
        )
    }

    suspend fun updateField(position: Int, field: ProfileField): Unit = mutex.withLock {
        val fields = checkNotNull(loadFieldsInternal()).toMutableList()
        fields[position] = field
        storeFields(fields)
    }

    suspend fun updateImage(bitmap: Bitmap?): Unit = mutex.withLock {
        storeImage(bitmap)
    }

    suspend fun loadFields(): List<ProfileField>? = mutex.withLock {
        loadFieldsInternal()
    }

    private suspend fun storeImage(bitmap: Bitmap?) = withContext(Dispatchers.IO) {
        if (bitmap == null) {
            deleteImage()
            return@withContext
        }

        val parent = checkNotNull(imageFile.parentFile)
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Can't create parent folder")
        }

        FileOutputStream(imageFile).use { stream ->
            // Compression with 90 quality compresses an image fast and into a small size.
            // But at the same time, it preserves high OCR quality.
            // Use other values according to your use case.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }
    }

    private suspend fun deleteImage() = withContext(Dispatchers.IO) {
        if (imageFile.exists()) {
            if (!imageFile.delete()) {
                throw IOException("Can't delete image file")
            }
        }
    }

    private suspend fun storeProfile(profile: Profile) = withContext(Dispatchers.Default) {
        sharedPreferences.edit {
            putString(PROFILE_KEY, profile.name)
        }
    }

    private suspend fun storeFields(fields: List<ProfileField>) = withContext(Dispatchers.Default) {
        val stringRepresentation = fields
            .map { field ->
                JSONObject().apply {
                    put(FIELD_ID_KEY, field.id)
                    put(FIELD_VALUE_KEY, field.value)
                }
            }
            .fold(JSONArray()) { acc, jsonObject -> acc.put(jsonObject) }
            .toString()

        sharedPreferences.edit {
            putString(FIELDS_KEY, stringRepresentation)
        }
    }

    private suspend fun deletePreferences() = withContext(Dispatchers.Main) {
        sharedPreferences.edit { clear() }
    }

    private suspend fun loadProfile(): String? = withContext(Dispatchers.Main) {
        sharedPreferences.getString(PROFILE_KEY, null)
    }

    private suspend fun loadFieldsInternal(): List<ProfileField>? =
        withContext(Dispatchers.Default) {
            val value = sharedPreferences.getString(FIELDS_KEY, null) ?: return@withContext null
            val jsonArray = JSONArray(value)
            val fields = mutableListOf<ProfileField>()
            for (index in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(index)
                val field = ProfileField(
                    id = jsonObject.getString(FIELD_ID_KEY),
                    value = jsonObject.getString(FIELD_VALUE_KEY)
                )
                fields.add(field)
            }
            fields
        }

    private suspend fun loadImage(): Bitmap? = withContext(Dispatchers.IO) {
        BitmapFactory.decodeFile(imageFile.path)
    }

    suspend fun loadLanguages(profile: Profile): Array<Language> =
        withContext(Dispatchers.Default) {
            val key = getLanguagesKey(profile = profile)
			sharedPreferences.getString(key, null)
                ?.split(LANGUAGES_SEPARATOR)
                ?.map(Language::valueOf)
                ?.toTypedArray()
                ?: arrayOf(Language.English)
        }

    suspend fun storeLanguages(profile: Profile, languages: Array<Language>) =
        withContext(Dispatchers.Default) {
            val key = getLanguagesKey(profile = profile)
            val value = languages.joinToString(
                separator = LANGUAGES_SEPARATOR,
                transform = { language -> language.name }
            )
			sharedPreferences.edit { putString(key, value) }
        }

    private fun getLanguagesKey(profile: Profile): String = when (profile) {
        Profile.TEXT -> TEXT_LANGUAGES_KEY
//        Profile.BUSINESS_CARD -> BUSINESS_CARD_LANGUAGES_KEY
//        Profile.IBAN -> IBAN_LANGUAGES_KEY
//        Profile.INVOICE -> INVOICE_LANGUAGES_KEY
    }

}
