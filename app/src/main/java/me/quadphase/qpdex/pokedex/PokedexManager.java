package me.quadphase.qpdex.pokedex;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import me.quadphase.qpdex.databaseAccess.PokemonFactory;
import me.quadphase.qpdex.pokemon.Ability;
import me.quadphase.qpdex.pokemon.EggGroup;
import me.quadphase.qpdex.pokemon.Evolution;
import me.quadphase.qpdex.pokemon.MinimalPokemon;
import me.quadphase.qpdex.pokemon.Move;
import me.quadphase.qpdex.pokemon.Pokemon;
import me.quadphase.qpdex.pokemon.Type;

/**
 * High level manager of all Pokemon related information
 * This class is a singleton that should be use when interaction is needed with activities.
 */
public class PokedexManager {

    /**
     * Internal class to generate a fail-safe Pokemon.
     * Can be used to check for other errors in logic or in application UI.
     * If this is ever displayed, then there's a problem that needs to be fixed
     */
    public class MissingNo extends Pokemon{
        public MissingNo(){
            super(
                    -1,                  // pokemonUniqueID,
                    0,                   // pokemonNationalID,
                    "MissingNo.",        // name,
                    "It is arguably the best known glitch Pokémon, closely followed by 'M (00) and it is the easiest glitch Pokémon to find in the localizations. It has five distinct forms, but the most frequent forms (that being the Red/Blue and Yellow normal forms) share 36 index numbers each.\n" +
                            "\n" +
                            "In later generations, other glitch Pokémon are sometimes referred to as \"a Missingno.\", such as ??????????, ?, and -----. Despite this, the name \"Missingno.\" is a misnomer in this case; they have little relation to the one found in Pokémon Red and Blue or Yellow. ",// description,
                    3.3,                 // height in Meters,
                    1590.8,              // weight in Kilograms,
                    136,                 // attack,
                    0,                   // defence,
                    33,                  // hp,
                    3,                   // spAttack,
                    3,                   // spDefence,
                    29,                  // speed,
                    false,               // caught,
                    1,                   // genFirstAppeared,
                    0,                   // hatchTime,
                    29,                  // catchRate,
                    -1,                  // genderRatioMale,
                    null,                // locations,
                    null,                // abilities,
                    null,                // moves,
                    Arrays.asList(       //types
                            new Type("Bird", 0),
                            new Type("Normal", 13)),
                    null,                // eggGroups,
                    null                 // evolutions,
            );
        }

        public MinimalPokemon minimal(){
            return new MinimalPokemon(getPokemonNationalID(), super.getName(), super.getDescription(), super.getTypes(), false);
        }

    }

    private class M00 extends Pokemon{ //Secret Class for UI testing.
        Random rng;
        public M00(){
            super(
                    -1,                  // pokemonUniqueID,
                    0,                   // pokemonNationalID,
                    "'M(00)",        // name,
                    "Often called the \"sister\" glitch counterpart to Missingno. due to having the same sprite and Pokédex number, and is found exclusively in Pokémon Red and Blue. If RBGlitchName00.png is traded to Pokémon Yellow, it will become a 3TrainerPoké $.\n" +
                            "\n" +
                            "Although similar to Missingno. at first glance, the two are separate glitch Pokémon with many differences as they have different index numbers; for example, RBGlitchName00.png can evolve into Kangaskhan while Missingno. cannot.",
                            // description,
                    7,                 // height in Meters,
                    399.4,              // weight in Kilograms,
                    0,                 // attack,
                    0,                   // defence,
                    0,                  // hp,
                    0,                   // spAttack,
                    0,                   // spDefence,
                    0,                  // speed,
                    false,               // caught,
                    1,                   // genFirstAppeared,
                    1999,                   // hatchTime,
                    29,                  // catchRate,
                    -1,                  // genderRatioMale,
                    null,                // locations,
                    Arrays.asList(// abilities,
                            new Ability("Glitch Master","Can corrupt anything in its path")),
                    Arrays.asList(// moves,
                            new Move("Water Gun","",0,0,0,"op",1,"",new Type("water", 18)),
                            new Move("Water Gun","",0,0,0,"op",1,"",new Type("water", 18)),
                            new Move("Sky Attack","",0,0,0,"op",1,"",new Type("Flying", 8))  ),
                    Arrays.asList(       //types
                            new Type("Bird", 0),
                            new Type("Normal", 13)),
                    Arrays.asList(new EggGroup("glitch")),                // eggGroups,
                    null// evolutions,
            );
            rng = new Random();
            rng.setSeed(System.nanoTime());
        }

        public MinimalPokemon minimal(){
            return new MinimalPokemon(getPokemonNationalID(), super.getName(), super.getDescription(), super.getTypes(), false);
        }

        @Override
        public int retrieveStatFromString(String specific){
            return rng.nextInt(200);
        }

    }

    //Entity variables that describe inner state and function
    private static PokedexManager instance=null;
    private static CentralAudioPlayer jukebox=null;
    private static PokemonFactory pkmnBuild=null;
    private boolean isMinimalReady=false;
    private boolean isReady=false;
    private boolean isDetailed=false;


    //Variables for context to handle global application state
    /**
     * The Generation when this Pokedex was updated/compiled
     */
    public static final int latestGeneration = 6;
    /**
     * Instance of the fail-safe class
     */
    public final MissingNo missingNo;

    private final M00 TrainerPoke$;

    private int maxPokemonNationalID = 721;

    private int restrictUpToGeneration = latestGeneration; //In practice, no restriction.

    private int currentPokemonNationalID = 0;

    private Pokemon currentDetailedPokemon;

    private MinimalPokemon currentMinimalPokemon;

    private BitmapDrawable currentOverviewSprite;

    private ArrayList<BitmapDrawable> allOverviewSprites;

    private BitmapDrawable currentMinimalType1;

    private BitmapDrawable currentMinimalType2;

    private BitmapDrawable currentDetailedType1;

    private BitmapDrawable currentDetailedType2;

    //Collections to assist the Pokedex display

    private Type[] allValidTypes;

    private MinimalPokemon[] allMinimalPokemon;

    private InputStream[] allMiniSprites;

    //private ArrayAdapter<MinimalPokemon> pokedexList; //Should probably stay with PokedexActivity class

    //Cache variables
    //private int cacheSize;
    //private HashMap<String,Pokemon> cachedDetailedPokemon; //Tentative, use an LRUHashMap
    private List<BitmapDrawable> cachedDisplaySprites;

    //Methods

    //Singleton Constructor
    protected PokedexManager(){
        jukebox = CentralAudioPlayer.getInstance();

        missingNo = new MissingNo();
        TrainerPoke$ = new M00();

        isReady = false;

        currentMinimalPokemon = missingNo.minimal();
        currentDetailedPokemon = TrainerPoke$;
    }

    /**
     * Retrieve the Singleton Instance
     */
    public static PokedexManager getInstance(){
        if(instance==null){
            instance = new PokedexManager();
        }
        return instance;
    }

    /**
     * Change the currently selected Pokemon in the Pokedex and send a message to update all classes
     * This will also store a reference to the assets the Pokemon with the National ID is associated with.
     * @param nationalID The ID of the minimal pokemon to build {@link PokedexArrayAdapter}
     * @param currentContext The context in which the update occurs (usually, "this" within an Activity)
     */
    public void updatePokedexSelection(int nationalID, final Context currentContext, boolean prefetch){
        if(nationalID>=0 && nationalID<=maxPokemonNationalID)
            this.updatePokedexSelection(allMinimalPokemon[nationalID],currentContext,prefetch);
        else
            Log.e("QPDEX_Manager",String.format("Sent invalid National ID %s with update. Manager has stopped update",nationalID));
    }

    /**
     * Change the currently selected Pokemon in the Pokedex and send a message to update all classes
     * This will also store a reference to the assets the Pokemon with the National ID is associated with.
     * @param pokedexSelection The minimal pokemon, preferrably from the {@link PokedexArrayAdapter}
     * @param currentContext The context in which the update occurs (usually, "this" within an Activity)
     */
    public void updatePokedexSelection(MinimalPokemon pokedexSelection, final Context currentContext, boolean prefetch){

        if(pkmnBuild==null){
            pkmnBuild = PokemonFactory.getPokemonFactory(currentContext);
        }

        isDetailed = false;
        currentMinimalPokemon = pokedexSelection;
        currentPokemonNationalID = pokedexSelection.getPokemonNationalID();

        //Update Media Controller
        jukebox.updateInstance(currentPokemonNationalID, PokedexAssetFactory.getPokemonCry(currentContext, currentPokemonNationalID));

        //Update Graphics Assets
        //Load Sprite
        currentOverviewSprite = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getPokemonSpriteInGeneration(currentContext,currentPokemonNationalID,restrictUpToGeneration));

        //Load first type
        currentMinimalType1 = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getTypeBadge(currentContext, pokedexSelection.getTypes().get(0).getName()));
        if(pokedexSelection.getTypes().size()>1) {
            //Load second type (if any)
            currentMinimalType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext, pokedexSelection.getTypes().get(1).getName()));
        }
        else{
            currentMinimalType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext,"empty"));
        }

        //The description to be heard is generally set on PokedexManager
        // The reason for this is to avoid leaking/misusing the TTSController resources.

        //TODO: Spawning off a thread may delay critical functionality to prioritize pre-fetching
        //  this method is called, primarily, from PokedexActivty. Setting the fully detailed pokemon
        //  should probably be delayed until the intent to open up the DetailedPokemonActivity has been
        //  fired.
        if (prefetch) {
            Log.d("QPDEX_Manager",String.format("Updating to %s",currentPokemonNationalID));
            Thread buildInDetail = new Thread(){
                @Override
                public void run(){
                    int nationalID= currentMinimalPokemon.getPokemonNationalID();
                    if (nationalID>0){
                            updatePokedexSelection(
                                    pkmnBuild.getPokemonByNationalID(nationalID),
                                    currentContext);
                    }
                    else{ //If the Manager isn't ready or we had an unexpected ID, then display MissingNo!!!
                        updatePokedexSelection(missingNo, currentContext);
                    }
                }
            };
            buildInDetail.run();
        }
    }

    /**
     * Change the currently selected Pokemon in the Pokedex, mainly in the DetailedPokemonActivty
     * This will also store a reference to the assets the Pokemon with the National ID is associated with.
     * @param detailedPokemon The fully detailed pokemon. Should be given from a Pokemon's evolution list
     * @param currentContext The context in which the update occurs (usually, "this" within an Activity)
     */
    public void updatePokedexSelection(Pokemon detailedPokemon, final Context currentContext){
        //For now, it just sets this variable. Later it should probably do more in terms of assets
        currentDetailedPokemon = detailedPokemon;

        Log.d("QPDEX_Manager",String.format("Updating detail to %s",detailedPokemon.getPokemonNationalID()));

        //Load Sprite
        currentOverviewSprite = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getPokemonSpriteInGeneration(
                        currentContext,currentDetailedPokemon.getPokemonNationalID(),restrictUpToGeneration));

        //Load first type
        currentDetailedType1 = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getTypeBadge(currentContext, detailedPokemon.getTypes().get(0).getName()));
        if(detailedPokemon.getTypes().size()>1) {
            //Load second type (if any)
            currentDetailedType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext, detailedPokemon.getTypes().get(1).getName()));
        }
        else{
            currentDetailedType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext,"empty"));
        }

        //Spawn a thread to collect overview sprites
        Thread bitmapRetrieve = new Thread(){
            @Override
            public void run(){
                cachedDisplaySprites = new LinkedList<BitmapDrawable>();
                for (int i = restrictUpToGeneration; i >=currentDetailedPokemon.getGenFirstAppeared(); i--) {
                    InputStream file = PokedexAssetFactory.getPokemonSpriteInGeneration(
                            currentContext,
                            currentDetailedPokemon.getPokemonNationalID(),
                            i);
                    BitmapDrawable sprite = new BitmapDrawable(
                            currentContext.getResources(),
                            file
                    );
                    if(file!=null)
                        cachedDisplaySprites.add(sprite);
                }
            }
        };
        bitmapRetrieve.start();

        //TODO: Load form specific overview sprite (if any)

    }

    /**
     * Change the currently selected Pokemon in the Pokedex, mainly in the DetailedPokemonActivty
     * This will also store a reference to the assets the Pokemon with the National ID is associated with.
     * @param uniquePokemonID The uniquePokemon ID. Should be given from a Pokemon's evolution list
     * @param currentContext The context in which the update occurs (usually, "this" within an Activity)
     */
    public void updatePokedexSelection(int uniquePokemonID, final Context currentContext){
        //For now, it just sets this variable. Later it should probably do more in terms of assets
        currentDetailedPokemon = pkmnBuild.getPokemonByPokemonID(uniquePokemonID);

        Log.d("QPDEX_Manager",String.format("Updating detail to %s",currentDetailedPokemon.getPokemonNationalID()));

        //Load Sprite
        currentOverviewSprite = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getPokemonSpriteInGeneration(
                        currentContext,currentDetailedPokemon.getPokemonNationalID(),restrictUpToGeneration));

        //Load first type
        currentDetailedType1 = new BitmapDrawable(currentContext.getResources(),
                PokedexAssetFactory.getTypeBadge(currentContext, currentDetailedPokemon.getTypes().get(0).getName()));
        if(currentDetailedPokemon.getTypes().size()>1) {
            //Load second type (if any)
            currentDetailedType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext, currentDetailedPokemon.getTypes().get(1).getName()));
        }
        else{
            currentDetailedType2 = new BitmapDrawable(currentContext.getResources(),
                    PokedexAssetFactory.getTypeBadge(currentContext,"empty"));
        }

        //Spawn a thread to collect overview sprites
        Thread bitmapRetrieve = new Thread(){
            @Override
            public void run(){
                cachedDisplaySprites = new LinkedList<BitmapDrawable>();
                for (int i = restrictUpToGeneration; i >=currentDetailedPokemon.getGenFirstAppeared(); i--) {
                    InputStream file = PokedexAssetFactory.getPokemonSpriteInGeneration(
                            currentContext,
                            currentDetailedPokemon.getPokemonNationalID(),
                            i);
                    BitmapDrawable sprite = new BitmapDrawable(
                            currentContext.getResources(),
                            file
                    );
                    if(file!=null)
                        cachedDisplaySprites.add(sprite);
                }
            }
        };
        bitmapRetrieve.start();

        //TODO: Load form specific overview sprite (if any)

    }

    //Getters and Setters
    /**
     * Check if the object is ready and can be called
     */
    public boolean isReady() {
        return isReady;
    }

    public boolean isMinimalReady(){
        return isMinimalReady;
    }

    public int getMaxPokemonNationalID(){
        return maxPokemonNationalID;
    }

    /**
     * Retrieve a reference to the MinimalPokemon currently loaded
     */
    public MinimalPokemon getCurrentMinimalPokemon() {
        return currentMinimalPokemon;
    }
    /**
     * Retrieve a reference to the MinimalPokemon currently loaded
     */
    public Pokemon getCurrentDetailedPokemon() {
        return currentDetailedPokemon;
    }
    /**
     * Get the currentMinimalPokemon's Sprite
     */
    public BitmapDrawable getSelectionOverviewSprite(){
        return currentOverviewSprite;
    }
    /**
     * Get the currentMinimalPokemon's 1st Type image badge
     */
    public BitmapDrawable getCurrentMinimalType1() {
        return currentMinimalType1;
    }
    /**
     * Get the currentMinimalPokemon's 2nd Type image badge. May return a transparent image if no 2nd type
     */
    public BitmapDrawable getCurrentMinimalType2() {
        return currentMinimalType2;
    }
    /**
     * Get the currentMinimalPokemon's 1st Type image badge
     */
    public BitmapDrawable getCurrentDetailedType1() {
        return currentDetailedType1;
    }
    /**
     * Get the currentMinimalPokemon's 2nd Type image badge. May return a transparent image if no 2nd type
     */
    public BitmapDrawable getCurrentDetailedType2() {
        return currentDetailedType2;
    }
    /**
     * Get the list of all Sprites valid for the current pokemon.
     */
    public List<BitmapDrawable> getAllDetailedPokemonSprites() {
        return cachedDisplaySprites;
    }

    public void beginCachingRoutines(final Context currentContext){

        // Spawn off a separate thread to avoid clogging up the caller thread
        Thread initMin = new Thread(){
            @Override
            public void run(){
                //First ask for the instance
                pkmnBuild = PokemonFactory.getPokemonFactory(currentContext);
                long startTime = System.nanoTime();
                allMinimalPokemon = pkmnBuild.getAllMinimalPokemon();
                long total = System.nanoTime()-startTime; //Consider removing on first release
                Log.w("QPDEX_Manager",String.format("Initialization took %s nanoseconds",total));

                maxPokemonNationalID = allMinimalPokemon.length;

                isMinimalReady=true;

                Log.d("QPDEX_Manager","All Minimal Pokemon Objects Created");
            }
        };

        // Start with the initMin thread
        initMin.start();
        initMin.setPriority(Thread.MAX_PRIORITY);


    }


}
