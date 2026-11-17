/**
 * Echter Test von Morni Dorfkrimi mit Test-Spielern (mineflayer) auf Paper 26.1.2 – gestartet von test/run.sh.
 *   node dorfkrimi-test.cjs <host> <port> <rcon-host:port> <rcon-passwort-datei> <server-ordner>
 *
 * Spieler: Anna (deutsch, Party-Leiterin mit Carl), Bert (englisch), Carl, Dora; in Runde 2 kommt Emil dazu, Fritz
 * schaut zu. Sprache und Party kommen vom Test-Netzwerk (test/fake-network), das Statistik, Zustand, „spielt gerade“,
 * „Platz freihalten“ und „zur Lobby“ in calls.log mitschreibt. Die Rollen werden für den Test vorgegeben
 * (/krimiadmin rolle), sonst entscheidet der Zufall.
 *
 * Runde 1 (Oberdorf bei Nacht): Dorflinde, Abstimmung, So geht's, Countdown, geheime Rollen, Tracht, Nacht, verborgene
 * Namen, Schnellleiste je Rolle, Notizbuch, Spur finden (Hinweis stimmt), Spur verwischen, Dorfversammlung (Unschuldiger
 * eingesperrt, Pech-Punkte), Schonzeit, Betäuben (Feder, Bank, unsichtbar für Wache), Feder finden, Bank-Chat,
 * richtige Festnahme → Dorf gewinnt, Auflösung, Statistik.
 * Runde 2 (Almwirtschaft im Nebel): fünf Leute und ein Zuschauer, falsche Festnahme (Gendarm blamiert, Mütze liegt),
 * Wilderer kann die Mütze nicht aufheben, Dorfbewohnerin schon, Verbindung weg und rechtzeitig zurück (gleiche Rolle),
 * Gendarm betäubt (Mütze fällt wieder), zu spät zurück, Zeit um → Wilderer gewinnt, Lobby-Knopf.
 *
 * Verfasst mit Claude 🤖
 */
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const mineflayer = require('mineflayer');
const { Vec3 } = require('vec3');
const { rcon } = require('./rcon.cjs');

const [HOST = '127.0.0.1', PORT = '29200', RCON = '127.0.0.1:29210', PASS_FILE, SERVER] = process.argv.slice(2);
const PASS = fs.readFileSync(PASS_FILE, 'utf8').trim();
const VERSION = process.env.MC_VERSION ?? '26.1';
const HALL = { x: 0.5, y: 101, z: 12.5 };
const COMEBACK = 20;

const results = [];
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
function check(name, ok, detail = '') {
  results.push({ name, ok: Boolean(ok) });
  console.log(`${ok ? '✓' : '✗'} ${name}${detail ? ` – ${detail}` : ''}`);
}

function plain(v) {
  if (v == null) return '';
  if (typeof v === 'string') {
    if (v.startsWith('{') || v.startsWith('[')) {
      try {
        return plain(JSON.parse(v));
      } catch {
        return v;
      }
    }
    return v;
  }
  if (Array.isArray(v)) return v.map(plain).join('');
  if (typeof v !== 'object') return String(v);
  if (v.type === 'compound') return plain(v.value);
  if (v.type === 'list') return plain(v.value?.value ?? []);
  if (v.type === 'string') return v.value;
  let s = '';
  if ('text' in v) s += plain(v.text);
  if ('' in v) s += plain(v['']);
  if ('translate' in v) s += plain(v.translate);
  if ('extra' in v) s += plain(v.extra);
  return s;
}

function offlineUuid(name) {
  const h = crypto.createHash('md5').update(`OfflinePlayer:${name}`, 'utf8').digest();
  h[6] = (h[6] & 0x0f) | 0x30;
  h[8] = (h[8] & 0x3f) | 0x80;
  const x = h.toString('hex');
  return `${x.slice(0, 8)}-${x.slice(8, 12)}-${x.slice(12, 16)}-${x.slice(16, 20)}-${x.slice(20)}`;
}

const cmd = (...commands) => rcon(RCON, PASS, commands);
const inWorld = (c) => `execute in minecraft:dorfkrimi run ${c}`;
const playerFile = (name) => path.join(SERVER, 'plugins/MorniDorfkrimi/spieler', `${offlineUuid(name)}.yml`);
const readFile = (file) => (fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : '');
const calls = () =>
  readFile(path.join(SERVER, 'plugins/MoinMorni/calls.log'))
    .split('\n')
    .filter(Boolean)
    .map((l) => JSON.parse(l));
const callsOf = (t, name, from = 0) => calls().slice(from).filter((c) => c.t === t && c.name === name);

async function waitFor(cond, ms = 5000, step = 150) {
  const start = Date.now();
  while (Date.now() - start < ms) {
    if (await cond()) return true;
    await sleep(step);
  }
  return Boolean(await cond());
}

let lastJoin = 0;
async function join(name) {
  const wait = lastJoin + 3500 - Date.now();
  if (wait > 0) await sleep(wait);
  lastJoin = Date.now();
  const bot = mineflayer.createBot({ host: HOST, port: Number(PORT), username: name, version: VERSION, auth: 'offline' });
  bot.chats = [];
  bot.bars = [];
  bot.titles = [];
  bot.on('messagestr', (m, position) => (position === 'game_info' ? bot.bars : bot.chats).push(m));
  bot._client.on('action_bar', (p) => bot.bars.push(plain(p.text)));
  bot._client.on('set_title_text', (p) => bot.titles.push(plain(p.text)));
  bot._client.on('set_title_subtitle', (p) => bot.titles.push(plain(p.text)));
  bot.hidden = new Set();
  bot._client.on('player_remove', (p) => (p.players ?? []).forEach((u) => bot.hidden.add(u)));
  bot.bossPackets = 0;
  bot._client.on('boss_bar', () => bot.bossPackets++);
  bot.on('error', (e) => console.log(`  (${name}: ${e.message})`));
  await new Promise((resolve, reject) => {
    bot.once('spawn', resolve);
    bot.once('kicked', (r) => reject(new Error(`${name} gekickt: ${plain(r)}`)));
    setTimeout(() => reject(new Error(`${name}: kein Spawn`)), 25000);
  });
  await sleep(1500);
  return bot;
}

const slot = (bot, i) => bot.inventory.slots[36 + i];
const near = (p, q, d) => Math.abs(p.x - q.x) < d && Math.abs(p.y - q.y) < d && Math.abs(p.z - q.z) < d;
const inHall = (bot) => near(bot.entity.position, HALL, 5) && bot.game.gameMode === 'adventure';
const sees = (bot, re) => bot.chats.some((c) => re.test(c));
const sawTitle = (bot, re) => bot.titles.some((t) => re.test(t));
const sawBar = (bot, re) => bot.bars.some((t) => re.test(t));
const clear = (...bots) =>
  bots.forEach((b) => {
    b.chats.length = 0;
    b.bars.length = 0;
    b.titles.length = 0;
  });

/** /krimiadmin status als Werte. */
async function status() {
  const lines = (await cmd('krimiadmin status'))[0].split('\n');
  const out = { info: {} };
  for (const part of lines[0].split(' ')) {
    const [k, v] = part.split('=');
    if (v !== undefined) out.info[k] = v;
  }
  for (const line of lines.slice(1)) {
    if (!line.startsWith('karte=')) continue;
    const s = { players: {}, traces: [], hats: {} };
    for (const part of line.split(' ')) {
      const i = part.indexOf('=');
      if (i < 0) continue;
      const k = part.slice(0, i);
      const v = part.slice(i + 1);
      let m;
      if ((m = /^spur(\d+)$/.exec(k))) {
        const [x, y, z, kind] = v.split(':');
        s.traces.push({ x: Number(x), y: Number(y), z: Number(z), kind });
      } else if ((m = /^muetze(\d+)$/.exec(k))) {
        const p = v.split(':');
        s.hats[m[1]] = p[0] === 'liegt' ? { lying: true, x: Number(p[1]), y: Number(p[2]), z: Number(p[3]) }
          : { holder: p[0], ready: p[1] === 'bereit' };
      } else if (k === 'glocke') {
        const [x, y, z] = v.split(':').map(Number);
        s.bell = { x, y, z };
      } else if (/^[A-Z]/.test(k)) {
        if (v === 'zuschauer') {
          s.players[k] = { state: 'zuschauer' };
        } else {
          const [role, status, online, p, st, a, c, w, outfit, hat, clues, vote, seat] = v.split(':');
          const n = (x) => Number(x.slice(1));
          const [hut, joppe, hose] = outfit.split('-');
          s.players[k] = { role, status, online, points: n(p), stuns: n(st), arrests: n(a), clues: n(c), waiting: n(w),
            hut, joppe, hose, hat: n(hat), notes: n(clues), vote: vote.slice(1), seated: n(seat) };
        }
      } else {
        s[k] = /^-?\d+(\.\d+)?$/.test(v) ? Number(v) : v;
      }
    }
    out[s.karte] = s;
  }
  return out;
}

async function openWithItem(bot, hotbar) {
  bot.setQuickBarSlot(hotbar);
  await sleep(250);
  const opened = new Promise((resolve) => {
    bot.once('windowOpen', resolve);
    setTimeout(() => resolve(null), 4000);
  });
  bot.activateItem();
  return opened;
}

async function tp(bot, x, y, z, yaw = 0, pitch = 0) {
  await cmd(inWorld(`tp ${bot.username} ${x} ${y} ${z} ${yaw} ${pitch}`));
  await waitFor(() => near(bot.entity.position, { x, y, z }, 1.5), 3000, 100);
  await sleep(250);
}

/** Schlag mit dem Gegenstand in Platz {@code hotbar} (Schlafpulver, Mütze). */
async function hit(attacker, victim, hotbar = 0, tries = 1) {
  attacker.setQuickBarSlot(hotbar);
  await sleep(250);
  for (let i = 0; i < tries; i++) {
    const target = attacker.players[victim.username]?.entity;
    if (target) {
      await attacker.lookAt(target.position.offset(0, 1.4, 0), true);
      attacker.attack(target);
    }
    await sleep(500);
  }
}

/** Zwei Leute nebeneinander stellen (der zweite schaut zum ersten). */
async function sideBySide(a, b) {
  const p = a.entity.position;
  await tp(b, p.x + 1.5, p.y, p.z, 90, 0);
}

/** Im offenen Menü den Eintrag mit diesem Namen anklicken. */
async function clickNamed(bot, re) {
  const w = bot.currentWindow;
  if (!w) return false;
  const i = w.slots.findIndex((s, idx) => idx < w.inventoryStart && s && re.test(plain(s.customName)));
  if (i < 0) return false;
  await bot.clickWindow(i, 0, 0);
  await sleep(400);
  return true;
}

async function waitWindow(bot, ms = 5000) {
  return waitFor(() => Boolean(bot.currentWindow), ms);
}

async function main() {
  // ------------------------------------------------------------------ Dorflinde
  await sleep(6000);
  const holos = String(await cmd(inWorld('execute positioned 0 101 0 if entity @e[type=text_display,distance=..40]')));
  check('Tafeln an der Dorflinde stehen (5 Tafeln × 2 Sprachen)', /: 10\b/.test(holos), holos.trim());
  const anna = await join('Anna');
  const bert = await join('Bert');
  check('Ankunft an der Dorflinde (Abenteuermodus)', inHall(anna), JSON.stringify(anna.entity.position));
  check('Schnellleiste: Kompass, Farbstoff, Buch, Gold, Bett',
    slot(anna, 0)?.name === 'compass' && slot(anna, 1)?.name === 'gray_dye' && slot(anna, 2)?.name === 'writable_book' &&
      slot(anna, 4)?.name === 'gold_nugget' && slot(anna, 8)?.name === 'red_bed', [0, 1, 2, 4, 8].map((i) => slot(anna, i)?.name).join(','));
  check('Deutsch: „Karte wählen“', plain(slot(anna, 0)?.customName).includes('Karte wählen'), plain(slot(anna, 0)?.customName));
  check('Englisch (Sprache vom Netzwerk): „Choose map“', plain(slot(bert, 0)?.customName).includes('Choose map'));
  check('Begrüßung im Chat (de/en)', sees(anna, /Servus an der Dorflinde/) && sees(bert, /Welcome to the Village Linden/));
  check('Netzwerk: Zustand „waiting“', calls().some((c) => c.t === 'state' && c.state === 'waiting'));

  let win = await openWithItem(anna, 0);
  check('Kompass öffnet „Welche Karte?“ mit beiden Karten', win && plain(win.title).includes('Welche Karte') &&
    plain(win.slots[11]?.customName).includes('Oberdorf bei Nacht') && plain(win.slots[15]?.customName).includes('Almwirtschaft'),
  win && plain(win.title));
  await anna.clickWindow(11, 0, 0);
  await sleep(600);
  check('Stimme für „Oberdorf bei Nacht“', sees(anna, /Deine Stimme: Oberdorf bei Nacht/));
  bert.chat('/karte oberdorf');
  await sleep(800);
  check('Bert stimmt per Befehl (englisch)', sees(bert, /Your vote: Upper Village by Night/), bert.chats.slice(-1)[0]);
  win = await openWithItem(anna, 2);
  check('Buch öffnet „So geht Dorfkrimi“ mit sechs Kapiteln', win && /So geht Dorfkrimi/.test(plain(win.title)) &&
    [10, 11, 12, 14, 15, 16].every((i) => win.slots[i]), win && plain(win.title));
  await anna.clickWindow(15, 0, 0);
  await sleep(500);
  check('Kapitel „Die Dorfversammlung“ erklärt sich im Chat', sees(anna, /Versammlung: Wer die Glocke läutet/));
  if (anna.currentWindow) anna.closeWindow(anna.currentWindow);

  // ------------------------------------------------------------------ Runde 1
  await cmd('krimiadmin rolle Bert wilderer', 'krimiadmin rolle Dora gendarm', 'krimiadmin rolle Anna dorf',
    'krimiadmin rolle Carl dorf');
  clear(anna, bert);
  await sleep(1500);
  check('Kein Countdown mit 2 von 4 nötigen Leuten', !anna.bars.some((b) => /Nächste Runde/.test(b)), anna.bars.slice(-1)[0]);
  const carl = await join('Carl');
  const dora = await join('Dora');
  const four = [anna, bert, carl, dora];
  check('Mit 4 Leuten läuft der Countdown', await waitFor(() => anna.bars.some((b) => /Nächste Runde in \d+ s · Oberdorf/.test(b)), 4000),
    anna.bars.slice(-1)[0]);
  for (const b of four) {
    b.setQuickBarSlot(1);
    await sleep(150);
    b.activateItem();
  }
  check('Start-Nachricht', await waitFor(() => sees(anna, /Auf geht's! Karte: Oberdorf bei Nacht · 4 Leute/), 12000),
    anna.chats.slice(-2).join(' | '));
  let s = (await status()).oberdorf;
  check('Rollen wie vorgegeben (geheim): Bert Wilderer, Dora Gendarm, Anna und Carl Dorf', s?.phase === 'START' &&
    s.players.Bert.role === 'wilderer' && s.players.Dora.role === 'gendarm' && s.players.Anna.role === 'dorf' &&
    s.players.Carl.role === 'dorf' && s.players.Dora.hat === 1, JSON.stringify(s?.players));
  check('Jede Rolle erfährt nur ihre eigene (Titel de/en)', await waitFor(() => sawTitle(anna, /Du gehörst zum Dorf/) &&
    sawTitle(bert, /You are the poacher/) && sawTitle(dora, /Du bist der Gendarm/), 4000) &&
    !sees(anna, /Bert/) && !sawTitle(anna, /Wilderer!/), anna.titles.join(' | '));
  const outfits = new Set(Object.values(s.players).map((p) => `${p.hut}-${p.joppe}-${p.hose}`));
  check('Vier verschiedene Trachten, angezogen (Lederhut, Joppe, Hose)', outfits.size === 4 &&
    anna.inventory.slots[5]?.name === 'leather_helmet' && anna.inventory.slots[6]?.name === 'leather_chestplate' &&
    anna.inventory.slots[7]?.name === 'leather_leggings', [...outfits].join(' '));
  check('Tracht steht im Chat', sees(anna, /Deine Tracht heute: .*Hut/));
  check('Alle stehen auf dem Dorfplatz', four.every((b) => Math.hypot(b.entity.position.x - 1000.5, b.entity.position.z - 0.5) < 7),
    four.map((b) => `${b.username}:${b.entity.position.x.toFixed(1)}/${b.entity.position.z.toFixed(1)}`).join(' '));
  check('Nacht im Oberdorf (nur für die Runde)', await waitFor(() => Math.abs((anna.time.timeOfDay ?? 0) - 18000) < 200, 3000),
    `${anna.time.timeOfDay}`);
  const hideTeam = Object.values(anna.teamMap ?? {}).find((t) => t.name === 'dkhide' || t.team === 'dkhide');
  check('Namen über den Köpfen verborgen', Boolean(hideTeam) &&
    /never/i.test(String(hideTeam.nameTagVisibility)), JSON.stringify(Object.keys(anna.teamMap ?? {})));
  await cmd('krimiadmin vorspulen los oberdorf');
  check('Countdown und „Los!“', await waitFor(() => sawTitle(anna, /Los!/) && sawTitle(bert, /Go!/), 6000));
  check('Schnellleiste: Bert Schlafpulver, Dora Dienstmütze, Anna nichts – alle Brotzeit, Notizbuch, Rollenkarte',
    await waitFor(() => slot(bert, 0)?.name === 'sugar' && slot(dora, 0)?.name === 'leather_helmet' && !slot(anna, 0) &&
      four.every((b) => slot(b, 1)?.name === 'bread' && slot(b, 4)?.name === 'book' && slot(b, 8)?.name === 'paper'), 3000),
    four.map((b) => [0, 1, 4, 8].map((i) => slot(b, i)?.name).join(',')).join(' | '));
  check('Netzwerk: „running“ und alle vier „spielen“', calls().some((c) => c.t === 'state' && c.state === 'running') &&
    ['Anna', 'Bert', 'Carl', 'Dora'].every((n) => callsOf('playing', n).some((c) => c.playing === true)));
  check('Leiste oben', await waitFor(() => anna.bossPackets > 0, 3000));

  // Notizbuch
  win = await openWithItem(anna, 4);
  check('Notizbuch zeigt vier Köpfe mit Tracht', win && /Notizbuch/.test(plain(win.title)) &&
    win.slots.slice(0, 18).filter((x) => x?.name === 'player_head').length === 4, win && plain(win.title));
  if (anna.currentWindow) anna.closeWindow(anna.currentWindow);

  // Spur finden
  clear(anna);
  await cmd('krimiadmin spur Anna');
  check('Anna findet eine Spur: „Hinweis!“', await waitFor(() => sees(anna, /Hinweis: (Der Wilderer|Einer der Wilderer)/), 3000),
    anna.chats.slice(-1)[0]);
  const code = (await cmd('krimiadmin hinweise Anna'))[0].replace('hinweise=', '').trim();
  s = (await status()).oberdorf;
  const bertOutfit = { hut: s.players.Bert.hut, joppe: s.players.Bert.joppe, hose: s.players.Bert.hose };
  const m = /^(hut|joppe|hose)(!?=)([a-z]+)#0$/.exec(code);
  check('Der Hinweis stimmt (passt zu Berts Tracht)', m && ((m[2] === '=') === (bertOutfit[m[1]] === m[3])), `${code} ${JSON.stringify(bertOutfit)}`);
  check('Hinweis zählt (+2 Punkte, im Notizbuch)', s.players.Anna.clues === 1 && s.players.Anna.points === 2 &&
    s.players.Anna.notes === 1, JSON.stringify(s.players.Anna));
  clear(bert);
  await cmd('krimiadmin spur Bert');
  check('Bert (Wilderer) verwischt eine Spur', await waitFor(() => sawBar(bert, /Clue wiped away/), 3000) &&
    (await status()).oberdorf.players.Bert.clues === 0, bert.bars.slice(-1)[0]);

  // Schonzeit: Bert darf noch nicht
  await sideBySide(carl, bert);
  clear(bert);
  await hit(bert, carl);
  s = (await status()).oberdorf;
  check('Schonzeit: Schlafpulver wirkt noch nicht', s.players.Carl.status === 'awake' && sawBar(bert, /ready again in/),
    `${s.players.Carl.status} ${bert.bars.slice(-1)[0]}`);
  check('Niemand nimmt Schaden', four.every((b) => b.health === 20), four.map((b) => b.health).join(','));

  // ------------------------------------------------------------------ Dorfversammlung (Unschuldiger)
  const bell = s.bell;
  await tp(anna, bell.x - 1.5, bell.y - 2, bell.z + 0.5, -90, -20);
  clear(...four);
  let rang = false;
  try {
    await anna.activateBlock(anna.blockAt(new Vec3(bell.x, bell.y, bell.z)));
    rang = true;
  } catch (e) {
    console.log(`  (Glocke: ${e.message})`);
  }
  check('Anna läutet die Glocke: Dorfversammlung', rang && await waitFor(async () => (await status()).oberdorf.phase === 'MEETING', 3000) &&
    await waitFor(() => sees(bert, /Village meeting!/) && sees(carl, /Dorfversammlung! Anna hat geläutet/), 2000),
  carl.chats.slice(-1)[0]);
  check('Alle stehen im Kreis um den Brunnen', await waitFor(() => four.every((b) =>
    Math.abs(Math.hypot(b.entity.position.x - 1000.5, b.entity.position.z - 0.5) - 7.5) < 1.2), 3000),
  four.map((b) => Math.hypot(b.entity.position.x - 1000.5, b.entity.position.z - 0.5).toFixed(1)).join(','));
  check('Stimmzettel geht von selbst auf', await waitFor(() => four.every((b) => b.currentWindow), 4000));
  // Anna und Bert wählen Carl, Carl wählt Bert, Dora wartet ab.
  check('Abstimmen per Klick', await clickNamed(anna, /^Carl$/) && await clickNamed(bert, /^Carl$/) &&
    await clickNamed(carl, /^Bert$/) && await clickNamed(dora, /Abwarten/));
  s = (await status()).oberdorf;
  check('Stimmen sind angekommen (wer wen gewählt hat, bleibt geheim)', s.players.Anna.vote === 'Carl' &&
    s.players.Dora.vote === 'abwarten' && sees(dora, /Bert hat abgestimmt \(\d\/4\)/) && !dora.chats.some((c) => /Bert.*Carl/.test(c)),
  JSON.stringify(s.players));
  check('Ergebnis: Carl (unschuldig) muss in den Arrest', await waitFor(() => sees(dora, /Carl war unschuldig/), 8000) &&
    await waitFor(async () => (await status()).oberdorf.players.Carl.status === 'jailed', 2000), dora.chats.slice(-2).join(' | '));
  s = (await status()).oberdorf;
  check('Pech-Punkte für Anna (2 − 3 = −1)', s.players.Anna.points === -1 && sees(anna, /Pech-Punkte/), JSON.stringify(s.players.Anna));
  check('Carl sitzt auf der Bank, „spielt“ nicht mehr', await waitFor(async () => (await status()).oberdorf.players.Carl.seated === 1, 3000) &&
    callsOf('playing', 'Carl').some((c) => c.playing === false) && sawTitle(carl, /Eingesperrt/));
  check('Die Wachen sehen Carl nicht mehr', await waitFor(() => anna.hidden.has(offlineUuid('Carl')) && bert.hidden.has(offlineUuid('Carl')), 3000));
  check('Zurück auf die Plätze (Phase PLAY)', (await status()).oberdorf.phase === 'PLAY');

  // Bank-Chat: Carl redet nur mit der Bank
  clear(anna, bert, dora);
  carl.chat('psst – nur für die Bank');
  await sleep(1200);
  check('Bank-Chat erreicht die Wachen nicht', !sees(anna, /psst/) && !sees(bert, /psst/) && !sees(dora, /psst/));

  // ------------------------------------------------------------------ Betäuben
  await sleep(4500); // Schonzeit nach der Versammlung
  await sideBySide(anna, bert);
  clear(anna, bert, dora, carl);
  await hit(bert, anna, 0, 2);
  check('Bert betäubt Anna', await waitFor(async () => (await status()).oberdorf.players.Anna.status === 'stunned', 3000) &&
    sawTitle(anna, /Betäubt/));
  check('Alle erfahren es (de/en), noch wach: 1', await waitFor(() => sees(dora, /Anna wurde betäubt! Noch wach im Dorf: 1/) &&
    sees(bert, /Anna was knocked out/), 2000), dora.chats.slice(-1)[0]);
  s = (await status()).oberdorf;
  const feather = s.traces.find((t) => t.kind === 'feder');
  check('Am Tatort liegt eine Feder', Boolean(feather), JSON.stringify(s.traces));
  check('Bert: 1 Betäubung, +5 Punkte (nach −3 Pech-Punkten: 2)', s.players.Bert.stuns === 1 && s.players.Bert.points === 2,
    JSON.stringify(s.players.Bert));
  clear(carl);
  anna.chat('Anna auf der Bank');
  check('Bank-Chat: Carl liest Anna', await waitFor(() => sees(carl, /\[Bank\] Anna » Anna auf der Bank/), 3000) && !sees(dora, /Anna auf der Bank/),
    carl.chats.slice(-1)[0]);
  clear(dora);
  await tp(dora, feather.x, feather.y, feather.z);
  check('Dora findet die Feder: Hutfarbe des Wilderers', await waitFor(() => sees(dora, /Eine Feder vom Tatort: Der Wilderer trägt einen/), 3000),
    dora.chats.slice(-1)[0]);
  const fcode = (await cmd('krimiadmin hinweise Dora'))[0];
  check('Die Feder stimmt', fcode.includes(`hut=${bertOutfit.hut}#0`), `${fcode} ${bertOutfit.hut}`);

  // ------------------------------------------------------------------ Festnahme → Dorf gewinnt
  mark = calls().length;
  await sleep(500);
  await sideBySide(bert, dora);
  clear(...four);
  await hit(dora, bert, 0, 2);
  check('Dora nimmt Bert fest: Das Dorf gewinnt', await waitFor(() => sawTitle(anna, /Das Dorf gewinnt/) &&
    sawTitle(bert, /The village wins/), 4000), anna.titles.slice(-2).join(' | '));
  check('Auflösung: „Der Wilderer war: Bert“ mit Tracht', await waitFor(() => sees(carl, /Der Wilderer war: Bert/) &&
    sees(carl, /Bert trug:/) && sees(carl, /Mit Dienstmütze unterwegs: Dora/), 2000), carl.chats.slice(-3).join(' | '));
  const st = calls().slice(mark).filter((c) => c.t === 'stats');
  const statOf = (n) => st.find((c) => c.name === n);
  check('Statistik: 4 Meldungen mit den richtigen Werten', st.length === 4 && statOf('Dora')?.add.wins === 1 &&
    statOf('Dora')?.add.wins_gendarm === 1 && statOf('Dora')?.add.arrests === 1 && statOf('Bert')?.add.wins === 0 &&
    statOf('Bert')?.add.stuns === 1 && statOf('Anna')?.add.clues === 1 && statOf('Carl')?.add.wins === 1 &&
    statOf('Dora')?.best.best_points > 0, JSON.stringify(st.map((c) => [c.name, c.add])));
  check('Niemand „spielt“ mehr', ['Anna', 'Bert', 'Dora'].every((n) => callsOf('playing', n).slice(-1)[0]?.playing === false));
  check('Spielerdatei mit Lose-Zählern', /lose:\s*\n\s*wilderer: 0/.test(readFile(playerFile('Bert'))) &&
    /siege-als-gendarm: 1/.test(readFile(playerFile('Dora'))), readFile(playerFile('Bert')).slice(0, 200));
  check('Alle zurück an der Dorflinde', await waitFor(() => four.every(inHall), 15000),
    four.map((b) => `${b.username}:${b.game.gameMode}`).join(' '));
  check('Tag an der Dorflinde, alle sehen sich wieder', await waitFor(() => Math.abs((anna.time.timeOfDay ?? 0) - 6000) < 300, 3000) &&
    Boolean(anna.players.Carl), `${anna.time.timeOfDay}`);
  check('Oberdorf wird wiederhergestellt', await waitFor(async () => (await status()).info.frei?.includes('oberdorf'), 40000, 1000));

  // ------------------------------------------------------------------ Runde 2 (Almwirtschaft)
  const emil = await join('Emil');
  await cmd('krimiadmin rolle Carl wilderer', 'krimiadmin rolle Anna gendarm', 'krimiadmin rolle Bert dorf',
    'krimiadmin rolle Dora dorf', 'krimiadmin rolle Emil dorf');
  for (const b of [anna, bert, carl, dora, emil]) b.chat('/karte almwirtschaft');
  await sleep(800);
  const five = [anna, bert, carl, dora, emil];
  await cmd('krimiadmin start almwirtschaft');
  check('Runde 2: Almwirtschaft mit fünf Leuten', await waitFor(() => sees(anna, /Karte: Almwirtschaft im Nebel · 5 Leute/), 5000),
    anna.chats.slice(-1)[0]);
  const fritz2 = await join('Fritz');
  fritz2.chat('/zuschauen almwirtschaft');
  check('Fritz schaut zu', await waitFor(() => fritz2.game.gameMode === 'spectator', 4000) &&
    (await status()).almwirtschaft.players.Fritz?.state === 'zuschauer');
  check('Morgengrauen auf der Alm', await waitFor(() => Math.abs((anna.time.timeOfDay ?? 0) - 23300) < 300, 3000), `${anna.time.timeOfDay}`);
  await cmd('krimiadmin vorspulen los almwirtschaft');
  await waitFor(() => slot(anna, 0)?.name === 'leather_helmet', 6000);

  // falsche Festnahme
  await sideBySide(bert, anna);
  clear(...five);
  await hit(anna, bert, 0, 2);
  check('Anna nimmt Bert fest – unschuldig: Anna blamiert, Bert bleibt', await waitFor(async () =>
    (await status()).almwirtschaft.players.Anna.status === 'blamed', 3000) && (await status()).almwirtschaft.players.Bert.status === 'awake' &&
    await waitFor(() => sees(dora, /Falsche Festnahme!/) && sees(bert, /Phew – you're innocent/), 2000), dora.chats.slice(-1)[0]);
  s = (await status()).almwirtschaft;
  const hat = s.hats['0'];
  check('Die Dienstmütze liegt am Boden (mit Schrift)', hat?.lying === true, JSON.stringify(s.hats));
  await tp(carl, hat.x, hat.y, hat.z);
  await sleep(800);
  check('Der Wilderer kann die Mütze nicht aufheben', (await status()).almwirtschaft.hats['0'].lying === true);
  await tp(carl, hat.x + 4, hat.y, hat.z);
  clear(dora);
  await tp(dora, hat.x, hat.y, hat.z);
  check('Dora hebt die Mütze auf und ist jetzt Gendarm', await waitFor(async () =>
    (await status()).almwirtschaft.hats['0'].holder === 'Dora', 3000) && sawTitle(dora, /Du bist jetzt Gendarm/) &&
    await waitFor(() => slot(dora, 0)?.name === 'leather_helmet', 2000));

  // Verbindung weg und rechtzeitig zurück
  mark = calls().length;
  emil.quit();
  check(`Emil weg: Platz bleibt ${COMEBACK} s frei`, await waitFor(() => callsOf('hold-slot', 'Emil', mark).some((c) => c.seconds === COMEBACK), 5000));
  await sleep(3000);
  const emil2 = await join('Emil');
  check('Emil ist rechtzeitig zurück: gleiche Rolle, wieder wach', await waitFor(() => sees(emil2, /Willkommen zurück/), 5000) &&
    (await status()).almwirtschaft.players.Emil.online === 'da' && (await status()).almwirtschaft.players.Emil.role === 'dorf' &&
    callsOf('hold-slot', 'Emil', mark).some((c) => c.seconds === 0) && slot(emil2, 1)?.name === 'bread', emil2.chats.slice(-1)[0]);

  // Der Wilderer betäubt die neue Gendarmin: Mütze fällt wieder
  await sleep(1000);
  await sideBySide(dora, carl);
  clear(...five);
  await hit(carl, dora, 0, 2);
  check('Carl betäubt Dora (Gendarmin): Mütze fällt', await waitFor(async () => {
    const x = (await status()).almwirtschaft;
    return x.players.Dora.status === 'stunned' && x.hats['0'].lying === true;
  }, 3000) && await waitFor(() => sees(bert, /constable is knocked out/), 2000));
  check('Carl: 8 Punkte (Gendarm betäubt)', (await status()).almwirtschaft.players.Carl.points === 8,
    JSON.stringify((await status()).almwirtschaft.players.Carl));

  // zu spät
  mark = calls().length;
  emil2.quit();
  check(`Nach ${COMEBACK} s: Emil ist nicht rechtzeitig zurück`, await waitFor(() => sees(bert, /Emil didn't come back in time/),
    (COMEBACK + 15) * 1000, 500));
  check('Angebot zurückgenommen (holdSlot 0)', callsOf('hold-slot', 'Emil', mark).some((c) => c.seconds === 0));
  const emil3 = await join('Emil');
  check('Emil kommt zu spät: Dorflinde und Hinweis', await waitFor(() => inHall(emil3) && sees(emil3, /Leider zu spät/), 6000));

  // Zeit um: Wilderer gewinnt
  mark = calls().length;
  clear(anna, bert, carl, dora, fritz2);
  await cmd('krimiadmin vorspulen ende almwirtschaft');
  check('Zeit um: Der Wilderer gewinnt', await waitFor(() => sawTitle(bert, /The poacher wins/) && sawTitle(carl, /Wilderer gewinnt/), 5000),
    bert.titles.slice(-2).join(' | '));
  const st2 = calls().slice(mark).filter((c) => c.t === 'stats');
  check('Statistik: 5 Meldungen (auch Emil), Carl wins_wilderer', st2.length === 5 &&
    st2.find((c) => c.name === 'Carl')?.add.wins_wilderer === 1 && st2.find((c) => c.name === 'Emil'),
  JSON.stringify(st2.map((c) => [c.name, c.add.wins])));
  check('Alle zurück an der Dorflinde (auch Fritz)', await waitFor(() => [anna, bert, carl, dora, fritz2].every(inHall), 15000),
    [anna, bert, carl, dora, fritz2].map((b) => `${b.username}:${b.game.gameMode}`).join(' '));

  // ------------------------------------------------------------------ Lobby-Knopf
  anna.setQuickBarSlot(8);
  await sleep(200);
  anna.activateItem();
  check('Bett: zur Lobby – fragt das Netzwerk (an der Dorflinde ohne Nachfrage)', await waitFor(() =>
    calls().some((c) => c.t === 'ask-lobby' && c.name === 'Anna' && c.asked === false), 4000), JSON.stringify(calls().slice(-2)));

  for (const b of [bert, carl, dora, emil3, fritz2]) b.quit();
  await sleep(800);
  const ok = results.filter((r) => r.ok).length;
  console.log(`\n${ok}/${results.length} Prüfungen bestanden`);
  process.exit(ok === results.length ? 0 : 1);
}

let mark = 0;
main().catch((e) => {
  console.error(e);
  const ok = results.filter((r) => r.ok).length;
  console.log(`\nAbgebrochen – ${ok}/${results.length} Prüfungen bestanden`);
  process.exit(1);
});
