# Margey - historique des versions

Numérotation : majeur.mineur.correctif. Le correctif monte à chaque livraison de corrections,
le mineur à chaque nouvelle fonction, le majeur pour un changement qui casse la compatibilité
entre téléphones. versionCode = majeur*10000 + mineur*100 + correctif.

## 1.2.0 - 2 novembre 2026
- Mise à jour : tirer la liste des discussions vers le bas cherche une nouvelle version sur
  ayneha.org (comme les autres applications AYNEHA) ; téléchargement avec empreinte SHA-256
  vérifiée, installation toujours confirmée par l'utilisateur.
- Autorisations : Internet (seulement pour cette vérification, et pour la liaison Wi-Fi directe
  entre téléphones) et installation d'applications. Les messages ne passent jamais par Internet :
  tout appel HTTP du code d'origine (Nostr, Tor, aperçus de liens...) reste bloqué.
- Confidentialité (application et PRIVACY_POLICY.md) et README mis à jour.

## 1.1.1 - 2 novembre 2026
- Logo de Margey : icône de l'application (adaptative, monochrome), notifications, écran de langue, À propos.
- Salons : code unique de 4 chiffres par salon, fiche signée par le créateur, durée de conservation
  propre à chaque salon (3 à 90 jours), liste des salons à portée, entrée avec le nom et le code.
- Réseau maillé : 20 sauts maximum (au lieu de 7).
- Vocal comme WhatsApp : une seule touche micro / envoi, maintenir pour parler, glisser vers le
  haut pour les mains libres, durée en direct avec barre qui s'arrête à la durée maximale.
- Photo reçue : ouverture en plein écran (pincer pour agrandir).
- Aperçus et notifications : « Photo » ou « Message vocal » au lieu du chemin du fichier.
- Traductions songhay : N01-N09, N22-N24, S15-S18.
- Version affichée dans Confidentialité / À propos.

## 1.0.0
- Première version de travail (onboarding AYNEHA, claviers AYNEHA et latin, groupes, salons,
  Tata-Set, alertes, facteurs, apprentissage).
