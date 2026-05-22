# 📱 Android RecyclerView — Guide Complet

---

## Étape 1 — Créer les ressources de base

Avant tout, configurer les couleurs et thèmes dans :

- `res/values/colors.xml`
- `res/values/themes.xml`

---

## Étape 2 — Créer l'Activity principale

Dans `activity_main.xml`, ajouter un **RecyclerView** dans le layout.  
Ce RecyclerView va afficher automatiquement les cartes.

Dans `movie_item_list.xml` (layout de chaque item), créer une **CardView** qui contient :

```xml
<CardView>
    <LinearLayout>
        <ImageView />       <!-- image du film -->
        <TextView />        <!-- nom du film -->
        <TextView />        <!-- date du film -->
    </LinearLayout>
</CardView>
```

> ⚠️ **Important :** Dans le root layout, toujours mettre `android:layout_height="wrap_content"` pour éviter les problèmes d'affichage.

---

## Étape 3 — Créer le modèle de données : `MyMovieData`

```java
public class MyMovieData {
    String name;      // nom du film
    String date;      // date (String)
    int imageRes;     // ID de l'image (integer)

    // Constructeur
    public MyMovieData(String name, String date, int imageRes) {
        this.name = name;
        this.date = date;
        this.imageRes = imageRes;
    }

    // Getters & Setters
    public String getName() { return name; }
    public String getDate() { return date; }
    public int getImageRes() { return imageRes; }
}
```

---

## Étape 4 — Initialiser les données dans `MainActivity`

```java
// 1. Récupérer le RecyclerView
RecyclerView recyclerView = findViewById(R.id.recyclerView);

// 2. Créer un tableau de films
MyMovieData[] movies = new MyMovieData[]{
    new MyMovieData("Inception", "2010", R.drawable.inception),
    new MyMovieData("Interstellar", "2014", R.drawable.interstellar),
    new MyMovieData("The Dark Knight", "2008", R.drawable.dark_knight)
};

// 3. Appliquer le LayoutManager
recyclerView.setLayoutManager(new LinearLayoutManager(this));

// 4. Créer et attacher l'adapter
MovieAdapter adapter = new MovieAdapter(movies, this);
recyclerView.setAdapter(adapter);
```

> 💡 Il existe deux types de `LayoutManager` :
> - `LinearLayoutManager` → liste verticale (1 colonne)
> - `GridLayoutManager(this, 2)` → grille (2 colonnes)

---

## Étape 5 — Créer l'Adapter : `MovieAdapter`

L'adapter fait le lien entre les **données** (tableau de films) et le **RecyclerView** (affichage).

```java
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    MyMovieData[] movies;
    Context context;

    // Constructeur
    public MovieAdapter(MyMovieData[] movies, Context context) {
        this.movies = movies;
        this.context = context;
    }
```

### Les 3 méthodes obligatoires à implémenter

| Méthode | Rôle |
|---|---|
| `onCreateViewHolder` | Fabriquer une nouvelle carte vide |
| `onBindViewHolder` | Remplir une carte avec les données d'un film |
| `getItemCount` | Retourner le nombre total de films |

---

## Étape 6 — Créer le `ViewHolder`

Le ViewHolder représente **une carte** dans la liste. Il garde les références aux vues de la carte.

```java
public class ViewHolder extends RecyclerView.ViewHolder {

    TextView txtViewMovieName;
    TextView txtViewMovieData;
    ImageView imageViewMovie;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        txtViewMovieName = itemView.findViewById(R.id.textViewMovieName);
        txtViewMovieData = itemView.findViewById(R.id.textViewMovieData);
        imageViewMovie   = itemView.findViewById(R.id.imageViewMovie);
    }
}
```

---

## Étape 7 — Implémenter `onCreateViewHolder`

Cette méthode **crée une nouvelle carte vide** à partir du fichier XML.

```java
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(context);

    // Convertit movie_item_list.xml en objet View
    View view = inflater.inflate(R.layout.movie_item_list, parent, false);

    return new ViewHolder(view);
}
```

### Pourquoi `false` et pas `true` ?

| Valeur | Comportement | Résultat |
|---|---|---|
| `true` | Attache directement la vue au parent | ❌ Crash — "view already has a parent" |
| `false` | Crée la vue sans l'attacher | ✅ Correct — le RecyclerView gère l'ajout |

> 💡 **`parent`** = le RecyclerView lui-même (le conteneur des cartes)  
> Le `LayoutInflater` transforme un fichier XML en objet `View` Java.

---

## Étape 8 — Implémenter `onBindViewHolder`

Cette méthode **remplit une carte** avec les données d'un film.

```java
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    MyMovieData movie = movies[position];

    holder.txtViewMovieName.setText(movie.getName());
    holder.txtViewMovieData.setText(movie.getDate());
    holder.imageViewMovie.setImageResource(movie.getImageRes());
}
```

---

## Étape 9 — Implémenter `getItemCount`

```java
@Override
public int getItemCount() {
    return movies.length;
}
```

---

## 🧠 Schéma de fonctionnement

```
MainActivity
    │
    ├── RecyclerView  (affiche la liste)
    │       │
    │       └── MovieAdapter  (fait le lien données ↔ affichage)
    │               │
    │               ├── onCreateViewHolder → crée une carte vide (ViewHolder)
    │               ├── onBindViewHolder   → remplit la carte avec MyMovieData
    │               └── getItemCount       → nombre d'items
    │
    └── MyMovieData[]  (tableau de données)
```

---

## 🎯 Phrase clé à retenir

> **`parent`** est le RecyclerView qui sert de conteneur pour les items de la liste.  
> On passe **`false`** à `inflate()` car le RecyclerView gère lui-même l'ajout des vues.

---

## Code complet de l'Adapter

```java
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    MyMovieData[] movies;
    Context context;

    public MovieAdapter(MyMovieData[] movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyMovieData movie = movies[position];
        holder.txtViewMovieName.setText(movie.getName());
        holder.txtViewMovieData.setText(movie.getDate());
        holder.imageViewMovie.setImageResource(movie.getImageRes());
    }

    @Override
    public int getItemCount() {
        return movies.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtViewMovieName;
        TextView txtViewMovieData;
        ImageView imageViewMovie;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtViewMovieName = itemView.findViewById(R.id.textViewMovieName);
            txtViewMovieData = itemView.findViewById(R.id.textViewMovieData);
            imageViewMovie   = itemView.findViewById(R.id.imageViewMovie);
        }
    }
}
```
Guide — Ajouter la recherche en temps réel
Cette fonctionnalité permet de filtrer la liste de films pendant que l'utilisateur tape dans une barre de recherche.

# 📱 MoviesApp — Guide Complet de A à Z
> Projet Android — Pr. OUHMIDA Asmae  
> Chaque fichier, chaque classe, chaque ligne est expliquée.

---

## 📋 Table des matières

1. [Architecture générale](#1-architecture-générale)
2. [build.gradle — Les dépendances](#2-buildgradle--les-dépendances)
3. [AndroidManifest.xml — Permissions](#3-androidmanifestxml--permissions)
4. [Les layouts XML](#4-les-layouts-xml)
5. [MyMovieData.java — Le modèle](#5-mymoviedatajava--le-modèle)
6. [MainActivity.java — Écran principal + API TMDB](#6-mainactivityjava--écran-principal--api-tmdb)
7. [MyMovieAdapter.java — Adapter + Filtre de recherche](#7-mymovieadapterjava--adapter--filtre-de-recherche)
8. [MovieDetailActivity.java — Détails + Maps](#8-moviedetailactivityjava--détails--maps)
9. [VideoPlayer.java — Lecteur de trailer](#9-videoplayerjava--lecteur-de-trailer)
10. [Flux de navigation complet](#10-flux-de-navigation-complet)

---

## 1. Architecture générale

### Schéma des fichiers du projet

```
MoviesApp/
│
├── java/.../
│   ├── MainActivity.java          ← Point d'entrée : liste des films depuis l'API
│   ├── MyMovieData.java           ← Objet "Film" : stocke les données d'un film
│   ├── MyMovieAdapter.java        ← Adapter : fait le lien données ↔ RecyclerView
│   ├── MovieDetailActivity.java   ← Détails d'un film + Google Maps + trailer
│   └── VideoPlayer.java           ← Lecteur du trailer YouTube via WebView
│
└── res/layout/
    ├── activity_main.xml          ← Écran principal (barre de recherche + liste)
    ├── activity_movie_item_list.xml ← Layout d'UNE carte film dans la liste
    ├── activity_movie_detail.xml  ← Layout des détails d'un film
    └── activity_video_player.xml  ← Layout du lecteur vidéo
```

### Les 3 briques fondamentales à comprendre

| Brique | Fichier | Rôle |
|--------|---------|------|
| **Modèle** | `MyMovieData.java` | Définit ce qu'est un film (nom, date, image, id) |
| **Adapter** | `MyMovieAdapter.java` | Transforme les données en cartes visuelles |
| **Vue** | `activity_main.xml` + `activity_movie_item_list.xml` | Ce que l'utilisateur voit à l'écran |

### Comment fonctionne l'app — Vue globale

```
① App démarre → MainActivity.onCreate()
        │
② Volley envoie une requête GET à l'API TMDB (internet)
        │
③ TMDB répond avec un JSON contenant la liste des films
        │
④ On lit (parse) ce JSON → on crée des objets MyMovieData[]
        │
⑤ On passe ce tableau à MyMovieAdapter → setAdapter()
        │
⑥ Le RecyclerView affiche les cartes films à l'écran
        │
⑦ L'utilisateur tape dans la barre → filtre en temps réel
        │
⑧ L'utilisateur clique → MovieDetailActivity (détails + map + trailer)
        │
⑨ L'utilisateur clique "Play" → VideoPlayer (YouTube embed)
```

---

## 2. build.gradle — Les dépendances

```gradle
dependencies {
    // Google Maps : afficher une carte interactive dans l'app
    implementation ("com.google.android.gms:play-services-maps:17.0.0")

    // Google Location : accéder à la position GPS de l'utilisateur
    implementation ("com.google.android.gms:play-services-location:17.0.0")

    // Volley : faire des requêtes HTTP vers l'API TMDB en arrière-plan
    // Sans Volley, on ne peut pas appeler internet depuis Android (ça bloquerait l'écran)
    implementation ("com.android.volley:volley:1.2.0")

    // Glide : charger et afficher des images depuis une URL internet
    // Il télécharge l'image en arrière-plan et la place dans un ImageView automatiquement
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    // Le compiler de Glide est nécessaire pour que ses annotations fonctionnent

    // RecyclerView, CardView, AppCompat, Material, ConstraintLayout
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
}
```

---

## 3. AndroidManifest.xml — Permissions

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!--
        PERMISSION INTERNET : OBLIGATOIRE pour appeler l'API TMDB et charger les images.
        Sans cette ligne, Android refuse toute connexion réseau et l'app plante.
    -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!--
        PERMISSION RÉSEAU : permet de vérifier si une connexion est disponible
        avant d'envoyer une requête (évite les erreurs inutiles).
    -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!--
        PERMISSION GPS PRÉCIS : nécessaire pour obtenir la position exacte de l'utilisateur.
        Utilisé dans MovieDetailActivity pour centrer la carte Google Maps sur l'utilisateur.
    -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!--
        PERMISSION POSITION APPROXIMATIVE : position via WiFi ou réseau mobile.
        Moins précise que le GPS, mais consomme moins de batterie.
        Requise en complément de ACCESS_FINE_LOCATION depuis Android 12.
    -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.Movie_app_bdcc">

        <!--
            CLÉ API GOOGLE MAPS : à obtenir sur https://cloud.google.com/
            Cette clé est lue automatiquement par le SDK Google Maps au démarrage.
            Sans elle, la carte affiche une erreur ou reste vide.
            ⚠️ REMPLACER "your_API_key" par votre vraie clé !
        -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="your_API_key" />

        <!--
            MAINACTIVITY : activité de démarrage de l'application.
            android:exported="true" = peut être lancée depuis l'extérieur (obligatoire pour LAUNCHER).
            L'intent-filter MAIN + LAUNCHER indique à Android que c'est le point d'entrée.
        -->
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <!--
            Toute activité utilisée dans l'app DOIT être déclarée ici.
            Si elle ne l'est pas → crash immédiat au lancement de cette activité.
        -->
        <activity android:name=".MovieDetailActivity"/>
        <activity android:name=".VideoPlayer"/>

    </application>
</manifest>
```

---

## 4. Les layouts XML

### 4.1 `activity_main.xml` — Écran principal

```xml
<!--
    LinearLayout VERTICAL : les enfants sont empilés de haut en bas.
    C'est le conteneur racine de tout l'écran.
    descendantFocusability="beforeDescendants" : le LinearLayout capte le focus
    avant ses enfants, ce qui empêche l'EditText de s'ouvrir automatiquement
    au démarrage de l'app.
-->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#BDBDBD"
    android:descendantFocusability="beforeDescendants">

    <!--
        BARRE DE RECHERCHE : champ texte dans lequel l'utilisateur tape.
        - hint="Search" : texte grisé affiché quand le champ est vide.
        - inputType="text" : clavier texte standard (pas de chiffres, pas de mails).
        - id="editTextSearch" : pour le retrouver dans MainActivity avec findViewById().
    -->
    <EditText
        android:id="@+id/editTextSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search"
        android:inputType="text"/>

    <!--
        RECYCLERVIEW : la liste qui affiche toutes les cartes films.
        - layout_height="wrap_content" : IMPORTANT → évite les problèmes de scroll.
          Si on met "match_parent", le RecyclerView prend tout l'écran et
          le scroll peut ne pas fonctionner correctement.
        - layout_margin="2dp" : petite marge autour de la liste.
        - id="recyclerView" : pour le retrouver dans MainActivity.
    -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="2dp"/>

</LinearLayout>
```

---

### 4.2 `activity_movie_item_list.xml` — Carte d'un film

```xml
<!--
    Ce fichier représente UNE SEULE CARTE film dans la liste.
    Il est "gonflé" (instancié) par le LayoutInflater dans l'Adapter,
    une fois pour chaque carte visible à l'écran (environ 7-10 fois total).
    ⚠️ layout_height="wrap_content" est CRUCIAL ici.
    Si on met "match_parent", chaque carte prendrait tout l'écran.
-->
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/CardView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="5dp"
    app:cardCornerRadius="10dp"
    app:cardElevation="10dp"
    app:cardBackgroundColor="#FFFFFF">

    <!--
        LinearLayout HORIZONTAL : image à gauche, textes à droite côte à côte.
    -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <!--
            IMAGE DU FILM (la pochette/affiche).
            - Dimensions fixes 120dp × 150dp (format portrait d'affiche).
            - L'image sera chargée par Glide depuis l'URL TMDB dans l'Adapter.
            - id="imageview" : utilisé dans le ViewHolder pour findViewById().
        -->
        <ImageView
            android:id="@+id/imageview"
            android:layout_width="120dp"
            android:layout_height="150dp"
            android:layout_margin="10dp"/>

        <!--
            LinearLayout VERTICAL : les textes sont empilés l'un sous l'autre.
            - layout_weight="1" : prend tout l'espace horizontal restant.
            - gravity="center" : centre les textes verticalement dans la carte.
        -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center">

            <!--
                NOM DU FILM.
                - textStyle="bold" : texte en gras.
                - textSize="18sp" : sp = scale-independent pixels, unité correcte pour les textes.
                  sp respecte les préférences d'accessibilité de l'utilisateur (taille de police).
                - id="textName" : utilisé dans le ViewHolder.
            -->
            <TextView
                android:id="@+id/textName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textStyle="bold"
                android:textSize="18sp"
                android:layout_margin="7dp"/>

            <!--
                DATE DE SORTIE DU FILM.
                - textStyle="italic" : texte en italique pour différencier visuellement.
                - id="textdate" : utilisé dans le ViewHolder.
            -->
            <TextView
                android:id="@+id/textdate"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textStyle="italic"
                android:textSize="14sp"
                android:layout_margin="5dp"/>

        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

### 4.3 `activity_movie_detail.xml` — Détails d'un film

```xml
<!--
    Même structure de carte que la liste, mais avec en plus :
    - Un TextView pour la description du film
    - Un bouton "Play Movie" pour lancer le trailer
    - Un fragment Google Maps en dessous
-->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="5dp"
        app:cardElevation="10dp"
        app:cardCornerRadius="10dp"
        app:cardBackgroundColor="#FFFFFF">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <!-- Image du film (pochette) -->
            <ImageView
                android:id="@+id/imageview"
                android:layout_width="120dp"
                android:layout_height="150dp"
                android:layout_margin="10dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center">

                <!-- Titre du film -->
                <TextView
                    android:id="@+id/textName"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textStyle="bold"
                    android:layout_margin="7dp"/>

                <!--
                    DESCRIPTION DU FILM (overview retourné par l'API TMDB).
                    textSize="12sp" car la description est longue,
                    on utilise une taille plus petite pour qu'elle rentre dans la carte.
                -->
                <TextView
                    android:id="@+id/Details"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textSize="12sp"
                    android:textColor="#000"
                    android:layout_margin="5dp"/>

            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <!--
        BOUTON PLAY MOVIE : lance le lecteur de trailer YouTube.
        layout_marginTop="16dp" : espace entre la carte et le bouton.
    -->
    <Button
        android:id="@+id/playButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Play Movie"
        android:layout_marginTop="16dp"/>

    <!--
        FRAGMENT GOOGLE MAPS : affiche une carte interactive.
        - android:name : indique quelle classe de fragment utiliser (SupportMapFragment).
        - layout_height="match_parent" : prend tout l'espace restant sous le bouton.
        - layout_marginTop="90dp" : décale la carte pour qu'elle ne se superpose pas au bouton.
        - id="map" : pour le retrouver dans MovieDetailActivity avec getSupportFragmentManager().
    -->
    <fragment
        android:id="@+id/map"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="90dp"/>

</LinearLayout>
```

---

### 4.4 `activity_video_player.xml` — Lecteur vidéo

```xml
<!--
    ConstraintLayout : conteneur racine flexible.
    Il contient un seul enfant : le WebView.
-->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!--
        WEBVIEW : un navigateur web intégré dans l'application.
        On l'utilise pour charger une URL YouTube "embed" (intégrée),
        ce qui permet de lire le trailer sans quitter l'app.
        - match_parent en largeur ET hauteur → plein écran.
        - id="webView" : pour le retrouver dans VideoPlayer.java.
    -->
    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## 5. MyMovieData.java — Le modèle

```java
package com.example.moviesapp;

/**
 * CLASSE MODÈLE = un "objet Film".
 * Elle représente UN film avec toutes ses informations.
 * C'est un POJO (Plain Old Java Object) : juste des données + getters.
 *
 * Utilisée partout dans l'app :
 * - MainActivity crée un tableau MyMovieData[] depuis le JSON de l'API
 * - MyMovieAdapter lit les données pour remplir les cartes
 * - MovieDetailActivity reçoit l'ID du film via Intent
 */
public class MyMovieData {

    // ─── ATTRIBUTS ───────────────────────────────────────────────────────────
    // "private" = encapsulation : on ne peut pas écrire "movie.movieName" depuis l'extérieur.
    // On doit obligatoirement passer par les getters : movie.getMovieName()
    // Cela protège les données et force un accès contrôlé.

    private int    movieId;    // Identifiant unique TMDB (ex: 550 pour Fight Club)
    private String movieName;  // Titre du film         (ex: "Inception")
    private String movieDate;  // Date de sortie        (ex: "2010-07-16")
    private String movieImage; // Chemin de l'affiche   (ex: "/abc123.jpg")
                               // ⚠️ C'est un chemin RELATIF retourné par TMDB.
                               // Pour l'URL complète : "https://image.tmdb.org/t/p/w500" + movieImage


    // ─── CONSTRUCTEUR ────────────────────────────────────────────────────────
    /**
     * Appelé à chaque fois qu'on crée un film : new MyMovieData(id, name, date, image)
     *
     * "this.movieId = movieId" :
     *   - "this.movieId"  → l'attribut de la classe (celui déclaré en haut)
     *   - "movieId"       → le paramètre reçu dans la parenthèse du constructeur
     *   Sans "this.", Java ne saurait pas lequel utiliser car ils ont le même nom.
     */
    public MyMovieData(int movieId, String movieName, String movieDate, String movieImage) {
        this.movieId    = movieId;
        this.movieName  = movieName;
        this.movieDate  = movieDate;
        this.movieImage = movieImage;
    }


    // ─── GETTERS ─────────────────────────────────────────────────────────────
    // Les getters permettent de LIRE les attributs privés depuis l'extérieur.
    // L'Adapter les utilise dans onBindViewHolder pour récupérer les données.

    public int    getMovieId()    { return movieId; }
    public String getMovieName()  { return movieName; }
    public String getMovieDate()  { return movieDate; }
    public String getMovieImage() { return movieImage; }
}
```

---

## 6. MainActivity.java — Écran principal + API TMDB

```java
package com.example.moviesapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // ─── CONSTANTES ──────────────────────────────────────────────────────────

    /**
     * Clé API TMDB : obtenir sur https://www.themoviedb.org/settings/api
     * ⚠️ REMPLACER par votre vraie clé avant de lancer l'app !
     * "private static final" = constante de classe, jamais modifiée.
     */
    private static final String TMDB_API_KEY = "your_api_key";

    /**
     * URL de base de l'API TMDB pour les films populaires.
     * On y ajoutera "?api_key=..." pour former l'URL complète.
     */
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/popular";

    /**
     * TAG = étiquette pour les logs dans la console Android (Logcat).
     * Quand on écrit Log.e(TAG, "message"), on peut filtrer par "MainActivity" dans Logcat.
     */
    private static final String TAG = "MainActivity";


    // ─── ATTRIBUTS DE LA CLASSE ──────────────────────────────────────────────

    private RecyclerView    recyclerView;    // La liste qui affiche les films
    private MyMovieAdapter  myMovieAdapter;  // L'adapter qui lie données ↔ RecyclerView
    private EditText        searchEditText;  // Le champ de recherche


    // ─── ONCREATE ────────────────────────────────────────────────────────────

    /**
     * onCreate() = première méthode appelée au démarrage de l'activité.
     * C'est ici qu'on initialise tout : vues, RecyclerView, requête API, recherche.
     *
     * savedInstanceState : données sauvegardées si l'app a été mise en pause
     * (rotation de l'écran par exemple). On ne l'utilise pas ici.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charge et affiche le layout XML de cet écran.
        // Sans cette ligne, l'écran serait vide.
        setContentView(R.layout.activity_main);


        // ── ÉTAPE 1 : Récupérer les vues depuis le XML ───────────────────────
        // findViewById() cherche dans le layout la vue avec cet ID
        // et retourne un objet Java qu'on peut utiliser dans le code.

        searchEditText = findViewById(R.id.editTextSearch);
        recyclerView   = findViewById(R.id.recyclerView);


        // ── ÉTAPE 2 : Configurer le RecyclerView ─────────────────────────────

        /**
         * setHasFixedSize(true) : optimisation.
         * On indique que la taille du RecyclerView ne changera pas
         * selon le contenu. Android peut ainsi mieux optimiser le rendu.
         */
        recyclerView.setHasFixedSize(true);

        /**
         * setLayoutManager() : indique comment les cartes sont arrangées.
         * LinearLayoutManager → liste verticale (une carte par ligne).
         * Alternative : GridLayoutManager(this, 2) → grille 2 colonnes.
         * Sans LayoutManager → plantage immédiat, le RecyclerView ne sait
         * pas comment disposer ses enfants.
         */
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // ── ÉTAPE 3 : Appeler l'API TMDB avec Volley ─────────────────────────
        fetchMovies();


        // ── ÉTAPE 4 : Connecter la recherche en temps réel ───────────────────
        setupSearch();
    }


    // ─── FETCHMOVIES : APPEL À L'API TMDB ────────────────────────────────────

    /**
     * POURQUOI VOLLEY ?
     * Android interdit les requêtes internet sur le thread principal
     * (ça gèlerait l'écran pendant l'attente). Volley fait la requête
     * dans un thread séparé en arrière-plan, puis revient sur le thread
     * principal une fois la réponse reçue.
     *
     *  Sans Volley : app bloquée → écran noir → crash ❌
     *  Avec Volley : app fluide, liste se remplit quand la réponse arrive ✅
     */
    private void fetchMovies() {

        /**
         * ÉTAPE A : Créer le gestionnaire de requêtes (la "file d'attente").
         * Toutes les requêtes Volley passent par cette RequestQueue.
         * "this" = le contexte de l'activité, nécessaire pour les ressources Android.
         */
        RequestQueue queue = Volley.newRequestQueue(this);

        /**
         * ÉTAPE B : Construire l'URL complète.
         *
         * BASE_URL  = "https://api.themoviedb.org/3/movie/popular"
         * "?"       = séparateur entre l'URL et les paramètres
         * "api_key" = nom du paramètre attendu par TMDB
         * TMDB_API_KEY = votre clé personnelle
         *
         * Résultat : "https://api.themoviedb.org/3/movie/popular?api_key=abc123xyz"
         *
         * 💡 Astuce : collez cette URL dans votre navigateur (avec votre vraie clé)
         * pour voir exactement le JSON que l'app reçoit.
         */
        String url = BASE_URL + "?api_key=" + TMDB_API_KEY;

        /**
         * ÉTAPE C : Créer la requête HTTP.
         *
         * JsonObjectRequest = requête qui attend un JSONObject en réponse
         * (et non un JSONArray ou du texte brut).
         *
         * Paramètres :
         *   1. Request.Method.GET → méthode HTTP "lire des données" (lecture seule)
         *   2. url                → l'adresse à appeler
         *   3. null               → corps de la requête (null pour un GET, on n'envoie rien)
         *   4. onResponse()       → ce qu'on fait si la requête RÉUSSIT
         *   5. onErrorResponse()  → ce qu'on fait si la requête ÉCHOUE
         */
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,

            // ── CALLBACK SUCCÈS ───────────────────────────────────────────────
            // Appelé automatiquement par Volley quand TMDB répond avec succès.
            // "response" contient tout le JSON retourné.
            // On est de retour sur le thread principal → on peut toucher l'UI.
            response -> {
                try {
                    parseMoviesAndDisplay(response);
                } catch (JSONException e) {
                    // JSONException : levée si une clé JSON est absente ou mal typée.
                    Log.e(TAG, "Erreur de lecture JSON : " + e.getMessage());
                }
            },

            // ── CALLBACK ERREUR ───────────────────────────────────────────────
            // Appelé si la requête échoue : pas de connexion, clé invalide,
            // serveur TMDB en panne, timeout réseau...
            error -> Log.e(TAG, "Erreur réseau Volley : " + error.getMessage())
        );

        /**
         * ÉTAPE D : Ajouter la requête à la file.
         * Volley l'enverra en arrière-plan dès que possible.
         * L'écran reste réactif pendant ce temps (pas de blocage).
         */
        queue.add(request);
    }


    // ─── PARSEMOVIESANDDISPLAY : LIRE LE JSON ET AFFICHER ────────────────────

    /**
     * Cette méthode prend le JSON retourné par TMDB et :
     *   1. Extrait le tableau de films ("results")
     *   2. Crée des objets MyMovieData pour chaque film
     *   3. Donne ce tableau à l'Adapter pour afficher la liste
     *
     * QU'EST-CE QUE LE JSON TMDB ?
     * L'API retourne quelque chose comme :
     * {
     *   "page": 1,
     *   "results": [
     *     {
     *       "id": 550,
     *       "title": "Fight Club",
     *       "release_date": "1999-10-15",
     *       "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
     *     },
     *     { ... deuxième film ... },
     *     ...
     *   ]
     * }
     */
    private void parseMoviesAndDisplay(JSONObject response) throws JSONException {

        /**
         * ÉTAPE 1 : Extraire le tableau "results".
         * getJSONArray("results") cherche la clé "results" dans le JSONObject principal
         * et retourne ce qu'il y a dedans : un tableau JSON [ { film1 }, { film2 }, ... ]
         *
         * ⚠️ Lance JSONException si la clé "results" n'existe pas dans le JSON.
         * C'est pour cela qu'on utilise un try/catch dans fetchMovies().
         */
        JSONArray results = response.getJSONArray("results");

        /**
         * ÉTAPE 2 : Créer un tableau Java de la même taille.
         * results.length() = nombre de films dans la réponse (souvent 20 par page).
         */
        MyMovieData[] movies = new MyMovieData[results.length()];

        /**
         * ÉTAPE 3 : Boucler sur chaque film du tableau JSON.
         * i commence à 0 (premier film) et va jusqu'à results.length()-1 (dernier film).
         */
        for (int i = 0; i < results.length(); i++) {

            /**
             * results.getJSONObject(i) : récupère l'objet JSON { ... } à la position i.
             * Cet objet représente UN film avec tous ses champs.
             */
            JSONObject movieJson = results.getJSONObject(i);

            // Lire chaque champ du film depuis le JSON
            int    id          = movieJson.getInt("id");           // Nombre entier → getInt()
            String title       = movieJson.getString("title");     // Texte → getString()
            String releaseDate = movieJson.getString("release_date"); // ex: "1999-10-15"
            String posterPath  = movieJson.getString("poster_path");
            // posterPath = chemin relatif ex: "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
            // ⚠️ Ce n'est PAS une URL complète. Pour l'image complète :
            // "https://image.tmdb.org/t/p/w500" + posterPath
            // → "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"

            /**
             * Créer un objet MyMovieData avec les données extraites du JSON.
             * movies[0] = premier film, movies[1] = deuxième, etc.
             */
            movies[i] = new MyMovieData(id, title, releaseDate, posterPath);
        }

        /**
         * ÉTAPE 4 : Créer l'Adapter et le brancher sur le RecyclerView.
         * On passe le tableau de films et le contexte (this) à l'Adapter.
         * setAdapter() "branche" l'Adapter : à partir de là, la liste s'affiche.
         */
        myMovieAdapter = new MyMovieAdapter(movies, MainActivity.this);
        recyclerView.setAdapter(myMovieAdapter);
    }


    // ─── SETUPSEARCH : RECHERCHE EN TEMPS RÉEL ───────────────────────────────

    /**
     * TextWatcher = un "observateur" qui surveille le champ de recherche.
     * À chaque frappe clavier (chaque lettre tapée ou effacée), il est notifié.
     * On l'utilise pour filtrer la liste en temps réel.
     *
     * COMMENT ÇA MARCHE :
     * Utilisateur tape "av"
     *   → TextWatcher.onTextChanged() appelé avec s = "av"
     *   → myMovieAdapter.getFilter().filter("av")
     *   → MyMovieAdapter.performFiltering("av") s'exécute en arrière-plan
     *   → Garde uniquement les films dont le nom contient "av"
     *   → MyMovieAdapter.publishResults() met à jour l'affichage
     */
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {

            /**
             * beforeTextChanged() : appelée AVANT que le texte change.
             * On n'en a pas besoin ici → corps vide.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Rien à faire
            }

            /**
             * onTextChanged() : appelée EN TEMPS RÉEL à chaque modification du texte.
             * C'est ici qu'on déclenche le filtrage.
             *
             * Paramètre "s" = le texte actuel dans le champ APRÈS la frappe.
             * Exemple : l'utilisateur a tapé "Av" → s = "Av"
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                /**
                 * On vérifie que l'adapter existe avant d'appeler getFilter().
                 * Si la requête API n'a pas encore répondu, myMovieAdapter est null.
                 * Appeler null.getFilter() causerait un NullPointerException → plantage.
                 */
                if (myMovieAdapter != null) {
                    myMovieAdapter.getFilter().filter(s);
                    // getFilter() → récupère l'objet Filter de l'Adapter
                    // .filter(s)  → lance le filtrage avec le texte "s"
                    // Cela déclenche performFiltering(s) dans MyMovieAdapter
                }
            }

            /**
             * afterTextChanged() : appelée APRÈS que le texte a changé.
             * On n'en a pas besoin ici → corps vide.
             */
            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire
            }
        });
    }
}
```

---

## 7. MyMovieAdapter.java — Adapter + Filtre de recherche

```java
package com.example.moviesapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ADAPTER = le pont entre les DONNÉES (MyMovieData[]) et l'AFFICHAGE (RecyclerView).
 *
 * Le RecyclerView ne sait pas ce qu'il affiche. Il pose 3 questions à l'Adapter :
 *   Q1 : Combien d'éléments ?           → getItemCount()
 *   Q2 : À quoi ressemble une carte ?   → onCreateViewHolder()
 *   Q3 : Que mettre dans la carte n°X ? → onBindViewHolder()
 *
 * "implements Filterable" : indique que cet Adapter sait se filtrer.
 * Cela rend obligatoire l'implémentation de getFilter().
 * Sans "implements Filterable", on ne pourrait pas appeler .getFilter().filter() depuis MainActivity.
 *
 * Le type entre < > → "extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>"
 * indique quel ViewHolder on utilise. Android sait ainsi quoi retourner dans onCreateViewHolder.
 */
public class MyMovieAdapter
        extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>
        implements Filterable {


    // ─── ATTRIBUTS ───────────────────────────────────────────────────────────

    /**
     * originalMovieData : la liste COMPLÈTE reçue de l'API.
     * Elle ne sera JAMAIS modifiée. C'est la référence permanente.
     * Quand l'utilisateur efface sa recherche, on repart de cette liste.
     */
    private MyMovieData[] originalMovieData;

    /**
     * filteredMovieData : la liste ACTUELLEMENT AFFICHÉE.
     * Peut être réduite si l'utilisateur cherche quelque chose.
     * C'est cette liste que le RecyclerView lit via getItemCount() et onBindViewHolder().
     *
     * Pourquoi List<> et pas [] ?
     * Parce qu'un tableau [] a une taille fixe. Une List<> est dynamique :
     * on peut y ajouter/supprimer des éléments → parfait pour le filtrage.
     */
    private List<MyMovieData> filteredMovieData;

    /**
     * context : le contexte Android de l'activité parente.
     * Nécessaire pour :
     *   - Charger les images avec Glide : Glide.with(context)
     *   - Lancer MovieDetailActivity : new Intent(context, ...)
     */
    private Context context;


    // ─── CONSTRUCTEUR ────────────────────────────────────────────────────────

    /**
     * Appelé depuis MainActivity : new MyMovieAdapter(movies, this)
     *
     * @param myMovieData  le tableau de films reçu de l'API
     * @param context      le contexte de MainActivity
     */
    public MyMovieAdapter(MyMovieData[] myMovieData, Context context) {

        // On garde le tableau original intact pour pouvoir réinitialiser le filtre
        this.originalMovieData = myMovieData;

        /**
         * On crée une COPIE du tableau sous forme de List modifiable.
         *
         * Arrays.asList(myMovieData) : convertit le tableau [] en List (non modifiable)
         * new ArrayList<>(...) : crée une copie modifiable de cette List
         *
         * ⚠️ Pourquoi une copie ?
         * On va modifier filteredMovieData (clear() + addAll()) pendant le filtrage.
         * Si on pointait directement sur originalMovieData, on l'écraserait.
         * Deux listes séparées = on peut toujours retrouver la liste complète.
         */
        this.filteredMovieData = new ArrayList<>(Arrays.asList(myMovieData));

        this.context = context;
    }


    // ─── ONCREATEVIEWHOLDER : FABRIQUER UNE NOUVELLE CARTE VIDE ─────────────

    /**
     * Appelée par Android quand il a besoin d'une NOUVELLE carte vide.
     *
     * LE CONCEPT DE RECYCLAGE :
     * Android crée seulement 8 à 10 cartes pour remplir l'écran visible.
     * Quand une carte sort en haut (défilement), elle est RÉUTILISÉE
     * pour afficher le prochain film en bas. C'est le "recycle" de RecyclerView.
     * → onCreateViewHolder n'est appelé qu'une dizaine de fois, pas une fois par film.
     *
     * @param parent   = le RecyclerView parent (contexte pour gonfler le layout)
     * @param viewType = si on avait plusieurs types de cartes (non utilisé ici)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /**
         * LayoutInflater = l'outil qui transforme un fichier XML en objet Java (View).
         * Sans lui, on ne peut pas utiliser movie_item_list.xml dans du code Java.
         * LayoutInflater.from(parent.getContext()) : récupère l'inflater du contexte parent.
         */
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        /**
         * inflate() : lit le XML et crée les vues Java correspondantes.
         *
         * Paramètres :
         *   1. R.layout.activity_movie_item_list → le fichier XML de la carte film
         *   2. parent → le RecyclerView (pour hériter des paramètres de layout)
         *   3. false  → NE PAS attacher la vue au parent maintenant
         *
         * POURQUOI false ET PAS true ?
         *   true  → Android créerait la vue ET l'ajouterait directement au RecyclerView
         *            PROBLÈME : le RecyclerView veut gérer lui-même l'ajout de ses enfants.
         *            Résultat : double ajout → crash "view already has a parent" ❌
         *   false → Android crée seulement la vue, sans l'ajouter nulle part.
         *            Le RecyclerView l'ajoutera lui-même au bon moment. ✅
         */
        View view = inflater.inflate(R.layout.activity_movie_item_list, parent, false);

        // Retourne un ViewHolder qui va mémoriser les références aux vues de cette carte
        return new ViewHolder(view);
    }


    // ─── ONBINDVIEWHOLDER : REMPLIR UNE CARTE AVEC LES DONNÉES D'UN FILM ────

    /**
     * Appelée à chaque fois qu'une carte doit afficher un film.
     * C'est ici que les DONNÉES rencontrent les VUES.
     *
     * @param holder   = la carte à remplir (contient textViewName, movieImage, etc.)
     * @param position = l'index du film à afficher (0 = premier, 1 = deuxième...)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        /**
         * Récupérer le film à la position demandée.
         * On utilise filteredMovieData (la liste affichée), PAS originalMovieData.
         * Si l'utilisateur cherche "Av" et qu'il reste 3 films,
         * filteredMovieData.get(0) est le premier film filtré.
         *
         * "final" : cette variable ne sera pas réassignée dans ce bloc.
         * Nécessaire pour l'utiliser dans le OnClickListener (classe anonyme interne).
         */
        final MyMovieData movie = filteredMovieData.get(position);

        // ── Remplir le nom du film ────────────────────────────────────────────
        // holder.textViewName = le TextView du nom dans la carte
        // movie.getMovieName() = "Inception", "Fight Club", etc.
        holder.textViewName.setText(movie.getMovieName());

        // ── Remplir la date de sortie ─────────────────────────────────────────
        holder.textViewDate.setText(movie.getMovieDate());

        // ── Charger l'image depuis l'URL TMDB avec Glide ─────────────────────
        /**
         * GLIDE = bibliothèque de chargement d'image asynchrone.
         *
         * Sans Glide, on devrait :
         *   1. Faire une requête HTTP pour télécharger l'image
         *   2. La décoder en Bitmap
         *   3. La mettre dans l'ImageView
         *   → tout ça en arrière-plan pour ne pas bloquer l'UI
         * Glide fait tout ça en une seule ligne.
         *
         * Construction de l'URL complète :
         *   "https://image.tmdb.org/t/p/w500"  → serveur d'images TMDB, taille 500px
         *   + movie.getMovieImage()             → "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
         *   = "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
         */
        Glide.with(context)
             .load("https://image.tmdb.org/t/p/w500" + movie.getMovieImage())
             .into(holder.movieImage);
        // .into(holder.movieImage) : place l'image dans cet ImageView quand chargée

        // ── Clic sur la carte → ouvrir les détails du film ───────────────────
        /**
         * holder.itemView = toute la carte (le CardView entier).
         * setOnClickListener : quand l'utilisateur appuie sur la carte.
         */
        holder.itemView.setOnClickListener(v -> {

            /**
             * Intent = ordre de navigation entre deux activités.
             * new Intent(context, MovieDetailActivity.class) :
             *   - context = d'où on part (MainActivity)
             *   - MovieDetailActivity.class = où on va
             */
            Intent intent = new Intent(context, MovieDetailActivity.class);

            /**
             * putExtra("clé", valeur) : envoie des données à l'activité suivante.
             * "movieId" est le nom (la clé) du paramètre.
             * movie.getMovieId() est la valeur (ex: 550).
             *
             * Dans MovieDetailActivity, on récupère avec :
             * getIntent().getIntExtra("movieId", -1)
             * (-1 est la valeur par défaut si "movieId" n'est pas trouvé)
             */
            intent.putExtra("movieId", movie.getMovieId());

            // Lance MovieDetailActivity. L'écran change.
            context.startActivity(intent);
        });
    }


    // ─── GETITEMCOUNT : COMBIEN DE FILMS À AFFICHER ? ────────────────────────

    /**
     * Le RecyclerView appelle cette méthode pour savoir combien de cartes créer.
     * On retourne la taille de la liste FILTRÉE (pas l'originale).
     * → Si l'utilisateur cherche "Av" et qu'il y a 3 résultats, retourne 3.
     * → Si la recherche est vide, retourne le nombre total de films.
     */
    @Override
    public int getItemCount() {
        return filteredMovieData.size();
    }


    // ─── GETFILTER : FOURNIR LE FILTRE DE RECHERCHE ──────────────────────────

    /**
     * Méthode imposée par l'interface Filterable.
     * Retourne le filtre "movieFilter" défini ci-dessous.
     * Appelée depuis MainActivity : myMovieAdapter.getFilter().filter(s)
     */
    @Override
    public Filter getFilter() {
        return movieFilter;
    }


    // ─── MOVIEFILTER : LA LOGIQUE DU FILTRE DE RECHERCHE ────────────────────

    /**
     * Filter est une classe abstraite Android qui :
     *   1. Exécute performFiltering() en ARRIÈRE-PLAN (thread séparé)
     *      → ne bloque pas l'écran pendant le filtrage
     *   2. Exécute publishResults() sur le THREAD PRINCIPAL
     *      → met à jour l'affichage (seul le thread principal peut toucher l'UI)
     */
    private final Filter movieFilter = new Filter() {

        /**
         * ÉTAPE 1 : performFiltering() — s'exécute EN ARRIÈRE-PLAN.
         *
         * @param constraint = le texte tapé par l'utilisateur (ex: CharSequence "av")
         * @return FilterResults contenant la liste filtrée
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            // Liste temporaire qui va recevoir les films qui correspondent à la recherche
            List<MyMovieData> filteredList = new ArrayList<>();

            /**
             * CAS 1 : Le champ de recherche est VIDE ou NULL.
             * constraint == null      → le TextWatcher a envoyé null
             * constraint.length() == 0 → l'utilisateur a tout effacé
             *
             * Action : remettre TOUS les films dans la liste affichée.
             */
            if (constraint == null || constraint.length() == 0) {

                /**
                 * Arrays.asList(originalMovieData) : convertit le tableau original en List.
                 * filteredList.addAll(...) : ajoute tous les films à filteredList.
                 * On repart de originalMovieData qui n'a jamais été modifié.
                 */
                filteredList.addAll(Arrays.asList(originalMovieData));

            } else {

                /**
                 * CAS 2 : L'utilisateur a tapé quelque chose.
                 * On prépare le texte de recherche :
                 *
                 * constraint.toString() : convertit CharSequence en String
                 * .toLowerCase()        : met en minuscules pour ignorer la casse
                 *                         "Avatar" = "avatar" = "AVATAR" → tous trouvés
                 * .trim()               : supprime les espaces inutiles en début/fin
                 *                         "  av  " devient "av"
                 */
                String pattern = constraint.toString().toLowerCase().trim();

                /**
                 * On parcourt TOUS les films de la liste ORIGINALE (jamais modifiée).
                 * Pour chaque film, on vérifie si son nom contient le texte recherché.
                 *
                 * ⚠️ On parcourt originalMovieData et PAS filteredMovieData.
                 * Si on filtrait filteredMovieData, une recherche précédente
                 * aurait déjà réduit la liste et on ne retrouverait plus tous les films.
                 */
                for (MyMovieData movie : originalMovieData) {

                    /**
                     * movie.getMovieName().toLowerCase() : nom du film en minuscules
                     * .contains(pattern) : est-ce que le nom contient le texte cherché ?
                     *
                     * Exemple :
                     *   pattern = "av"
                     *   "Avatar: The Way of Water".toLowerCase() = "avatar: the way of water"
                     *   .contains("av") → true ✅ → on ajoute ce film
                     *
                     *   "Inception".toLowerCase() = "inception"
                     *   .contains("av") → false ❌ → on n'ajoute pas ce film
                     */
                    if (movie.getMovieName().toLowerCase().contains(pattern)) {
                        filteredList.add(movie); // Film correspondant → on le garde
                    }
                }
            }

            /**
             * On empaquète le résultat dans un FilterResults.
             * C'est le format imposé par la classe Filter pour transmettre
             * le résultat à publishResults().
             */
            FilterResults results = new FilterResults();
            results.values = filteredList; // La liste filtrée
            return results;
        }


        /**
         * ÉTAPE 2 : publishResults() — s'exécute sur le THREAD PRINCIPAL.
         * Appelée automatiquement par Android après performFiltering().
         * C'est ici qu'on met à jour l'affichage.
         *
         * @param constraint = le texte de recherche (pas utilisé ici)
         * @param results    = le FilterResults retourné par performFiltering()
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            /**
             * filteredMovieData.clear() : vide COMPLÈTEMENT la liste affichée actuelle.
             * Nécessaire pour ne pas garder les anciens résultats.
             */
            filteredMovieData.clear();

            /**
             * filteredMovieData.addAll(...) : ajoute tous les films filtrés.
             * (List) results.values : cast — on dit à Java que results.values est une List.
             * (FilterResults.values est de type Object, il faut le caster en List)
             */
            filteredMovieData.addAll((List) results.values);

            /**
             * notifyDataSetChanged() : INDISPENSABLE.
             * Prévient le RecyclerView que les données ont changé.
             * Sans cette ligne, l'écran ne se mettrait PAS à jour même si
             * filteredMovieData contient de nouvelles données.
             * Le RecyclerView va alors rappeler onBindViewHolder() et getItemCount()
             * pour redessiner toutes les cartes visibles.
             */
            notifyDataSetChanged();
        }
    };


    // ─── VIEWHOLDER : LE CONTENEUR DES VUES D'UNE CARTE ─────────────────────

    /**
     * Le ViewHolder stocke les RÉFÉRENCES aux vues d'une carte film.
     * Ces références sont trouvées UNE SEULE FOIS dans le constructeur
     * (quand la carte est créée dans onCreateViewHolder).
     *
     * POURQUOI C'EST IMPORTANT ?
     * Sans ViewHolder, onBindViewHolder appellerait findViewById() à chaque défilement.
     * findViewById() est LENT : il parcourt tout l'arbre de vues de la carte.
     * Avec ViewHolder, on fait ce travail une seule fois par carte créée.
     * Résultat : défilement fluide même avec 10 000 films. ✅
     *
     * "static" : cette classe interne ne dépend pas d'une instance de MyMovieAdapter.
     * Économise de la mémoire → bonne pratique.
     *
     * "extends RecyclerView.ViewHolder" : on hérite du ViewHolder d'Android.
     * Le parent gère la position, l'id d'item, etc.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Références aux vues de la carte (déclarées comme attributs pour les réutiliser)
        ImageView movieImage;    // La pochette du film
        TextView  textViewName;  // Le titre du film
        TextView  textViewDate;  // La date de sortie

        /**
         * @param itemView = la vue de la carte entière (le CardView gonflé par inflate())
         * @NonNull = garantit que itemView n'est pas null
         */
        public ViewHolder(@NonNull View itemView) {

            /**
             * super(itemView) : OBLIGATOIRE.
             * Appelle le constructeur du parent RecyclerView.ViewHolder.
             * Le parent stocke "itemView" et gère position, stable IDs, etc.
             */
            super(itemView);

            /**
             * findViewByid() cherche dans itemView (la carte) les vues avec ces IDs.
             * Ces IDs sont définis dans activity_movie_item_list.xml.
             *
             * ✅ Ce travail est fait UNE SEULE FOIS par carte créée.
             * Dans onBindViewHolder, on utilise directement holder.textViewName
             * sans rechercher (déjà trouvé et stocké ici).
             */
            movieImage   = itemView.findViewById(R.id.imageview); // ImageView de la carte
            textViewName = itemView.findViewById(R.id.textName);  // TextView du nom
            textViewDate = itemView.findViewById(R.id.textdate);  // TextView de la date
        }
    }
}
```

---

## 8. MovieDetailActivity.java — Détails + Maps

```java
package com.example.moviesapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ACTIVITÉ DÉTAILS D'UN FILM.
 *
 * Elle fait 3 choses :
 *   1. Appelle l'API TMDB pour récupérer les détails du film (titre, description, image)
 *   2. Appelle l'API TMDB pour récupérer la clé YouTube du trailer
 *   3. Affiche une carte Google Maps avec un marqueur de cinéma
 *
 * "implements OnMapReadyCallback" : interface Google Maps.
 * Oblige à implémenter onMapReady() qui est appelé quand la carte est prête.
 */
public class MovieDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MovieDetailActivity";

    // Code de requête pour la permission GPS (nombre arbitraire, identifie la demande)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Vues de l'écran détail
    private ImageView img;
    private TextView  nameTextView;
    private TextView  descriptionTextView;
    private Button    playButton;

    // Volley : gestionnaire de requêtes réseau
    private RequestQueue requestQueue;

    // Clé YouTube du trailer, remplie après la requête /videos
    // Exemple : "dQw4w9WgXcQ" (la partie après youtube.com/watch?v=)
    private String trailerKey;

    // Objet Google Map, disponible seulement après onMapReady()
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // ── Récupérer les vues ────────────────────────────────────────────────
        img                 = findViewById(R.id.imageview);
        nameTextView        = findViewById(R.id.textName);
        descriptionTextView = findViewById(R.id.Details);
        playButton          = findViewById(R.id.playButton);

        // ── Initialiser Volley ────────────────────────────────────────────────
        requestQueue = Volley.newRequestQueue(this);

        // ── Récupérer l'ID du film envoyé par l'Adapter via Intent ───────────
        /**
         * getIntent() : récupère l'Intent qui a lancé cette activité.
         * getIntExtra("movieId", -1) :
         *   - "movieId" : la clé définie dans putExtra() dans l'Adapter
         *   - -1 : valeur par défaut si "movieId" n'existe pas dans l'Intent
         */
        int movieId = getIntent().getIntExtra("movieId", -1);

        if (movieId != -1) {
            // On a un ID valide → on lance les deux requêtes API
            fetchMovieDetails(movieId);
            fetchMovieTrailer(movieId);
        } else {
            // Cas d'erreur : pas d'ID transmis
            descriptionTextView.setText("Film introuvable.");
        }

        // ── Configurer le bouton Play ─────────────────────────────────────────
        playButton.setOnClickListener(v -> playTrailer());

        // ── Initialiser la carte Google Maps ─────────────────────────────────
        /**
         * getSupportFragmentManager() : gestionnaire de fragments.
         * findFragmentById(R.id.map) : récupère le fragment déclaré dans le XML.
         * (SupportMapFragment) : cast, on sait que c'est un SupportMapFragment.
         * getMapAsync(this) : demande à Google Maps de préparer la carte.
         *   Quand c'est prêt, onMapReady() sera appelé automatiquement.
         *   "this" = MovieDetailActivity implémente OnMapReadyCallback.
         */
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    // ─── FETCHMOVIEDETAILS : REQUÊTE 1 — DÉTAILS DU FILM ─────────────────────

    /**
     * Appelle : https://api.themoviedb.org/3/movie/{movieId}?api_key=...
     * Récupère : title, overview (description), poster_path (image)
     */
    private void fetchMovieDetails(int movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId
                   + "?api_key=your_api_key";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    // Lire les champs du JSON de réponse
                    String name        = response.getString("title");
                    String description = response.getString("overview");
                    // overview = description/synopsis du film en anglais
                    String imageUrl    = "https://image.tmdb.org/t/p/w500"
                                       + response.getString("poster_path");

                    // Mettre à jour l'interface
                    nameTextView.setText(name);
                    descriptionTextView.setText(description);
                    Glide.with(MovieDetailActivity.this).load(imageUrl).into(img);

                } catch (JSONException e) {
                    Log.e(TAG, "Erreur parsing détails : " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Erreur réseau détails : " + error.getMessage());
                descriptionTextView.setText("Impossible de charger les détails.");
            }
        );

        requestQueue.add(request);
    }


    // ─── FETCHMOVIETRAILER : REQUÊTE 2 — RÉCUPÉRER LE TRAILER ───────────────

    /**
     * Appelle : https://api.themoviedb.org/3/movie/{movieId}/videos?api_key=...
     * Récupère : un tableau de vidéos associées au film.
     * On cherche la première vidéo de type "Trailer" et on stocke sa "key" YouTube.
     */
    private void fetchMovieTrailer(int movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId
                   + "/videos?api_key=your_api_key";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    /**
                     * Le JSON retourné ressemble à :
                     * {
                     *   "results": [
                     *     { "type": "Trailer", "key": "dQw4w9WgXcQ", "site": "YouTube" },
                     *     { "type": "Teaser",  "key": "xyz123",       "site": "YouTube" }
                     *   ]
                     * }
                     */
                    JSONArray results = response.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject video = results.getJSONObject(i);

                        /**
                         * On cherche la première vidéo dont le type est "Trailer".
                         * Les autres types possibles : "Teaser", "Clip", "Featurette"...
                         */
                        if (video.getString("type").equals("Trailer")) {
                            trailerKey = video.getString("key");
                            // "key" = l'identifiant YouTube ex: "dQw4w9WgXcQ"
                            // URL YouTube complète : https://youtube.com/watch?v=dQw4w9WgXcQ
                            // URL embed (pour WebView) : https://youtube.com/embed/dQw4w9WgXcQ
                            break; // On s'arrête au premier trailer trouvé
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur parsing trailer : " + e.getMessage());
                }
            },
            error -> Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }


    // ─── PLAYTRAILER : LANCER LE LECTEUR DE TRAILER ──────────────────────────

    /**
     * Construit l'URL embed YouTube et lance VideoPlayer.
     * Appelée quand l'utilisateur clique sur le bouton "Play Movie".
     */
    private void playTrailer() {
        if (trailerKey != null && !trailerKey.isEmpty()) {
            /**
             * URL embed YouTube : format spécial pour intégrer une vidéo dans un WebView.
             * https://www.youtube.com/embed/ + clé
             * Exemple : https://www.youtube.com/embed/dQw4w9WgXcQ
             *
             * (Différent de l'URL normale : https://www.youtube.com/watch?v=dQw4w9WgXcQ)
             * L'URL embed affiche seulement le player, sans l'interface YouTube complète.
             */
            String trailerUrl = "https://www.youtube.com/embed/" + trailerKey;

            Intent intent = new Intent(MovieDetailActivity.this, VideoPlayer.class);
            intent.putExtra("videoUrl", trailerUrl);
            startActivity(intent);

        } else {
            // trailerKey est null → la requête /videos n'a pas encore répondu,
            // ou il n'y a pas de trailer disponible pour ce film.
            Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show();
        }
    }


    // ─── ONMAPREADY : CARTE GOOGLE MAPS PRÊTE ────────────────────────────────

    /**
     * Appelée automatiquement par Google Maps quand la carte est initialisée
     * et prête à être utilisée. On configure la carte ici.
     *
     * @param googleMap l'objet GoogleMap sur lequel on peut ajouter des marqueurs, etc.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /**
         * Vérifier si la permission de localisation est accordée.
         * ContextCompat.checkSelfPermission() = vérification sans déclencher de popup.
         * PackageManager.PERMISSION_GRANTED = la permission a été accordée.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Permission accordée : activer le bouton "Ma position" sur la carte
            mMap.setMyLocationEnabled(true);

            // Ajouter le marqueur du cinéma
            LatLng cinema = new LatLng(33.596460, -7.615480); // Casablanca, Maroc
            addCinemaMarker(cinema);

            // Centrer la carte sur la position de l'utilisateur
            moveToCurrentLocation();

        } else {
            /**
             * Permission non accordée → demander à l'utilisateur.
             * Un popup Android s'affichera avec "Autoriser / Refuser".
             * Le résultat arrive dans onRequestPermissionsResult().
             *
             * LOCATION_PERMISSION_REQUEST_CODE : code arbitraire pour identifier
             * cette demande dans onRequestPermissionsResult().
             */
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    // ─── ADDCINEMAMARKER : AJOUTER UN MARQUEUR SUR LA CARTE ─────────────────

    private void addCinemaMarker(LatLng cinemaLocation) {
        mMap.addMarker(new MarkerOptions()
            .position(cinemaLocation)           // Coordonnées GPS du marqueur
            .title("Cinéma")                    // Titre affiché quand on tape le marqueur
            .snippet("Votre cinéma le plus proche")); // Sous-titre du marqueur
    }


    // ─── MOVETOCURRENTLOCATION : CENTRER LA CARTE SUR L'UTILISATEUR ─────────

    private void moveToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null) {
            try {
                /**
                 * getLastKnownLocation() : retourne la DERNIÈRE position connue du GPS.
                 * C'est instantané (pas de nouveau calcul GPS).
                 * Peut être null si le GPS n'a jamais été utilisé sur cet appareil.
                 */
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    LatLng currentLocation = new LatLng(
                        location.getLatitude(),   // Coordonnée nord-sud
                        location.getLongitude()   // Coordonnée est-ouest
                    );

                    /**
                     * moveCamera() : déplace la caméra de la carte vers ces coordonnées.
                     * newLatLngZoom(position, zoom) :
                     *   - position = là où centrer la carte
                     *   - zoom 15 = niveau "quartier" (1=monde entier, 21=bâtiment)
                     */
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission GPS refusée : " + e.getMessage());
            }
        }
    }


    // ─── ONREQUESTPERMISSIONSRESULT : RÉSULTAT DE LA DEMANDE DE PERMISSION ──

    /**
     * Appelée automatiquement quand l'utilisateur a répondu au popup de permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Vérifie que c'est bien notre demande de localisation
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // L'utilisateur a accepté → on centre la carte sur sa position
                moveToCurrentLocation();
            } else {
                // L'utilisateur a refusé → on l'informe
                Toast.makeText(this, "Permission de localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
```

---

## 9. VideoPlayer.java — Lecteur de trailer

```java
package com.example.moviesapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ACTIVITÉ LECTEUR DE TRAILER.
 *
 * Utilise un WebView (navigateur intégré) pour charger et lire
 * un trailer YouTube via une URL embed.
 *
 * Pourquoi WebView et pas l'app YouTube ?
 * → WebView garde l'utilisateur dans l'app.
 * → L'URL embed affiche seulement le player, sans l'interface YouTube complète.
 */
public class VideoPlayer extends AppCompatActivity {

    private WebView webView;
    private String  videoUrl; // L'URL YouTube embed reçue de MovieDetailActivity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // ── Récupérer l'URL envoyée par MovieDetailActivity ───────────────────
        /**
         * getStringExtra("videoUrl") : récupère la String transmise via putExtra().
         * Exemple : "https://www.youtube.com/embed/dQw4w9WgXcQ"
         */
        videoUrl = getIntent().getStringExtra("videoUrl");

        // ── Configurer le WebView ─────────────────────────────────────────────
        webView = findViewById(R.id.webView);

        /**
         * setJavaScriptEnabled(true) : OBLIGATOIRE pour YouTube.
         * Le player YouTube utilise JavaScript pour fonctionner.
         * Sans cette ligne, la vidéo ne s'affiche pas (page blanche ou erreur).
         *
         * ⚠️ Note de sécurité : n'activer JavaScript que si c'est nécessaire.
         * Ici c'est indispensable pour YouTube.
         */
        webView.getSettings().setJavaScriptEnabled(true);

        /**
         * setWebViewClient(new WebViewClient()) : force les liens à s'ouvrir
         * dans ce WebView et non dans le navigateur externe (Chrome, etc.).
         * Sans ça, cliquer sur un lien dans la page ouvrirait Chrome.
         */
        webView.setWebViewClient(new WebViewClient());

        /**
         * loadUrl() : charge et affiche l'URL dans le WebView.
         * C'est équivalent à taper l'URL dans un navigateur.
         */
        webView.loadUrl(videoUrl);
    }


    /**
     * onConfigurationChanged() : appelée quand l'utilisateur tourne son téléphone
     * (passage de portrait à paysage ou inversement).
     *
     * On recharge la vidéo pour qu'elle s'adapte à la nouvelle orientation.
     * En paysage, la vidéo peut prendre plus de place → meilleure expérience.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (webView != null && videoUrl != null) {
            webView.loadUrl(videoUrl);
        }
    }


    /**
     * onBackPressed() : quand l'utilisateur appuie sur le bouton retour.
     * Si le WebView peut revenir en arrière dans son historique (navigation interne),
     * on revient en arrière dans le WebView.
     * Sinon, on ferme l'activité normalement.
     */
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // Retour dans l'historique du WebView
        } else {
            super.onBackPressed(); // Ferme VideoPlayer, retour à MovieDetailActivity
        }
    }
}
```

---

## 10. Flux de navigation complet

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUX COMPLET DE L'APPLICATION                    │
│                                                                     │
│  ① App démarre → MainActivity.onCreate()                            │
│                                                                     │
│  ② Volley → GET https://api.themoviedb.org/3/movie/popular          │
│     (en arrière-plan, l'écran reste réactif)                        │
│                                                                     │
│  ③ TMDB répond avec JSON { "results": [{film1}, {film2}, ...] }     │
│                                                                     │
│  ④ parseMoviesAndDisplay() :                                         │
│     - Lecture du JSON → création de MyMovieData[]                   │
│     - new MyMovieAdapter(movies) → recyclerView.setAdapter()        │
│                                                                     │
│  ⑤ RecyclerView demande les cartes à l'Adapter :                    │
│     - onCreateViewHolder() → gonfle activity_movie_item_list.xml    │
│     - onBindViewHolder()   → remplit nom + date + image (Glide)     │
│                                                                     │
│  ⑥ L'utilisateur tape "av" dans la barre de recherche :             │
│     - TextWatcher.onTextChanged("av")                               │
│     - myMovieAdapter.getFilter().filter("av")                       │
│     - performFiltering() (arrière-plan) : filtre les films          │
│     - publishResults() (thread principal) : met à jour l'affichage  │
│                                                                     │
│  ⑦ L'utilisateur clique sur une carte :                             │
│     - Intent("movieId" = 550) → startActivity(MovieDetailActivity)  │
│                                                                     │
│  ⑧ MovieDetailActivity :                                            │
│     - Requête 1 → GET /movie/550 → titre, description, image        │
│     - Requête 2 → GET /movie/550/videos → trailerKey YouTube        │
│     - Google Maps → onMapReady() → marqueur cinéma + position GPS   │
│                                                                     │
│  ⑨ L'utilisateur clique "Play Movie" :                              │
│     - trailerUrl = "https://youtube.com/embed/" + trailerKey        │
│     - Intent("videoUrl") → startActivity(VideoPlayer)               │
│                                                                     │
│  ⑩ VideoPlayer :                                                    │
│     - WebView avec JavaScript activé                                │
│     - webView.loadUrl(trailerUrl) → lecture du trailer YouTube      │
└─────────────────────────────────────────────────────────────────────┘
```

### Récapitulatif des endpoints API TMDB utilisés

| Endpoint | Ce qu'on récupère |
|----------|-------------------|
| `GET /3/movie/popular?api_key=...` | Liste des films populaires (20 par page) |
| `GET /3/movie/{id}?api_key=...` | Détails d'un film (titre, description, image) |
| `GET /3/movie/{id}/videos?api_key=...` | Vidéos du film (trailers, teasers...) |
| `https://image.tmdb.org/t/p/w500{poster_path}` | URL complète de l'affiche du film |

### Points critiques à retenir pour l'examen

| Point | Explication |
|-------|-------------|
| `false` dans `inflate()` | Ne pas attacher au parent → le RecyclerView gère l'ajout lui-même |
| `originalMovieData` vs `filteredMovieData` | Deux listes séparées pour que le filtre puisse se réinitialiser |
| `notifyDataSetChanged()` | Obligatoire après chaque modification des données sinon l'écran ne se met pas à jour |
| `layout_height="wrap_content"` | Sur le RecyclerView et les cartes pour éviter les bugs de scroll |
| `setJavaScriptEnabled(true)` | Obligatoire dans le WebView pour que YouTube fonctionne |
| `trailerKey` | C'est la clé YouTube (après `watch?v=`), l'URL embed = `youtube.com/embed/` + key |
| `poster_path` TMDB | Chemin RELATIF → toujours ajouter `https://image.tmdb.org/t/p/w500` devant |

---

*Pr. OUHMIDA Asmae — Projet Android MoviesApp*