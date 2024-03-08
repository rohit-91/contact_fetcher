package com.vahak.contact_fetcher

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


/** ContactFetcherPlugin */
class ContactFetcherPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: FlutterActivity
    private lateinit var contentResolver: ContentResolver
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val contactList = mutableListOf<JSONObject>()
    private var pageLength = 10


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "contact_fetcher")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        contentResolver = context.contentResolver
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (checkPermission()) {
            if (call.method == "get_all_contact") {
                pageLength = call.arguments<Map<String, Any>>()!!.get("limit") as Int
                mainScope.launch {
                    try {
                        val data: List<JSONObject>
                        withContext(Dispatchers.Default) {
                            data = fetchContacts()
                        }
                        result.success(data.toString())
                    } catch (e: Exception) {
                        Log.e("Contact fetcher Plugin", e.message.toString())
                    }
                }
            } else {
                result.notImplemented()
            }
        } else {
            Log.e("Contact fetcher Plugin", "Contact permission is not enabled")
            result.error("Contact Permission not enabled", "", ArrayList<JSONObject>().toString())
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        channel.setMethodCallHandler(null)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
    }

    override fun onDetachedFromActivity() {
        channel.setMethodCallHandler(null)
    }

    private fun getPageNumber(): Int {
        return (contactList.size / pageLength)
    }


    private fun fetchContacts(): List<JSONObject> {
        fetchByPage()
        return contactList
    }

    @SuppressLint("Range")
    @TargetApi(Build.VERSION_CODES.M)
    private fun fetchByPage() {
        val startIndex = (getPageNumber() * pageLength).coerceAtLeast(1) - 1
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ), null, null, null
        )
        if (cursor != null && cursor.moveToPosition(startIndex)) {
            bindDataFromCursor(cursor)
            cursor.close()
        }
    }

    @SuppressLint("Range")
    private fun bindDataFromCursor(cursor: Cursor) {
        var count = 0
        do {
            val contactObject = JSONObject()
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            contactObject.put("id", id)
            contactObject.put(
                "name",
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            )
            val phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
                arrayOf(id),
                null
            )
            val phoneNumberList = JSONArray()
            while (phoneCursor!!.moveToNext()) {
                phoneNumberList.put(
                    phoneCursor.getString(
                        phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                    )
                )
            }
            phoneCursor.close()
            contactObject.put("phone_numbers", phoneNumberList)
            val bytes: ByteArray? = extractImageFromCursor(cursor)
            if (bytes != null) {
                contactObject.put("photo", bytes.toList())
            }
            if (contactObject.has("name") && contactObject.getString("name")
                    .isNotEmpty() && contactObject.getJSONArray("phone_numbers").length() != 0
            ) {
                contactList.add(contactObject)
            }
            ++count
        } while (cursor.moveToNext() && count < pageLength)
    }

    @SuppressLint("Recycle")
    private fun extractImageFromCursor(cursor: Cursor): ByteArray? {
        var imageBytes: ByteArray? = null
        val columnIndex: Int = cursor.getColumnIndex(
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        if (columnIndex == -1) {
            return null
        }
        val imageUri: String = cursor.getString(
            columnIndex
        ) ?: return null
        try {
            val fis: InputStream? = context.contentResolver.openInputStream(Uri.parse(imageUri))
            imageBytes = fis?.readBytes()!!
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageBytes
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
