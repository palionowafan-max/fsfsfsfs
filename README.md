# robloxcloudly.gg – mod na serwer Fabric 1.21.11

**Mod tylko na serwer.** Gracze wchodzą zwykłym Minecraftem 1.21.11 – nic nie instalują.
Modele petów, bossów, jajek i hoverboarda są zbudowane z *display entities* (kolorowe bloki), menu są w skrzynkach.

## Co jest w modzie
- **Lobby** (osobny wymiar): latająca wyspa, spawn, wielki napis `robloxcloudly.gg`, chmury, fioletowy portal.
- **Skin na podium** – manekin z prawdziwym skinem gracza `Ziomsigiemka_1`. PPM → menu ze **100 mapami po 5 graczy**.
- **Hoverboard z boku lobby** – kliknij deskę → dostajesz Hoverboard. PPM nim = deska pod nogami + 35% prędkości.
- **Steal an Egg** (osobny wymiar): każda mapa = hub z **5 farmami** + **9 światów**
  (Las, Jezioro, Pustynia, Dżungla, Śnieżna Kraina, Wulkan, Otchłań Oceanu, Prehistoria, Kosmos).
- W każdym świecie 6 jajek na piedestałach (PPM = kradniesz). **Boss świata cię goni** – jak złapie, jajko wraca.
- Doniesione jajko wykluwa się na Twojej farmie → pet z modelem 3D, zarabia monety/s.
- Monety zbierasz stając na **złotym polu** przy farmie. **Bieżnia** (czarno-żółta) przy farmie = trening szybkości ⚡.
- Lepsze światy wymagają więcej szybkości (30…600). Sklep: 7 bieżni. Indeks: wszystkie pety + sprzedawanie.
- Postęp graczy zapisuje się w `world/robloxcloudly/players/`.

## Komendy
`/lobby`, `/farma`, `/mapy`, `/graj <1-100>`, `/sklep`, `/indeks`, `/hoverboard`
Admin (op): `/cloudly monety <ile>`, `/cloudly szybkosc <ile>`, `/cloudly reset`, `/cloudly odbuduj_lobby`

## Jak zbudować .jar (GitHub Actions)
1. Wrzuć zawartość tego folderu do nowego repo na GitHubie.
2. Zakładka **Actions** → workflow „Build mod” uruchomi się sam (albo *Run workflow*).
3. Po kilku minutach: zakończony build → **Artifacts → robloxcloudly-fabric** → w zipie jest `robloxcloudly-1.0.0.jar`.
   (Weź plik BEZ `-sources` w nazwie.)

## Instalacja na serwerze
- Serwer **Fabric 1.21.11** (Fabric Loader 0.18.1 lub nowszy) + **Fabric API** dla 1.21.11.
- `robloxcloudly-1.0.0.jar` i Fabric API → folder `mods` serwera. Java 21.
- Po starcie lobby buduje się samo; każda mapa buduje się przy pierwszym wejściu (kilka sekund).
- Gracz po wejściu na serwer od razu ląduje w lobby.

## Zmiana skina / nicku
`src/main/java/com/robloxcloudly/game/Lobby.java` → `NPC_NAME`. Potem `/cloudly odbuduj_lobby`.
