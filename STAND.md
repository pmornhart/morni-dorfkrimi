# Stand – Morni Dorfkrimi

**Was es ist:** Der Rätsel-Modus des MoinMorni-Netzwerks (Paper-Plugin `MorniDorfkrimi`, Java 25, Paper 26.x): Im Dorf
treibt ein Wilderer sein Unwesen, die Dorfleute müssen herausfinden, wer es ist. Geheime Rollen (Wilderer, Gendarm,
Dorfleute), Trachten, Spuren, Dienstmütze, Dorfversammlung. 4–16 Leute, nichts Blutiges: Betäubte schlafen auf der
Bank vor der Wirtschaft. Netzwerk-Kennung `dorfkrimi`, englisch „Village Mystery“. Repo `pmornhart/morni-dorfkrimi`.

## Aufbau (`src/main/java/de/moinmorni/dorfkrimi/`)

- `DorfkrimiPlugin` (Start, Welt, Takt), `Settings`, `Texts` (de/en), `Ui`, `Items`, `Holo`, `Sidebar` (Leiste,
  verborgene Namen), `Boards`, `Commands` (`/karte`, `/bereit`, `/notizbuch`, `/abstimmen`, `/bank`, `/zuschauen`,
  `/verlassen`, `/stats`, `/top`, `/krimiadmin`).
- `logic/` (ohne Server, mit Tests): `Role`, `Roles` (gerechte Verteilung mit Losen, Partys nie zwei Sonderrollen),
  `Colour`, `Part`, `Outfit`, `Outfits` (verschiedene Trachten, jede Farbe des Wilderers trägt noch jemand), `Clue`,
  `Clues` (wahre Hinweise, die eingrenzen, aber nicht gleich verraten), `Round` (betäuben, festnehmen, Mütze, Versammlung,
  Sieg, Punkte), `Meeting`, `Outcome`, `Score`, `Groups`, `Votes`, `Comeback`, `Cooldowns`.
- `data/`: `PlayerData` (mit Lose-Zählern), `Store`, `Leaderboard`.
- `world/` (Baupläne als Text): `Plan`, `Blocks`, `Decor`, `Kit` (Häuser mit zwei Türen und Leiter nach oben), `Rooms`
  (Einrichtung), `Town` (Kirche mit Turm, Scheune, Glockenstuhl, Brunnen, Bank, Hochsitz, Wald …), `OberdorfPlan`,
  `AlmPlan`, `HallPlan` (Dorflinde), `MapLayout`, `MapType` (Spuren-Stellen aus dem Bauplan), `Walk` (wohin man zu Fuß
  kommt), `Construction`, `VoidGenerator`.
- `game/`: `Game` (Ablauf, Rollen, Tracht, Spuren, Feder, Fußspuren, Mütze, Versammlung, Bank, Sichtbarkeit, Zurück ins
  Spiel, Auflösung, Statistik), `Games`, `Suspect`, `Menus` (Notizbuch, Stimmzettel).
- `hall/`: `Hall`, `HallMenus`. `menu/`, `listen/` (`PlayerListener`, `GameListener` mit Bank-Chat, `GuardListener`), `net/`.
- `test/`: `run.sh` (Ports 29200/29210), `dorfkrimi-test.cjs`, `rcon.cjs`, `fake-network/`.

## Entscheidungen (17.11.2026, erster Stand)

- Rollen: 1 Wilderer und 1 Gendarm, ab 10 Leuten je 2 (`runde.zwei-wilderer-ab`). Zwei Wilderer kennen sich
  („Jagdgeselle“) und können sich nicht betäuben.
- Gerechtigkeit: Lose je Person (Runden seit der letzten Wilderer- bzw. Gendarm-Rolle, bis 12), Gewicht (1 + Runden)²,
  gespeichert in der Spielerdatei. Test über 4000 Runden mit 8 Leuten: jeder 12,5 % ± 2 %, zweimal hintereinander
  Wilderer unter 2 % (Zufall wäre 12,5 %).
- Partys: kommen zusammen in eine Runde, bekommen aber nie zwei Sonderrollen (auch nicht Wilderer + Gendarm), solange es
  anders geht. Rollen sieht nur jeder selbst; auch Ausgeschiedene erfahren sie erst am Ende.
- Gendarm: eine Festnahme – richtig: nach einer Pause (`gendarm.festnahme-pause`) darf er wieder; falsch: Gendarm
  blamiert und raus, Mütze liegt; der Unschuldige bleibt im Spiel. Die Mütze fällt auch, wenn der Gendarm betäubt wird,
  bei der Versammlung gewählt wird oder nicht zurückkommt. Aufheben: nur wach und aus dem Dorf (nicht der Wilderer),
  durch Hingehen (auch Bedrock).
- Hinweise: nur über die Tracht (Hut, Joppe, Hose; 48 Kombinationen, keine doppelt). Spuren tauchen alle 20 s an
  festen, erreichbaren Stellen auf (auch in Häusern und Scheunen), nie direkt neben jemandem; nur der Finder erfährt den
  Hinweis. Der Wilderer verwischt Spuren. Feder am Tatort = Hutfarbe, Fußspuren 8 s nach dem Betäuben.
- Versammlung: Glocke (Rathaus bzw. Almglocke), höchstens 2 je Runde, frühestens nach 60 s, 30 s Abstimmung (endet
  früher, wenn alle gestimmt haben); Festnahme nur mit mindestens 2 Stimmen und mehr als jede andere Wahl inklusive
  „abwarten“; Unschuldige → Arrest (raus) und 3 Pech-Punkte für jeden, der ihn gewählt hat. Während der Versammlung steht
  die Uhr; danach steht jeder wieder an seinem Platz, 4 s Schonzeit.
- Ausgeschiedene: Bank vor der Wirtschaft (16 Plätze), unsichtbar für Wache, Bank-Chat nur untereinander und mit
  Zuschauern (Chat geht nicht ins Netzwerk), „Umschauen“ = Zuschauermodus, `/bank` zurück. `playing` endet beim
  Ausscheiden.
- Namen über den Köpfen verborgen (abschaltbar), alle tragen dieselbe Schnellleiste (Brotzeit, Notizbuch, Rollenkarte) –
  nur das Werkzeug in Platz 1 verrät die Rolle, wenn man es in der Hand hält.
- Zurück ins Spiel: Vorgabe 60 s, gleiche Rolle, gleicher Ort; zu spät = raus, ein Wilderer, der nicht zurückkommt, lässt
  das Dorf gewinnen („aus dem Staub gemacht“).

## Stand (17.11.2026)

- Version 0.1.0 (Branch `claude/erster-stand`, wartet auf Philips Go).
- Unit-Tests: **44** grün (Rollen inkl. Gerechtigkeit über 4000 Runden und Partys über 2000 Runden, Trachten, Hinweise
  über 500 Runden, Runde mit Betäuben/Festnehmen/Mütze/Versammlung/Sieg, Abstimmung, Daten, Texte, Einstellungen,
  Baupläne: Plätze, Glocke, Bank, Spuren-Stellen in allen Vierteln, Obergeschosse und Kirchturm erreichbar, Rand).
  Übersetzt gegen Paper-API 26.3 und 26.1.2.
- Echter Test (`test/run.sh`, Paper 26.1.2 + Test-Netzwerk, sechs Test-Spieler): **77/77**, Server-Log ohne Fehler
  (nur Mojang-429 für Köpfe im Offline-Test). Neubau ≈ 808 000 Blöcke in 76 s.
- Gefunden im echten Test: Die Wartezeiten wurden zu Beginn der Versammlung um die ganze Abstimmzeit verschoben, auch
  wenn alle früher gestimmt hatten – jetzt um die tatsächliche Dauer. Bei den Bildern: Der Wilderer verwischte die Feder
  sofort, weil er direkt neben dem Betäubten stand – Federn kann er jetzt nicht verwischen (Test prüft es); im Client
  tauchten „Rezept freigeschaltet“-Hinweise auf (jetzt abgefangen); die Leiste beim Start war zu lang. Danach echter
  Test wieder **77/77**.
- Bilder mit echtem Client 26.3 (über ViaVersion, nur lokal unter `~/mm-cache/bilder-dorfkrimi/`): Dorflinde, Oberdorf
  von oben, Dorfplatz, Glockenstuhl am Rathaus, Gasthaus mit Bank, Ausblick aus dem Kirchturm, Scheune, Haus innen,
  Wald mit Hochsitz, Alm von oben, Almwirtschaft mit Terrasse, Kapelle und Sennerei; in der Runde bei Nacht: Rolle und
  Tracht beim Start, „Hinweis!“, Feder am Tatort, liegende Dienstmütze mit Schrift, Blick von der Bank.

## Offen

- Philip: Go für `main`.
- Netzwerk-Repo: `DEFAULT_MODES`, `MODE_REPOS` (`dorfkrimi: 'pmornhart/morni-dorfkrimi'`), `MODE_JARS`
  (`Morni-Dorfkrimi.jar`), Lobby-Figur, Webseite (Statistik `games`, `wins`, `wins_wilderer`, `wins_gendarm`, `arrests`,
  `stuns`, `clues`, `points`, Bestwert `best_points`), Einstellungs-Katalog (alle Schlüssel der config.yml, Befehl
  `/krimiconfig`).
- Nicht echt geprüft: Bedrock (Geyser: verborgene Namen, Sitzen, Truhen-Menüs), echte Spieler (Gleichgewicht, wie
  schnell die Hinweise zum Ziel führen), zwei Wilderer mit echten Leuten (nur Unit-Tests), Paper 26.3 als Server.
