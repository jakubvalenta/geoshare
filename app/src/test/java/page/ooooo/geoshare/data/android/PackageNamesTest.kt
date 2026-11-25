package page.ooooo.geoshare.data.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNamesTest {
    private val carApps = listOf(
        PackageNames.MINI_NA,
        PackageNames.MINI_ROW,
        PackageNames.MY_BMW_NA,
        PackageNames.MY_BMW_ROW,
    )

    @Test
    fun isCarApp_whenPackageIsCarApp_returnsTrue() {
        for (packageName in carApps) {
            assertTrue(packageName, PackageNames.isCarApp(packageName))
        }
    }

    @Test
    fun isCarApp_whenPackageIsNotCarApp_returnsFalse() {
        for (packageName in listOf(
            null,
            "",
            PackageNames.GOOGLE_MAPS,
            PackageNames.WHATSAPP,
            // Similar package names must not match, because the check is exact, not a prefix check
            "${PackageNames.MY_BMW_ROW}.evil",
            "de.bmw.connected",
            PackageNames.MY_BMW_ROW.uppercase(),
        )) {
            assertFalse(packageName.toString(), PackageNames.isCarApp(packageName))
        }
    }

    @Test
    fun textReceivers_containsAllCarApps() {
        assertTrue(PackageNames.textReceivers.containsAll(carApps))
    }
}
