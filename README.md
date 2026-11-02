<p align="center"><img src="docs/logo/margey-logo-512.png" width="160" alt="Margey"></p>

# Margey

Messagerie Android sans Internet, sans compte et sans serveur, en trois langues : AYNEHA (songhay), français et anglais.
Le choix de la langue se fait au premier lancement, puis à tout moment avec l'icône globe en haut de chaque écran.
Les téléphones se parlent en Bluetooth (et en Wi-Fi Aware quand il est activé) et forment une maille :
chaque téléphone relaie les messages des autres, jusqu'à 20 sauts.

Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS) - contact : gwokmt2q@duck.com
Dépôt : github.com/maigus223/ayneha-margey

## Confidentialité

Voir PRIVACY_POLICY.md. Le README d'origine de bitchat est conservé dans docs/bitchat/.

## Licences

- Code : GNU GPL v3 ou ultérieure (voir LICENSE.md). AYNEHA Margey est dérivé de bitchat-android
  (github.com/permissionlesstech/bitchat-android), lui-même sous GPL v3.
- Police AYNEHA Pro (app/src/main/res/font/ayneha.ttf) : SIL Open Font License 1.1.
- Images et textes : CC BY-SA 4.0.

## Ce qui a été changé par rapport à bitchat

- Interface en trois langues (AYNEHA, français, anglais) ; clavier AYNEHA intégré ou clavier habituel du téléphone, au choix.
- Comptage des membres des groupes et des salons par la méthode Tata-Set (voir docs/TATA-SET.md).
- Photos et vocaux partout (privé, groupes chiffrés, salons) ; vocaux de 60 s (30 s dans les salons).
- Facteurs : un message pour un absent est fermé avec sa clé (X25519 + AES-256-GCM) et transporté par tous
  les téléphones pendant 7 jours (100 Mo au plus par téléphone), sans qu'ils puissent le lire.
- Alertes d'urgence écrites ou vocales, horodatées, signées, avec fin de prise en charge.
- Partage de l'application de téléphone à téléphone, et apprentissage de l'AYNEHA (en français ou en anglais).

- Nom du paquet : com.maigus.aynehamargey, nom affiché : Margey.
- Aucune autorisation Internet : Nostr, Tor et les relais sont neutralisés (AppConstants.MARGEY_OFFLINE_ONLY).
- Toute l'interface est réécrite en AYNEHA, affichée de droite à gauche (paquet com.maigus.margey) :
  Discussions, Autour de moi, Salons, Réglages, Contact par QR, Partage en image, Confidentialité.
- Clavier AYNEHA intégré (10-10-7, variantes par appui long, page des chiffres).
- Salons de proximité : chaque salon a un nom et un code unique de 4 chiffres (deux salons peuvent
  porter le même nom sans se mélanger). Sa fiche, signée par son créateur, fixe la durée de conservation
  des messages, de 3 jours (minimum) à 90 jours (maximum). Les salons des téléphones à portée
  s'affichent dans une liste : un toucher pour entrer ; on peut aussi entrer avec le nom et le code.
- Salons : 500 personnes au maximum par salon.
- Groupes privés : invitation par QR, messages chiffrés (AES-256-GCM) que seuls les membres peuvent lire,
  100 personnes au maximum, messages jamais effacés automatiquement (seul l'effacement d'urgence les supprime).
- Verrou par code à 4 chiffres, effacement automatique, effacement d'urgence, thème clair / sombre / automatique,
  taille du texte.
- Les mentions pas encore traduites s'affichent provisoirement en français (liste Ay.A_TRADUIRE).
- Les textes AYNEHA sont dans app/src/main/java/com/maigus/margey/text/Ay.kt (générés depuis le tableau des mentions).

## Ouvrir et compiler dans Android Studio

1. Installer Android Studio (version récente) depuis developer.android.com/studio.
2. Décompresser AYNEHA-Margey-source.zip (dossier ayneha-margey).
3. Dans Android Studio : menu File > Open..., choisir le dossier décompressé, puis cliquer sur OK.
4. Attendre la fin de « Gradle sync » (barre en bas de la fenêtre). Si Android Studio propose d'installer
   un SDK manquant, cliquer sur Install.
5. Pour essayer sur un téléphone branché en USB : activer le débogage USB sur le téléphone, le choisir dans
   la liste des appareils en haut de la fenêtre, puis cliquer sur le bouton Run (triangle vert).
6. Créer ta clé de signature (une seule fois, sur ton ordinateur, hors Internet) :
   - menu Build > Generate Signed App Bundle or APK... ;
   - cocher APK, puis cliquer sur Next ;
   - sous « Key store path », cliquer sur Create new... ;
   - « Key store path » : cliquer sur l'icône de dossier, choisir où enregistrer le fichier, nommer ayneha-margey.jks, cliquer sur OK ;
   - « Password » et « Confirm » : ton mot de passe du magasin de clés ;
   - « Alias » : margey ; « Password » et « Confirm » de la clé : ton mot de passe de la clé ;
   - « Validity (years) » : 50 ;
   - « First and Last Name » : Mahamadou Issiaka MAIGA ; « Organization » : AYNEHA ; « Country Code » : ML ;
   - cliquer sur OK.
7. Fabriquer l'APK signé : dans la même fenêtre, la clé est remplie ; cliquer sur Next, choisir « release »,
   puis cliquer sur Create. Android Studio affiche « locate » en bas à droite : cliquer dessus pour ouvrir
   le dossier app/release qui contient l'APK.
8. Pour les mises à jour suivantes, refaire l'étape 7 avec « Choose existing... » et le même fichier ayneha-margey.jks.

Important : garde ayneha-margey.jks et ses mots de passe hors d'Internet, avec une copie de secours
(clé USB par exemple). Sans eux, les téléphones refuseront les mises à jour de l'application déjà installée.

## Captures d'écran automatiques (facultatif)

    ./gradlew :app:testDebugUnitTest --tests com.maigus.margey.MargeyScreenshotTest -PmargeyShots=/chemin/captures
