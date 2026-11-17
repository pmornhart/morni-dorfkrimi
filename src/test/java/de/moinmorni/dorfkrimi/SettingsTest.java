package de.moinmorni.dorfkrimi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Die mitgelieferte config.yml ist dasselbe wie die Vorgaben im Code; unsinnige Werte werden gestutzt. */
class SettingsTest {

    private static YamlConfiguration shipped() throws IOException, InvalidConfigurationException {
        YamlConfiguration y = new YamlConfiguration();
        y.loadFromString(Files.readString(Path.of("src/main/resources/config.yml"), StandardCharsets.UTF_8));
        return y;
    }

    @Test
    void shippedConfigEqualsTheDefaults() throws Exception {
        Settings fromFile = Settings.load(shipped());
        Settings empty = Settings.load(new YamlConfiguration());
        assertEquals(empty, fromFile);
        assertEquals(60, fromFile.comebackSeconds());
        assertEquals(360, fromFile.roundSeconds());
        assertEquals(10, fromFile.twoFrom());
    }

    @Test
    void everyKeyInTheShippedConfigIsRead() throws Exception {
        YamlConfiguration y = shipped();
        String code = Files.readString(Path.of("src/main/java/de/moinmorni/dorfkrimi/Settings.java"),
                StandardCharsets.UTF_8);
        for (String key : y.getKeys(true)) {
            if (!y.isConfigurationSection(key)) {
                assertTrue(code.contains("\"" + key + "\""), "config.yml-Schlüssel wird nicht gelesen: " + key);
            }
        }
    }

    @Test
    void silllyValuesAreClamped() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("rueckkehr-sekunden", 99999);
        y.set("runde.minuten", 0);
        y.set("dorflinde.mindestens-spieler", 1);
        y.set("runde.hoechstens-spieler", 50);
        y.set("runde.zwei-wilderer-ab", 99);
        y.set("versammlung.hoechstens", -3);
        y.set("hinweise.alle-sekunden", 1);
        Settings s = Settings.load(y);
        assertEquals(600, s.comebackSeconds());
        assertEquals(120, s.roundSeconds());
        assertEquals(4, s.hallMinPlayers());
        assertEquals(16, s.maxPlayers());
        assertEquals(Settings.NEVER_TWO, s.twoFrom());
        assertEquals(0, s.meetingMax());
        assertEquals(5, s.clueEvery());
        assertTrue(s.hideNames() && s.feathers() && s.meetings());
    }
}
