package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import org.w3c.dom.Element
import org.w3c.dom.NodeList

@Patch(
    name = "Open alternative links patch",
    description = "Allows opening alternative site links directly in the Twitter app. " +
        "Currently supported alternative links: vxtwitter, fxtwitter, twittpr, and fixupx. " +
        "(After installing, make sure to have the app set up to open the links under Android settings)",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
)
@Suppress("unused")
object OpenAlternativeLinksPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file
            val alternativeLinkUrls = listOf("vxtwitter.com", "fxtwitter.com", "twittpr.com", "fixupx.com")

            val intentFilters = document.getElementsByTagName("intent-filter")

            // iterate through the intent filters to find the one for all the top level URLs that alternative links replace
            for (i in 0 until intentFilters.length) {
                val intentFilter = intentFilters.item(i) as? Element ?: continue

                val dataElements = intentFilter.getElementsByTagName("data")
                for (j in 0 until dataElements.length) {
                    val dataElement = dataElements.item(j) as? Element ?: continue

                    if (dataElement.getAttribute("android:host") == "www.x.com") {
                        // Found the intent that opens x.com links, now add alt links as additional children
                        alternativeLinkUrls.forEach { host ->
                            val altLinkElement = document.createElement("data")
                            altLinkElement.setAttribute("android:host", host)
                            intentFilter.appendChild(altLinkElement)

                            val altLinkElementWwwVersion = document.createElement("data")
                            altLinkElementWwwVersion.setAttribute("android:host", "www." + host)
                            intentFilter.appendChild(altLinkElementWwwVersion)
                        }
                    }
                }

                // since our alt links aren't well-known links, have to disable auto-verification
                intentFilter.removeAttribute("android:autoVerify")
            }

            // have to remove asset_statements as well to disable auto-verification
            val metadataElements = document.getElementsByTagName("meta-data")
            for (i in 0 until metadataElements.length) {
                val element = metadataElements.item(i) as? Element ?: continue

                if (element.getAttribute("android:name") == "asset_statements") {
                    element.parentNode.removeChild(element)
                }
            }
        }
    }
}
