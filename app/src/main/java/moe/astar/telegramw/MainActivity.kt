/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package moe.astar.telegramw

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dagger.hilt.android.AndroidEntryPoint
import moe.astar.telegramw.ui.App
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var notificationService: NotificationService? = null
    private var bound: Boolean = false
    lateinit var navController: NavHostController

    /** Defines callbacks for service binding, passed to bindService()  */
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            notificationService = (binder as NotificationService.NotificationBinder).service
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            notificationService = null
            bound = false
        }
    }

    private fun enableNotification() {
        val serviceIntent = Intent(applicationContext, NotificationService::class.java)
        applicationContext.startForegroundService(serviceIntent)

        Intent(this, NotificationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        notificationService?.also {
            it.onCreate()
        }
    }

    private fun disableNotification() {
        Log.d("MainActivity", "disable notification")
        notificationService?.also {
            it.onDestroy()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        var isFirst = true
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        setContent {
            navController = rememberSwipeDismissableNavController()
            App(
                navController = navController,
                enableNotification = { this.enableNotification() },
                disableNotification = { this.disableNotification() }
            )
            if (isFirst) {
                val action = intent?.action
                val data = intent?.data
                when (action) {
                    "android.intent.action.VIEW" -> {
                        data?.also {
                            navController.navigate(
                                Screen.Info.buildRoute(
                                    "search",
                                    0,
                                    it.path.toString().replace("/", "")
                                )
                            )
                        }
                    }
                }
                isFirst = false
            }
        }
    }
}
