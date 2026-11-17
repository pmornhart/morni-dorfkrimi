# Morni Dorfkrimi

@STAND.md

## Regeln für dieses Projekt

- Repo ist **öffentlich**: keine Geheimnisse, Tokens oder privaten Daten committen (Geheimes nur in `.env`, nie im Repo).
  Auch keine Test-Server-Ordner, Welten, Spielerdaten oder RCON-Passwörter.
- Der Modus heißt **Dorfkrimi** (englisch „Village Mystery“), das Repo `pmornhart/morni-dorfkrimi`, die Jar
  `Morni-Dorfkrimi.jar`, das Plugin `MorniDorfkrimi`, die Kennung im Netzwerk `dorfkrimi`.
- Texte im Spiel auf Deutsch (Standard) und Englisch, freundlich, keine Fachbegriffe ohne Erklärung. Alle Texte stehen
  in `Texts.java` (MiniMessage, beide Sprachen); `TextsTest` prüft Platzhalter, Zeichen, dass jeder Schlüssel aus dem
  Quelltext einen Text hat, dass jede Karte, Rolle, Trachtfarbe, jeder Hinweis, Ausgang und Grund fürs Ausscheiden einen
  Text hat und dass kein Platzhalter wie ein Standard-Tag heißt. Eigene Farb-Tags: `<sky>` Himmelblau, `<au>` Gold,
  `<em>` Smaragd, `<bl>` Blau (Gendarm), `<rt>` Rot (Warnung); Wilderer-Grün als `<#8FBF4A>`.
- **„Verfasst mit Claude 🤖“** als letzte Zeile in Commit-Messages (durch eine Leerzeile getrennt), in PRs, Issues und
  Release-Notes; einmal als Fußzeile im README.
- Commits auf Deutsch mit Stichpunkten `- Neu:` / `- Behoben:` / `- Verbessert:` / `- Entfernt:`.
- Keine fremden Marken, Figuren, Namen, Texte oder Grafiken (nichts von Mojang, Nintendo, Hypixel oder anderen Servern
  und Spielen; auch nicht „Murder Mystery“, „TTT“, „Among Us“): nur die Mechanik „geheime Rollen, Rätselraten“ als
  Vorbild, eigene Namen (Wilderer, Gendarm, Dienstmütze, Dorfversammlung), eigene Karten, eigene Bauten.
- **Nichts Blutiges:** Niemand nimmt Schaden. Der Wilderer betäubt mit Schlafpulver, der Gendarm nimmt fest; wer raus ist,
  setzt sich auf die Bank vor der Wirtschaft und schaut zu. Nichts für echtes Geld.
- **Nichts verraten:** Rollen kennt nur jeder selbst (Partys bekommen nie zwei Sonderrollen); Ausgeschiedene und
  Zuschauer sind für die Wachen unsichtbar und reden nur untereinander (Bank-Chat, geht nicht ins Netzwerk).
- Die Netzwerk-Schnittstelle `src/api/java/de/moinmorni/api/MoinMorni.java` ist eine Kopie aus `pmornhart/moinmorni.net`
  (`plugins/moinmorni-network/src/main/java/de/moinmorni/api/MoinMorni.java`) und muss **genau gleich** bleiben. Sie wird
  nur mitübersetzt und kommt **nicht** in die Jar. Nur `net/Connected` darf sie benutzen.
- Dorfkrimi muss auch ohne Netzwerk laufen (`softdepend: [MoinMorni]`; Sprache dann aus dem Client).
- Paper 26.x, Java 25. Der Code muss auch gegen Paper 26.1 übersetzen (`mvn -Ppaper-26.1 compile`, läuft in der CI).
- Regeln ohne Server (Rollen gerecht verteilen, Trachten, Hinweise, Runde mit Betäuben/Festnehmen/Mütze/Versammlung,
  Sieg, Punkte, Abstimmung, Zurück ins Spiel) gehören in `logic/`, Daten in `data/` – ohne Bukkit, mit Tests. Werte dort
  nur zusammen mit den Tests ändern.
- Baupläne (`world/`) arbeiten nur mit Blocknamen als Text – so prüft `MapPlanTest` ohne Server (mit `Walk`), dass
  Start- und Versammlungsplätze frei und erreichbar sind, die Glocke erreichbar ist, die Bank 16 Plätze hat, Spuren-Stellen
  überall im Dorf liegen, Obergeschosse und Kirchturm begehbar sind und es nur Blöcke von 26.x gibt (z. B. `iron_chain`).
- Ändern sich Dorflinde oder Karten: `Construction.VERSION` erhöhen – dann baut der Server beim nächsten Start neu.
  Nach jeder Runde stellt `Construction.restore` die Karte Block für Block nach dem Bauplan wieder her.
- Neue Version: `<version>` in `pom.xml` erhöhen und `docs/releases/v<version>.md` anlegen; ein Push auf `main` baut das
  Release mit `Morni-Dorfkrimi.jar` – das Netzwerk holt es sich von dort.
- Vor jeder Veröffentlichung (Merge nach `main`) Philips Go abwarten.
- Nach jeder sichtbaren Änderung Bilder wie im echten Minecraft (echter Client aus `moinmorni.net/tools/mc-client`,
  Texturen und Bilder nur lokal, nie ins Repo).
- „Alles im Panel“: jede Einstellung, die per Befehl geht, muss auch im Panel gehen. Darum hat Dorfkrimi nur die
  `config.yml` und Verwaltungs-Befehle zum Testen (`/krimiadmin`); die Anbindung an den Einstellungs-Katalog gehört ins
  Netzwerk-Repo.
- Bedrock-Spieler (Geyser) spielen mit: alle Menüs sind Truhen-Fenster, alles geht per Rechtsklick/Antippen (Betäuben
  und Festnehmen auch durch Anklicken), Spuren und Mütze hebt man durch Hingehen auf.
- Echter Test nach jeder Änderung am Spielablauf: `test/run.sh` (siehe README).
- `STAND.md` nach jedem größeren Schritt aktualisieren.
