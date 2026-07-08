package page.ooooo.geoshare

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The intent-filter blocks inside ConversionActivity are grouped by a `<!-- Name -->` comment
 * per map service, and both the blocks and the hosts within each block are meant to be kept in
 * alphabetical order (see geoshare-contribution.md). "2GIS / Urbi" and
 * "Maps.me, Organic Maps, CoMaps" bundle multiple services under one comment and are exempt,
 * since splitting them apart would require restructuring the shared intent-filters.
 */
class AndroidManifestOrderTest {

    private val exemptGroups = setOf(
        "2GIS / Urbi",
        "Maps.me, Organic Maps, CoMaps",
    )

    private fun findManifestFile(): File =
        listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml"),
        ).firstOrNull { it.exists() }
            ?: error("Could not find AndroidManifest.xml from working directory ${File(".").absolutePath}")

    private fun firstChildElement(node: Element, tagName: String): Element? {
        val children = node.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child is Element && child.tagName == tagName) {
                return child
            }
        }
        return null
    }

    /**
     * Returns the map service groups in the order their `<!-- Name -->` comments appear, together
     * with the `android:host` values declared by the intent-filters under each comment.
     */
    private fun parseConversionActivityGroups(): Pair<List<String>, Map<String, List<String>>> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(findManifestFile())
        val activities = doc.getElementsByTagName("activity")
        var foundConversionActivity: Element? = null
        for (i in 0 until activities.length) {
            val activity = activities.item(i) as Element
            if (activity.getAttribute("android:name") == "page.ooooo.geoshare.ConversionActivity") {
                foundConversionActivity = activity
                break
            }
        }
        val conversionActivity =
            checkNotNull(foundConversionActivity) { "Could not find ConversionActivity in AndroidManifest.xml" }

        val groupOrder = mutableListOf<String>()
        val hostsByGroup = mutableMapOf<String, MutableList<String>>()
        var currentGroup: String? = null

        val children = conversionActivity.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            when {
                child.nodeType == Node.COMMENT_NODE -> {
                    currentGroup = child.textContent.trim()
                    groupOrder.add(currentGroup)
                }

                child is Element && child.tagName == "intent-filter" -> {
                    val host = firstChildElement(child, "data")?.getAttribute("android:host").orEmpty()
                    if (currentGroup != null && host.isNotEmpty()) {
                        hostsByGroup.getOrPut(currentGroup) { mutableListOf() }.add(host)
                    }
                }
            }
        }
        return groupOrder to hostsByGroup
    }

    @Test
    fun conversionActivityIntentFilters_groupsAreAlphabetical() {
        val (groupOrder, _) = parseConversionActivityGroups()
        val relevantGroups = groupOrder.filterNot { it in exemptGroups }

        assertEquals(relevantGroups.sortedBy { it.lowercase() }, relevantGroups)
    }

    @Test
    fun conversionActivityIntentFilters_hostsWithinEachGroupAreAlphabetical() {
        val (_, hostsByGroup) = parseConversionActivityGroups()

        for ((group, hosts) in hostsByGroup) {
            if (group in exemptGroups) continue
            assertEquals("Hosts in \"$group\" are not sorted alphabetically", hosts.sorted(), hosts)
        }
    }
}
