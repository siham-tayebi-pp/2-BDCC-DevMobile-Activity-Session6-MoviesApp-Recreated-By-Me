# 📱 MoviesApp — Guide Révision Examen
> **Pr. OUHMIDA Asmae** — Toutes les étapes, ligne par ligne, avec tableaux récapitulatifs détaillés.

---

## 📋 Ordre de création

```
ÉTAPE 1  →  build.gradle
ÉTAPE 2  →  AndroidManifest.xml
ÉTAPE 3  →  MyMovieData.java
ÉTAPE 4  →  activity_movie_item_list.xml
ÉTAPE 5  →  activity_main.xml
ÉTAPE 6  →  activity_movie_detail.xml
ÉTAPE 7  →  activity_video_player.xml
ÉTAPE 8  →  MyMovieAdapter.java
ÉTAPE 9  →  MainActivity.java
ÉTAPE 10 →  MovieDetailActivity.java
ÉTAPE 11 →  VideoPlayer.java
```

---

## ÉTAPE 1 — build.gradle

```gradle
dependencies {
    // Maps : affiche une carte Google Maps interactive dans l'app
    implementation ("com.google.android.gms:play-services-maps:17.0.0")

    // Location : accède au GPS du téléphone (position de l'utilisateur)
    implementation ("com.google.android.gms:play-services-location:17.0.0")

    // Volley : envoie des requêtes HTTP (appels API TMDB) EN ARRIÈRE-PLAN
    // Android interdit les requêtes réseau sur le thread principal → crash sinon
    implementation ("com.android.volley:volley:1.2.0")

    // Glide : télécharge et affiche une image depuis une URL internet dans un ImageView
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    // compiler : processeur d'annotations OBLIGATOIRE pour que Glide fonctionne
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    // ExoPlayer : lecteur vidéo avancé (déclaré mais on utilise WebView pour YouTube)
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
}
```

### 📋 Récap build.gradle

| Dépendance | Rôle | Où utilisée | Sans elle |
|---|---|---|---|
| `play-services-maps` | Affiche Google Maps | `MovieDetailActivity` | Carte invisible / crash |
| `play-services-location` | Accède au GPS | `MovieDetailActivity` | Position introuvable |
| `volley` | Requêtes HTTP vers TMDB | `MainActivity`, `MovieDetailActivity` | Crash réseau |
| `glide` | Charge images depuis URL | `MyMovieAdapter`, `MovieDetailActivity` | Pas d'images |
| `glide:compiler` | Nécessaire à la compilation Glide | (build) | Erreur de compilation |
| `exoplayer-core` | Lecteur vidéo (déclaré pour le futur) | (non utilisé) | Rien |

---

## ÉTAPE 2 — AndroidManifest.xml

```xml
<!-- ══ PERMISSIONS ══ déclarées AVANT <application> ══════════════════════════ -->

<!-- INTERNET : autorise toute connexion réseau. ABSOLUMENT OBLIGATOIRE.
     Sans cette ligne → crash immédiat dès le premier appel API. -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- ACCESS_NETWORK_STATE : vérifie si le réseau est disponible avant d'envoyer une requête -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- ACCESS_FINE_LOCATION : GPS précis → position exacte de l'utilisateur sur la carte -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- ACCESS_COARSE_LOCATION : position approximative via WiFi/réseau mobile.
     OBLIGATOIRE en complément de FINE_LOCATION depuis Android 12. -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- ACCESS_BACKGROUND_LOCATION : accès à la position quand l'app est en arrière-plan -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- CAMERA : accès à la caméra (prévu pour des fonctionnalités futures) -->
<uses-permission android:name="android.permission.CAMERA" />

<application ...>

    <!-- CLÉ API GOOGLE MAPS : lue automatiquement par le SDK Maps au démarrage.
         ⚠️ Remplacer "your_API_key" par votre vraie clé (Google Cloud Console). -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="your_API_key" />

    <!-- MAINACTIVITY : point d'entrée de l'app.
         android:exported="true" = OBLIGATOIRE pour une activité avec LAUNCHER.
         intent-filter MAIN + LAUNCHER = cette activité démarre au lancement de l'app. -->
    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
    </activity>

    <!-- TOUTE activité utilisée DOIT être déclarée ici.
         Si absente → crash immédiat dès qu'on tente de l'ouvrir. -->
    <activity android:name=".MovieDetailActivity"/>
    <activity android:name=".VideoPlayer"/>

</application>
```

### 📋 Récap AndroidManifest.xml

| Élément | Rôle | Sans lui |
|---|---|---|
| `INTERNET` | Autorise toutes les connexions réseau | Crash à la 1ère requête API |
| `ACCESS_NETWORK_STATE` | Vérifie la connectivité avant une requête | Requête envoyée même sans réseau |
| `ACCESS_FINE_LOCATION` | GPS précis pour centrer la carte | Carte ne peut pas se centrer |
| `ACCESS_COARSE_LOCATION` | Position approx. (requis avec FINE sur Android 12+) | Erreur de permission |
| `ACCESS_BACKGROUND_LOCATION` | Position en arrière-plan | GPS coupé si app minimisée |
| `geo.API_KEY` | Clé d'accès Google Maps | Carte vide ou message d'erreur |
| `android:exported="true"` | MainActivity accessible depuis le système (lanceur) | L'app ne se lance pas |
| `intent-filter MAIN + LAUNCHER` | Définit l'activité de démarrage | L'icône n'apparaît pas dans le lanceur |
| `<activity .MovieDetailActivity/>` | Déclare MovieDetailActivity | Crash quand on clique une carte |
| `<activity .VideoPlayer/>` | Déclare VideoPlayer | Crash quand on clique "Play Movie" |

---

## ÉTAPE 3 — MyMovieData.java (Modèle / POJO)

```java
package net.ouhmida.testquizappall;

// CLASSE MODÈLE : représente UN film avec ses données.
// POJO = Plain Old Java Object : attributs privés + constructeur + getters.
// Utilisée partout : MainActivity, MyMovieAdapter, MovieDetailActivity.

public class MyMovieData {

    // ── ATTRIBUTS privés : encapsulation ──────────────────────────────────────
    private String movieName;        // Titre du film          ex: "Inception"
    private String movieDate;        // Date de sortie         ex: "2010-07-16"
    private String movieImage;       // Chemin RELATIF affiché ex: "/abc123.jpg"
                                     // ⚠️ URL complète = "https://image.tmdb.org/t/p/w500" + movieImage
    private String movieDescription; // Synopsis (non passé au constructeur, lu séparément)
    private int    movieId;          // ID unique TMDB          ex: 550 (Fight Club)


    // ── CONSTRUCTEUR ──────────────────────────────────────────────────────────
    // Appelé avec : new MyMovieData(id, "Inception", "2010-07-16", "/abc.jpg")
    // "this.x = x" : différencie l'ATTRIBUT de classe du PARAMÈTRE reçu (même nom)
    public MyMovieData(int movieId, String movieName, String movieDate, String movieImage) {
        this.movieName  = movieName;   // this.movieName = attribut / movieName = paramètre
        this.movieDate  = movieDate;   // idem
        this.movieImage = movieImage;  // idem
        this.movieId    = movieId;     // idem
    }


    // ── GETTERS : permettent de LIRE les attributs privés depuis l'extérieur ──
    // L'adapter appelle movieData.getMovieName() → retourne le titre.
    public int    getMovieId()          { return movieId; }
    public String getMovieName()        { return movieName; }
    public String getMovieDate()        { return movieDate; }
    public String getMovieImage()       { return movieImage; }
    public String getMovieDescription() { return movieDescription; }
}
```

### 📋 Récap MyMovieData.java

| Élément | Type | Rôle | Exemple de valeur |
|---|---|---|---|
| `private String movieName` | Attribut privé | Titre du film | `"Inception"` |
| `private String movieDate` | Attribut privé | Date de sortie | `"2010-07-16"` |
| `private String movieImage` | Attribut privé | Chemin RELATIF de l'affiche | `"/abc.jpg"` |
| `private String movieDescription` | Attribut privé | Synopsis | `"Un voleur qui..."` |
| `private int movieId` | Attribut privé | ID unique TMDB | `550` |
| `public MyMovieData(int, String, String, String)` | Constructeur | Crée un objet film | `new MyMovieData(550, "Fight Club", "1999", "/img.jpg")` |
| `this.x = x` | Affectation | Différencie attribut et paramètre | Évite la confusion sur les noms identiques |
| `getMovieName()` | Getter | Lit movieName depuis l'extérieur | Appelé dans l'adapter |
| `getMovieId()` | Getter | Lit movieId | Utilisé dans le `putExtra` pour naviguer |
| `private` sur attributs | Encapsulation | Interdit l'accès direct depuis l'extérieur | Oblige à passer par les getters |

---

## ÉTAPE 4 — activity_movie_item_list.xml (Carte d'un film)

> Layout d'**une seule carte** dans la liste. Réutilisé ~8-10 fois par le RecyclerView.

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- LinearLayout racine VERTICAL : enveloppe la CardView.
     layout_height="wrap_content" → CRITIQUE : la hauteur s'adapte au contenu.
     Si "match_parent" → chaque carte prendrait TOUT l'écran ! -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    android:layout_height="wrap_content">

    <!-- CARDVIEW : carte avec coins arrondis et ombre portée.
         cardElevation="10dp"    → ombre sous la carte (effet de profondeur)
         cardCornerRadius="10dp" → coins arrondis
         layout_margin="5dp"     → espace entre les cartes de la liste
         cardBackgroundColor     → fond blanc de la carte -->
    <androidx.cardview.widget.CardView
        app:cardElevation="10dp"
        app:cardCornerRadius="10dp"
        android:layout_margin="5dp"
        app:cardBackgroundColor="#FFFFFF"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <!-- LinearLayout HORIZONTAL : image à gauche, textes à droite côte à côte -->
        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <!-- IMAGE DU FILM : 120×150dp = format portrait d'une affiche cinéma.
                 id="imageview" → référencé dans ViewHolder : itemView.findViewById(R.id.imageview) -->
            <ImageView
                android:layout_margin="10dp"
                android:id="@+id/imageview"
                android:layout_width="120dp"
                android:layout_height="150dp"/>

            <!-- LinearLayout VERTICAL : empile le nom et la date.
                 layout_weight="1" → prend tout l'espace horizontal restant après l'image.
                 gravity="center"  → centre les textes verticalement dans la carte. -->
            <LinearLayout
                android:layout_weight="1"
                android:gravity="center"
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <!-- NOM DU FILM.
                     textStyle="bold" → texte en gras
                     textSize="25sp"  → sp = respecte les réglages d'accessibilité (pas dp !)
                     id="textName"    → référencé dans ViewHolder pour remplir le nom -->
                <TextView
                    android:id="@+id/textName"
                    android:textColor="#000"
                    android:textStyle="bold"
                    android:textSize="25sp"
                    android:layout_margin="10dp"
                    android:text="Movie Name"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

                <!-- DATE DE SORTIE DU FILM.
                     id="textdate" → référencé dans ViewHolder pour remplir la date -->
                <TextView
                    android:id="@+id/textdate"
                    android:layout_margin="10dp"
                    android:text="Movie Date"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>
</LinearLayout>
```

### 📋 Récap activity_movie_item_list.xml

| Vue | Attribut clé | Valeur | Pourquoi |
|---|---|---|---|
| `LinearLayout` racine | `layout_height` | `wrap_content` | ⚠️ CRITIQUE : sans ça chaque carte = tout l'écran |
| `CardView` | `cardElevation` | `10dp` | Crée une ombre sous la carte (effet 3D) |
| `CardView` | `cardCornerRadius` | `10dp` | Arrondit les coins de la carte |
| `CardView` | `layout_margin` | `5dp` | Espace visible entre les cartes |
| `CardView` | `cardBackgroundColor` | `#FFFFFF` | Fond blanc de la carte |
| `LinearLayout` enfant | `orientation` | `horizontal` | Image à gauche, textes à droite |
| `ImageView` | `id` | `imageview` | Référencé dans `ViewHolder.findViewByid` |
| `ImageView` | `layout_width/height` | `120dp × 150dp` | Format portrait affiche cinéma |
| `LinearLayout` textes | `layout_weight` | `1` | Prend tout l'espace restant après l'image |
| `LinearLayout` textes | `gravity` | `center` | Centre les textes verticalement |
| `TextView` nom | `id` | `textName` | Référencé dans `ViewHolder` |
| `TextView` nom | `textSize` | `25sp` | `sp` respecte l'accessibilité utilisateur |
| `TextView` nom | `textStyle` | `bold` | Titre en gras |
| `TextView` date | `id` | `textdate` | Référencé dans `ViewHolder` |

---

## ÉTAPE 5 — activity_main.xml (Écran principal)

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- LinearLayout VERTICAL : empile les vues de haut en bas.
     background="#BDBDBD" → fond gris clair de l'écran.
     descendantFocusability="beforeDescendants" → capture le focus AVANT les enfants :
     empêche l'EditText de s'ouvrir automatiquement avec le clavier au démarrage. -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:background="#BDBDBD"
    android:layout_height="match_parent"
    android:descendantFocusability="beforeDescendants">

    <!-- EDITTEXT DE RECHERCHE : champ de saisie pour filtrer les films.
         id="editTextSearch"  → récupéré dans MainActivity avec findViewByid
         hint="Search"        → texte grisé quand le champ est vide
         inputType="text"     → affiche le clavier texte standard -->
    <EditText
        android:id="@+id/editTextSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Search"
        android:inputType="text"/>

    <!-- BOUTON SEARCH (déclaré mais la recherche est en temps réel via TextWatcher) -->
    <Button
        android:id="@+id/buttonSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Search"/>

    <!-- RECYCLERVIEW : liste scrollable qui affiche les cartes de films.
         id="recyclerView"           → récupéré dans MainActivity
         layout_height="wrap_content" → IMPORTANT : évite les bugs de scroll
         focusableInTouchMode="true"  → la liste peut recevoir le focus au toucher -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_margin="2dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:focusableInTouchMode="true"/>

</LinearLayout>
```

### 📋 Récap activity_main.xml

| Vue | Attribut clé | Valeur | Pourquoi |
|---|---|---|---|
| `LinearLayout` | `orientation` | `vertical` | Empile EditText → Button → RecyclerView |
| `LinearLayout` | `background` | `#BDBDBD` | Fond gris clair de l'écran |
| `LinearLayout` | `descendantFocusability` | `beforeDescendants` | Empêche le clavier de s'ouvrir seul au démarrage |
| `EditText` | `id` | `editTextSearch` | Récupéré avec `findViewByid` dans MainActivity |
| `EditText` | `hint` | `"Search"` | Texte grisé indicatif quand vide |
| `EditText` | `inputType` | `text` | Clavier texte standard (pas numérique) |
| `Button` | `id` | `buttonSearch` | Déclaré mais non utilisé (TextWatcher le remplace) |
| `RecyclerView` | `id` | `recyclerView` | Récupéré avec `findViewByid` dans MainActivity |
| `RecyclerView` | `layout_height` | `wrap_content` | ⚠️ CRITIQUE : évite que la liste bloque le scroll |
| `RecyclerView` | `focusableInTouchMode` | `true` | Reçoit le focus quand l'utilisateur touche la liste |

---

## ÉTAPE 6 — activity_movie_detail.xml (Écran détails)

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- Même structure de carte que activity_movie_item_list.xml
     + description + bouton Play + fragment Google Maps -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    android:layout_height="wrap_content">

    <!-- CARDVIEW : même principe que la liste, contient image + titre + description -->
    <androidx.cardview.widget.CardView
        app:cardElevation="10dp"
        app:cardCornerRadius="10dp"
        android:layout_margin="5dp"
        app:cardBackgroundColor="#FFFFFF"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <!-- IMAGE DU FILM : id="imageview" → chargé par Glide dans MovieDetailActivity -->
            <ImageView
                android:layout_margin="10dp"
                android:id="@+id/imageview"
                android:layout_width="120dp"
                android:layout_height="150dp"/>

            <LinearLayout
                android:layout_weight="1"
                android:gravity="center"
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <!-- TITRE DU FILM : id="textName" → rempli par MovieDetailActivity -->
                <TextView
                    android:id="@+id/textName"
                    android:textStyle="bold"
                    android:layout_margin="7dp"
                    android:text="Movie Name"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

                <!-- DESCRIPTION DU FILM : id="Details" → rempli avec response.getString("overview")
                     textSize="12dp" → petite taille car le synopsis est souvent long -->
                <TextView
                    android:id="@+id/Details"
                    android:layout_margin="5dp"
                    android:textColor="#000"
                    android:textSize="12dp"
                    android:text="Movie Date"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <!-- BOUTON PLAY MOVIE : lance le lecteur de trailer YouTube.
         id="playButton"          → récupéré dans MovieDetailActivity
         layout_marginTop="16dp"  → espace entre la carte et le bouton -->
    <Button
        android:id="@+id/playButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Play Movie"
        android:layout_marginTop="16dp"/>

    <!-- FRAGMENT GOOGLE MAPS : carte interactive intégrée dans l'écran.
         android:name="SupportMapFragment" → classe de fragment utilisée par Google Maps
         id="map"                  → récupéré avec getSupportFragmentManager().findFragmentById
         layout_height="match_parent" → prend tout l'espace restant
         layout_marginTop="90dp"   → décale la carte vers le bas pour ne pas couvrir le bouton -->
    <fragment
        android:id="@+id/map"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="90dp" />

</LinearLayout>
```

### 📋 Récap activity_movie_detail.xml

| Vue | ID | Rempli par | Détail |
|---|---|---|---|
| `ImageView` | `@+id/imageview` | `Glide.with(...).load(url).into(img)` | Affiche l'affiche du film |
| `TextView` titre | `@+id/textName` | `Name.setText(movieName)` | Titre en gras |
| `TextView` description | `@+id/Details` | `descriptionTextView.setText(overview)` | Synopsis retourné par TMDB |
| `Button` | `@+id/playButton` | `setOnClickListener → playTrailer()` | Lance le lecteur YouTube |
| `fragment` Maps | `@+id/map` | `mapFragment.getMapAsync(this)` | Carte Google Maps interactive |
| `fragment android:name` | `SupportMapFragment` | (SDK Google Maps) | Indique la classe de fragment à utiliser |
| `Button layout_marginTop` | `16dp` | — | Espace entre la carte et le bouton |
| `fragment layout_marginTop` | `90dp` | — | Évite que la carte couvre le bouton |

---

## ÉTAPE 7 — activity_video_player.xml (Lecteur vidéo)

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- ConstraintLayout : conteneur racine flexible, occupe tout l'écran -->
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- WEBVIEW : navigateur web INTÉGRÉ dans l'application.
         Charge une URL YouTube en format "embed" pour lire un trailer
         SANS quitter l'app et SANS ouvrir l'application YouTube.
         match_parent partout → vidéo en plein écran.
         id="webView" → récupéré dans VideoPlayer.java avec findViewByid(R.id.webView) -->
    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 📋 Récap activity_video_player.xml

| Vue | Attribut | Valeur | Pourquoi |
|---|---|---|---|
| `ConstraintLayout` | `layout_width/height` | `match_parent` | Occupe toute la fenêtre |
| `WebView` | `id` | `webView` | Référencé dans `VideoPlayer.java` avec `findViewByid` |
| `WebView` | `layout_width` | `match_parent` | Vidéo pleine largeur |
| `WebView` | `layout_height` | `match_parent` | Vidéo pleine hauteur |
| Format URL | YouTube embed | `youtube.com/embed/KEY` | Format spécial pour intégration dans WebView |

---

## ÉTAPE 8 — MyMovieAdapter.java (Adapter + Filtre)

> L'adapter est le **pont** entre les données (`MyMovieData[]`) et le `RecyclerView`.

```java
package net.ouhmida.testquizappall;

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

// "extends RecyclerView.Adapter<ViewHolder>" : hérite de l'Adapter Android
//   Le <ViewHolder> entre < > = le type de ViewHolder qu'on utilise
// "implements Filterable" : déclare que cet Adapter sait se filtrer
//   Oblige à implémenter getFilter(). Sans ça, .getFilter().filter(s) = crash.

public class MyMovieAdapter extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>
        implements Filterable {

    // ── ATTRIBUTS ──────────────────────────────────────────────────────────────

    // originalMovieData : liste COMPLÈTE reçue de l'API, JAMAIS modifiée
    // Sert de référence permanente : quand on efface la recherche, on repart de là
    private MyMovieData[] originalMovieData;

    // filteredMovieData : liste ACTUELLEMENT AFFICHÉE dans le RecyclerView
    // List<> au lieu de [] car une List est dynamique (taille variable)
    // Peut être réduite si l'utilisateur cherche quelque chose
    private List<MyMovieData> filteredMovieData;

    // context : contexte Android (MainActivity)
    // Nécessaire pour Glide (charger images) et pour lancer des Intents (navigation)
    private Context context;


    // ── CONSTRUCTEUR ──────────────────────────────────────────────────────────
    // Appelé depuis MainActivity : new MyMovieAdapter(movies, MainActivity.this)
    public MyMovieAdapter(MyMovieData[] myMovieData, Context context) {
        this.originalMovieData = myMovieData;  // Stocke la liste originale INTACTE
        // Arrays.asList() → convertit le tableau [] en List (non modifiable)
        // new ArrayList<>(...) → crée une COPIE modifiable pour le filtrage
        // ⚠️ Copie obligatoire car on fera clear() + addAll() sur filteredMovieData
        this.filteredMovieData = new ArrayList<>(Arrays.asList(myMovieData));
        this.context = context;
    }


    // ── ONCREATEVIEWHOLDER : fabriquer une nouvelle carte VIDE ───────────────
    // Android appelle cette méthode ~8-10 fois seulement (recyclage des vues).
    // Quand une carte sort de l'écran, elle est réutilisée pour le film suivant.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // LayoutInflater : convertit un fichier XML en objet Java View
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        // inflate() : lit le XML et crée les vues Java correspondantes
        // Param 1 : R.layout.activity_movie_item_list → le fichier XML de la carte
        // Param 2 : parent → le RecyclerView parent
        // Param 3 : FALSE → ne pas attacher au parent maintenant
        //   Si TRUE → crash "view already has a parent" (le RecyclerView gère lui-même)
        View view = layoutInflater.inflate(R.layout.activity_movie_item_list, parent, false);

        return new ViewHolder(view); // Retourne un ViewHolder avec les références aux vues
    }


    // ── ONBINDVIEWHOLDER : remplir une carte avec les données d'un film ───────
    // Appelée à chaque fois qu'une carte doit afficher un film.
    // position = index du film dans filteredMovieData (0 = 1er film, 1 = 2ème...)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // "final" : nécessaire pour utiliser movieData dans le OnClickListener (classe anonyme)
        final MyMovieData movieData = filteredMovieData.get(position);

        holder.textViewName.setText(movieData.getMovieName()); // Remplit le nom
        holder.textViewDate.setText(movieData.getMovieDate()); // Remplit la date

        // Glide : charge l'image depuis l'URL TMDB en arrière-plan
        // URL complète = préfixe TMDB + chemin relatif retourné par l'API
        Glide.with(context)
             .load("https://image.tmdb.org/t/p/w500" + movieData.getMovieImage())
             .into(holder.movieImage); // Place l'image dans l'ImageView quand chargée

        // Clic sur la carte : ouvre MovieDetailActivity avec l'ID du film
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MovieDetailActivity.class);
                // putExtra("clé", valeur) : envoie l'ID du film à l'activité suivante
                // Récupéré dans MovieDetailActivity avec : getIntent().getIntExtra("movieId", -1)
                intent.putExtra("movieId", movieData.getMovieId());
                context.startActivity(intent); // Lance MovieDetailActivity
            }
        });
    }


    // ── GETITEMCOUNT : combien d'éléments afficher ? ──────────────────────────
    // RecyclerView appelle cette méthode pour savoir combien de cartes créer
    // Retourne la taille de la liste FILTRÉE (pas l'originale)
    @Override
    public int getItemCount() {
        return filteredMovieData.size();
    }


    // ── VIEWHOLDER : stocke les références aux vues d'une carte ──────────────
    // POURQUOI : sans ViewHolder, findViewByid() serait appelé à CHAQUE défilement → lent
    // Avec ViewHolder : findViewByid() est fait UNE SEULE FOIS par carte créée
    // "static" : ne dépend pas d'une instance de MyMovieAdapter → économise de la mémoire
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView movieImage;    // Référence à l'ImageView de la carte
        TextView  textViewName;  // Référence au TextView du nom
        TextView  textViewDate;  // Référence au TextView de la date

        // itemView = la carte entière (le CardView gonflé par inflate())
        public ViewHolder(@NonNull View itemView) {
            super(itemView); // OBLIGATOIRE : appelle le constructeur RecyclerView.ViewHolder
            // Cherche les vues UNE SEULE FOIS dans la carte et stocke les références
            // Les IDs doivent correspondre à ceux dans activity_movie_item_list.xml
            movieImage   = itemView.findViewById(R.id.imageview);
            textViewName = itemView.findViewById(R.id.textName);
            textViewDate = itemView.findViewById(R.id.textdate);
        }
    }


    // ── GETFILTER : fournit l'objet filtre ────────────────────────────────────
    // Méthode imposée par l'interface Filterable
    // Appelée depuis MainActivity : myMovieAdapter.getFilter().filter(s)
    @Override
    public Filter getFilter() {
        return movieFilter;
    }


    // ── MOVIEFILTER : la logique du filtre de recherche ───────────────────────
    // Filter = classe abstraite Android à 2 étapes :
    //   1. performFiltering() → s'exécute EN ARRIÈRE-PLAN (ne bloque pas l'UI)
    //   2. publishResults()   → s'exécute sur le THREAD PRINCIPAL (met à jour l'affichage)

    private Filter movieFilter = new Filter() {

        // ÉTAPE 1 : performFiltering — exécuté EN ARRIÈRE-PLAN
        // constraint = le texte tapé par l'utilisateur (ex: "av")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<MyMovieData> filteredList = new ArrayList<>(); // Liste temporaire des résultats

            // CAS 1 : champ vide ou null → remettre TOUS les films originaux
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(Arrays.asList(originalMovieData)); // Ajoute tous les films

            } else {
                // CAS 2 : l'utilisateur a tapé quelque chose
                // .toString()  → CharSequence vers String
                // .toLowerCase() → minuscules (insensible à la casse : "Avatar" = "avatar")
                // .trim()       → supprime les espaces inutiles en début/fin
                String filterPattern = constraint.toString().toLowerCase().trim();

                // ⚠️ On parcourt originalMovieData et PAS filteredMovieData !
                // Si on filtrait filteredMovieData, une 2e recherche s'appliquerait
                // sur des résultats déjà filtrés → certains films disparaissent définitivement
                for (MyMovieData movie : originalMovieData) {
                    // .contains(pattern) → vérifie si le titre contient le texte cherché
                    if (movie.getMovieName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(movie); // Film correspondant → ajouté aux résultats
                    }
                }
            }

            // Empaquette le résultat dans FilterResults (format imposé par Filter)
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        // ÉTAPE 2 : publishResults — exécuté sur le THREAD PRINCIPAL
        // Appelée automatiquement après performFiltering → met à jour l'affichage
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredMovieData.clear();              // Vide la liste actuellement affichée
            filteredMovieData.addAll((List) results.values); // Ajoute les films filtrés
            // OBLIGATOIRE : prévient le RecyclerView que les données ont changé
            // Sans cette ligne → l'écran ne se met PAS à jour même si les données ont changé
            notifyDataSetChanged();
        }
    };
}
```

### 📋 Récap MyMovieAdapter.java

| Méthode / Élément | Rôle | Point clé à retenir |
|---|---|---|
| `extends RecyclerView.Adapter<ViewHolder>` | Hérite de l'Adapter Android | `<ViewHolder>` = type de ViewHolder utilisé |
| `implements Filterable` | Déclare que l'Adapter peut se filtrer | Oblige à implémenter `getFilter()` |
| `originalMovieData[]` | Liste COMPLÈTE jamais modifiée | Référence permanente pour le filtre |
| `filteredMovieData` (List) | Liste actuellement affichée | Peut être réduite par la recherche |
| `new ArrayList<>(Arrays.asList(...))` | Copie modifiable du tableau | Permet `clear()` + `addAll()` |
| `onCreateViewHolder()` | Crée une carte VIDE | Appelée seulement ~8-10 fois grâce au recyclage |
| `inflate(layout, parent, false)` | Convertit XML en View Java | `false` = ne pas attacher au parent (sinon crash) |
| `onBindViewHolder()` | Remplit la carte avec les données du film | Appelée à chaque défilement |
| `Glide.with(ctx).load(url).into(view)` | Charge l'image TMDB dans l'ImageView | URL = `"https://image.tmdb.org/t/p/w500"` + chemin relatif |
| `holder.itemView.setOnClickListener` | Clic sur la carte | Lance `MovieDetailActivity` avec `putExtra("movieId")` |
| `getItemCount()` | Retourne le nombre d'éléments | Retourne `filteredMovieData.size()` (pas l'originale) |
| `ViewHolder` (inner class static) | Stocke références aux vues | `findViewByid` UNE SEULE FOIS → performance |
| `super(itemView)` dans ViewHolder | Appelle le constructeur parent | OBLIGATOIRE |
| `getFilter()` | Retourne l'objet filtre | Imposé par `Filterable` |
| `performFiltering()` | Filtre les films (arrière-plan) | ⚠️ Parcourt `originalMovieData`, JAMAIS `filteredMovieData` |
| `.toLowerCase().trim().contains(pattern)` | Recherche insensible à la casse | "Avatar" et "avatar" → même résultat |
| `publishResults()` | Met à jour l'affichage (thread principal) | `clear()` + `addAll()` + `notifyDataSetChanged()` |
| `notifyDataSetChanged()` | Rafraîchit le RecyclerView | ⚠️ OBLIGATOIRE sinon l'écran ne change pas |

---

## ÉTAPE 9 — MainActivity.java (API TMDB + Recherche)

```java
package net.ouhmida.testquizappall;

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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // ── CONSTANTES ────────────────────────────────────────────────────────────
    // "private static final" = constante de classe, jamais modifiée
    private static final String TMDB_API_KEY = "your api key"; // ⚠️ À remplacer
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/popular"; // URL de base films populaires
    private static final String TAG = "MainActivity"; // Étiquette pour filtrer les logs Logcat

    // ── ATTRIBUTS ─────────────────────────────────────────────────────────────
    private RecyclerView   recyclerView;   // Liste qui affiche les films
    private MyMovieAdapter myMovieAdapter; // L'adapter : données ↔ RecyclerView
    private EditText       searchEditText; // Champ de recherche


    // ── ONCREATE : méthode appelée AU DÉMARRAGE de l'activité ─────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Appel obligatoire au parent
        setContentView(R.layout.activity_main); // Charge et affiche le layout XML

        // ÉTAPE 1 : récupérer les vues depuis le XML
        searchEditText = findViewById(R.id.editTextSearch); // Récupère l'EditText
        recyclerView   = findViewById(R.id.recyclerView);   // Récupère le RecyclerView

        // ÉTAPE 2 : configurer le RecyclerView
        recyclerView.setHasFixedSize(true); // Optimisation : taille de la liste fixe
        // LinearLayoutManager : affiche les cartes en liste verticale (une par ligne)
        // Sans LayoutManager → crash immédiat au démarrage
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ÉTAPE 3 : appel à l'API TMDB avec Volley
        // ─────────────────────────────────────────────────────────────────────
        // POURQUOI VOLLEY : Android interdit les requêtes réseau sur le thread principal
        // Volley envoie la requête en ARRIÈRE-PLAN → l'UI reste fluide
        // Sans Volley : écran noir → crash "NetworkOnMainThreadException"

        RequestQueue queue = Volley.newRequestQueue(this); // Crée le gestionnaire de requêtes

        // Construit l'URL complète : BASE_URL + ?api_key= + clé
        // Résultat : "https://api.themoviedb.org/3/movie/popular?api_key=abc123"
        String url = BASE_URL + "?api_key=" + TMDB_API_KEY;

        // JsonObjectRequest : attend un JSONObject en réponse
        // Request.Method.GET → lecture seule, pas de body
        // null → corps de la requête (null pour un GET)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,

            // CALLBACK SUCCÈS : appelé quand TMDB répond avec succès
            // "response" contient tout le JSON retourné
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // JSON TMDB ressemble à :
                        // { "results": [ {"id":550,"title":"Fight Club","release_date":"1999-10-15","poster_path":"/img.jpg"}, {...} ] }

                        JSONArray results = response.getJSONArray("results"); // Extrait le tableau "results"
                        MyMovieData[] movies = new MyMovieData[results.length()]; // Tableau Java de la même taille

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject movieObject = results.getJSONObject(i); // Objet JSON d'UN film

                            int    id          = movieObject.getInt("id");             // ID entier du film
                            String title       = movieObject.getString("title");       // Titre du film
                            String releaseDate = movieObject.getString("release_date"); // Date de sortie
                            String imageUrl    = movieObject.getString("poster_path"); // Chemin RELATIF de l'affiche

                            movies[i] = new MyMovieData(id, title, releaseDate, imageUrl); // Crée l'objet Film
                        }

                        myMovieAdapter = new MyMovieAdapter(movies, MainActivity.this); // Crée l'Adapter avec les données
                        recyclerView.setAdapter(myMovieAdapter); // Branche l'Adapter → la liste s'affiche

                    } catch (JSONException e) {
                        e.printStackTrace(); // Affiche l'erreur si une clé JSON est absente
                    }
                }
            },

            // CALLBACK ERREUR : appelé si la requête échoue (pas internet, clé invalide, timeout...)
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error occurred: " + error.getMessage()); // Log l'erreur dans Logcat
                }
            }
        );

        queue.add(jsonObjectRequest); // Ajoute la requête → Volley l'envoie en arrière-plan


        // ÉTAPE 4 : recherche en temps réel via TextWatcher
        // ─────────────────────────────────────────────────────────────────────
        // TextWatcher = observateur sur l'EditText, notifié à CHAQUE frappe clavier
        // L'utilisateur tape "av" → onTextChanged("av") → getFilter().filter("av")

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Appelée AVANT le changement → rien à faire ici
            }

            // Appelée EN TEMPS RÉEL à chaque lettre tapée ou effacée
            // "s" = le texte actuel dans le champ
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Vérification OBLIGATOIRE : myMovieAdapter peut être null
                // si la requête API n'a pas encore répondu → NullPointerException sinon
                if (myMovieAdapter != null) {
                    myMovieAdapter.getFilter().filter(s); // Déclenche performFiltering(s)
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Appelée APRÈS le changement → rien à faire ici
            }
        });
    }
}
```

### 📋 Récap MainActivity.java

| Méthode / Élément | Rôle | Point clé |
|---|---|---|
| `setContentView(R.layout.activity_main)` | Charge le layout XML de cet écran | Sans ça → écran vide |
| `findViewByid(R.id.recyclerView)` | Récupère le RecyclerView Java | Retourne un objet Java utilisable |
| `setHasFixedSize(true)` | Optimisation RecyclerView | Taille de la liste ne change pas = plus rapide |
| `new LinearLayoutManager(this)` | Disposition en liste verticale | ⚠️ OBLIGATOIRE : sans ça → crash |
| `Volley.newRequestQueue(this)` | Crée le gestionnaire de requêtes | Toutes les requêtes passent par lui |
| `BASE_URL + "?api_key=" + TMDB_API_KEY` | Construit l'URL complète | Testable directement dans un navigateur |
| `new JsonObjectRequest(GET, url, null, ...)` | Crée la requête HTTP | `GET` = lecture, `null` = pas de body |
| `onResponse(JSONObject response)` | Reçoit le JSON TMDB si succès | S'exécute sur le thread principal |
| `response.getJSONArray("results")` | Extrait le tableau de films | Lance `JSONException` si clé absente |
| `movieObject.getInt("id")` | Lit un entier du JSON | Pour l'ID du film |
| `movieObject.getString("title")` | Lit un texte du JSON | Pour le titre, la date, le chemin image |
| `movies[i] = new MyMovieData(...)` | Crée un objet Film par item JSON | Remplit le tableau |
| `new MyMovieAdapter(movies, this)` | Crée l'Adapter | Passe les données + le contexte |
| `recyclerView.setAdapter(adapter)` | Branche l'Adapter → films affichés | C'est ici que la liste apparaît à l'écran |
| `onErrorResponse(VolleyError error)` | Appelé si la requête échoue | Logcat → filtre avec TAG |
| `queue.add(request)` | Envoie la requête en arrière-plan | L'écran reste réactif |
| `addTextChangedListener(TextWatcher)` | Écoute chaque frappe clavier | En temps réel (pas besoin de bouton) |
| `onTextChanged(CharSequence s, ...)` | Appelée à chaque lettre tapée/effacée | `s` = texte actuel |
| `myMovieAdapter.getFilter().filter(s)` | Déclenche le filtrage | Appelle `performFiltering(s)` dans l'Adapter |
| `if (myMovieAdapter != null)` | Protection contre le crash | Évite NullPointerException si API pas encore répondue |

---

## ÉTAPE 10 — MovieDetailActivity.java (Détails + Maps + Trailer)

```java
package net.ouhmida.testquizappall;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import java.util.ArrayList;
import java.util.List;

// "implements OnMapReadyCallback" : interface Google Maps
// Oblige à implémenter onMapReady() appelée quand la carte est prête

public class MovieDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ── ATTRIBUTS ─────────────────────────────────────────────────────────────
    private SupportMapFragment mapFragment;   // Fragment de la carte Maps
    private TextView descriptionTextView;     // TextView description (id="Details")
    private TextView Name;                    // TextView titre (id="textName")
    private ImageView img;                    // ImageView affiche (id="imageview")
    private String trailerKey;               // Clé YouTube du trailer ex: "dQw4w9WgXcQ"
    private RequestQueue requestQueue;       // Gestionnaire Volley
    private Button playButton;               // Bouton "Play Movie" (id="playButton")
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001; // Code arbitraire pour la demande de permission
    private GoogleMap mMap;                  // Objet GoogleMap (disponible après onMapReady)
    private List<LatLng> cinemaLocations = new ArrayList<>(); // Coordonnées GPS des cinémas


    // ── ONCREATE ──────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail); // Charge le layout de l'écran détails

        // Récupère les vues depuis le XML
        descriptionTextView = findViewById(R.id.Details);
        img                 = findViewById(R.id.imageview);
        Name                = findViewById(R.id.textName);

        requestQueue = Volley.newRequestQueue(this); // Crée le gestionnaire Volley

        // Récupère l'ID du film envoyé par l'Adapter via Intent
        // getIntent() → récupère l'Intent qui a lancé cette activité
        // getIntExtra("movieId", -1) → lit la valeur envoyée avec putExtra("movieId", id)
        //   -1 = valeur par défaut si "movieId" n'est pas trouvé dans l'Intent
        int movieId = getIntent().getIntExtra("movieId", -1);
        if (movieId != -1) {
            fetchMovieDetails(movieId); // Lance les 2 requêtes API (détails + trailer)
        } else {
            descriptionTextView.setText("No movie ID provided"); // Cas d'erreur
        }

        // Configure le bouton Play
        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrailer(); // Lance le lecteur de trailer
            }
        });

        cinemaLocations.add(new LatLng(33.596460, -7.615480)); // Ajoute les coordonnées du cinéma (Casablanca)

        // Initialise la carte Google Maps
        // findFragmentById(R.id.map) → récupère le fragment déclaré dans le XML
        // getMapAsync(this) → "this" implémente OnMapReadyCallback → onMapReady() appelée quand prête
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    // ── FETCHMOVIEDETAILS : 2 requêtes en parallèle ───────────────────────────
    private void fetchMovieDetails(int movieId) {
        String TMDB_API_KEY = "your api key"; // ⚠️ Remplacer par votre vraie clé

        // URL 1 : détails du film → https://api.themoviedb.org/3/movie/550?api_key=...
        String movieDetailsUrl = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + TMDB_API_KEY;

        // URL 2 : vidéos (trailers) → https://api.themoviedb.org/3/movie/550/videos?api_key=...
        String movieVideosUrl  = "https://api.themoviedb.org/3/movie/" + movieId + "/videos?api_key=" + TMDB_API_KEY;


        // ─── REQUÊTE 1 : DÉTAILS ──────────────────────────────────────────────
        JsonObjectRequest movieDetailsRequest = new JsonObjectRequest(
            Request.Method.GET, movieDetailsUrl, null,

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String movieName        = response.getString("title");     // Titre du film
                        String movieDescription = response.getString("overview");  // Synopsis du film
                        // poster_path = chemin RELATIF. On ajoute le préfixe pour l'URL complète
                        String imageUrl = "https://image.tmdb.org/t/p/w500" + response.getString("poster_path");

                        Name.setText(movieName);                          // Affiche le titre
                        descriptionTextView.setText(movieDescription);    // Affiche le synopsis
                        Glide.with(MovieDetailActivity.this).load(imageUrl).into(img); // Charge l'image

                    } catch (JSONException e) {
                        // Gestion fine : on identifie quelle clé JSON est manquante
                        if (e.getMessage().contains("title")) {
                            Log.e(TAG, "Error: Missing 'title' key in response");
                        } else if (e.getMessage().contains("overview")) {
                            Log.e(TAG, "Error: Missing 'overview' key in response");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error fetching movie details: " + error.getMessage());
                    descriptionTextView.setText("Failed to fetch movie details"); // Message d'erreur visible
                }
            }
        );


        // ─── REQUÊTE 2 : VIDÉOS (TRAILER) ────────────────────────────────────
        // JSON retourné : { "results": [ {"type":"Trailer","key":"dQw4w9WgXcQ",...}, ... ] }
        JsonObjectRequest movieVideosRequest = new JsonObjectRequest(
            Request.Method.GET, movieVideosUrl, null,

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.has("results")) { // Vérifie que la clé "results" existe
                            JSONArray results = response.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject video = results.getJSONObject(i); // Une vidéo

                                // On cherche la première vidéo de type "Trailer"
                                // (autres types : "Teaser", "Clip", "Featurette")
                                if (video.getString("type").equals("Trailer")) {
                                    trailerKey = video.getString("key"); // ex: "dQw4w9WgXcQ"
                                    Log.d(TAG, "Trailer Key: " + trailerKey);
                                    break; // On s'arrête au premier trailer trouvé
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error fetching movie videos: " + error.getMessage());
                    Toast.makeText(MovieDetailActivity.this, "Trailer not available", Toast.LENGTH_SHORT).show();
                }
            }
        );

        requestQueue.add(movieDetailsRequest); // Envoie la requête détails
        requestQueue.add(movieVideosRequest);  // Envoie la requête vidéos (en parallèle)
    }


    // ── PLAYTRAILER : lancer le lecteur de trailer ────────────────────────────
    private void playTrailer() {
        if (trailerKey != null && !trailerKey.isEmpty()) {
            // URL embed = format pour WebView (sans toute l'interface YouTube)
            // Différent de l'URL normale : youtube.com/watch?v=KEY
            String trailerUrl = "https://www.youtube.com/embed/" + trailerKey;

            Intent intent = new Intent(MovieDetailActivity.this, VideoPlayer.class);
            intent.putExtra("videoUrl", trailerUrl); // Envoie l'URL à VideoPlayer
            startActivity(intent); // Lance VideoPlayer

        } else {
            // trailerKey null → requête /videos pas encore répondue ou pas de trailer disponible
            Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
        }
    }


    // ── ONMAPREADY : la carte Google Maps est prête ───────────────────────────
    // Appelée automatiquement par le SDK Google Maps quand la carte est initialisée
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Stocke la référence de la carte

        // Vérifie si la permission GPS est accordée (sans afficher de popup)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true); // Active le bouton "Ma position" sur la carte

            LatLng cinemaLocation = new LatLng(33.596460, -7.615480); // Coordonnées du cinéma
            addCinemaMarker(cinemaLocation); // Ajoute le marqueur sur la carte

            moveToCurrentLocation(); // Centre la carte sur la position GPS de l'utilisateur

        } else {
            // Permission non accordée → affiche un popup de demande à l'utilisateur
            // Résultat dans onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE); // Code pour identifier cette demande
        }
    }


    // ── ADDCINEMAMARKER : ajouter un marqueur sur la carte ────────────────────
    private void addCinemaMarker(LatLng cinemaLocation) {
        mMap.addMarker(new MarkerOptions()
            .position(cinemaLocation)           // Coordonnées GPS du marqueur
            .title("Cinema")                    // Titre affiché quand on tape le marqueur
            .snippet("Location of the cinema")); // Sous-titre du marqueur
    }


    // ── MOVETOCURRENTLOCATION : centrer la carte sur l'utilisateur ────────────
    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = null;

            try {
                // getLastKnownLocation() : retourne la DERNIÈRE position GPS connue
                // Instantané (pas de nouveau calcul GPS) → peut être null si GPS jamais utilisé
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                return;
            }

            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                // moveCamera : déplace la caméra de la carte
                // newLatLngZoom(position, zoom) : position + niveau de zoom
                //   zoom 15 = niveau quartier (1=monde entier, 21=bâtiment individuel)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            } else {
                Toast.makeText(this, "Last known location not available", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // ── ONREQUESTPERMISSIONSRESULT : résultat du popup de permission ──────────
    // Appelée quand l'utilisateur répond "Autoriser" ou "Refuser" dans le popup
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) { // C'est bien notre demande de localisation
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToCurrentLocation(); // Permission accordée → centre la carte
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
```

### 📋 Récap MovieDetailActivity.java

| Méthode / Élément | Rôle | Point clé |
|---|---|---|
| `implements OnMapReadyCallback` | Déclare qu'on gère la carte Maps | Oblige à implémenter `onMapReady()` |
| `getIntent().getIntExtra("movieId", -1)` | Récupère l'ID envoyé par l'Adapter | `-1` = valeur par défaut si absent |
| `fetchMovieDetails(movieId)` | Lance les 2 requêtes en parallèle | Détails + vidéos simultanément |
| `response.getString("title")` | Titre du film depuis JSON | Clé TMDB : `"title"` |
| `response.getString("overview")` | Synopsis du film | Clé TMDB : `"overview"` |
| `"https://image.tmdb.org/t/p/w500" + poster_path` | URL complète de l'affiche | `poster_path` est RELATIF → ajouter le préfixe |
| `Glide.with(this).load(url).into(img)` | Charge l'image dans ImageView | En arrière-plan, UI non bloquée |
| `video.getString("type").equals("Trailer")` | Cherche le premier trailer | Autres types : `Teaser`, `Clip`... |
| `trailerKey = video.getString("key")` | Stocke la clé YouTube | ex: `"dQw4w9WgXcQ"` |
| `"https://www.youtube.com/embed/" + trailerKey` | URL embed YouTube pour WebView | Différent de `watch?v=KEY` |
| `intent.putExtra("videoUrl", trailerUrl)` | Envoie l'URL à VideoPlayer | Récupéré avec `getStringExtra("videoUrl")` |
| `mapFragment.getMapAsync(this)` | Demande la préparation de la carte | `onMapReady()` appelée quand prête |
| `onMapReady(GoogleMap googleMap)` | Carte prête → configuration | Vérifie permission → ajoute marqueur → centre |
| `mMap.setMyLocationEnabled(true)` | Bouton "Ma position" sur la carte | Nécessite permission GPS accordée |
| `mMap.addMarker(new MarkerOptions().position(...).title(...))` | Ajoute un marqueur sur la carte | `.title()` = texte au tap du marqueur |
| `CameraUpdateFactory.newLatLngZoom(pos, 15)` | Centre la carte avec zoom | 15 = niveau quartier |
| `ContextCompat.checkSelfPermission(...)` | Vérifie si permission accordée | Sans afficher de popup |
| `ActivityCompat.requestPermissions(...)` | Affiche le popup de permission GPS | Résultat dans `onRequestPermissionsResult` |
| `LOCATION_PERMISSION_REQUEST_CODE = 1001` | Code pour identifier la demande | Nombre arbitraire mais unique |
| `getLastKnownLocation(GPS_PROVIDER)` | Dernière position GPS connue | Peut être `null` si GPS jamais utilisé |

---

## ÉTAPE 11 — VideoPlayer.java (Lecteur YouTube via WebView)

```java
package net.ouhmida.testquizappall;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

// Activité : affiche un trailer YouTube dans un WebView (navigateur intégré).
// On charge l'URL YouTube embed pour lire la vidéo SANS quitter l'app.

public class VideoPlayer extends AppCompatActivity {

    private WebView webView;   // Navigateur web intégré
    private String  videoUrl;  // URL YouTube embed reçue de MovieDetailActivity
                               // ex: "https://www.youtube.com/embed/dQw4w9WgXcQ"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Affichage bord à bord (vidéo sous la barre de statut)
        setContentView(R.layout.activity_video_player); // Charge le layout

        // Récupère l'URL envoyée par MovieDetailActivity avec putExtra("videoUrl", ...)
        videoUrl = getIntent().getStringExtra("videoUrl");

        webView = findViewById(R.id.webView); // Récupère le WebView depuis le layout XML

        // OBLIGATOIRE : active JavaScript dans le WebView
        // Le player YouTube utilise JavaScript → sans cette ligne = page blanche
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(videoUrl); // Charge et affiche l'URL YouTube dans le WebView
    }


    // Appelée quand l'utilisateur tourne son téléphone (portrait ↔ paysage)
    // On recharge la vidéo pour qu'elle s'adapte à la nouvelle orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (webView != null) {
            webView.loadUrl(videoUrl); // Recharge la vidéo dans la nouvelle orientation
        }
    }
}
```

### 📋 Récap VideoPlayer.java

| Méthode / Élément | Rôle | Point clé |
|---|---|---|
| `getIntent().getStringExtra("videoUrl")` | Récupère l'URL YouTube embed | Envoyée par `MovieDetailActivity` avec `putExtra` |
| `webView.getSettings().setJavaScriptEnabled(true)` | Active JavaScript dans WebView | ⚠️ OBLIGATOIRE : sans ça = page blanche pour YouTube |
| `webView.loadUrl(videoUrl)` | Charge et affiche la vidéo | URL format : `youtube.com/embed/KEY` |
| `onConfigurationChanged()` | Rotation portrait ↔ paysage | Recharge la vidéo pour adapter l'affichage |
| `EdgeToEdge.enable(this)` | Affichage bord à bord | Vidéo visible sous la barre de statut |

---

## 🔄 Flux complet de l'application

```
① App démarre → MainActivity.onCreate()
② RecyclerView configuré (LinearLayoutManager)
③ Volley → GET api.themoviedb.org/3/movie/popular?api_key=...  (arrière-plan)
④ TMDB répond JSON → parsing → MyMovieData[]
⑤ new MyMovieAdapter(movies) → recyclerView.setAdapter() → films affichés
⑥ Utilisateur tape "av" dans la barre :
   TextWatcher.onTextChanged("av")
   → getFilter().filter("av")
   → performFiltering() [arrière-plan] → parcourt originalMovieData
   → publishResults() [thread principal] → clear + addAll + notifyDataSetChanged
⑦ Utilisateur clique une carte :
   Intent("movieId" = 550) → startActivity(MovieDetailActivity)
⑧ MovieDetailActivity :
   Requête 1 → GET /movie/550 → titre + description + image
   Requête 2 → GET /movie/550/videos → trailerKey YouTube
   Google Maps → onMapReady() → marqueur cinéma + position GPS
⑨ Utilisateur clique "Play Movie" :
   trailerUrl = "https://youtube.com/embed/" + trailerKey
   Intent("videoUrl") → startActivity(VideoPlayer)
⑩ VideoPlayer :
   WebView + JavaScript activé
   webView.loadUrl(trailerUrl) → trailer YouTube en plein écran
```

---

## ⚠️ Points CRITIQUES pour l'examen

| Point critique | Règle à retenir | Conséquence si oublié |
|---|---|---|
| `layout_height="wrap_content"` sur RecyclerView et cartes | Toujours `wrap_content`, JAMAIS `match_parent` | Chaque carte prend tout l'écran, scroll cassé |
| `false` dans `inflate(layout, parent, false)` | Toujours `false` | Crash : "view already has a parent" |
| `notifyDataSetChanged()` dans `publishResults()` | Toujours appeler après `addAll()` | La liste ne se met pas à jour visuellement |
| Parcourir `originalMovieData` dans le filtre | JAMAIS `filteredMovieData` | La liste réduite ne revient jamais à la liste complète |
| `setJavaScriptEnabled(true)` dans WebView | Obligatoire pour YouTube | Page blanche, vidéo ne se charge pas |
| Déclarer toutes les activités dans `AndroidManifest` | `.MovieDetailActivity`, `.VideoPlayer` | Crash immédiat à l'ouverture |
| Permission `INTERNET` dans `AndroidManifest` | Obligatoire pour tout appel réseau | Crash à la première requête réseau |
| URL image = préfixe + `poster_path` | `"https://image.tmdb.org/t/p/w500"` + chemin | Image non chargée |
| URL trailer = `embed/` + `trailerKey` | `"https://www.youtube.com/embed/"` + key | Mauvais format, YouTube ne se charge pas |
| `getIntExtra("movieId", -1)` | Toujours fournir une valeur par défaut | Exception sans valeur par défaut |
| `if (myMovieAdapter != null)` avant `.getFilter()` | Vérifier null avant utilisation | NullPointerException si API pas encore répondu |
| `setLayoutManager(new LinearLayoutManager(this))` | Obligatoire sur le RecyclerView | Crash immédiat |
| `super(itemView)` dans le constructeur ViewHolder | Premier appel dans le constructeur | Erreur de compilation |

---

*Pr. OUHMIDA Asmae — MoviesApp — Guide révision examen pratique*
------------------------
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