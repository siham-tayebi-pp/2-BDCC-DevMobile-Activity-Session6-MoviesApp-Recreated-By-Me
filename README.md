APi key for TMDB: https://www.themoviedb.org/settings/account
Api key for map for SDK: https://cloud.google.com/
Site for firebase :  https://console.firebase.google.com/
-----------------------
# 📱 MoviesApp — Documentation Complète

> Projet Android — Pr. OUHMIDA Asmae  
> Guide pédagogique complet : chaque fichier, chaque classe, chaque ligne expliquée, suivi d'un guide pas à pas pour ajouter les fonctionnalités avancées du cours.

---

## Table des matières

1. [Architecture générale](#1-architecture-générale)
2. [Fichiers XML — Les layouts](#2-fichiers-xml--les-layouts)
3. [Fichiers Java — La logique](#3-fichiers-java--la-logique)
4. [Les bugs dans ton code actuel](#4-les-bugs-dans-ton-code-actuel)
5. [Guide — Ajouter la recherche en temps réel](#5-guide--ajouter-la-recherche-en-temps-réel)
6. [Guide — Connecter l'API TMDB avec Volley](#6-guide--connecter-lapi-tmdb-avec-volley)
7. [Guide — Ajouter l'écran Détail du film](#7-guide--ajouter-lécran-détail-du-film)
8. [Guide — Ajouter le lecteur de trailer](#8-guide--ajouter-le-lecteur-de-trailer)
9. [Guide — Ajouter Google Maps](#9-guide--ajouter-google-maps)
10. [AndroidManifest et build.gradle](#10-androidmanifest-et-buildgradle)

---

## 1. Architecture générale

### Comment fonctionne l'app ?

```
┌──────────────────────────────────────────────────────────────┐
│                   FLUX DE L'APPLICATION                      │
│                                                              │
│  1. L'app démarre → MainActivity.onCreate() s'exécute        │
│  2. On crée un tableau de MyMovieData[] (les films)          │
│  3. On crée un MyMovieAdapter avec ce tableau                │
│  4. On "branche" l'adapter sur le RecyclerView               │
│  5. L'écran affiche la liste des films en cartes             │
│  6. L'utilisateur clique une carte → Toast avec le nom       │
└──────────────────────────────────────────────────────────────┘
```

### Les 3 briques à comprendre

| Brique | Fichier | Rôle | Analogie |
|--------|---------|------|----------|
| **Modèle** | `MyMovieData.java` | Définit ce qu'est un film | La fiche d'un produit en magasin |
| **Adapter** | `MyMovieAdapter.java` | Fait le lien données ↔ affichage | Le vendeur qui place les produits en rayon |
| **Vue** | `activity_main.xml` + `movie_item_list.xml` | Ce que l'utilisateur voit | Le rayon et les étiquettes des produits |

### Schéma des fichiers

```
MoviesApp/
│
├── java/com/example/movie_app_bdcc/
│   ├── MainActivity.java        ← Point d'entrée, orchestre tout
│   ├── MyMovieAdapter.java      ← Adapter pour le RecyclerView
│   └── MyMovieData.java         ← Modèle de données (objet Film)
│
└── res/layout/
    ├── activity_main.xml        ← Écran principal (contient le RecyclerView)
    └── movie_item_list.xml      ← Layout d'une carte film
```

---

## 2. Fichiers XML — Les layouts

### `activity_main.xml` — L'écran principal

```xml
<LinearLayout
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/colorAccent">
```

| Attribut | Valeur | Explication |
|----------|--------|-------------|
| `layout_width="match_parent"` | toute la largeur | S'étire sur tout l'écran en largeur |
| `layout_height="match_parent"` | toute la hauteur | S'étire sur tout l'écran en hauteur |
| `orientation="vertical"` | vertical | Les enfants sont empilés du haut vers le bas |
| `background="@color/colorAccent"` | couleur | Couleur de fond définie dans `colors.xml` |

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/RecyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_margin="2dp"/>
```

| Attribut | Explication |
|----------|-------------|
| `id="@+id/RecyclerView"` | Identifiant pour retrouver cette vue depuis le code Java avec `findViewById()` |
| `layout_margin="2dp"` | Espace de 2dp entre le bord de l'écran et la liste |
| `RecyclerView` | Vue en liste intelligente : elle **recycle** les cartes qui sortent de l'écran pour éviter de créer des centaines de vues en mémoire |

---

### `movie_item_list.xml` — La carte d'un film

Ce fichier définit **l'apparence d'une seule carte** dans la liste. Il est instancié autant de fois qu'il y a de films.

```xml
<androidx.cardview.widget.CardView
    android:id="@+id/CardView"
    app:cardBackgroundColor="@color/colorAccent"
    app:cardCornerRadius="10dp"
    app:cardElevation="10dp"
    android:layout_margin="10dp"/>
```

| Attribut | Explication |
|----------|-------------|
| `cardCornerRadius="10dp"` | Arrondit les coins de la carte |
| `cardElevation="10dp"` | Crée une ombre sous la carte (effet de profondeur/relief) |
| `layout_margin="10dp"` | Espace entre les cartes dans la liste |

```xml
<ImageView
    android:id="@+id/ImageView"
    android:layout_width="120dp"
    android:layout_height="150dp"
    android:src="@drawable/avatar"/>
```

Affiche l'image du film. `120dp × 150dp` = dimensions fixes de la pochette. `src="@drawable/avatar"` est l'image par défaut affichée dans l'éditeur (elle sera remplacée dynamiquement par l'adapter).

```xml
<TextView
    android:id="@+id/TextViewName"
    android:textSize="25sp"
    android:textStyle="bold"
    android:textColor="@color/black"/>

<TextView
    android:id="@+id/TextViewDate"
    android:textSize="25sp"
    android:textStyle="italic|bold"
    android:textColor="@color/colorPrimaryDark"/>
```

`sp` (scale-independent pixels) est l'unité à utiliser pour les textes car elle respecte les préférences de taille de police de l'utilisateur. `dp` est réservé aux marges et dimensions.

---

## 3. Fichiers Java — La logique

### `MyMovieData.java` — Le modèle de données

**Rôle** : représenter un film. C'est un simple objet Java (POJO) qui stocke les informations d'un film.

```java
public class MyMovieData {
    private String movieName;   // Titre du film
    private String movieData;   // Date de sortie
    private Integer movieImage; // ID de la ressource image (R.drawable.xxx)
```

`private` = **encapsulation** : les champs ne sont pas accessibles directement depuis l'extérieur. On doit passer par les getters et setters.

```java
public MyMovieData(String movieName, String movieData, Integer movieImage) {
    this.movieName = movieName;
    this.movieData = movieData;
    this.movieImage = movieImage;
}
```

Le **constructeur** est appelé quand on écrit `new MyMovieData("Avengers", "2019 film", R.drawable.avatar)`. `this.movieName` désigne le champ de la classe, `movieName` désigne le paramètre reçu.

```java
public String getMovieName() { return movieName; }
public String getMovieData()  { return movieData; }
public Integer getMovieImage(){ return movieImage; }
```

Les **getters** permettent de lire les valeurs depuis l'extérieur (depuis l'adapter notamment).

---

### `MainActivity.java` — Le chef d'orchestre

**Rôle** : point d'entrée de l'app. `onCreate()` est la première méthode appelée au démarrage.

```java
EdgeToEdge.enable(this);
setContentView(R.layout.activity_main);
```

`EdgeToEdge.enable(this)` permet à l'app de s'afficher jusqu'aux bords de l'écran (sous la barre de statut et la barre de navigation). `setContentView()` charge le fichier XML et l'affiche à l'écran.

```java
ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
    return insets;
});
```

Ce bloc ajoute automatiquement un padding pour que le contenu ne se retrouve pas caché derrière la barre de statut ou la barre de navigation. C'est nécessaire car on a activé EdgeToEdge.

```java
RecyclerView recyclerView = findViewById(R.id.RecyclerView);
recyclerView.setHasFixedSize(true);
recyclerView.setLayoutManager(new LinearLayoutManager(this));
```

- `findViewById(R.id.RecyclerView)` cherche dans le layout XML la vue avec l'id `RecyclerView` et retourne un objet Java.
- `setHasFixedSize(true)` : optimisation — on dit au RecyclerView que sa taille ne change pas selon le contenu.
- `setLayoutManager(new LinearLayoutManager(this))` : définit que les éléments s'affichent en **liste verticale**. Sans LayoutManager, le RecyclerView ne sait pas comment arranger ses enfants.

```java
MyMovieData[] myMovieData = new MyMovieData[]{
    new MyMovieData("Avengers", "2019 film", R.drawable.avatar),
    new MyMovieData("Wednesday", "2019 film", R.drawable.avatar),
    // ...
};
```

On crée un tableau de 9 films codés en dur. `R.drawable.avatar` est l'identifiant entier de l'image `avatar.png` placée dans le dossier `res/drawable/`.

```java
MyMovieAdapter MY = new MyMovieAdapter(myMovieData, MainActivity.this);
recyclerView.setAdapter(MY);
```

On crée l'adapter en lui passant les données et le contexte, puis on le "branche" sur le RecyclerView avec `setAdapter()`. À partir de là, la liste s'affiche.

---

### `MyMovieAdapter.java` — Le cœur de la liste

**Rôle** : faire le lien entre les données (le tableau `MyMovieData[]`) et les vues (les cartes XML). Il répond à trois questions posées par le RecyclerView :
1. Combien d'éléments y a-t-il ? → `getItemCount()`
2. À quoi ressemble une carte ? → `onCreateViewHolder()`
3. Quelles données mettre dans la carte numéro X ? → `onBindViewHolder()`

```java
public class MyMovieAdapter extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder> {
    MyMovieData[] myMovieData;
    Context context;
```

`extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>` : on hérite de la classe Adapter fournie par Android. Le type entre `< >` indique quel ViewHolder on utilise.

#### `onCreateViewHolder` — Fabriquer une nouvelle carte vide

```java
public MyMovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    View view = layoutInflater.inflate(R.layout.movie_item_list, parent, false);
    return new ViewHolder(view);
}
```

- `LayoutInflater` : outil qui convertit un fichier XML en objets Java (View). Sans lui, on ne peut pas utiliser un fichier XML dans du code Java.
- `.inflate(R.layout.movie_item_list, parent, false)` : lit le XML `movie_item_list.xml` et crée la vue correspondante. `false` = ne pas l'attacher immédiatement au parent, le RecyclerView s'en charge.
- `return new ViewHolder(view)` : retourne un ViewHolder qui va mémoriser les références aux vues de cette carte.

**Pourquoi le RecyclerView "recycle" ?** Android ne crée que 8 à 10 cartes pour remplir l'écran. Quand une carte sort en haut, elle est réutilisée pour afficher le film suivant en bas. `onCreateViewHolder` n'est donc appelé qu'une dizaine de fois, pas une fois par film.

#### `onBindViewHolder` — Remplir une carte avec les données d'un film

```java
public void onBindViewHolder(MyMovieAdapter.ViewHolder holder, int position) {
    final MyMovieData myMovieDataList = myMovieData[position];
    holder.textViewName.setText(myMovieDataList.getMovieName());
    holder.textViewData.setText(myMovieDataList.getMovieData());
    holder.movieImage.setImageResource(myMovieDataList.getMovieImage());
```

`position` est l'indice du film à afficher (0 = premier film, 1 = deuxième, etc.). On récupère le film à cet indice, puis on remplit les vues du ViewHolder avec ses données.

```java
    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(context, myMovieDataList.getMovieName(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

`setOnClickListener` attache un écouteur de clic sur toute la carte. Quand l'utilisateur appuie dessus, un `Toast` (petit message temporaire en bas d'écran) affiche le nom du film.

#### La classe `ViewHolder` — Le conteneur de vues

```java
public class ViewHolder extends RecyclerView.ViewHolder {
    TextView textViewName;
    TextView textViewData;
    ImageView movieImage;

    public ViewHolder(View itemView) {
        super(itemView);
        textViewName = itemView.findViewById(R.id.ImageView);   // ⚠️ voir bug section 4
        textViewData = itemView.findViewById(R.id.TextViewDate);
        itemView     = itemView.findViewById(R.id.ImageView);   // ⚠️ voir bug section 4
    }
}
```

Le ViewHolder stocke les références aux vues d'une carte. Sans lui, `onBindViewHolder` appellerait `findViewById()` à chaque défilement (très lent). Avec lui, `findViewById()` n'est appelé qu'une seule fois par carte créée.

---

## 4. Les bugs dans ton code actuel

Ton code contient deux bugs importants dans la classe `ViewHolder` qui empêchent les images et les noms de s'afficher correctement.

### Bug 1 — Mauvais ID pour `textViewName`

```java
// ❌ Code actuel (BUG)
textViewName = itemView.findViewById(R.id.ImageView);

// ✅ Correction
textViewName = itemView.findViewById(R.id.TextViewName);
```

`R.id.ImageView` est l'ID de l'ImageView, pas du TextView du nom. Il faut utiliser `R.id.TextViewName`.

### Bug 2 — Écrasement de `itemView`

```java
// ❌ Code actuel (BUG)
itemView = itemView.findViewById(R.id.ImageView);
// Cette ligne écrase la variable itemView avec un ImageView, ce qui casse tout

// ✅ Correction
movieImage = itemView.findViewById(R.id.ImageView);
```

Il manque l'assignation à `movieImage`. La ligne actuelle écrase `itemView` (la carte entière) avec l'ImageView, ce qui est une erreur grave.

### Code corrigé complet du ViewHolder

```java
public ViewHolder(View itemView) {
    super(itemView);
    textViewName = itemView.findViewById(R.id.TextViewName);  // ✅ bon ID
    textViewData = itemView.findViewById(R.id.TextViewDate);
    movieImage   = itemView.findViewById(R.id.ImageView);     // ✅ bonne variable
}
```

---

## 5. Guide — Ajouter la recherche en temps réel

Cette fonctionnalité permet de filtrer la liste de films pendant que l'utilisateur tape dans une barre de recherche.

### Étape 1 — Modifier `activity_main.xml`

Ajoute un `EditText` (champ de texte) **avant** le RecyclerView :

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/colorAccent">

    <!-- NOUVEAU : barre de recherche -->
    <EditText
        android:id="@+id/editTextSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Rechercher un film..."
        android:inputType="text"/>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/RecyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_margin="2dp"/>

</LinearLayout>
```

### Étape 2 — Modifier `MyMovieAdapter.java` pour implémenter `Filterable`

`Filterable` est une interface Android qui oblige l'adapter à fournir un filtre. Voici le fichier complet corrigé et augmenté :

```java
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Ajoute "implements Filterable" à la déclaration de la classe
public class MyMovieAdapter extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>
        implements Filterable {

    private MyMovieData[] originalMovieData; // Liste originale, jamais modifiée
    private List<MyMovieData> filteredMovieData; // Liste affichée (peut être filtrée)
    private Context context;

    public MyMovieAdapter(MyMovieData[] myMovieData, Context context) {
        this.originalMovieData  = myMovieData;
        this.filteredMovieData  = new ArrayList<>(Arrays.asList(myMovieData));
        // On copie le tableau dans une List modifiable.
        // On ne modifie JAMAIS originalMovieData, pour pouvoir
        // revenir à la liste complète quand l'utilisateur efface sa recherche.
        this.context = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Utilise filteredMovieData (pas originalMovieData)
        final MyMovieData movie = filteredMovieData.get(position);
        holder.textViewName.setText(movie.getMovieName());
        holder.textViewData.setText(movie.getMovieData());
        holder.movieImage.setImageResource(movie.getMovieImage());

        holder.itemView.setOnClickListener(v ->
            Toast.makeText(context, movie.getMovieName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return filteredMovieData.size(); // Taille de la liste filtrée
    }

    // Méthode imposée par Filterable
    @Override
    public Filter getFilter() {
        return movieFilter;
    }

    private Filter movieFilter = new Filter() {

        // S'exécute en arrière-plan (thread séparé) pour ne pas bloquer l'UI
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<MyMovieData> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // Champ vide → on remet tous les films
                filteredList.addAll(Arrays.asList(originalMovieData));
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                // .toLowerCase() : rend la recherche insensible à la casse
                // .trim()         : supprime les espaces au début et à la fin

                for (MyMovieData movie : originalMovieData) {
                    if (movie.getMovieName().toLowerCase().contains(pattern)) {
                        filteredList.add(movie); // Film correspondant → on le garde
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        // S'exécute sur le thread principal pour mettre à jour l'affichage
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredMovieData.clear();
            filteredMovieData.addAll((List) results.values);
            notifyDataSetChanged(); // Indispensable : prévient le RecyclerView
            // que les données ont changé et qu'il doit se redessiner
        }
    };

    // ViewHolder corrigé
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewData;
        ImageView movieImage;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.TextViewName);
            textViewData = itemView.findViewById(R.id.TextViewDate);
            movieImage   = itemView.findViewById(R.id.ImageView);
        }
    }
}
```

### Étape 3 — Modifier `MainActivity.java` pour connecter la recherche

```java
public class MainActivity extends AppCompatActivity {

    private MyMovieAdapter myMovieAdapter; // Déclare l'adapter en attribut de classe
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ... (padding EdgeToEdge, inchangé)

        searchEditText = findViewById(R.id.editTextSearch);
        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MyMovieData[] myMovieData = new MyMovieData[]{
            new MyMovieData("Avengers",       "2019 film", R.drawable.avatar),
            new MyMovieData("Wednesday",      "2019 film", R.drawable.avatar),
            new MyMovieData("Twilight",       "2019 film", R.drawable.avatar),
            new MyMovieData("Maleficent",     "2019 film", R.drawable.avatar),
            new MyMovieData("Venom",          "2019 film", R.drawable.avatar),
            new MyMovieData("Batman Begins",  "2019 film", R.drawable.avatar),
            new MyMovieData("Good Deeds",     "2019 film", R.drawable.avatar),
            new MyMovieData("Jumanji",        "2019 film", R.drawable.avatar),
            new MyMovieData("Hulk",           "2019 film", R.drawable.avatar)
        };

        myMovieAdapter = new MyMovieAdapter(myMovieData, MainActivity.this);
        recyclerView.setAdapter(myMovieAdapter);

        // TextWatcher : écouteur de changement sur le champ de recherche
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Rien à faire ici
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Appelé à chaque frappe clavier, en temps réel
                // "s" contient le texte actuel dans le champ
                if (myMovieAdapter != null) {
                    myMovieAdapter.getFilter().filter(s);
                    // Déclenche performFiltering(s) dans l'adapter
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Rien à faire ici
            }
        });
    }
}
```

---

## 6. Guide — Connecter l'API TMDB avec Volley

Au lieu de films codés en dur, on va récupérer les vrais films populaires depuis internet.

### Pourquoi Volley ?

Android interdit les requêtes internet dans le thread principal (ça bloquerait l'écran). Volley fait la requête **en arrière-plan** et appelle ton code quand la réponse est prête.

```
SANS Volley → app bloquée pendant la requête ❌
AVEC Volley → app fluide, requête en arrière-plan ✅
```

### Étape 1 — Ajouter Volley et Glide dans `build.gradle`

```gradle
dependencies {
    implementation("com.android.volley:volley:1.2.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}
```

**Glide** est une bibliothèque qui charge des images depuis une URL en arrière-plan et les place automatiquement dans un ImageView.

### Étape 2 — Ajouter la permission internet dans `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Sans cette ligne, Android refuse toute connexion réseau.

### Étape 3 — Modifier `MyMovieData.java` pour ajouter l'ID et changer le type d'image

```java
public class MyMovieData {
    private int movieId;          // ID unique TMDB (ex: 550)
    private String movieName;
    private String movieData;
    private String movieImage;    // Maintenant une URL (String), plus un int

    public MyMovieData(int movieId, String movieName, String movieData, String movieImage) {
        this.movieId    = movieId;
        this.movieName  = movieName;
        this.movieData  = movieData;
        this.movieImage = movieImage;
    }

    public int    getMovieId()   { return movieId; }
    public String getMovieName() { return movieName; }
    public String getMovieData() { return movieData; }
    public String getMovieImage(){ return movieImage; }
}
```

### Étape 4 — Modifier `MainActivity.java` pour appeler l'API

```java
public class MainActivity extends AppCompatActivity {

    // Remplace "your_api_key" par ta vraie clé obtenue sur themoviedb.org
    private static final String TMDB_API_KEY = "your_api_key";
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/popular";
    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private MyMovieAdapter myMovieAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.editTextSearch);
        recyclerView   = findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Construire l'URL complète avec la clé API
        String url = BASE_URL + "?api_key=" + TMDB_API_KEY;
        // Résultat : "https://api.themoviedb.org/3/movie/popular?api_key=abc123"

        RequestQueue queue = Volley.newRequestQueue(this);
        // Crée le gestionnaire de requêtes HTTP. "this" = contexte Android.

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, // GET = lire des données (pas envoyer)
            url,
            null,               // Corps de la requête : null car c'est un GET

            // Callback de succès : appelé quand TMDB répond
            response -> {
                try {
                    JSONArray results = response.getJSONArray("results");
                    // "results" est la clé du tableau de films dans le JSON TMDB

                    MyMovieData[] movies = new MyMovieData[results.length()];

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject movie = results.getJSONObject(i);

                        int    id          = movie.getInt("id");
                        String title       = movie.getString("title");
                        String releaseDate = movie.getString("release_date");
                        String posterPath  = movie.getString("poster_path");
                        // poster_path = chemin relatif ex: "/pB8BM7.jpg"
                        // L'URL complète sera construite dans l'adapter

                        movies[i] = new MyMovieData(id, title, releaseDate, posterPath);
                    }

                    myMovieAdapter = new MyMovieAdapter(movies, MainActivity.this);
                    recyclerView.setAdapter(myMovieAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },

            // Callback d'erreur : appelé si la requête échoue
            error -> Log.e(TAG, "Erreur réseau : " + error.getMessage())
        );

        queue.add(request); // Envoie la requête (en arrière-plan)

        // TextWatcher (identique à la section précédente)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (myMovieAdapter != null) myMovieAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
```

### Étape 5 — Modifier l'adapter pour utiliser Glide

Dans `onBindViewHolder`, remplace `setImageResource()` (qui ne fonctionne qu'avec des ressources locales) par Glide :

```java
@Override
public void onBindViewHolder(ViewHolder holder, int position) {
    final MyMovieData movie = filteredMovieData.get(position);

    holder.textViewName.setText(movie.getMovieName());
    holder.textViewData.setText(movie.getMovieData());

    // Glide charge l'image depuis l'URL en arrière-plan
    // "https://image.tmdb.org/t/p/w500" est le préfixe de toutes les images TMDB
    // "w500" = largeur 500px
    Glide.with(context)
         .load("https://image.tmdb.org/t/p/w500" + movie.getMovieImage())
         .into(holder.movieImage);
}
```

---

## 7. Guide — Ajouter l'écran Détail du film

Quand l'utilisateur clique sur un film, on ouvre un nouvel écran avec la description complète.

### Étape 1 — Créer `activity_movie_detail.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <androidx.cardview.widget.CardView
        app:cardElevation="10dp"
        app:cardCornerRadius="10dp"
        android:layout_margin="5dp"
        app:cardBackgroundColor="#FFFFFF"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <ImageView
                android:id="@+id/imageview"
                android:layout_margin="10dp"
                android:layout_width="120dp"
                android:layout_height="150dp"/>

            <LinearLayout
                android:layout_weight="1"
                android:gravity="center"
                android:orientation="vertical"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <TextView
                    android:id="@+id/textName"
                    android:textStyle="bold"
                    android:layout_margin="7dp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

                <TextView
                    android:id="@+id/Details"
                    android:layout_margin="5dp"
                    android:textColor="#000"
                    android:textSize="12sp"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

</LinearLayout>
```

### Étape 2 — Créer `MovieDetailActivity.java`

```java
package com.example.movie_app_bdcc;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetailActivity extends AppCompatActivity {

    private TextView descriptionTextView;
    private TextView nameTextView;
    private ImageView img;
    private RequestQueue requestQueue;
    private static final String TAG = "MovieDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        descriptionTextView = findViewById(R.id.Details);
        nameTextView        = findViewById(R.id.textName);
        img                 = findViewById(R.id.imageview);

        requestQueue = Volley.newRequestQueue(this);

        // Récupère l'ID du film transmis par l'Intent
        int movieId = getIntent().getIntExtra("movieId", -1);
        // -1 est la valeur par défaut si "movieId" n'est pas trouvé

        if (movieId != -1) {
            fetchMovieDetails(movieId);
        } else {
            descriptionTextView.setText("Aucun film sélectionné.");
        }
    }

    private void fetchMovieDetails(int movieId) {
        String apiKey = "your_api_key";
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    String name        = response.getString("title");
                    String description = response.getString("overview");
                    String imageUrl    = "https://image.tmdb.org/t/p/w500"
                                         + response.getString("poster_path");

                    nameTextView.setText(name);
                    descriptionTextView.setText(description);
                    Glide.with(MovieDetailActivity.this).load(imageUrl).into(img);

                } catch (JSONException e) {
                    Log.e(TAG, "Erreur parsing JSON : " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Erreur réseau : " + error.getMessage());
                descriptionTextView.setText("Impossible de charger les détails.");
            }
        );

        requestQueue.add(request);
    }
}
```

### Étape 3 — Déclarer `MovieDetailActivity` dans `AndroidManifest.xml`

Toute nouvelle Activity doit être déclarée dans le Manifest, sinon l'app plante :

```xml
<application ...>
    <activity android:name=".MainActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
    </activity>

    <!-- NOUVEAU -->
    <activity android:name=".MovieDetailActivity"/>

</application>
```

### Étape 4 — Modifier le clic dans l'adapter pour ouvrir le détail

Dans `onBindViewHolder` de `MyMovieAdapter`, remplace le Toast par une navigation :

```java
holder.itemView.setOnClickListener(v -> {
    Intent intent = new Intent(context, MovieDetailActivity.class);
    intent.putExtra("movieId", movie.getMovieId());
    // putExtra("clé", valeur) : envoie des données à la prochaine Activity
    // Dans MovieDetailActivity, on récupère avec getIntent().getIntExtra("movieId", -1)
    context.startActivity(intent);
});
```

---

## 8. Guide — Ajouter le lecteur de trailer

### Étape 1 — Créer `activity_video_player.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

`WebView` est un navigateur web intégré dans l'app. On l'utilise pour charger et lire le trailer YouTube via une URL embed.

### Étape 2 — Créer `VideoPlayer.java`

```java
package com.example.movie_app_bdcc;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayer extends AppCompatActivity {

    private WebView webView;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra("videoUrl");
        // Récupère l'URL YouTube embed envoyée par MovieDetailActivity

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        // OBLIGATOIRE : le player YouTube utilise JavaScript.
        // Sans cette ligne, la vidéo ne s'affiche pas.

        webView.loadUrl(videoUrl);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Appelé quand l'utilisateur tourne son téléphone
        if (webView != null) {
            webView.loadUrl(videoUrl); // Recharge la vidéo en plein écran paysage
        }
    }
}
```

### Étape 3 — Ajouter le bouton "Play" et la requête trailer dans `MovieDetailActivity`

Dans `activity_movie_detail.xml`, ajoute ce bouton après le CardView :

```xml
<Button
    android:id="@+id/playButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="▶ Voir le trailer"
    android:layout_marginTop="16dp"/>
```

Dans `MovieDetailActivity.java`, ajoute la logique :

```java
private String trailerKey; // Clé YouTube du trailer (ex: "dQw4w9WgXcQ")
private Button playButton;

@Override
protected void onCreate(Bundle savedInstanceState) {
    // ... (code existant)

    playButton = findViewById(R.id.playButton);
    playButton.setOnClickListener(v -> playTrailer());

    if (movieId != -1) {
        fetchMovieDetails(movieId);
        fetchMovieTrailer(movieId); // Nouvelle méthode
    }
}

private void fetchMovieTrailer(int movieId) {
    String url = "https://api.themoviedb.org/3/movie/" + movieId
               + "/videos?api_key=your_api_key";

    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
        response -> {
            try {
                JSONArray results = response.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject video = results.getJSONObject(i);
                    if (video.getString("type").equals("Trailer")) {
                        trailerKey = video.getString("key");
                        // "key" = l'ID YouTube ex: "dQw4w9WgXcQ"
                        break; // On s'arrête au premier trailer trouvé
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        },
        error -> Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show()
    );

    requestQueue.add(request);
}

private void playTrailer() {
    if (trailerKey != null && !trailerKey.isEmpty()) {
        String trailerUrl = "https://www.youtube.com/embed/" + trailerKey;
        // URL format embed YouTube : lisible dans un WebView

        Intent intent = new Intent(MovieDetailActivity.this, VideoPlayer.class);
        intent.putExtra("videoUrl", trailerUrl);
        startActivity(intent);
    } else {
        Toast.makeText(this, "Trailer non disponible", Toast.LENGTH_SHORT).show();
    }
}
```

N'oublie pas de déclarer `VideoPlayer` dans `AndroidManifest.xml` :

```xml
<activity android:name=".VideoPlayer"/>
```

---

## 9. Guide — Ajouter Google Maps

### Étape 1 — Dépendances dans `build.gradle`

```gradle
implementation("com.google.android.gms:play-services-maps:17.0.0")
implementation("com.google.android.gms:play-services-location:17.0.0")
```

### Étape 2 — Clé API dans `AndroidManifest.xml`

Obtiens une clé sur [Google Cloud Console](https://cloud.google.com/), active l'API "Maps SDK for Android", puis :

```xml
<application ...>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="ta_cle_google_maps"/>
</application>
```

Permissions à ajouter :

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

### Étape 3 — Ajouter la carte dans `activity_movie_detail.xml`

```xml
<fragment
    android:id="@+id/map"
    android:name="com.google.android.gms.maps.SupportMapFragment"
    android:layout_width="match_parent"
    android:layout_height="300dp"
    android:layout_marginTop="16dp"/>
```

### Étape 4 — Implémenter la carte dans `MovieDetailActivity.java`

```java
public class MovieDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ... (code existant)

        SupportMapFragment mapFragment = (SupportMapFragment)
            getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // getMapAsync() appelle onMapReady() quand la carte est prête
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Coordonnées GPS d'un cinéma à Casablanca
        LatLng cinema = new LatLng(33.596460, -7.615480);

        // Ajouter un marqueur sur la carte
        mMap.addMarker(new MarkerOptions()
            .position(cinema)
            .title("Cinéma")
            .snippet("Votre cinéma le plus proche"));

        // Centrer la caméra sur le marqueur, zoom 15 (niveau quartier)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cinema, 15));

        // Activer le bouton "Ma position" si la permission est accordée
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }
}
```

---

## 10. AndroidManifest et build.gradle

### `AndroidManifest.xml` complet

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.Movie_app_bdcc">

        <!-- Clé Google Maps -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="ta_cle_google_maps"/>

        <!-- Activités déclarées -->
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <activity android:name=".MovieDetailActivity"/>
        <activity android:name=".VideoPlayer"/>

    </application>
</manifest>
```

### `build.gradle` complet

```gradle
dependencies {
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("com.google.android.gms:play-services-location:17.0.0")
    implementation("com.android.volley:volley:1.2.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
```

---

## Récapitulatif du flux complet

```
┌─────────────────────────────────────────────────────────────┐
│                   FLUX COMPLET FINAL                        │
│                                                             │
│  ① App démarre → MainActivity.onCreate()                    │
│        │                                                    │
│  ② Volley envoie GET api.themoviedb.org/3/movie/popular     │
│        │                                                    │
│  ③ TMDB répond avec JSON → parsing → MyMovieData[]          │
│        │                                                    │
│  ④ MyMovieAdapter + setAdapter() → films affichés           │
│        │                                                    │
│  ⑤ Utilisateur tape → TextWatcher → filter() → liste filtre │
│        │                                                    │
│  ⑥ Utilisateur clique → Intent → MovieDetailActivity        │
│        │                                                    │
│  ⑦ Deux requêtes : détails du film + trailer key            │
│        │                                                    │
│  ⑧ Carte Google Maps affichée avec marqueur cinéma          │
│        │                                                    │
│  ⑨ Utilisateur clique "Play" → VideoPlayer (WebView YouTube)│
└─────────────────────────────────────────────────────────────┘
```

---

*Projet réalisé en cours — Pr. OUHMIDA Asmae*----------------------------------
Un Adapter est le pont entre :

📦 Données (films)  →  🧠 Adapter  →  📱 Interface (RecyclerView)
Le ViewHolder = un objet qui contient les vues d’une carte film.

# 📘 Explications Détaillées — MoviesApp
> Guide pédagogique complet : chaque concept, chaque ligne, chaque mot-clé expliqué.

---

## 🧠 COMPRENDRE LE FONCTIONNEMENT DE L'APP AVANT TOUT

Avant de lire la moindre ligne de code, il faut comprendre **ce que fait l'application** :

```
┌─────────────────────────────────────────────────────────────────┐
│                   COMMENT FONCTIONNE L'APP ?                    │
│                                                                 │
│  1. L'app démarre → elle contacte le serveur TMDB (internet)    │
│  2. TMDB répond avec une liste de films en format JSON          │
│  3. L'app lit (parse) ce JSON pour créer des objets Film        │
│  4. Ces objets sont donnés à l'Adaptateur                       │
│  5. L'Adaptateur les affiche dans le RecyclerView (la liste)    │
│  6. L'utilisateur peut chercher un film → la liste se filtre    │
│  7. L'utilisateur clique → l'app ouvre les détails du film      │
└─────────────────────────────────────────────────────────────────┘
```

**Les 3 grandes briques à comprendre :**

| Brique | Rôle | Analogie |
|--------|------|----------|
| **Volley** | Fait la requête internet vers TMDB | Le livreur qui va chercher la commande |
| **JSON Parsing** | Lit et comprend la réponse reçue | Déballer et trier le colis reçu |
| **RecyclerView + Adapter** | Affiche les films en liste | Le présentoir qui expose les produits |

---

## 📡 PARTIE 1 — LA REQUÊTE VOLLEY (Comment l'app parle à internet)

### 🔑 C'est quoi Volley ?

Android **n'autorise pas** de faire des requêtes internet directement dans le code principal
(ça bloquerait l'écran et l'app planterait). **Volley** est une bibliothèque qui :

- Fait la requête internet **en arrière-plan** (sans bloquer l'écran)
- Rappelle votre code quand la réponse est arrivée
- Gère automatiquement les erreurs réseau

```
SANS Volley :                          AVEC Volley :
─────────────────────────────          ─────────────────────────────────────
Thread principal                       Thread principal     Thread arrière-plan
     │                                      │                      │
     │── Requête internet ──┐               │── confie à Volley ──▶│── Requête ──▶ TMDB
     │   (APP BLOQUÉE !)    │               │                      │
     │◀─ Réponse ───────────┘               │◀── Volley rappelle ──│◀── Réponse ──
     │                                      │    (onResponse)
     │ continue...                          │ continue...
```

### 📝 Le code Volley expliqué ligne par ligne

```java
// ═══════════════════════════════════════════════════════════════
// ÉTAPE 1 : Créer la file d'attente des requêtes
// ═══════════════════════════════════════════════════════════════

RequestQueue queue = Volley.newRequestQueue(this);
//                   ──────────────────────
//                   Crée un gestionnaire de requêtes HTTP.
//                   "this" = le contexte de l'activité (nécessaire
//                   pour accéder aux ressources Android).
//                   Toutes les requêtes passent par cette "queue".


// ═══════════════════════════════════════════════════════════════
// ÉTAPE 2 : Construire l'URL complète
// ═══════════════════════════════════════════════════════════════

String url = BASE_URL + "?api_key=" + TMDB_API_KEY;
//           ────────   ──────────   ──────────────
//           |          |            Votre clé personnelle TMDB
//           |          Le "?" sépare l'URL des paramètres
//           https://api.themoviedb.org/3/movie/popular
//
// Résultat final de url :
// "https://api.themoviedb.org/3/movie/popular?api_key=abc123xyz"
//
// C'est exactement comme taper cette adresse dans votre navigateur !
// Essayez : ouvrez un navigateur et collez cette URL avec votre clé.
// Vous verrez le JSON brut que l'app reçoit.


// ═══════════════════════════════════════════════════════════════
// ÉTAPE 3 : Créer la requête et définir ce qu'on fait avec la réponse
// ═══════════════════════════════════════════════════════════════

JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(

    Request.Method.GET,
    // ────────────────
    // La méthode HTTP. GET = "donne-moi des données" (lecture seule).
    // (Autres méthodes : POST = envoyer, PUT = modifier, DELETE = supprimer)

    url,
    // ───
    // L'URL qu'on vient de construire.

    null,
    // ────
    // Le corps (body) de la requête. Pour un GET, on n'envoie rien → null.
    // (On enverrait du JSON ici pour un POST)

    new Response.Listener<JSONObject>() {
    // ───────────────────────────────────
    // CALLBACK DE SUCCÈS : ce bloc de code s'exécute automatiquement
    // quand TMDB répond avec succès.
    // "Listener" = quelqu'un qui "écoute" et attend la réponse.
    // JSONObject = le type de réponse attendu (un objet JSON, pas un tableau).

        @Override
        public void onResponse(JSONObject response) {
        // ──────────────────────────────────────────
        // Cette méthode est appelée par Volley quand la réponse arrive.
        // "response" contient tout le JSON retourné par TMDB.
        // On est maintenant dans le Thread principal → on peut toucher l'UI.
            
            // ... (parsing du JSON, voir Partie 2)
        }
    },

    new Response.ErrorListener() {
    // ─────────────────────────────
    // CALLBACK D'ERREUR : s'exécute si la requête échoue
    // (pas internet, clé invalide, serveur en panne, timeout...)

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error occurred: " + error.getMessage());
            // Log.e = afficher une erreur dans la console Android (Logcat).
            // TAG = "MainActivity" → permet de filtrer les logs.
            // Utile pour déboguer sans bloquer l'app.
        }
    }
);

// ═══════════════════════════════════════════════════════════════
// ÉTAPE 4 : Envoyer la requête
// ═══════════════════════════════════════════════════════════════

queue.add(jsonObjectRequest);
// ──────────────────────────
// On ajoute la requête à la file.
// Volley va l'envoyer en arrière-plan et rappeler onResponse ou
// onErrorResponse selon le résultat. L'écran reste réactif pendant ce temps.
```

---

## 📦 PARTIE 2 — LE PARSING JSON (Lire et comprendre la réponse de TMDB)

### 🔑 C'est quoi le JSON ?

Quand vous appelez l'API TMDB, elle vous renvoie du texte structuré appelé **JSON**.
C'est universel, lisible par tous les langages.

**Voici un exemple RÉEL de ce que TMDB renvoie :**

```json
{
  "page": 1,
  "results": [
    {
      "id": 550,
      "title": "Fight Club",
      "release_date": "1999-10-15",
      "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
      "overview": "A ticking-time-bomb insomniac and a slippery soap salesman..."
    },
    {
      "id": 680,
      "title": "Pulp Fiction",
      "release_date": "1994-10-14",
      "poster_path": "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
      "overview": "A burger-loving hit man, his philosophical partner..."
    }
  ],
  "total_pages": 500,
  "total_results": 10000
}
```

**Comprendre la structure JSON :**

```
{                          ← JSONObject (accolades = objet)
  "page": 1,
  "results": [             ← JSONArray (crochets = tableau)
    {                      ← JSONObject (chaque film est un objet)
      "id": 550,           ← champ entier (int)
      "title": "...",      ← champ texte (String)
      "release_date": ".." ← champ texte (String)
    },
    { ... }                ← deuxième film
  ]
}
```

### 📝 Le parsing expliqué ligne par ligne

```java
// ═══════════════════════════════════════════════════════════════
// ÉTAPE 1 : Extraire le tableau "results" du JSON principal
// ═══════════════════════════════════════════════════════════════

JSONArray results = response.getJSONArray("results");
//         ───────          ────────────────────────
//         |                Cherche la clé "results" dans le JSON
//         |                et récupère ce qu'il y a dedans.
//         Variable de type JSONArray : représente un tableau JSON [ ]
//
// ⚠️ Peut lancer une JSONException si la clé "results" n'existe pas.
// C'est pourquoi tout ça est dans un try/catch.
//
// Après cette ligne, "results" contient :
// [ {film1}, {film2}, {film3}, ... ]


// ═══════════════════════════════════════════════════════════════
// ÉTAPE 2 : Préparer le tableau qui va stocker nos objets Film
// ═══════════════════════════════════════════════════════════════

MyMovieData[] movies = new MyMovieData[results.length()];
//             ──────                  ───────────────────
//             |                       results.length() = nombre de films
//             |                       dans le tableau JSON (souvent 20)
//             Tableau Java de taille fixe pour stocker nos objets Film.
//             movies[0] = premier film, movies[1] = deuxième, etc.


// ═══════════════════════════════════════════════════════════════
// ÉTAPE 3 : Boucler sur chaque film du tableau
// ═══════════════════════════════════════════════════════════════

for (int i = 0; i < results.length(); i++) {
//   ─────────   ──────────────────────
//   |            Tant que i est inférieur au nombre de films
//   i commence à 0 (premier élément du tableau)


    // ─────────────────────────────────────────────────────────
    // Extraire l'objet JSON du film numéro i
    // ─────────────────────────────────────────────────────────
    JSONObject movieObject = results.getJSONObject(i);
    //          ──────────          ─────────────────
    //          |                   Récupère l'objet { } à la position i
    //          Variable qui représente UN film dans le JSON


    // ─────────────────────────────────────────────────────────
    // Lire chaque champ du film
    // ─────────────────────────────────────────────────────────
    int id = movieObject.getInt("id");
    //                  ──────────────
    //                  getInt() car "id" est un nombre entier dans le JSON
    //                  Exemple : 550

    String title = movieObject.getString("title");
    //                         ──────────────────
    //                         getString() car "title" est du texte
    //                         Exemple : "Fight Club"

    String releaseDate = movieObject.getString("release_date");
    //                                          ──────────────
    //                                          Exemple : "1999-10-15"

    String imageUrl = movieObject.getString("poster_path");
    //                                       ───────────────
    //                                       Exemple : "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
    //                                       ⚠️ C'est un chemin RELATIF !
    //                                       Pour l'URL complète, il faut ajouter :
    //                                       "https://image.tmdb.org/t/p/w500" devant.


    // ─────────────────────────────────────────────────────────
    // Créer un objet Java MyMovieData avec les données extraites
    // ─────────────────────────────────────────────────────────
    movies[i] = new MyMovieData(id, title, releaseDate, imageUrl);
    //           ──────────────────────────────────────────────────
    //           On crée un objet Film Java avec les données du JSON.
    //           movies[0] = Fight Club, movies[1] = Pulp Fiction, etc.
}


// ═══════════════════════════════════════════════════════════════
// ÉTAPE 4 : Donner les films à l'Adaptateur pour les afficher
// ═══════════════════════════════════════════════════════════════

myMovieAdapter = new MyMovieAdapter(movies, MainActivity.this);
//                                  ──────  ────────────────────
//                                  |       Le contexte Android (pour lancer
//                                  |       des Intents, charger des images...)
//                                  Le tableau de films qu'on vient de remplir

recyclerView.setAdapter(myMovieAdapter);
// ───────────────────────────────────────
// On "branche" l'adaptateur sur le RecyclerView.
// À partir de ce moment, la liste de films s'affiche à l'écran !
```

---

## 🔍 PARTIE 3 — LA RECHERCHE EN TEMPS RÉEL (TextWatcher)

### 🔑 C'est quoi un TextWatcher ?

Un **TextWatcher** est un "observateur" qui surveille un champ texte.
À chaque fois que l'utilisateur tape ou efface une lettre, il est notifié.

```
L'utilisateur tape "Av" dans la barre de recherche
         │
         ▼
   TextWatcher.onTextChanged() est appelé avec s = "Av"
         │
         ▼
   myMovieAdapter.getFilter().filter("Av")
         │
         ▼
   Le filtre parcourt tous les films et garde ceux
   dont le titre contient "av" (insensible à la casse)
         │
         ▼
   La liste se met à jour → n'affiche que les films filtrés
```

```java
searchEditText.addTextChangedListener(new TextWatcher() {
// ────────────────────────────────────────────────────────
// On attache un TextWatcher à l'EditText de recherche.
// "addTextChangedListener" = "commence à écouter les changements"


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    // ──────────────────────────────────────────────────────────────────────────────
    // Appelée AVANT que le texte change.
    // Paramètres :
    //   s     = le texte actuel (avant modification)
    //   start = position du curseur
    //   count = nombre de caractères sur le point d'être remplacés
    //   after = nombre de caractères qui vont les remplacer
    //
    // Ici : rien à faire. On ignore cette méthode.
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    // ───────────────────────────────────────────────────────────────────────────
    // Appelée EN TEMPS RÉEL à chaque frappe, PENDANT que le texte change.
    // C'est ici qu'on déclenche la recherche.
    //
    // Paramètre "s" = le nouveau texte dans le champ (après la frappe)
    // Exemple : l'utilisateur a tapé "Av" → s = "Av"

        if (myMovieAdapter != null) {
        // ─────────────────────────────
        // Sécurité : on vérifie que l'adaptateur existe.
        // Si la requête API n'est pas encore terminée, l'adaptateur
        // serait null et causerait un plantage (NullPointerException).

            myMovieAdapter.getFilter().filter(s);
            //              ─────────────────────
            //              getFilter() → récupère le filtre de l'adaptateur
            //              .filter(s)  → lance le filtrage avec le texte "s"
            //
            // Cela déclenche performFiltering(s) dans MyMovieAdapter
            // (voir Partie 4 ci-dessous pour le détail)
        }
    }


    @Override
    public void afterTextChanged(Editable s) {
    // ──────────────────────────────────────
    // Appelée APRÈS que le texte a changé.
    // Ici : rien à faire. On ignore cette méthode.
    //
    // Note : "Editable" est différent de "CharSequence" →
    // c'est un texte modifiable. On pourrait l'utiliser pour
    // formater ou transformer ce que l'utilisateur a tapé.
    }
});
```

---

## 🔧 PARTIE 4 — MyMovieAdapter.java (Le chef d'orchestre de la liste)

### 🔑 C'est quoi un Adaptateur ?

Imaginez un **RecyclerView** comme un cadre vide.
Il ne sait pas quoi afficher, ni comment.
L'**Adaptateur** lui dit :
- Combien d'éléments y a-t-il ? (`getItemCount`)
- À quoi ressemble chaque élément ? (`onCreateViewHolder`)
- Quelles données mettre dans l'élément numéro X ? (`onBindViewHolder`)

```
┌─────────────────────────────────────────────────────────────────┐
│                     SCHÉMA MENTAL                               │
│                                                                 │
│  DONNÉES          ADAPTATEUR            RECYCLERVIEW            │
│  ─────────        ───────────           ────────────            │
│  movies[0]  ────▶                ────▶  ┌──────────┐           │
│  movies[1]  ────▶  MyMovieAdapter ────▶  │  Carte 1 │           │
│  movies[2]  ────▶                ────▶  │  Carte 2 │           │
│  ...               (traduit les          │  Carte 3 │           │
│                     données en            │  ...     │           │
│                     vues visuelles)       └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

### 🔑 C'est quoi un ViewHolder ?

Un **ViewHolder** est un objet qui **garde en mémoire les références** aux vues
d'une carte (l'ImageView, les TextViews...) pour ne pas avoir à les chercher à chaque fois.

**Pourquoi c'est important ?**

```
SANS ViewHolder (mauvaise façon) :
───────────────────────────────────────────────────────────────────
L'utilisateur fait défiler la liste.
Pour chaque carte visible, Android appelle findViewById() → LENT !
findViewById() parcourt tout l'arbre de vues à chaque fois.
Sur 1000 films, ça rame fortement. ❌

AVEC ViewHolder (bonne façon) :
───────────────────────────────────────────────────────────────────
On cherche les vues UNE SEULE FOIS dans onCreateViewHolder().
On les stocke dans le ViewHolder.
Pour les défilements suivants, on réutilise le ViewHolder → RAPIDE ! ✅
```

**Le "Recycler" dans RecyclerView :**

```
RECYCLAGE DES CARTES :
──────────────────────────────────────────────────────────────
L'utilisateur voit 7 cartes à l'écran.
Il fait défiler vers le bas.
La carte 1 sort de l'écran (en haut).
Android NE DÉTRUIT PAS cette carte. Il la RECYCLE.
Il prend le ViewHolder de la carte 1 et y met les données du film 8.
La "nouvelle" carte 8 apparaît en bas → c'est en fait la carte 1 réutilisée !

   Avant défilement :        Après défilement :
   ┌────────────┐            ┌────────────┐
   │  Film 1   │ ──recycle──▶│  Film 8   │ (même carte, nouvelles données)
   ├────────────┤            ├────────────┤
   │  Film 2   │            │  Film 2   │
   │  Film 3   │            │  Film 3   │
   │  Film 4   │            │  Film 4   │
   │  Film 5   │            │  Film 5   │
   │  Film 6   │            │  Film 6   │
   │  Film 7   │            │  Film 7   │
   └────────────┘            └────────────┘
   
Résultat : jamais plus de 8-10 cartes en mémoire, même avec 10 000 films ! ✅
```

### 📝 Le code de MyMovieAdapter expliqué ligne par ligne

```java
// ═══════════════════════════════════════════════════════════════
// DÉCLARATION DE LA CLASSE
// ═══════════════════════════════════════════════════════════════

public class MyMovieAdapter
    extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder>
//  ──────────────────────────────────────────────────────
//  On hérite de RecyclerView.Adapter.
//  Le <MyMovieAdapter.ViewHolder> entre chevrons indique
//  quel type de ViewHolder on utilise.
//  Android sait ainsi quel ViewHolder retourner dans onCreateViewHolder.

    implements Filterable {
//  ──────────────────────
//  On dit qu'on sait se filtrer (interface Filterable).
//  Ça oblige à implémenter la méthode getFilter().
//  Sans ça, myMovieAdapter.getFilter() ne serait pas possible.


// ═══════════════════════════════════════════════════════════════
// LES ATTRIBUTS DE LA CLASSE
// ═══════════════════════════════════════════════════════════════

private MyMovieData[] originalMovieData;
// ─────────────────────────────────────
// Garde la liste COMPLÈTE des films telle que reçue de l'API.
// Ne sera JAMAIS modifiée. Sert de référence pour le filtre.
// Quand l'utilisateur efface sa recherche, on repart de cette liste.

private List<MyMovieData> filteredMovieData;
// ──────────────────────────────────────────
// Liste des films ACTUELLEMENT AFFICHÉS (peut être filtrée).
// C'est cette liste que le RecyclerView affiche.
// Quand on cherche "Av" → filteredMovieData ne contient que les films
// avec "Av" dans le titre.
// List<> (avec crochets) = liste dynamique, peut grandir/réduire.

private Context context;
// ─────────────────────
// Le contexte Android. Nécessaire pour :
//   - Charger des images avec Glide (Glide.with(context))
//   - Lancer des activités (new Intent(context, ...))
//   - Accéder aux ressources de l'app


// ═══════════════════════════════════════════════════════════════
// LE CONSTRUCTEUR
// ═══════════════════════════════════════════════════════════════

public MyMovieAdapter(MyMovieData[] myMovieData, Context context) {

    this.originalMovieData = myMovieData;
    //  ───────────────────────────────────
    //  Stocke le tableau original.
    //  "this.originalMovieData" = l'attribut de la classe
    //  "myMovieData" = le paramètre reçu
    //  Sans "this.", Java ne saurait pas lequel choisir.

    this.filteredMovieData = new ArrayList<>(Arrays.asList(myMovieData));
    //                        ──────────────────────────────────────────
    //                        Arrays.asList() : convertit le tableau [] en List
    //                        new ArrayList<>(...) : crée une copie modifiable
    //
    //  ⚠️ Pourquoi une copie et pas directement myMovieData ?
    //  Parce qu'on va modifier filteredMovieData (ajouter/supprimer
    //  des éléments lors du filtrage). On veut que originalMovieData
    //  reste intact pour pouvoir "réinitialiser" la liste.

    this.context = context;
}


// ═══════════════════════════════════════════════════════════════
// onCreateViewHolder — "Fabrique" une nouvelle carte vide
// ═══════════════════════════════════════════════════════════════

@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
// ──────────────────────────────────────────────────────────────
// Appelée par Android quand il a BESOIN d'une nouvelle carte.
// Rappel : Android n'en crée que 8-10 pour tout l'écran.
// Paramètres :
//   parent   = le RecyclerView parent (contexte pour le layout)
//   viewType = si on avait plusieurs types de cartes (non utilisé ici)

    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    //              ──────────────────────────────────────────────────────────
    //              LayoutInflater = l'outil qui "gonfle" (convertit) un fichier
    //              XML en objets Java (View).
    //              C'est lui qui transforme activity_movie_item_list.xml
    //              en une vraie vue affichable.

    View view = layoutInflater.inflate(R.layout.activity_movie_item_list, parent, false);
    //           ─────────────────────────────────────────────────────────────────────────
    //           .inflate() = lire le XML et créer les vues Java correspondantes
    //
    //   R.layout.activity_movie_item_list → le fichier XML de la carte film
    //   parent   → le RecyclerView parent (pour hériter des paramètres de layout)
    //   false    → NE PAS ajouter la vue au parent maintenant (le RecyclerView le fera)
    //
    //   Résultat : "view" contient la carte complète avec ImageView + TextViews

    return new ViewHolder(view);
    //     ───────────────────────
    //     On crée un ViewHolder qui va stocker les références aux vues
    //     de cette carte. (Voir la classe ViewHolder ci-dessous)
}


// ═══════════════════════════════════════════════════════════════
// onBindViewHolder — "Remplit" une carte avec les données d'un film
// ═══════════════════════════════════════════════════════════════

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
// ─────────────────────────────────────────────────────────────────
// Appelée à chaque fois qu'une carte doit afficher un film.
// C'est ici que les données rencontrent les vues.
// Paramètres :
//   holder   = la carte à remplir (ViewHolder avec ses vues)
//   position = l'index du film à afficher (0 = premier film, etc.)

    final MyMovieData movieData = filteredMovieData.get(position);
    //    ─────────────────────────────────────────────────────────
    //    Récupère le film à la position demandée dans la liste filtrée.
    //    "final" : cette variable ne sera pas réassignée.
    //    (utilisé dans le listener de clic ci-dessous)

    holder.textViewName.setText(movieData.getMovieName());
    //     ─────────────────────────────────────────────
    //     holder.textViewName = le TextView du nom dans la carte
    //     .setText()          = met le texte dans ce TextView
    //     movieData.getMovieName() = "Fight Club", "Inception", etc.

    holder.textViewDate.setText(movieData.getMovieDate());
    //     ─────────────────────────────────────────────
    //     Même chose pour la date de sortie.
    //     Exemple : "1999-10-15"

    Glide.with(context)
    //    ──────────────
    //    Démarre le chargement d'image avec Glide.
    //    context = contexte Android (nécessaire pour Glide)

         .load("https://image.tmdb.org/t/p/w500" + movieData.getMovieImage())
    //   ──────────────────────────────────────────────────────────────────────
    //   .load() = URL de l'image à charger
    //
    //   "https://image.tmdb.org/t/p/w500" = URL de base du serveur d'images TMDB
    //   "w500" = largeur de l'image demandée (500 pixels)
    //   movieData.getMovieImage() = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
    //
    //   Résultat complet : "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
    //
    //   Glide télécharge l'image EN ARRIÈRE-PLAN et la met dans l'ImageView
    //   automatiquement quand c'est prêt. Pas de blocage de l'UI !

         .into(holder.movieImage);
    //   ─────────────────────────
    //   .into() = dans quelle ImageView placer l'image chargée
    //   holder.movieImage = l'ImageView de la carte courante

    holder.itemView.setOnClickListener(new View.OnClickListener() {
    //   ─────────────────────────────────────────────────────────
    //   holder.itemView = toute la carte (le CardView entier)
    //   setOnClickListener = quand l'utilisateur tape sur la carte
    //   new View.OnClickListener() = objet qui "écoute" le clic

        @Override
        public void onClick(View v) {
        // Exécuté quand l'utilisateur appuie sur la carte

            Intent intent = new Intent(context, MovieDetailActivity.class);
            //      ──────────────────────────────────────────────────────
            //      Intent = ordre de navigation entre activités.
            //      context = d'où on part (MainActivity)
            //      MovieDetailActivity.class = où on va

            intent.putExtra("movieId", movieData.getMovieId());
            //              ─────────  ────────────────────────
            //              "movieId" = la clé (nom) du paramètre qu'on envoie
            //              movieData.getMovieId() = la valeur (ex: 550)
            //
            //              C'est comme passer un argument à une fonction,
            //              mais entre deux activités différentes.
            //              Dans MovieDetailActivity, on récupère avec :
            //              getIntent().getIntExtra("movieId", -1)

            context.startActivity(intent);
            // ─────────────────────────────
            // Lance l'activité MovieDetailActivity.
            // L'écran change et affiche les détails du film.
        }
    });
}


// ═══════════════════════════════════════════════════════════════
// getItemCount — Combien d'éléments y a-t-il dans la liste ?
// ═══════════════════════════════════════════════════════════════

@Override
public int getItemCount() {
    return filteredMovieData.size();
    // ────────────────────────────────
    // Retourne le nombre de films dans la liste FILTRÉE (pas l'originale).
    // Le RecyclerView appelle cette méthode pour savoir combien de cartes créer.
    // Si on cherche "Av" et qu'il y a 3 résultats → retourne 3.
}


// ═══════════════════════════════════════════════════════════════
// LA CLASSE ViewHolder — Le conteneur des vues d'une carte
// ═══════════════════════════════════════════════════════════════

public static class ViewHolder extends RecyclerView.ViewHolder {
// ──────────────────────────────────────────────────────────────
// "static" : cette classe interne ne dépend pas d'une instance
//            de MyMovieAdapter. Elle peut exister seule.
//            Cela économise de la mémoire.
//
// "extends RecyclerView.ViewHolder" : on hérite de ViewHolder.
//            Le parent ViewHolder gère la position, l'ID, etc.

    ImageView movieImage;    // Référence à l'ImageView de la carte (la pochette)
    TextView textViewName;   // Référence au TextView du nom du film
    TextView textViewDate;   // Référence au TextView de la date

    public ViewHolder(@NonNull View itemView) {
    // ─────────────────────────────────────────
    // Constructeur : reçoit la vue de la carte (le CardView gonflé par inflate())
    // @NonNull : garantit que itemView ne peut pas être null

        super(itemView);
        // ───────────────
        // Appelle le constructeur du parent RecyclerView.ViewHolder.
        // Obligatoire. Le parent garde une référence à itemView.

        movieImage   = itemView.findViewById(R.id.imageview);
        textViewName = itemView.findViewById(R.id.textName);
        textViewDate = itemView.findViewById(R.id.textdate);
        // ──────────────────────────────────────────────────
        // findViewByid() cherche dans itemView (la carte) les vues
        // avec ces IDs définis dans activity_movie_item_list.xml.
        //
        // ✅ Ce travail est fait UNE SEULE FOIS par carte créée.
        // Ensuite, dans onBindViewHolder, on utilise directement
        // holder.textViewName (déjà trouvé, pas besoin de re-chercher).
    }
}


// ═══════════════════════════════════════════════════════════════
// LE FILTRE DE RECHERCHE — Comment ça marche ?
// ═══════════════════════════════════════════════════════════════

private Filter movieFilter = new Filter() {

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
    // ──────────────────────────────────────────────────────────────
    // S'exécute EN ARRIÈRE-PLAN (thread séparé) pour ne pas bloquer l'UI.
    // constraint = le texte tapé par l'utilisateur (ex: "av")

        List<MyMovieData> filteredList = new ArrayList<>();
        // Nouvelle liste vide qui va recevoir les résultats du filtrage.

        if (constraint == null || constraint.length() == 0) {
        // ─────────────────────────────────────────────────
        // Si le champ de recherche est vide ou null :
        // → on remet TOUS les films dans la liste

            filteredList.addAll(Arrays.asList(originalMovieData));
            // ────────────────────────────────────────────────────
            // Copie tous les films originaux dans filteredList.
            // L'utilisateur a effacé sa recherche → retour à la normale.

        } else {

            String filterPattern = constraint.toString().toLowerCase().trim();
            //     ─────────────────────────────────────────────────────────
            //     .toString()   : convertit CharSequence en String
            //     .toLowerCase(): met en minuscules pour comparaison insensible à la casse
            //                     "Avatar" et "avatar" et "AVATAR" seront tous trouvés
            //     .trim()       : supprime les espaces au début et à la fin
            //                     "  av  " devient "av"

            for (MyMovieData movie : originalMovieData) {
            // ───────────────────────────────────────────
            // On parcourt TOUS les films originaux (pas les filtrés !)
            // "for each" : pour chaque film dans le tableau original

                if (movie.getMovieName().toLowerCase().contains(filterPattern)) {
                //  ──────────────────────────────────────────────────────────────
                //  .getMovieName()  : ex "Avatar: The Way of Water"
                //  .toLowerCase()   : ex "avatar: the way of water"
                //  .contains("av")  : est-ce que le nom contient "av" ? → OUI
                //
                //  Si OUI → on ajoute ce film aux résultats

                    filteredList.add(movie);
                }
            }
        }

        FilterResults results = new FilterResults();
        results.values = filteredList;
        //               ────────────
        //               On empaquète la liste filtrée dans FilterResults.
        //               C'est le format attendu par publishResults().

        return results;
    }


    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
    // ────────────────────────────────────────────────────────────────────────────
    // S'exécute sur le THREAD PRINCIPAL après performFiltering.
    // C'est ici qu'on met à jour l'affichage.

        filteredMovieData.clear();
        // ──────────────────────
        // Vide complètement la liste affichée actuellement.

        filteredMovieData.addAll((List) results.values);
        // ───────────────────────────────────────────────
        // Ajoute tous les films filtrés.
        // (List) = cast : on dit à Java que results.values est une List.

        notifyDataSetChanged();
        // ───────────────────
        // ⚠️ IMPORTANT : prévient le RecyclerView que les données ont changé.
        // Sans ça, l'écran ne se mettrait PAS à jour malgré les nouvelles données.
        // Le RecyclerView va re-appeler onBindViewHolder pour chaque carte visible.
    }
};
```

---

## 🗺️ RÉSUMÉ VISUEL DU FLUX COMPLET

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUX COMPLET DE L'APPLICATION                    │
│                                                                     │
│  ① App démarre → onCreate()                                         │
│        │                                                            │
│        ▼                                                            │
│  ② Volley envoie GET https://api.themoviedb.org/3/movie/popular    │
│        │                                                            │
│        ▼  (quelques millisecondes plus tard)                        │
│  ③ TMDB répond avec JSON { "results": [{...}, {...}, ...] }         │
│        │                                                            │
│        ▼                                                            │
│  ④ onResponse() : parsing JSON → création de MyMovieData[]          │
│        │                                                            │
│        ▼                                                            │
│  ⑤ new MyMovieAdapter(movies) → setAdapter()                        │
│        │                                                            │
│        ▼                                                            │
│  ⑥ RecyclerView demande les cartes à l'Adapter :                    │
│     - onCreateViewHolder() : gonfle le XML de la carte             │
│     - onBindViewHolder()   : remplit la carte (textes + image)     │
│        │                                                            │
│  ⑦ Glide charge les images en arrière-plan                          │
│        │                                                            │
│  ⑧ L'utilisateur tape dans la barre → TextWatcher → filter()       │
│     → performFiltering() → publishResults() → notifyDataSetChanged()│
│        │                                                            │
│  ⑨ L'utilisateur clique une carte → Intent → MovieDetailActivity   │
└─────────────────────────────────────────────────────────────────────┘
```


--------------------------------------------------
----------------------------
----------------------------------
================================================

# 📱 Documentation Complète — MoviesApp (Android)
> Préparé par : Pr. OUHMIDA Asmae  
> Objectif : Comprendre chaque fichier, chaque classe, chaque ligne de code du projet.

---

## 📁 Structure Générale du Projet

```
MoviesApp/
│
├── app/src/main/
│   ├── java/net/ouhmida/testquizappall/
│   │   ├── MainActivity.java          ← Activité principale (liste des films)
│   │   ├── MovieDetailActivity.java   ← Détails d'un film + carte + trailer
│   │   ├── MyMovieAdapter.java        ← Adaptateur RecyclerView
│   │   ├── MyMovieData.java           ← Modèle de données (objet Film)
│   │   ├── VideoPlayer.java           ← Lecteur vidéo (trailer YouTube)
│   │   └── movie_item_list.java       ← Activité item (layout seul)
│   │
│   ├── res/layout/
│   │   ├── activity_main.xml          ← Layout de la liste principale
│   │   ├── activity_movie_item_list.xml ← Layout d'une carte film
│   │   ├── activity_movie_detail.xml  ← Layout des détails d'un film
│   │   └── activity_video_player.xml  ← Layout du lecteur vidéo
│   │
│   └── AndroidManifest.xml            ← Configuration globale de l'app
│
└── build.gradle                       ← Dépendances du projet
```

---

## 🔧 build.gradle — Les Dépendances

```gradle
dependencies {
    implementation ("com.google.android.gms:play-services-maps:17.0.0")
    implementation ("com.google.android.gms:play-services-location:17.0.0")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.android.volley:volley:1.2.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
}
```

| Bibliothèque | Rôle |
|---|---|
| `play-services-maps` | Afficher une Google Map dans l'application |
| `play-services-location` | Accéder à la position GPS de l'utilisateur |
| `play-services-vision` | Fonctionnalités de vision par caméra (QR, visages…) |
| `volley` | Faire des requêtes HTTP vers l'API TMDB |
| `glide` | Charger et afficher des images depuis une URL |
| `glide:compiler` | Processeur d'annotations nécessaire à Glide |
| `exoplayer-core` | Lire des vidéos (inclus mais la lecture se fait via WebView ici) |

---

## 📄 AndroidManifest.xml — Permissions & Configuration

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="your_API_key" />
```

| Permission | Pourquoi elle est nécessaire |
|---|---|
| `INTERNET` | Pour appeler l'API TMDB et charger les images |
| `ACCESS_NETWORK_STATE` | Vérifier si une connexion réseau est disponible |
| `ACCESS_FINE_LOCATION` | Obtenir la position GPS précise de l'utilisateur |
| `ACCESS_COARSE_LOCATION` | Obtenir une position approximative (réseau mobile/WiFi) |
| `ACCESS_BACKGROUND_LOCATION` | Accéder à la position même quand l'app est en arrière-plan |
| `CAMERA` | Accès à la caméra (prévu pour d'éventuelles fonctionnalités) |
| `geo.API_KEY` | Clé API Google Maps pour afficher la carte dans le détail du film |

---

## 🗂️ Les Layouts XML

---

### 1. `activity_main.xml` — Écran Principal

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:background="#BDBDBD"
    android:layout_height="match_parent"
    android:descendantFocusability="beforeDescendants">
```

| Attribut | Valeur | Explication |
|---|---|---|
| `layout_width` | `match_parent` | S'étire sur toute la largeur de l'écran |
| `orientation` | `vertical` | Les enfants sont empilés du haut vers le bas |
| `background` | `#BDBDBD` | Fond gris clair |
| `layout_height` | `match_parent` | Occupe toute la hauteur de l'écran |
| `descendantFocusability` | `beforeDescendants` | Le LinearLayout reçoit le focus avant ses enfants (évite l'auto-focus sur l'EditText) |

```xml
<EditText
    android:id="@+id/editTextSearch"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Search"
    android:inputType="text"/>
```

| Attribut | Explication |
|---|---|
| `id="editTextSearch"` | Identifiant pour retrouver ce champ dans le code Java |
| `layout_height="wrap_content"` | La hauteur s'adapte au contenu (pas plus grand que nécessaire) |
| `hint="Search"` | Texte grisé affiché quand le champ est vide |
| `inputType="text"` | Le clavier affiché est le clavier texte standard |

```xml
<Button
    android:id="@+id/buttonSearch"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Search"/>
```

> Bouton de recherche. `text="Search"` est l'étiquette affichée. Ce bouton n'est pas utilisé dans le code Java final (la recherche se fait en temps réel via `TextWatcher`), mais il est présent dans le layout.

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerView"
    android:layout_margin="2dp"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:focusableInTouchMode="true"/>
```

| Attribut | Explication |
|---|---|
| `RecyclerView` | Vue en liste qui réutilise les cases (plus performant que ListView) |
| `layout_margin="2dp"` | Marge de 2dp tout autour de la liste |
| `focusableInTouchMode="true"` | Permet à la liste de capturer le focus au toucher |

---

### 2. `activity_movie_item_list.xml` — Carte d'un Film

Ce layout représente **une seule carte film** dans la liste.

```xml
<androidx.cardview.widget.CardView
    app:cardElevation="10dp"
    app:cardCornerRadius="10dp"
    android:layout_margin="5dp"
    app:cardBackgroundColor="#FFFFFF"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
```

| Attribut | Explication |
|---|---|
| `cardElevation="10dp"` | Ombre portée sous la carte (effet de profondeur) |
| `cardCornerRadius="10dp"` | Coins arrondis de la carte |
| `layout_margin="5dp"` | Espace entre les cartes dans la liste |
| `cardBackgroundColor="#FFFFFF"` | Fond blanc pour la carte |

```xml
<ImageView
    android:id="@+id/imageview"
    android:layout_width="120dp"
    android:layout_height="150dp"/>
```

> Affiche la **pochette du film**. Dimensions fixes : 120×150dp. L'image sera chargée par Glide depuis l'URL de TMDB.

```xml
<TextView android:id="@+id/textName" .../>   <!-- Nom du film -->
<TextView android:id="@+id/textdate" .../>   <!-- Date de sortie -->
```

---

### 3. `activity_movie_detail.xml` — Détails d'un Film

Reprend le même layout carte que ci-dessus, mais ajoute :

```xml
<Button
    android:id="@+id/playButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Play Movie"
    android:layout_marginTop="16dp"/>
```

> Bouton qui lance le lecteur de trailer YouTube. `layout_marginTop="16dp"` ajoute de l'espace au-dessus.

```xml
<fragment
    android:id="@+id/map"
    android:name="com.google.android.gms.maps.SupportMapFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginTop="90dp" />
```

| Attribut | Explication |
|---|---|
| `android:name` | Indique quelle classe de fragment utiliser → ici Google Maps |
| `layout_marginTop="90dp"` | Décale la carte vers le bas pour ne pas la superposer au bouton |
| `layout_height="match_parent"` | La carte occupe tout l'espace restant |

---

### 4. `activity_video_player.xml` — Lecteur Vidéo

```xml
<WebView
    android:id="@+id/webView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

> Un `WebView` est un navigateur web intégré dans l'app. Ici il est utilisé pour **charger et afficher un trailer YouTube** via une URL `youtube.com/embed/...`. Il occupe tout l'écran (`match_parent` en largeur et hauteur).

---

## ☕ Les Fichiers Java

---

### 1. `MyMovieData.java` — Le Modèle de Données

C'est la classe **objet Film**. Elle définit ce qu'est un film dans l'application.

```java
public class MyMovieData {
    private String movieName;       // Titre du film
    private String movieDate;       // Date de sortie
    private String movieImage;      // Chemin de l'image (poster_path)
    private String movieDescription;// Description (non utilisée dans le constructeur)
    private int movieId;            // Identifiant unique TMDB
```

**Le constructeur :**
```java
public MyMovieData(int movieId, String movieName, String movieDate, String movieImage) {
    this.movieName = movieName;
    this.movieDate = movieDate;
    this.movieImage = movieImage;
    this.movieId = movieId;
}
```

> `this.X = X` : le mot-clé `this` désigne l'objet courant. On assigne les valeurs reçues en paramètre aux attributs de la classe.

**Les Getters :** (méthodes pour lire les attributs depuis l'extérieur)
```java
public int getMovieId()            { return movieId; }
public String getMovieName()       { return movieName; }
public String getMovieDate()       { return movieDate; }
public String getMovieImage()      { return movieImage; }
public String getMovieDescription(){ return movieDescription; }
```

> `private` sur les attributs = encapsulation. On ne peut y accéder qu'à travers les getters.

---

### 2. `MainActivity.java` — Activité Principale

**Déclarations :**
```java
private static final String TMDB_API_KEY = "your api key";
private static final String BASE_URL = "https://api.themoviedb.org/3/movie/popular";
private static final String TAG = "MainActivity";
```

| Variable | Rôle |
|---|---|
| `TMDB_API_KEY` | Clé d'accès à l'API TMDB (à remplacer par votre vraie clé) |
| `BASE_URL` | URL de base pour récupérer les films populaires |
| `TAG` | Étiquette utilisée dans les logs `Log.e(TAG, ...)` pour identifier la source |

**Dans `onCreate()` :**

```java
recyclerView.setHasFixedSize(true);
```
> Optimisation : indique que la taille du RecyclerView ne change pas selon son contenu.

```java
recyclerView.setLayoutManager(new LinearLayoutManager(this));
```
> Définit comment les éléments sont arrangés → en **liste verticale** (LinearLayoutManager).

**La requête Volley :**
```java
RequestQueue queue = Volley.newRequestQueue(this);
String url = BASE_URL + "?api_key=" + TMDB_API_KEY;

JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
    Request.Method.GET,   // Méthode HTTP : GET (lecture)
    url,                  // URL cible
    null,                 // Corps de la requête (null car c'est un GET)
    new Response.Listener<JSONObject>() {   // Callback succès
        @Override
        public void onResponse(JSONObject response) { ... }
    },
    new Response.ErrorListener() {          // Callback erreur
        @Override
        public void onErrorResponse(VolleyError error) { ... }
    }
);
queue.add(jsonObjectRequest); // Envoi de la requête
```

**Parsing du JSON retourné par TMDB :**
```java
JSONArray results = response.getJSONArray("results"); // Tableau des films
MyMovieData[] movies = new MyMovieData[results.length()];

for (int i = 0; i < results.length(); i++) {
    JSONObject movieObject = results.getJSONObject(i);
    int id           = movieObject.getInt("id");
    String title     = movieObject.getString("title");
    String releaseDate = movieObject.getString("release_date");
    String imageUrl  = movieObject.getString("poster_path");

    movies[i] = new MyMovieData(id, title, releaseDate, imageUrl);
}
```

> L'API TMDB retourne un JSON. On extrait le tableau `results`, puis pour chaque film on lit ses champs : `id`, `title`, `release_date`, `poster_path`.

**La recherche en temps réel (TextWatcher) :**
```java
searchEditText.addTextChangedListener(new TextWatcher() {

    @Override
    public void beforeTextChanged(...) { /* Rien à faire */ }

    @Override
    public void onTextChanged(CharSequence s, ...) {
        // Appelé à chaque frappe clavier
        if (myMovieAdapter != null) {
            myMovieAdapter.getFilter().filter(s); // Filtre la liste
        }
    }

    @Override
    public void afterTextChanged(...) { /* Rien à faire */ }
});
```

> `TextWatcher` écoute les changements dans l'EditText. `onTextChanged` est appelé à chaque caractère saisi. Le filtre de l'adaptateur est alors déclenché.

---

### 3. `MyMovieAdapter.java` — L'Adaptateur RecyclerView

Le rôle de l'adaptateur est de **faire le lien entre les données (MyMovieData[]) et les vues (les cartes XML)**.

**Attributs :**
```java
private MyMovieData[] originalMovieData; // Liste complète d'origine (pour le reset du filtre)
private List<MyMovieData> filteredMovieData; // Liste filtrée (affichée)
private Context context;                  // Contexte Android (pour lancer des intents)
```

**Constructeur :**
```java
public MyMovieAdapter(MyMovieData[] myMovieData, Context context) {
    this.originalMovieData = myMovieData;
    this.filteredMovieData = new ArrayList<>(Arrays.asList(myMovieData));
    this.context = context;
}
```

> On garde l'original intact pour pouvoir réafficher tous les films quand la recherche est effacée.

**`onCreateViewHolder` :**
```java
View view = layoutInflater.inflate(R.layout.activity_movie_item_list, parent, false);
return new ViewHolder(view);
```

> Gonfle (crée) le layout XML d'une carte film et retourne un ViewHolder qui contient les vues.

**`onBindViewHolder` :**
```java
final MyMovieData movieData = filteredMovieData.get(position);
holder.textViewName.setText(movieData.getMovieName());
holder.textViewDate.setText(movieData.getMovieDate());

Glide.with(context)
     .load("https://image.tmdb.org/t/p/w500" + movieData.getMovieImage())
     .into(holder.movieImage);
```

> Pour chaque position de la liste :
> - On récupère le film correspondant
> - On remplit le nom et la date
> - **Glide** charge l'image depuis l'URL complète : `https://image.tmdb.org/t/p/w500` + le chemin `poster_path`

**Clic sur une carte :**
```java
holder.itemView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra("movieId", movieData.getMovieId()); // Transmet l'ID du film
        context.startActivity(intent); // Lance l'activité détail
    }
});
```

> `putExtra("movieId", ...)` : on envoie l'identifiant du film à l'activité suivante via l'`Intent`.

**La classe ViewHolder :**
```java
public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView movieImage;
    TextView textViewName;
    TextView textViewDate;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        movieImage    = itemView.findViewById(R.id.imageview);
        textViewName  = itemView.findViewById(R.id.textName);
        textViewDate  = itemView.findViewById(R.id.textdate);
    }
}
```

> Le ViewHolder garde des références aux vues pour éviter d'appeler `findViewById` à chaque défilement (optimisation clé du RecyclerView).

**Le Filtre de recherche :**
```java
private Filter movieFilter = new Filter() {

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        List<MyMovieData> filteredList = new ArrayList<>();

        if (constraint == null || constraint.length() == 0) {
            // Pas de texte → on remet tous les films
            filteredList.addAll(Arrays.asList(originalMovieData));
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();
            for (MyMovieData movie : originalMovieData) {
                // On garde les films dont le nom contient le texte recherché
                if (movie.getMovieName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(movie);
                }
            }
        }

        FilterResults results = new FilterResults();
        results.values = filteredList;
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        filteredMovieData.clear();
        filteredMovieData.addAll((List) results.values);
        notifyDataSetChanged(); // Met à jour l'affichage du RecyclerView
    }
};
```

> `performFiltering` s'exécute en arrière-plan. `publishResults` met à jour la liste sur le thread principal.

---

### 4. `MovieDetailActivity.java` — Détails du Film

**Récupération de l'ID transmis par l'Intent :**
```java
int movieId = getIntent().getIntExtra("movieId", -1);
// -1 est la valeur par défaut si "movieId" n'existe pas dans l'Intent
if (movieId != -1) {
    fetchMovieDetails(movieId);
}
```

**Ajout d'un marqueur de cinéma :**
```java
cinemaLocations.add(new LatLng(33.596460, -7.615480));
// Coordonnées GPS : Casablanca, Maroc
```

**`fetchMovieDetails(int movieId)` — Deux requêtes simultanées :**

*Requête 1 : Détails du film*
```
URL : https://api.themoviedb.org/3/movie/{id}?api_key=...
```
```java
String movieName        = response.getString("title");
String movieDescription = response.getString("overview");
String imageUrl = "https://image.tmdb.org/t/p/w500" + response.getString("poster_path");

Name.setText(movieName);
descriptionTextView.setText(movieDescription);
Glide.with(MovieDetailActivity.this).load(imageUrl).into(img);
```

*Requête 2 : Vidéos du film (trailer)*
```
URL : https://api.themoviedb.org/3/movie/{id}/videos?api_key=...
```
```java
JSONArray results = response.getJSONArray("results");
for (int i = 0; i < results.length(); i++) {
    JSONObject video = results.getJSONObject(i);
    if (video.getString("type").equals("Trailer")) {
        trailerKey = video.getString("key"); // Exemple : "dQw4w9WgXcQ"
        break; // On s'arrête au premier trailer trouvé
    }
}
```

> `trailerKey` est la clé YouTube du trailer (la partie après `youtube.com/watch?v=`).

**Lecture du trailer :**
```java
private void playTrailer() {
    if (trailerKey != null && !trailerKey.isEmpty()) {
        String trailerUrl = "https://www.youtube.com/embed/" + trailerKey;
        Intent intent = new Intent(MovieDetailActivity.this, VideoPlayer.class);
        intent.putExtra("videoUrl", trailerUrl);
        startActivity(intent);
    } else {
        Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
    }
}
```

**Gestion de la carte Google Maps :**

```java
@Override
public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    if (/* permission accordée */) {
        mMap.setMyLocationEnabled(true);         // Bouton "ma position" sur la carte
        addCinemaMarker(new LatLng(33.596460, -7.615480)); // Marqueur cinéma
        moveToCurrentLocation();                 // Centre la carte sur l'utilisateur
    } else {
        // Demande la permission à l'utilisateur
        ActivityCompat.requestPermissions(...);
    }
}
```

```java
private void addCinemaMarker(LatLng cinemaLocation) {
    mMap.addMarker(new MarkerOptions()
        .position(cinemaLocation)    // Position GPS du marqueur
        .title("Cinema")             // Titre affiché au tap
        .snippet("Location of the cinema")); // Sous-titre
}
```

```java
private void moveToCurrentLocation() {
    Location location = locationManager
        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

    if (location != null) {
        LatLng currentLocation = new LatLng(
            location.getLatitude(),
            location.getLongitude()
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // zoom 15 = vue quartier (1=monde, 21=bâtiment)
    }
}
```

**Gestion du résultat de la demande de permission :**
```java
@Override
public void onRequestPermissionsResult(int requestCode, ...) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
        if (/* permission accordée */) {
            moveToCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", ...).show();
        }
    }
}
```

---

### 5. `VideoPlayer.java` — Lecteur de Trailer

```java
videoUrl = getIntent().getStringExtra("videoUrl"); // Récupère l'URL YouTube embed

webView = findViewById(R.id.webView);
webView.getSettings().setJavaScriptEnabled(true); // OBLIGATOIRE pour que YouTube fonctionne
webView.loadUrl(videoUrl);                         // Charge la page YouTube dans le WebView
```

> Sans `setJavaScriptEnabled(true)`, le player YouTube ne s'affichera pas car il utilise JavaScript.

```java
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (webView != null) {
        webView.loadUrl(videoUrl); // Recharge la vidéo si l'orientation change
    }
}
```

> Quand l'utilisateur fait pivoter son téléphone, la vidéo est rechargée pour s'adapter à la nouvelle orientation.

---

## 🔄 Flux de Navigation de l'Application

```
┌─────────────────────────────┐
│        MainActivity         │
│  - Appel API TMDB popular   │
│  - Affichage RecyclerView   │
│  - Barre de recherche       │
└────────────┬────────────────┘
             │ Clic sur un film
             ▼
┌─────────────────────────────┐
│     MovieDetailActivity     │
│  - Appel API détails film   │
│  - Appel API vidéos film    │
│  - Affichage image + desc.  │
│  - Carte Google Maps        │
│  - Bouton "Play Movie"      │
└────────────┬────────────────┘
             │ Clic sur Play Movie
             ▼
┌─────────────────────────────┐
│        VideoPlayer          │
│  - WebView YouTube embed    │
│  - Lecture du trailer       │
└─────────────────────────────┘
```

---

## 🌐 L'API TMDB — Résumé des Endpoints Utilisés

| Endpoint | Description |
|---|---|
| `GET /movie/popular?api_key=...` | Liste des films populaires |
| `GET /movie/{id}?api_key=...` | Détails d'un film spécifique |
| `GET /movie/{id}/videos?api_key=...` | Vidéos (trailers) d'un film |
| `https://image.tmdb.org/t/p/w500{poster_path}` | URL complète de l'affiche du film |

---

## ⚠️ Points Importants à Retenir

1. **Remplacer les clés API** : `"your api key"` dans `MainActivity.java`, `MovieDetailActivity.java` et `AndroidManifest.xml` doit être remplacé par une vraie clé.

2. **Volley vs Retrofit** : Volley est plus simple pour des requêtes basiques. Retrofit est préféré en production.

3. **Glide** charge les images de façon **asynchrone** → l'UI ne se bloque pas.

4. **Le filtre** travaille sur `originalMovieData` (jamais modifié) et met à jour `filteredMovieData` (affichée).

5. **`poster_path`** retourné par TMDB est un chemin relatif (ex: `/abc123.jpg`). Il faut lui ajouter le préfixe `https://image.tmdb.org/t/p/w500` pour obtenir l'URL complète.

6. **`trailerKey`** est la clé YouTube (ex: `dQw4w9WgXcQ`). L'URL embed est : `https://www.youtube.com/embed/{key}`.
