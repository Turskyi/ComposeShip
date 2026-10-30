package com.composeship.core.domain.platform

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
    override val type: PlatformType = PlatformType.WEB
    override val isMac: Boolean = false
    override val initialPath: String?
        get() = try { kotlinx.browser.window.location.pathname } catch (_: Exception) { null }

    override fun updateUrl(path: String) {
        try {
            if (kotlinx.browser.window.location.pathname != path) {
                kotlinx.browser.window.history.pushState(null, "", path)
            }
        } catch (_: Exception) {}
    }

    override fun observePathChanges(onPathChanged: (String) -> Unit) {
        try {
            kotlinx.browser.window.addEventListener("popstate", { _: org.w3c.dom.events.Event ->
                onPathChanged(kotlinx.browser.window.location.pathname)
            })
        } catch (_: Exception) {}
    }
}

actual fun getPlatform(): Platform = JsPlatform()
