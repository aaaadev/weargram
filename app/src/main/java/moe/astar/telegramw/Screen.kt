package moe.astar.telegramw

import androidx.navigation.NavBackStackEntry
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    object Home : Screen("home")
    object MainMenu : Screen("mainMenu")
    object Login : Screen("login")

    object Topic : Screen("topic/{chatId}") {
        fun buildRoute(chatId: Long): String = "topic/${chatId}"

        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()
    }

    object Chat : Screen("chat/{chatId}/{threadId}") {
        fun buildRoute(chatId: Long, threadId: Long): String = "chat/${chatId}/${threadId}"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getThreadId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("threadId")?.toLong()
    }

    object ChatMenu : Screen("chatMenu/{chatId}") {
        fun buildRoute(chatId: Long): String = "chatMenu/${chatId}"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()
    }

    object MessageMenu : Screen("messageMenu/{chatId}/{messageId}") {
        fun buildRoute(chatId: Long, messageId: Long): String = "messageMenu/$chatId/$messageId"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()
    }

    object SelectReaction : Screen("selectReaction/{chatId}/{messageId}") {
        fun buildRoute(chatId: Long, messageId: Long): String = "selectReaction/$chatId/$messageId"
        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()
    }

    object Info : Screen("info/{type}/{id}/{username}") {
        fun buildRoute(type: String, id: Long, username: String? = null): String =
            "info/$type/$id/$username"

        fun getId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("id")?.toLong()

        fun getType(entry: NavBackStackEntry): String? =
            entry.arguments?.getString("type")

        fun getUsername(entry: NavBackStackEntry): String? =
            entry.arguments?.getString("username")
    }

    object Video : Screen("video/{path}") {
        fun buildRoute(path: String): String =
            "video/${URLEncoder.encode(path, StandardCharsets.UTF_8.toString())}"

        fun getPath(entry: NavBackStackEntry): String = URLDecoder.decode(
            entry.arguments!!.getString("path"),
            StandardCharsets.UTF_8.toString()
        )

    }

    object Map : Screen("map/{latitude}/{longitude}") {
        fun buildRoute(latitude: Double, longitude: Double): String =
            "map/$latitude/$longitude"

        fun getCoordinates(entry: NavBackStackEntry): Pair<Double, Double> =
            Pair(
                entry.arguments?.getString("latitude")?.toDouble() ?: 0.0,
                entry.arguments?.getString("longitude")?.toDouble() ?: 0.0
            )

    }

    object Settings : Screen("settings") {
        fun buildRoute(): String = "settings"
    }

    object About : Screen("about") {
        fun buildRoute(): String = "about"
    }

    object TopicSelect : Screen("topicSelect/{chatId}/{messageId}/{fromChatId}/{destId}") {
        fun buildRoute(chatId: Long, messageId: Long, fromChatId: Long, destId: Int): String =
            "topicSelect/${chatId}/${messageId}/${fromChatId}/${destId}"

        fun getDestId(entry: NavBackStackEntry): Int? =
            entry.arguments?.getString("destId")?.toInt()

        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()

        fun getFromChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("fromChatId")?.toLong()
    }

    object ChatSelect : Screen("chatSelect/{messageId}/{fromChatId}/{destId}") {
        fun buildRoute(messageId: Long, fromChatId: Long, destId: Int): String =
            "chatSelect/${messageId}/${fromChatId}/${destId}"

        fun getDestId(entry: NavBackStackEntry): Int? =
            entry.arguments?.getString("destId")?.toInt()

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()

        fun getFromChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("fromChatId")?.toLong()
    }

    object SelectStickers : Screen("selectStickers/{chatId}/{messageId}") {
        fun buildRoute(messageId: Long, chatId: Long): String =
            "selectStickers/${chatId}/${messageId}"

        fun getMessageId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("messageId")?.toLong()

        fun getChatId(entry: NavBackStackEntry): Long? =
            entry.arguments?.getString("chatId")?.toLong()
    }
    //object CreateChat : Screen("createChat")
}
