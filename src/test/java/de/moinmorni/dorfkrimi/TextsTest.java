package de.moinmorni.dorfkrimi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.moinmorni.dorfkrimi.logic.Colour;
import de.moinmorni.dorfkrimi.logic.Outcome;
import de.moinmorni.dorfkrimi.logic.Part;
import de.moinmorni.dorfkrimi.logic.Role;
import de.moinmorni.dorfkrimi.logic.Round;
import java.util.Locale;
import de.moinmorni.dorfkrimi.world.MapType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.Test;

class TextsTest {

    /** Formatierungen, die keine Platzhalter sind. */
    private static final Set<String> STYLES = Set.of("gray", "white", "sky", "au", "em", "bl", "rt", "dark_gray", "b",
            "bold");
    /** Namen von Standard-Tags: ein Platzhalter darf nie so heißen (sonst wäre er eine Farbe oder fett). */
    private static final Set<String> STANDARD = Set.of("red", "blue", "green", "gold", "yellow", "aqua", "black", "b",
            "i", "u", "st", "bold", "italic", "white", "gray", "grey", "dark_gray", "br", "reset", "obf", "newline");
    // Tags wie <name> oder </gray> – aber nicht \<spieler> (maskiert).
    // https://regex101.com/?flavor=java&regex=(%3F%3C!%5C%5C)%3C%2F%3F(%5Ba-z_%5D%2B)%3E&testString=%3Cgray%3EHi%20%3Cname%3E%3C%2Fgray%3E%20%5C%3Cspieler%3E
    private static final Pattern TAG = Pattern.compile("(?<!\\\\)</?([a-z_]+)>");
    /** Text-Schlüssel als feste Zeichenkette im Quelltext, z. B. "tree.water" (nicht "map." + id). */
    // https://regex101.com/?flavor=java&regex=%22((%3F%3Ahall%7Ctrace%29%5C.%5Ba-z0-9.%5C-%5D*%5Ba-z0-9%5D)%22&testString=Texts.msg(lang%2C%20%22trace.found%22)%20%22map.%22
    private static final Pattern KEY = Pattern.compile("\"((?:hall|item|menu|game|role|outfit|clue|out|ghost|stun|arrest"
            + "|hat|trace|meeting|snack|time|bar|side|end|spectate|leave|stats|top|board|lobby|only|unknown|map|howto)"
            + "[.\\-][a-z0-9.\\-]*[a-z0-9])\"");

    private static Set<String> placeholders(String text) {
        Set<String> out = new TreeSet<>();
        Matcher m = TAG.matcher(text);
        while (m.find()) {
            if (!STYLES.contains(m.group(1))) {
                out.add(m.group(1));
            }
        }
        return out;
    }

    @Test
    void everyTextExistsInBothLanguagesWithTheSamePlaceholders() {
        for (String key : Texts.keys()) {
            String de = Texts.raw("de", key);
            String en = Texts.raw("en", key);
            assertFalse(de.isBlank(), key);
            assertFalse(en.isBlank(), key);
            assertEquals(placeholders(de), placeholders(en), "Platzhalter in " + key);
            for (String p : placeholders(de)) {
                assertFalse(STANDARD.contains(p), "Platzhalter heißt wie ein Standard-Tag: " + key + " <" + p + ">");
            }
        }
    }

    @Test
    void everyTextParsesStrictly() {
        MiniMessage strict = MiniMessage.builder().strict(true)
                .editTags(t -> t.resolvers(
                        TagResolver.resolver("sky", Tag.styling(TextColor.color(Texts.SKY))),
                        TagResolver.resolver("au", Tag.styling(TextColor.color(Texts.GOLD))),
                        TagResolver.resolver("em", Tag.styling(TextColor.color(Texts.EMERALD))),
                        TagResolver.resolver("bl", Tag.styling(TextColor.color(Texts.BLUE))),
                        TagResolver.resolver("rt", Tag.styling(TextColor.color(Texts.RED)))))
                .build();
        for (String key : Texts.keys()) {
            for (String lang : new String[] {"de", "en"}) {
                String text = Texts.raw(lang, key);
                TagResolver.Builder resolvers = TagResolver.builder();
                placeholders(text).forEach(p -> resolvers.resolver(Placeholder.unparsed(p, "x")));
                TagResolver all = resolvers.build();
                for (String line : text.split("\n")) {
                    assertDoesNotThrow(() -> strict.deserialize(line, all), lang + " " + key + ": " + line);
                }
            }
        }
    }

    /** Minecraft zeigt nur Zeichen der „Basic Multilingual Plane“ – Emoji wären ein leeres Kästchen. */
    @Test
    void onlyCharactersMinecraftCanShow() {
        for (String key : Texts.keys()) {
            for (String lang : new String[] {"de", "en"}) {
                String text = Texts.raw(lang, key);
                assertEquals(text.length(), text.codePointCount(0, text.length()), lang + " " + key);
            }
        }
    }

    @Test
    void englishIsReallyEnglish() {
        assertNotEquals(Texts.raw("de", "end.village.title"), Texts.raw("en", "end.village.title"));
        assertEquals(Texts.raw("de", "map.oberdorf"), Texts.raw("fr", "map.oberdorf"), "unbekannte Sprache → Deutsch");
        assertEquals("missing.key", Texts.raw("de", "missing.key"));
    }

    /** Jede Karte, Rolle, Farbe, jeder Hinweis, Ausgang, Grund fürs Ausscheiden und jedes Kapitel hat Texte. */
    @Test
    void everyGameThingHasAName() {
        Set<String> keys = Texts.keys();
        for (MapType m : MapType.values()) {
            for (String s : new String[] {"", ".desc", ".short"}) {
                assertTrue(keys.contains("map." + m.id() + s), m.id() + s);
            }
        }
        for (Role r : Role.values()) {
            assertTrue(keys.contains("role." + r.id()), r.id());
            for (String k : new String[] {"role.title.", "role.sub.", "role.chat."}) {
                assertTrue(keys.contains(k + r.id()), k + r.id());
            }
        }
        for (Part part : Part.values()) {
            for (Colour c : part.colours()) {
                assertTrue(keys.contains("outfit." + part.id() + "." + c.id()), part + " " + c);
                assertTrue(keys.contains("clue." + part.id() + ".ja." + c.id()), part + " " + c);
                assertTrue(keys.contains("clue." + part.id() + ".nein." + c.id()), part + " " + c);
            }
        }
        for (String k : new String[] {"goal", "roles", "clues", "hat", "meeting", "bench"}) {
            for (String s : new String[] {"", ".text", ".chat"}) {
                assertTrue(keys.contains("howto." + k + s), k + s);
            }
        }
        for (Outcome.How how : Outcome.How.values()) {
            String k = how.name().toLowerCase(Locale.ROOT);
            assertTrue(keys.contains("end." + k) && keys.contains("end.sub." + k), k);
        }
        for (String why : new String[] {"stunned", "arrested", "blamed", "voted", "jailed"}) {
            for (String s : new String[] {".title", ".sub", ".chat"}) {
                assertTrue(keys.contains("out." + why + s), why + s);
            }
        }
        for (Round.Status st : Round.Status.values()) {
            assertTrue(keys.contains("menu.notes.status." + st.name().toLowerCase(Locale.ROOT)), st.name());
        }
    }

    /** Jeder Schlüssel, der im Quelltext steht, hat einen Text. */
    @Test
    void everyKeyUsedInTheCodeExists() throws IOException {
        Set<String> missing = new TreeSet<>();
        int found = 0;
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = KEY.matcher(Files.readString(f, StandardCharsets.UTF_8));
                while (m.find()) {
                    found++;
                    if (!Texts.keys().contains(m.group(1))) {
                        missing.add(m.group(1) + " (" + f.getFileName() + ")");
                    }
                }
            }
        }
        assertTrue(found > 100, "nur " + found + " Schlüssel gefunden – stimmt das Muster?");
        assertEquals(Set.of(), missing);
    }

    @Test
    void numbersAndClock() {
        assertEquals("▮▮▮▯▯", Ui.bar(0.6, 5));
        assertEquals("1:05", Ui.clock(65));
        assertEquals("0:00", Ui.clock(-3));
    }
}
