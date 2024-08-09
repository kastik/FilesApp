package com.kastik.files

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kastik.files.data.UploadWorker
import com.kastik.files.ui.MainUI
import data.UserPreferencesRepo


class MainActivity : ComponentActivity() {

    //val DEFAULT_PHOTO_LOCATION = this.cacheDir.toString() + "/photos/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {} //TODO Check for permissions

        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        )


        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()
        val work = PeriodicWorkRequestBuilder<UploadWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(work)


        val preferences = UserPreferencesRepo()

        setContent {
            MaterialTheme {
                Surface {
                    MainUI(preferences)
                }
            }
        }
    }
}