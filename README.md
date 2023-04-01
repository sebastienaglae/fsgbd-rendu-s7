# FSGB - Rendu simulation table SQL

Vous trouverez dans ce repository le rendu de notre groupe simulant une table SQL :
- AGLAE Sebastien
- CHIAPPE Mike
- HAMDI Ons
- FERUGLIO Téo
- LAPSHINA Valeriaa

Le programme simule une table avec 4 colonnes : 
- id: Clé primaire
- email: Unique
- nom: Texte
- adresse: Texte

Le programme permet de :
- Ajouter une ligne via les champs de saisie
- Supprimer une ligne par son id
- Ajouter x lignes aléatoirement
- Visualiser la table par les valeurs des colonnes :
  - si la table utilise un index, alors le BTree+ est affiché
  - sinon, la table est affichée sous forme de liste
- Sauvegarder la table dans un fichier (via ObjectOutputStream)
- Charger une table depuis un fichier (via ObjectInputStream)
- Exporter une table dans un fichier CSV
- Importer une table depuis un fichier CSV
- Benchmark de la table (avec et sans index)

### Résultats du benchmark testant l'insertion et la recherche avec et sans index BTree+

Voici les résultats qu'on a obtenu en faisant tourner le benchmark sur notre machine (AMD Ryzen 9 5900X, 32GB RAM) :

|                   | Without Index                           | With Index                            |
|-------------------|-----------------------------------------|---------------------------------------|
| Insert 1k items   | min=0.062µs, max=9.825µs, avg=0.093µs   | min=0.033µs, max=0.446µs, avg=0.116µs |
| Insert 50k items  | min=0.042µs, max=8.616µs, avg=0.087µs   | min=0.033µs, max=0.831µs, avg=0.119µs |
| Random read (1k)  | min=0.03µs, max=3575.7µs, avg=40.231µs  | min=0.056µs, max=1.019µs, avg=0.066µs |                                     |
| Random read (50k) | min=0.035µs, max=3592.2µs, avg=16.429µs | min=0.055µs, max=0.247µs, avg=0.066µs |

On peut conclure que l'insertion est plus rapide sans index.
En revanche, le résultat de la recherche varie en fonction de la taille de la table.
Si la table est petite (< 100 éléments), la recherche est plus rapide sans index.
Si la table est grande (> 100 éléments), la recherche est plus rapide avec index.
La raison est que l'index BTree+ permet de réduire le nombre de comparaisons à faire pour trouver un élément dans la table.