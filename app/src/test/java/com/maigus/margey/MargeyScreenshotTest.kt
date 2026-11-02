package com.maigus.margey

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.bitchat.android.model.BitchatMessage
import com.maigus.margey.text.Ay
import com.maigus.margey.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h860dp-xxhdpi")
class MargeyScreenshotTest {
    private val compose = androidx.compose.ui.test.junit4.createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(object : ExternalResource() {
            override fun before() {
                val app = RuntimeEnvironment.getApplication()
                shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
            }
        })
        .around(compose)

    private fun shoot(name: String, content: @Composable () -> Unit) {
        compose.setContent { content() }
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(bmp))
        val dir = File(System.getProperty("margey.shots") ?: "build/margey-shots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun lock() { M.themeMode = 2; shoot("01_verrou") { MargeyTheme { LockScreen() } } }
    @Test fun permissions() { M.themeMode = 2; shoot("02_autorisations") { MargeyPermissionScreen {} } }
    @Test fun location() { M.themeMode = 2; shoot("07_localisation") { MargeyInfoScreen(MargeyIcons.location, Ay.L01, Ay.L02, Ay.G09, {}) } }
    @Test fun battery() { M.themeMode = 2; shoot("08_batterie") { MargeyInfoScreen(MargeyIcons.battery, null, Ay.L03, Ay.G09, {}, Ay.G08) } }
    @Test fun groupTexts() { M.themeMode = 2; shoot("09_groupes") { MargeyTheme {
        Column(Modifier.fillMaxSize().background(M.Night).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(Ay.K01, Ay.K02, Ay.K03, Ay.K04, fill(Ay.K05, n = 12), Ay.K06, Ay.K07, Ay.K08, Ay.K09, Ay.K10, Ay.S14, Ay.L04).forEach { AyText(it, size = 22f.let { androidx.compose.ui.unit.TextUnit(it, androidx.compose.ui.unit.TextUnitType.Sp) }) }
        } } } }
    @Test fun langScreen() { M.themeMode = 2; com.maigus.margey.text.Lang.current = 0; shoot("10_langue") { LanguageScreen {} } }
    @Test fun chatFr() { M.themeMode = 2; com.maigus.margey.text.Lang.current = 1; Kb.ayneha = false; shoot("11_conversation_fr") { MargeyTheme { chatSample() } }; com.maigus.margey.text.Lang.current = 0; Kb.ayneha = true }
    @Test fun permEn() { M.themeMode = 2; com.maigus.margey.text.Lang.current = 2; shoot("12_autorisations_en") { MargeyPermissionScreen {} }; com.maigus.margey.text.Lang.current = 0 }
    @Test fun learnAy() { M.themeMode = 2; com.maigus.margey.text.Lang.current = 0; shoot("13_apprendre") { MargeyTheme { LearnScreen {} } } }
    @Test fun alertFr() {
        M.themeMode = 2; com.maigus.margey.text.Lang.current = 1; Kb.ayneha = false
        shoot("14_alerte_fr") { MargeyTheme { androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().background(M.Night)) {
            androidx.compose.foundation.layout.Box(Modifier.weight(1f)) { AlertsBanner() }
            AlertSender("mamma")
        } } }
        com.maigus.margey.text.Lang.current = 0; Kb.ayneha = true
    }
    private fun salonSample(): Set<String> {
        com.maigus.margey.salon.SalonDirectory.wipe()
        com.maigus.margey.tata.TataKeys.initForTest(7)
        val mine = com.maigus.margey.salon.SalonDirectory.create("Marché de Gao", 30, "Maïgus")
        com.maigus.margey.tata.TataKeys.initForTest(8)
        val other1 = com.maigus.margey.salon.SalonDirectory.create("Marché de Gao", 7, "Moussa")
        com.maigus.margey.tata.TataKeys.initForTest(9)
        val other2 = com.maigus.margey.salon.SalonDirectory.create("Puits", 3, "Aïcha")
        com.maigus.margey.salon.SalonDirectory.forget(other1.id); com.maigus.margey.salon.SalonDirectory.forget(other2.id)
        com.maigus.margey.salon.SalonDirectory.onControl("\u0002MGS" + org.json.JSONArray().put(other1.toJson().put("n", 23)).put(other2.toJson().put("n", 5)))
        return setOf(mine.id)
    }
    @Test fun salonsFr() {
        M.themeMode = 2; com.maigus.margey.text.Lang.current = 1; Kb.ayneha = false
        val joined = salonSample()
        shoot("15_salons_fr") { MargeyTheme { androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(M.Night)) { SalonsContent(joined, { "Maïgus" }, { _, _ -> }, {}) } } }
        com.maigus.margey.text.Lang.current = 0; Kb.ayneha = true
    }
    @Test fun salonJoinAy() {
        M.themeMode = 2; com.maigus.margey.text.Lang.current = 0
        val joined = salonSample()
        shoot("16_salon_code_ay") { MargeyTheme { androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().background(M.Night)) { SalonsContent(joined, { "Maïgus" }, { _, _ -> }, {}, startPanel = 2) } } }
    }
    @Test fun bluetooth() { M.themeMode = 2; shoot("03_bluetooth") { MargeyBluetoothScreen {} } }

    private fun msg(text: String, sender: String) = BitchatMessage(sender = sender, content = text, timestamp = Date(1_761_480_000_000))

    @Composable private fun chatSample() {
        Column(Modifier.fillMaxSize().background(M.Night)) {
            Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Bubble(msg(Ay.B02, "ture"), outgoing = false)
                Bubble(msg(Ay.B03, "mamma"), outgoing = true)
                Bubble(msg(Ay.S13, "sise"), outgoing = false, showSender = true)
            }
            AynehaKeyboard(onChar = {}, onBackspace = {}, onEnter = {})
        }
    }
    @Test fun chatDark() { M.themeMode = 2; shoot("04_conversation_sombre") { MargeyTheme { chatSample() } } }
    @Test fun chatLight() { M.themeMode = 1; shoot("05_conversation_clair") { MargeyTheme { chatSample() } } }

    @Test fun settingsLight() {
        M.themeMode = 1
        shoot("06_reglages_clair") {
            MargeyTheme {
                Column(Modifier.fillMaxSize().background(M.Night).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingGroup {
                        SettingRow(Icons.Filled.Palette, Ay.E14) {}
                        ChipRow(Ay.E15.split(" / "), 1) {}
                        SettingRow(Icons.Filled.Lock, Ay.E10) {}
                        ChipRow(listOf(Ay.E11, Ay.S11 + " " + ayNum("1"), Ay.S11 + " " + ayNum("7"), Ay.S11 + " " + ayNum("30")), 0) {}
                    }
                    PinSetupPanel({}, {})
                }
            }
        }
    }
}
