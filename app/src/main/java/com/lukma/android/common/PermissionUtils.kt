package com.lukma.android.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.ambientOf
import androidx.core.app.ActivityCompat
import com.lukma.android.R

class PermissionUtils(private val activity: Activity) {
    private var onPermissionGranted: (() -> Unit)? = null

    fun runWithPermission(permissions: Array<String>, onPermissionGranted: () -> Unit) {
        this.onPermissionGranted = onPermissionGranted
        if (!hasPermissions(permissions)) {
            requestPermission(permissions)
        } else {
            this.onPermissionGranted?.invoke()
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION && permissions.isNotEmpty()) {
            when {
                grantResults.none { it != PackageManager.PERMISSION_GRANTED } ->
                    onPermissionGranted?.invoke()
                permissions.none { !isNeedRequestPermissionRationale(it) } ->
                    showNeedPermissionDialog(permissions)
                else -> showGoToSettingDialog()
            }
        }
    }

    private fun requestPermission(permissions: Array<out String>) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION)
    }

    private fun hasPermissions(permissions: Array<String>) =
        !permissions.map { isNeedPermission(activity, it) }.contains(true)

    private fun isNeedPermission(context: Context, permission: String) =
        ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED

    private fun isNeedRequestPermissionRationale(permission: String) =
        if (isNeedPermission(activity, permission)) {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        } else {
            false
        }

    private fun showNeedPermissionDialog(permissions: Array<out String>) {
        activity.showAlertDialog(
            message = R.string.warning_need_permission,
            onPositiveClick = {
                requestPermission(permissions)
            },
            onNegativeClick = {}
        )
    }

    private fun showGoToSettingDialog() {
        activity.showAlertDialog(
            message = R.string.warning_need_go_to_settings,
            onPositiveClick = {
                activity.startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity.packageName, null)
                })
            },
            onNegativeClick = {}
        )
    }

    companion object {
        private const val REQUEST_PERMISSION = 0x0
    }
}

private var alertDialog: AlertDialog? = null

@JvmOverloads
fun Context.showAlertDialog(
    @StringRes message: Int,
    @StringRes labelPositive: Int = 0,
    @StringRes labelNegative: Int = 0,
    isCancelable: Boolean = false,
    onPositiveClick: (() -> Unit)? = null,
    onNegativeClick: (() -> Unit)? = null,
    isSingleLaunch: Boolean = false
) {
    if (isSingleLaunch && alertDialog == null ||
        isSingleLaunch && alertDialog != null && alertDialog?.isShowing == false ||
        !isSingleLaunch
    ) {
        alertDialog = AlertDialog.Builder(this@showAlertDialog)
            .apply {
                setMessage(message)
                setCancelable(isCancelable)
                onPositiveClick?.run {
                    setPositiveButton(if (labelPositive != 0) labelPositive else android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        onPositiveClick()
                    }
                }
                onNegativeClick?.run {
                    setNegativeButton(if (labelNegative != 0) labelNegative else android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                        onNegativeClick()
                    }
                }
            }
            .create()
        alertDialog?.show()
    }
}

val PermissionUtilsAmbient = ambientOf<PermissionUtils> { error("Permission not initialized") }
