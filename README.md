# Morni Dorfkrimi

Der Rätsel-Modus des Minecraft-Netzwerks **MoinMorni** (moinmorni.net): **Im Dorf treibt ein Wilderer sein Unwesen –
findet heraus, wer es ist!** Geheime Rollen, Spuren, eine Dienstmütze und die Dorfversammlung. 4 bis 16 Leute, ganz
ohne Blut: Wer erwischt wird, schläft ein und setzt sich auf die Bank vor der Wirtschaft. Ein Paper-Plugin für
Paper 26.x (Java 25), im Netzwerk und auch ganz allein.

Englischer Name: **Village Mystery**.

## So wird gespielt

1. An der **Dorflinde** stimmt man mit dem **Kompass** über die Karte ab, liest im **Buch**, wie es geht, und sagt mit
   dem **grünen Farbstoff** „bereit“. Ab 4 Leuten läuft ein Countdown; sind alle bereit, geht es nach 5 Sekunden los.
2. Alle stehen auf dem Dorfplatz und erfahren ihre **geheime Rolle** (nur sie selbst):
   - **Wilderer** (einer, ab zehn Leuten zwei – sie kennen sich): betäubt mit dem **Schlafpulver** (in der Hand halten
     und jemanden anschlagen oder anklicken), danach muss er kurz warten; ganz am Anfang gilt eine kurze Schonzeit.
   - **Gendarm** (einer, ab zehn Leuten zwei): nimmt mit der **Dienstmütze** fest. Ein Wilderer: gefasst – nach einer
     Pause darf er wieder. Ein Unschuldiger: Der Gendarm ist blamiert und raus, seine **Mütze bleibt liegen**.
   - **Dorfleute**: alle anderen.
3. Jeder trägt eine eigene **Tracht** (Hut, Joppe, Lederhose in verschiedenen Farben), Namen über den Köpfen sind
   verborgen – man erkennt sich an der Tracht.
4. **Spuren**: Im Dorf tauchen leuchtende Spuren auf. Wer aus dem Dorf hingeht, erfährt etwas Wahres über die Tracht
   des Wilderers („Der Wilderer trägt keine rote Joppe“) – nur er selbst, also erzählt es den anderen! Der Wilderer
   verwischt Spuren, wenn er darübergeht (nur Federn nicht). Wo jemand betäubt wurde, liegt eine **Feder** (Farbe vom Hut des Wilderers),
   und kurz nach dem Betäuben leuchten seine **Fußspuren**. Das **Notizbuch** zeigt alle mit ihrer Tracht und wie gut
   sie zu den eigenen Hinweisen passen.
5. **Dienstmütze**: Liegt sie irgendwo (falsche Festnahme, Gendarm betäubt oder weg), hebt sie auf, wer aus dem Dorf
   hingeht – und ist der neue Gendarm. Der Wilderer kann sie nicht nehmen.
6. **Dorfversammlung**: Wer die **Glocke** läutet (Rathaus bzw. Almglocke), ruft alle in einen Kreis. Auf dem
   Stimmzettel wählt jeder, wer festgenommen wird – oder „abwarten“. Wer die meisten Stimmen hat (mindestens zwei, mehr
   als jede andere Wahl), wird festgenommen. War es ein Unschuldiger, gibt es Pech-Punkte für alle, die ihn gewählt
   haben. Danach steht jeder wieder dort, wo er war.
7. **Sieg**: Das Dorf gewinnt, wenn alle Wilderer gefasst sind (oder sich aus dem Staub gemacht haben). Der Wilderer
   gewinnt, wenn alle anderen schlafen oder die Zeit abläuft (Vorgabe 6 Minuten). Zum Schluss werden die Rollen verraten.
8. **Auf der Bank**: Wer betäubt, festgenommen, blamiert oder eingesperrt ist, sitzt vor der Wirtschaft, ist für die
   Wachen unsichtbar und redet nur mit den anderen auf der Bank (der Chat geht nicht ins Netzwerk). Mit dem **Auge**
   schwebt man herum, mit `/bank` geht es zurück.

**Punkte:** Spur +2, Mütze aufheben +2, Festnahme eines Wilderers +15, falsche Festnahme −5, richtig abgestimmt +4,
Pech-Punkte −3 (einstellbar), Betäuben +5 (den Gendarm +3 dazu), Sieg fürs Dorf +10 (wer noch wach ist +5), Sieg für den
Wilderer +20.

**Gerecht:** Wer lange keine Sonderrolle hatte, hat bessere Chancen darauf (Lose je Person, gespeichert) – so ist
niemand dauernd Wilderer. Partys spielen zusammen, bekommen aber nie zwei Sonderrollen (nie zwei Wilderer, nie Wilderer
und Gendarm aus einer Party), solange es anders geht.

**Für alle gleich in der Schnellleiste:** Brotzeit (kurz schneller), Notizbuch, Rollenkarte. Alles geht per Rechtsklick
und Antippen – auch auf Bedrock.

**Zurück ins Spiel:** Verliert jemand mitten in der Runde die Verbindung, bleibt sein Platz eine Weile frei
(`rueckkehr-sekunden`, Vorgabe 60) – mit derselben Rolle, an derselben Stelle. Zu spät: raus (eine Mütze bleibt liegen).
Sind alle Wachen weg, hält die Runde an.

## Karten (eigene Entwürfe, aus dem Code gebaut)

| Karte | Was es gibt |
|---|---|
| **Oberdorf bei Nacht** – ein Bergdorf bei Vollmond | Dorfplatz mit Brunnen (Start und Versammlung), Rathaus mit Glockenstuhl, Gasthaus „Zur Post“ mit Bank, Kirche mit begehbarem Turm, acht Häuser mit zwei Türen und Kammern oben, zwei Scheunen mit Heuboden, Backhaus, Holzlager, Obstgarten, Kräutergarten, Wald mit Jagdhütte und Hochsitz – Laternen leuchten |
| **Almwirtschaft im Nebel** – eine Hochalm im Morgengrauen | Almwirtschaft mit Terrasse und Bank, Almwiese mit Marterl und Almglocke, Kapelle mit Türmchen, Sennerei, Kuhstall, drei Heustadel, drei Holzhütten, Weiher, zwei Hochsitze, Bergwald – Nebelschwaden ziehen um jeden herum |

Dorflinde und Karten entstehen beim ersten Start in einer eigenen leeren Welt (`dorfkrimi`); auf jeder Karte läuft
höchstens eine Runde gleichzeitig. Ein Zaun mit unsichtbarer Wand hält alle auf der Karte.

## Befehle

| Befehl | Was er tut |
|---|---|
| `/karte [oberdorf\|almwirtschaft]` | über die Karte abstimmen (ohne Angabe: Menü) – auch `/map` |
| `/bereit` | bereit melden – auch `/ready` |
| `/notizbuch` | Notizbuch der Runde – auch `/notes` |
| `/abstimmen` | Stimmzettel der Dorfversammlung – auch `/vote` |
| `/bank` | ausgeschieden: zurück auf die Bank – auch `/bench` |
| `/zuschauen [spieler\|karte]` | einer Runde zuschauen – auch `/spectate` |
| `/verlassen` | Runde oder Zuschauen verlassen – auch `/leave` |
| `/stats [spieler]` · `/top` | Statistik und die besten Dorfdetektive |
| `/krimiadmin info\|status\|start [karte]\|vorspulen <los\|ende\|sekunden> [karte]\|stopp [karte]\|rolle <spieler> <rolle>\|spur <spieler>\|versammlung [karte]\|hinweise <spieler>\|wiederherstellen\|neubauen\|neuladen` | Verwaltung und Tests (`moinmorni.dorfkrimi.admin`) – auch `/dkadmin` |

Einstellungen stehen nur in der `config.yml` (mit Erklärungen): Zurück-ins-Spiel-Zeit, Countdown, Mindestzahl,
Höchstzahl, Start-, Spiel- und Nachspielzeit, ab wann zwei Wilderer, Namen verbergen, Schlafpulver-Pause, Schonzeit,
Fußspuren, Festnahme-Pause, Spuren (Takt, Anzahl, Federn), Versammlung (an/aus, Anzahl, ab wann, Abstimmzeit,
Pech-Punkte). Es gibt keine Einstellungs-Befehle – im Netzwerk kommen sie über den Einstellungs-Katalog des Panels.

## Im Netzwerk

Das Netzwerk-Plugin („MoinMorni“) stellt die Schnittstelle `de.moinmorni.api.MoinMorni` bereit; Dorfkrimi nutzt sie,
wenn sie da ist (`softdepend`): Sprache, Party (zusammen in die Runde), Statistik, Zustand, zurück zur Lobby. Ab Version 2
der Schnittstelle meldet Dorfkrimi, wer gerade **spielt** (`playing`: vom Rundenstart, solange man wach ist), schickt das
Bett an der Dorflinde über `askToLobby` in die Lobby und hält nach einem Verbindungsabbruch den Platz frei
(`holdSlot(uuid, sekunden)`, danach `holdSlot(uuid, 0)`). Ein älteres Netzwerk-Plugin kennt diese Methoden nicht – dann
geht es ohne Nachfrage weiter wie bisher.

Statistik nach jeder Runde (`stats("dorfkrimi", …)`, höher ist besser):

- hochgezählt: `games`, `wins`, `wins_wilderer`, `wins_gendarm` (Sieg mit Dienstmütze, auch aufgehoben), `arrests`
  (Festnahmen als Gendarm), `stuns` (betäubt als Wilderer), `clues` (Spuren gefunden), `points` (Pech-Punkte zählen als 0)
- Bestwert: `best_points`

Zustand: `waiting` oder `running`, dazu die Spielerzahl. Lokal: `spieler/<uuid>.yml` (mit den Lose-Zählern) und
`bestenliste.yml`.

Bei jedem Push auf `main` baut GitHub ein Release mit `Morni-Dorfkrimi.jar` (Version aus `pom.xml`).

## Bauen und testen

```bash
mvn package                      # Tests + target/Morni-Dorfkrimi.jar (Java 25)
mvn -Ppaper-26.1 compile         # passt der Code auch zu Paper 26.1?
test/run.sh                      # echter Test: Paper-Server + Test-Spieler (mineflayer)
FRESH=1 test/run.sh              # dasselbe, Welt vorher neu bauen
KEEP=1 SKIP_TEST=1 test/run.sh   # nur den Test-Server starten (z. B. für Bilder)
```

`test/run.sh` baut das Plugin und ein Test-Netzwerk (`test/fake-network`: Bert spricht Englisch, Anna und Carl sind
eine Party, alle Meldungen landen in `calls.log`), richtet einen Paper-26.1.2-Server ein (offline, Port 29200, RCON
29210, kurze Zeiten, 20 s Zurück ins Spiel) und spielt zwei Runden durch (`test/dorfkrimi-test.cjs`, Rollen per
`/krimiadmin rolle` vorgegeben): im Oberdorf zu viert (Dorflinde, Abstimmung, Rollen, Tracht, Nacht, verborgene Namen,
Notizbuch, Spur finden und verwischen, Dorfversammlung mit Unschuldigem und Pech-Punkten, Schonzeit, Betäuben, Feder,
Bank-Chat, richtige Festnahme, Auflösung, Statistik) und auf der Alm zu fünft mit Zuschauer (falsche Festnahme, Mütze
aufheben, Verbindung weg und zurück, Gendarm betäubt, zu spät zurück, Zeit um, Lobby-Knopf).

---

Verfasst mit Claude 🤖
