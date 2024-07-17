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
    private var pageLength = 10
    private var pageNumber = 1


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "contact_fetcher")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        contentResolver = context.contentResolver
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (PermissionUtils.checkPermission(context)) {
            if (call.method == "get_all_contact") {
                pageLength = call.arguments<Map<String, Any>>()!!.get("limit") as Int
                pageNumber = call.arguments<Map<String, Any>>()!!.get("page_number") as Int
                mainScope.launch {
                    try {
                        val data: List<JSONObject>
                        withContext(Dispatchers.Default) {
                            data = ContactUtils(contentResolver).fetchContactByPage(
                                pageNumber,
                                pageLength
                            )
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
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromActivity() {
        channel.setMethodCallHandler(null)
    }
}
