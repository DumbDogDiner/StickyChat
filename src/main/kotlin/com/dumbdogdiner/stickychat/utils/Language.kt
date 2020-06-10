package com.dumbdogdiner.stickychat.utils

import com.dumbdogdiner.stickychat.StickyChatPlugin

object Language {
    private const val path = "language"
    private val plugin = StickyChatPlugin.instance
    private val config = plugin.config

    private fun get(path: String) = StringUtils.colorize(config.getString(path)!!)
    private fun getList(path: String) = config.getStringList(path).map { StringUtils.colorize(it) }

    private val prefix
        get() = get("$path.prefix")

    val signSpySignCreated
        get() = "$prefix${get("$path.signSpySignCreated")}"
}