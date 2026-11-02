# Tata-Set - méthode de comptage des membres de Margey

Auteur : Mahamadou Issiaka MAIGA (MAIGUS), créateur d'AYNEHA et d'AYNEHA Margey.
Nom donné en hommage à sa mère, surnommée Tata.
Première version des règles : 27 octobre 2026. Code : github.com/maigus223/ayneha-margey.

Tata-Set est une liste de membres partagée sans serveur entre les téléphones d'un groupe ou
d'un salon, sur un réseau maillé (Bluetooth), avec des états actif / inactif, un plafond
de membres actifs tenu par une marge de sécurité, et un remplacement automatique de
l'administrateur. Elle s'inspire des ensembles répliqués sans conflit (CRDT : G-Set, 2P-Set,
∞P-Set) et du journal des membres de Berty / Wesh, en y ajoutant ces règles propres.

## 1. Règles communes aux groupes et aux salons

La liste des membres
- Chaque groupe et chaque salon a une liste cachée, partagée par tous ses membres.
- Chaque ligne : code unique et pseudo de la personne, état (actif / inactif), rang d'entrée
  daté, date de dernière présence vue en ligne, compteur de changements.
- Nombre de membres = nombre de lignes « actif ».
- Quand deux téléphones se croisent dans la maille, ils fusionnent leurs listes : pour une même
  personne, la ligne au compteur le plus grand l'emporte ; pour la date de présence, la plus récente.

Les états
- Actif : membre, même si son téléphone est éteint ou loin.
- Inactif : a quitté, a été exclu par l'administrateur, ou a été exclu d'office.

L'exclusion d'office
- Tout membre (administrateur compris) qu'aucun membre n'a vu en ligne pendant plus de 45 jours
  devient inactif automatiquement. Cela libère des places pour les nouveaux.

L'administrateur
- Au départ, le créateur. Lui seul peut exclure.
- S'il quitte ou est exclu d'office, il est remplacé automatiquement par le membre actif au plus
  ancien rang d'entrée.
- Un ancien administrateur qui revient reçoit un nouveau rang (fin de file) : simple membre.

Les retours
- Un membre qui revient prend toujours un nouveau rang d'entrée.
- Un exclu par l'administrateur ne peut être refait entrer que par l'administrateur en fonction.

Le dépassement du plafond
- Si des entrées simultanées font dépasser le plafond absolu, les dernières entrées en surplus
  (selon l'heure d'entrée) sont retirées automatiquement, avec un message. À heure égale, le
  code le plus petit est gardé.

Les messages d'information
- Entrée (qui a fait entrer qui), départ, exclusion, exclusion d'office, retrait pour surplus,
  nouvel administrateur.

Effacement par l'auteur
- Chacun peut effacer ses propres messages ; l'ordre d'effacement voyage dans la maille
  (un téléphone éteint garde le message jusqu'à ce qu'il reçoive l'ordre).

## 2. Groupes - plafond 100
- Entrée sur invitation par QR ; tout membre peut faire entrer jusqu'à 80 actifs.
- Marge 81 à 100 : réservée à l'administrateur ; elle absorbe aussi les entrées simultanées
  faites par des membres au-delà de 80.
- Un exclu d'office peut être réintégré par n'importe quel membre tant que le groupe a moins
  de 80 actifs.
- Messages chiffrés, lisibles par les seuls membres, jamais effacés automatiquement.

## 3. Salons - 500, plafond absolu 550
- Accès libre (entrer, sortir, revenir) jusqu'à 500 actifs.
- Marge de 10 % (501 à 550) : seulement pour les entrées simultanées ayant fait dépasser 500.
- Un exclu par l'administrateur ne peut pas revenir de lui-même.
- Messages publics, effacés après la durée choisie (3 à 90 jours) ou plus tôt par leur auteur.

## Comment citer
MAIGA, Mahamadou Issiaka (MAIGUS). « Tata-Set : méthode de comptage des membres sans serveur
pour réseau maillé ». AYNEHA Margey, 2026. https://github.com/maigus223/ayneha-margey

## Mise en œuvre dans Margey (version 1.0)
- Code : app/src/main/java/com/maigus/margey/tata/TataSet.kt (règles), group/Groups.kt (groupes),
  salon/SalonTata.kt (salons). Tests : app/src/test/java/com/maigus/margey/TataSetTest.kt.
- Chaque ligne porte un compteur de changements de type horloge logique : max(ancien + 1, heure actuelle).
- Chaque message porte une empreinte courte de la liste de son auteur ; si elle diffère, le téléphone
  qui la reçoit renvoie sa liste complète (au plus une fois toutes les 20 s pour un groupe, 60 s pour un salon).
- Dans les groupes, la liste voyage chiffrée avec la clé du groupe ; dans les salons, elle est publique.
- Signatures (protection contre l'usurpation) : chaque téléphone a sa propre clé Ed25519, créée sur
  l'appareil ; son code public est l'identifiant du membre. Chaque ligne de la liste est signée par
  la personne qui avait le droit de faire ce changement :
  - entrer, quitter, revenir, changer de pseudo : la personne elle-même ;
  - entrer dans un groupe : en plus, l'invitation (QR) porte la signature de celui qui invite ;
  - exclure, réintégrer un exclu : l'administrateur en fonction ;
  - constater 45 jours d'absence : l'administrateur (ou son suivant si c'est lui qui est absent) ;
  - réintégrer un absent de 45 jours : n'importe quel membre actif ;
  - effacer un message : son auteur.
  Tout changement mal signé, ou signé par quelqu'un qui n'en avait pas le droit, est refusé par
  chaque téléphone. Le surplus n'est pas signé : chaque téléphone le calcule de la même façon.
- Rang d'entrée protégé : personne ne peut se donner un rang plus ancien pour devenir administrateur
  (une entrée par invitation est toujours postérieure à l'invitation, elle-même postérieure à
  l'entrée de celui qui invite ; un retour est toujours postérieur à la sortie). Le créateur d'un
  groupe est connu par l'invitation.
- Limite restante (salons seulement, faute d'invitation) : un téléphone qui découvre un salon pour la
  première fois ne peut pas savoir qui l'a créé ; il fait confiance à la première liste reçue. Les
  téléphones qui connaissent déjà le salon refusent toute entrée antidatée.
