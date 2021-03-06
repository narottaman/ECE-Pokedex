package me.quadphase.qpdex.databaseAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.lang.Enum;
import java.util.Queue;

import me.quadphase.qpdex.exceptions.PartyFullException;
import me.quadphase.qpdex.pokedex.PokedexManager;
import me.quadphase.qpdex.pokemon.Ability;
import me.quadphase.qpdex.pokemon.EggGroup;
import me.quadphase.qpdex.pokemon.Evolution;
import me.quadphase.qpdex.pokemon.Game;
import me.quadphase.qpdex.pokemon.Location;
import me.quadphase.qpdex.pokemon.MinimalPokemon;
import me.quadphase.qpdex.pokemon.Move;
import me.quadphase.qpdex.pokemon.MoveSet;
import me.quadphase.qpdex.pokemon.Party;
import me.quadphase.qpdex.pokemon.PartyPokemon;
import me.quadphase.qpdex.pokemon.Pokemon;
import me.quadphase.qpdex.pokemon.Type;

/**
 * Created by Nicole on 28-Jul-15.
 *
 * Class used to retrieve pokemon and their information from the database.
 */
public class PokemonFactory {

    private final boolean PRINT_DEBUG = false;

    private static final String DB_NAME = "pokedex.db";

    // database constant strings:
    private final String ABILITIES_TABLE = "abilities";
    private final String CATEGORY_TABLE = "category";
    private final String EGG_GROUPS_TABLE = "eggGroups";
    private final String GAMES_TABLE = "games";
    private final String LOCATIONS_TABLE = "locations";
    private final String MOVES_TABLE = "moves";
    private final String TYPES_TABLE = "types";
    private final String PARTY_TABLE = "party";
    private final String PARTY_MOVESET_TABLE = "party_moveSet";
    private final String POKEMON_COMMON_INFO_TABLE = "pokemon_common_info";
    private final String POKEMON_UNIQUE_INFO_TABLE = "pokemon_unique_info";
    private final String POKEMON_ABILITIES_TABLE = "pokemon_abilities";
    private final String POKEMON_CAUGHT_TABLE = "pokemon_caught";
    private final String POKEMON_EGG_GROUPS_TABLE = "pokemon_eggGroups";
    private final String POKEMON_EVOLUTIONS_TABLE = "pokemon_evolutions";
    private final String POKEMON_LOCATIONS_TABLE = "pokemon_locations";
    private final String POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE = "pokemon_nationalID";
    private final String POKEMON_SUFFIX_TABLE = "pokemon_suffix";
    private final String POKEMON_TYPES_TABLE = "pokemon_types";
    private final String TYPE_EFFECTIVENESS_TABLE = "types_effectiveness";
    private final String POKEMON_MOVES_TABLE = "pokemon_moves";

    private final String NAME = "name";
    private final String DESCRIPTION = "description";
    private final String POWER = "power";
    private final String ACCURACY = "accuracy";
    private final String PP = "pp";
    private final String AFFECTS = "affects";
    private final String GENERATION_FIRST_APPEARED = "genFirstAppeared";
    private final String HEIGHT = "height";
    private final String WEIGHT = "weight";
    private final String ATTACK = "attack";
    private final String DEFENCE = "defence";
    private final String HP = "hp";
    private final String SPECIAL_ATTACK = "spattack";
    private final String SPECIAL_DEFENCE = "spdefence";
    private final String SPEED = "speed";
    private final String TOTAL_BASE_STATS = "basestat";
    private final String CATCH_RATE = "catchRate";
    private final String CAUGHT = "caught";
    private final String CONDITION = "condition";
    private final String GENDER_RATIO_MALE = "genderRatioMale";
    private final String HATCH_TIME = "hatchTime";
    private final String LEARN_METHOD = "learnMethod";
    private final String SUFFIX = "suffix";
    private final String EFFECTIVE_LEVEL = "effectiveLevel";

    private final String ABILITIY_ID = "abilityID";
    private final String CATEGORY_ID = "categoryID";
    private final String EGG_GROUP_ID = "eggGroupID";
    private final String GAME_ID = "gameID";
    private final String GENERATION_ID = "generationID";
    private final String LOCATION_ID = "locationID";
    private final String MOVE_ID = "moveID";
    private final String TYPE_ID = "typeID";
    private final String PARTY_ID = "partyID";
    private final String POKEMON_UNIQUE_ID = "pokemonUniqueID";
    private final String POKEMON_NATIONAL_ID = "pokemonNationalID";
    private final String FROM_TYPE_ID = "fromTypeID";
    private final String TO_TYPE_ID = "toTypeID";
    private final String FROM_POKEMON_ID = "fromPokemonID";
    private final String TO_POKEMON_ID = "toPokemonID";

    private MinimalPokemon[] allMinimalPokemon;
    private Pokemon[] detailedPokemonShortList;
    private Pokemon[] allDetailedPokemon;

    private static PokemonFactory instance = null;

    private Type[] types;
    private EggGroup[] eggGroups;
    private Ability[] abilities;
    private int[] allNationalIDMappedUnique;
    private String[] generations;
    private int MAX_UNIQUE_ID;
    private int MAX_NATIONAL_ID;


    /**
     * Type effectiveness sparse matrix.
     * [attacking type][defending type]
     */
    private final double[][] typeEffectiveness;

    /**
     * SQLite database handle
     */
    private SQLiteDatabase database;

    /**
     * Singleton constructor of the Pokemon Factory
     */
    private PokemonFactory(Context context){
        //Initialize the Database Handler
        ExternalDbOpenHelper dbOpenHelper = new ExternalDbOpenHelper(context, DB_NAME);
        database = dbOpenHelper.openDataBase();

        setupLargeCacheLists();

        // loads all Type objects into memory
        types = new Type[getMaxTypeID() + 1];
        abilities = new Ability[getMaxAbilityID() + 1];
        allNationalIDMappedUnique = new int[MAX_UNIQUE_ID + 1];
        eggGroups = new EggGroup[getMaxEggGroupID() + 1];
        generations = new String[getMaxGeneration() + 1];

        loadAllTypes();

        // Load the type effectiveness sparse matrix:
        typeEffectiveness = buildTypeEffectivenessTable();
        loadAllAbilities();
        loadAllUniqueToNationalID();
        loadAllEggGroups();
        loadAllGenerations();


    }

    private void setupLargeCacheLists(){
        // Cache max unique and national id
        MAX_NATIONAL_ID = loadMaxNationalID();
        MAX_UNIQUE_ID = loadMaxUniqueID();

        //Create new short list (matches minimalPokemon with a corresponding Pokemon)
        detailedPokemonShortList = new Pokemon[MAX_NATIONAL_ID+1];

        //Create a new Large Pokemon List
        allDetailedPokemon = new Pokemon[MAX_UNIQUE_ID+1];

        //Set the fail-safe
        allDetailedPokemon[0] = PokedexManager.getInstance().missingNo;
        detailedPokemonShortList[0] = allDetailedPokemon[0];


    }

    /**
     * Method used to get the singleton {@link PokemonFactory}
     *
     * @param context context of the app
     * @return a PokemonFactory
     */
    public static PokemonFactory getPokemonFactory(Context context) {
        if (instance == null) {
            instance = new PokemonFactory(context);
        }
        return instance;
    }


    /**
     * Retrieves from database a list of all pokemon
     *
     * @return List of all main (no suffix) minimalPokemon in the database
     */
    public MinimalPokemon[] getAllMinimalPokemon() {

        if (allMinimalPokemon==null) {

            allMinimalPokemon = new MinimalPokemon[MAX_NATIONAL_ID+1];


            //Secretly added the fail-safe Pokemon...
            allMinimalPokemon[0] = PokedexManager.getInstance().missingNo.minimal();


            //SECTION I: INITIAL QUERIES
            //5 Cursors initialized after executing 8 queries

            final Cursor mapCursor = database.rawQuery(String.format("SELECT * FROM %s WHERE %s NOT IN (SELECT %s FROM %s)",
                    POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE,
                    POKEMON_UNIQUE_ID,
                    POKEMON_UNIQUE_ID, POKEMON_SUFFIX_TABLE), null);

            final Cursor caughtCursor = database.query(POKEMON_CAUGHT_TABLE, null, null, null, null, null, POKEMON_NATIONAL_ID);

            final Cursor describeCursor = database.query(POKEMON_COMMON_INFO_TABLE, new String[]{POKEMON_NATIONAL_ID, DESCRIPTION}, null, null, null, null, POKEMON_NATIONAL_ID);

            final Cursor nameCursor = database.rawQuery(String.format("SELECT %s,%s FROM %s WHERE %s NOT IN (SELECT %s FROM %s)",
                    POKEMON_UNIQUE_ID, NAME, POKEMON_UNIQUE_INFO_TABLE,
                    POKEMON_UNIQUE_ID,
                    POKEMON_UNIQUE_ID, POKEMON_SUFFIX_TABLE), null);

            final Cursor typeCursor = database.rawQuery(String.format("SELECT %s,%s FROM %s WHERE %s NOT IN (SELECT %s FROM %s)",
                    POKEMON_UNIQUE_ID,TYPE_ID,POKEMON_TYPES_TABLE,
                    POKEMON_UNIQUE_ID,
                    POKEMON_UNIQUE_ID,POKEMON_SUFFIX_TABLE),null);


            //SECTION 2: PARALLEL SETUP
            //Defining 5 small, very specific threads for next section


            Thread caughtInit = new Thread(){
                @Override
                public void run(){
                    minimalPokemonCaughtInitializer(caughtCursor);
                }
            };
            caughtInit.setPriority(Thread.MAX_PRIORITY);

            Thread describeInit = new Thread(){
                @Override
                public void run(){
                    minimalPokemonDescriptionInitializer(describeCursor);
                }
            };

            Thread nameInit = new Thread(){
                @Override
                public void run(){
                    minimalPokemonNameInitializer(nameCursor);
                }
            };

            Thread typeInit = new Thread(){
                @Override
                public void run(){
                    minimalPokemonTypeInitializer(typeCursor);
                }
            };

            //SECTION 3: THREAD MANAGEMENT
            //Actual operations on threads
            try {
                //Start creating objects and setup the hashMap
                caughtInit.start();

                caughtInit.join(); //Wait for all objects to initialize

                //Now begin calling the others
                describeInit.start();


                //Signal the start of the last two threads
                typeInit.start();
                nameInit.start();

                //Finally, wait for all.
                describeInit.join();
                typeInit.join();
                nameInit.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //Close ALL cursors
            mapCursor.close();
            caughtCursor.close();
            describeCursor.close();
            nameCursor.close();
            typeCursor.close();

        }

        return allMinimalPokemon;
    }

    /**
     * This method initializes the allMinimalPokemon list through the nationalID and Caught values.
     * @param cursor A Database Cursor that result covers all rows from the pokemon_caught table
     */
    private void minimalPokemonCaughtInitializer(Cursor cursor){
        if(cursor!=null && allMinimalPokemon!=null){
            cursor.moveToFirst();

            int nationalIDColumn = cursor.getColumnIndex(POKEMON_NATIONAL_ID);
            int caughtColumn = cursor.getColumnIndex(CAUGHT);

            do {
                //Initialize the Pokemon
                int nationalID = cursor.getInt(nationalIDColumn);
                int caught = cursor.getInt(caughtColumn);

                //Set it in the Array.
                allMinimalPokemon[nationalID] = new MinimalPokemon(nationalID, caught>0);
            }while (cursor.moveToNext());
        }
    }

    /**
     * This method sets the description of a Pokemon
     * @param cursor A DB cursor pointing to rows in pokemon_common_info
     */
    private void minimalPokemonDescriptionInitializer(Cursor cursor){
        if(cursor!=null && allMinimalPokemon!=null){
            cursor.moveToFirst();

            int nationalIDColumn = cursor.getColumnIndex(POKEMON_NATIONAL_ID);
            int descriptColumn =cursor.getColumnIndex(DESCRIPTION);

            do{
                MinimalPokemon currentBuild = allMinimalPokemon[cursor.getInt(nationalIDColumn)];
                currentBuild.setDescription(cursor.getString(descriptColumn));
            }while (cursor.moveToNext());
        }
    }

    /**
     * This method takes a cursor to the pokemon_caught table and intializes the allMinimalPokemon list
     * @param cursor A DB cursor pointing to rows in pokemon_types
     */
    private void minimalPokemonTypeInitializer(Cursor cursor){
        if(cursor!=null && allMinimalPokemon!=null){
            cursor.moveToFirst();

            int typeColumn = cursor.getColumnIndex(TYPE_ID);
            int uniqueIDColumn = cursor.getColumnIndex(POKEMON_UNIQUE_ID);

            List<Type> pkmnTypes = new LinkedList<>();

            int currentID = allNationalIDMappedUnique[cursor.getInt(uniqueIDColumn)];
            int formerID = currentID;

            do{
                //Get the ID of current row
                currentID = allNationalIDMappedUnique[cursor.getInt(uniqueIDColumn)];

                //If ID is not the same with the last row
                if(currentID!=formerID){
                    //Dump the current list of Types
                    allMinimalPokemon[formerID].setTypes(pkmnTypes);
                    pkmnTypes = new LinkedList<>();
                    formerID = currentID;
                }

                //Add the type being read in.
                pkmnTypes.add(types[cursor.getInt(typeColumn)]);


            }while (cursor.moveToNext());

            if(!pkmnTypes.isEmpty()){
                allMinimalPokemon[currentID].setTypes(pkmnTypes);
            }
        }
    }

    /**
     * Sets the name of objects within the allMinimalPokemon list.
     * @param cursor A DB cursor pointing to rows in pokemon_common_info
     */
    private void minimalPokemonNameInitializer(Cursor cursor){
        if (cursor!=null && allMinimalPokemon!=null) {
            cursor.moveToFirst();
            int nameColumn = cursor.getColumnIndex(NAME);
            int uniqueIDColumn = cursor.getColumnIndex(POKEMON_UNIQUE_ID);

            do{
                int nationalID = allNationalIDMappedUnique[cursor.getInt(uniqueIDColumn)];
                String name = cursor.getString(nameColumn);
                allMinimalPokemon[nationalID].setName(name);
            }while (cursor.moveToNext());
        }
    }


    public void getAllDetailedPokemon(){

        //NOTE: This method is here for testing purposes. Avoid using in production builds.

        if (allDetailedPokemon==null || detailedPokemonShortList==null) {
            setupLargeCacheLists();
        }

        for (int i = 1; i <= MAX_NATIONAL_ID; i++) {

            //If the entry is null
            if(detailedPokemonShortList[i]==null){
                //Check the mapping to the long list if it's there by any chance
                if(allDetailedPokemon[checkUniqueIDFromNationalID(i)]!=null){
                    detailedPokemonShortList[i] = allDetailedPokemon[checkUniqueIDFromNationalID(i)];
                }
                //Or build it from scratch
                else {
                    detailedPokemonShortList[i] = getPokemonByNationalID(i);
                }
            }

            if(PRINT_DEBUG)
                Log.d("QPDEX",String.format("Building %s",i));
        }

    }


    private void buildPartOfDetailedPokemonList(int start, int end){
        for (int i = start; i < end; i++) {
            //If the entry is null
            if(detailedPokemonShortList[i]==null){
                //Check the mapping to the long list if it's there by any chance
                if(allDetailedPokemon[checkUniqueIDFromNationalID(i)]!=null){
                    detailedPokemonShortList[i] = allDetailedPokemon[checkUniqueIDFromNationalID(i)];
                }
                //Or build it from scratch
                else {
                    detailedPokemonShortList[i] = getPokemonByNationalID(i);
                }
            }

            Log.d("QPDEX",String.format("Building %s",i));
        }
    }

    /**
     * Constructs the {@link me.quadphase.qpdex.pokemon.Pokemon} from the information in the database.
     *
     * @param pokemonID - pokemon ID in the database of the pokemon to be brought into memory
     * @return the complete pokemon object including all of its information such as type(s),
     *          move(s) and ability(ies)
     */
    public Pokemon getPokemonByPokemonID(int pokemonID) {
        if (allDetailedPokemon[pokemonID]!=null) {
            return allDetailedPokemon[pokemonID];
        } else {
            // move the cursor to the correct entry of the pokemon table
            Cursor cursor = database.query(POKEMON_UNIQUE_INFO_TABLE, null, POKEMON_UNIQUE_ID +"=?", new String[]{String.valueOf(pokemonID)}, null, null, null, null);
            cursor.moveToFirst();

            // get all of the information stored in pokemon table:
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            double height = cursor.getFloat(cursor.getColumnIndex(HEIGHT));
            double weight = cursor.getFloat(cursor.getColumnIndex(WEIGHT));
            int attack = cursor.getInt(cursor.getColumnIndex(ATTACK));
            int defence = cursor.getInt(cursor.getColumnIndex(DEFENCE));
            int hp = cursor.getInt(cursor.getColumnIndex(HP));
            int spAttack = cursor.getInt(cursor.getColumnIndex(SPECIAL_ATTACK));
            int spDefence = cursor.getInt(cursor.getColumnIndex(SPECIAL_DEFENCE));
            int speed = cursor.getInt(cursor.getColumnIndex(SPEED));

            // get the nationalID:
            String[] selectionArg = {String.valueOf(pokemonID)};
            cursor = database.query(POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE, null, POKEMON_UNIQUE_ID+"=?", selectionArg, null, null, null);
            cursor.moveToFirst();
            int nationalID = cursor.getInt(cursor.getColumnIndex(POKEMON_NATIONAL_ID));

            // get the description:
            selectionArg[0] = String.valueOf(nationalID);
            cursor = database.query(POKEMON_COMMON_INFO_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null, null);
            cursor.moveToFirst();
            String description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
            int genFirstAppeared = cursor.getInt(cursor.getColumnIndex(GENERATION_FIRST_APPEARED));
            int hatchTime = cursor.getInt(cursor.getColumnIndex(HATCH_TIME));
            int catchRate = cursor.getInt(cursor.getColumnIndex(CATCH_RATE));
            int genderRatioMale = cursor.getInt(cursor.getColumnIndex(GENDER_RATIO_MALE));

            // close the cursor
            cursor.close();

            boolean caught = isCaught(nationalID);

            // lists to fetch:
            List<Location> locations = null;//getLocations(nationalID);
            List<Ability> abilities = getAbilities(pokemonID);
            List<Move> moves = null;//getMoves(nationalID);
            List<Type> types = getTypes(pokemonID);
            List<EggGroup> eggGroups = getEggGroups(nationalID);
            Evolution evolutions = getEvolutions(pokemonID);


             Pokemon newEntry = new Pokemon(pokemonID, nationalID, name, description, height, weight, attack, defence, hp,
                    spAttack, spDefence, speed, caught, genFirstAppeared, hatchTime, catchRate, genderRatioMale,
                    locations, abilities, moves, types, eggGroups, evolutions);

            allDetailedPokemon[pokemonID] = newEntry;

            return newEntry;
        }
    }

    /**
     * Constructs the main {@link me.quadphase.qpdex.pokemon.Pokemon} from the information in
     * the database.
     *
     * @param nationalID - national ID of the pokemon to be brought into memory
     * @return the complete pokemon object including all of its information such as type(s),
     *          move(s) and ability(ies)
     */
    public Pokemon getPokemonByNationalID(int nationalID) {
        if (!isDetailedNationalIDBuiltAndReady(nationalID)) {
            Pokemon returnObject = getPokemonByPokemonID(checkUniqueIDFromNationalID(nationalID));
            detailedPokemonShortList[nationalID] = returnObject;
            return returnObject;
        } else {
            return detailedPokemonShortList[nationalID];
        }
    }

    public int checkUniqueIDFromNationalID(int nationalID){
        // find the pokemonID from the nationalID using the pokemon_nationalID mapping table
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor cursor = database.query(POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);

        // HACK: We are assuming that the first pokemon with this nationalID is the original pokemon
        // that we want to fetch, since there are many pokemon with the same nationalID in the case
        // of mega evolutions and various types.
        cursor.moveToFirst();
        int retVal = cursor.getInt(cursor.getColumnIndex(POKEMON_UNIQUE_ID));
        cursor.close();
        return retVal;

    }

    /**
     * Goes into the locations table to retrieve the pokemon's locations
     *
     * @param nationalID nationalID in the pokemon table
     * @return the list of locations that the pokemon is found at
     */
    private List<Location> getLocations(int nationalID) {
        List<Location> locations = new LinkedList<>();
        // move the cursor to the pokemon location mapping table
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor mappingCursor = database.query(POKEMON_LOCATIONS_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);
        mappingCursor.moveToFirst();

        // Get the cursors for the locations and games tables:
        Cursor locationCursor = database.query(LOCATIONS_TABLE, null, null, null, null, null, null);
        Cursor gameCursor = database.query(GAMES_TABLE, null, null, null, null, null, null);

        while (!mappingCursor.isAfterLast()) {
            selectionArg[0] = String.valueOf(mappingCursor.getInt(mappingCursor.getColumnIndex(LOCATION_ID)));
            locationCursor = database.query(LOCATIONS_TABLE, null, LOCATION_ID + "=?", selectionArg, null, null, null);
            locationCursor.moveToFirst();

            int gameID = locationCursor.getInt(locationCursor.getColumnIndex(GAME_ID));

            selectionArg[0] = String.valueOf(gameID);
            gameCursor = database.query(GAMES_TABLE, null, GAME_ID + "=?", selectionArg, null, null, null);
            gameCursor.moveToFirst();

            Game game = new Game(gameCursor.getString(gameCursor.getColumnIndex(NAME)), gameCursor.getInt(gameCursor.getColumnIndex(GENERATION_ID)));
            Location loc = new Location(locationCursor.getString(locationCursor.getColumnIndex(NAME)), game);

            locations.add(loc);

            // go to the next location for this pokemonID
            mappingCursor.moveToNext();
        }

        // close the cursors
        mappingCursor.close();
        locationCursor.close();
        gameCursor.close();

        return locations;
    }

    /**
     * goes into the abilities table to retrieve a list of the pokemon's abilities
     * @param pokemonID pokemonID in the pokemon table (not the nationalID)
     * @return a list of the pokemon's abilities
     */
    private List<Ability> getAbilities(int pokemonID) {
        List<Ability> abilities = new LinkedList<>();
        // move the cursor to the pokemon abilities mapping table
        String[] selectionArg = {String.valueOf(pokemonID)};
        Cursor mappingCursor = database.query(POKEMON_ABILITIES_TABLE, null, POKEMON_UNIQUE_ID + "=?", selectionArg, null, null, null);
        mappingCursor.moveToFirst();

        // Get the cursor for the ability table:
        Cursor cursor = database.query(ABILITIES_TABLE, null, null, null, null, null, null);

        while (!mappingCursor.isAfterLast()) {
            selectionArg[0] = String.valueOf(mappingCursor.getInt(mappingCursor.getColumnIndex(ABILITIY_ID)));
            cursor = database.query(ABILITIES_TABLE, null, ABILITIY_ID + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();

            Ability ability = new Ability(cursor.getString(cursor.getColumnIndex(NAME)), cursor.getString(cursor.getColumnIndex(DESCRIPTION)));

            abilities.add(ability);

            // go to the next ability for this pokemonID
            mappingCursor.moveToNext();
        }

        // close the cursors
        cursor.close();
        mappingCursor.close();

        return abilities;
    }

    /**
     * Goes into the moves table to retrieve the pokemon's moves
     *
     * @param nationalID pokemonID in the pokemon table (not the nationalID)
     * @return the list of moves that the pokemon is found at
     */
    private List<Move> getMoves(int nationalID) {
        List<Move> moves = new LinkedList<>();
        // move the cursor to the pokemon moves mapping table
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor mappingCursor = database.query(POKEMON_MOVES_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);
        mappingCursor.moveToFirst();

        while (!mappingCursor.isAfterLast()) {
            moves.add(getMove(mappingCursor.getInt(mappingCursor.getColumnIndex(MOVE_ID))));

            // go to the next move for this pokemonID
            mappingCursor.moveToNext();
        }

        // close the cursor
        mappingCursor.close();

        return moves;
    }

    /**
     * Gets an object of type Move of the given moveID.
     *
     * @param moveID unique id of the move in the database
     * @return {@link Move} object
     */
    private Move getMove(int moveID) {
        // Get the cursor for the moves tables:
        String[] selectionArg = {String.valueOf(moveID)};
        Cursor cursor = database.query(MOVES_TABLE, null, MOVE_ID + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();

        Move move = new Move(cursor.getString(cursor.getColumnIndex(NAME)),
                cursor.getString(cursor.getColumnIndex(DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(POWER)),
                cursor.getInt(cursor.getColumnIndex(ACCURACY)),
                cursor.getInt(cursor.getColumnIndex(PP)),
                cursor.getString(cursor.getColumnIndex(AFFECTS)),
                cursor.getInt(cursor.getColumnIndex(GENERATION_FIRST_APPEARED)),
                getCategory(cursor.getInt(cursor.getColumnIndex(CATEGORY_ID))),
                getType(cursor.getInt(cursor.getColumnIndex(TYPE_ID))));

        cursor.close();

        return move;
    }

    /**
     * Goes into the types table to retrieve the pokemon's types
     *
     * @param pokemonID pokemonID in the pokemon table (not the nationalID)
     * @return the list of types that the pokemon is
     */
    private List<Type> getTypes(int pokemonID) {

        List<Type> types = new LinkedList<>();
        // move the cursor to the pokemon types mapping table
        String[] selectionArg = {String.valueOf(pokemonID)};
        Cursor mappingCursor = database.query(POKEMON_TYPES_TABLE, null, POKEMON_UNIQUE_ID + "=?", selectionArg, null, null, null);
        mappingCursor.moveToFirst();

        while (!mappingCursor.isAfterLast()) {
            types.add(getType(mappingCursor.getInt(mappingCursor.getColumnIndex(TYPE_ID))));

            // go to the next location for this pokemonID
            mappingCursor.moveToNext();
        }

        // close the cursor
        mappingCursor.close();

        return types;
    }

    /**
     * Goes into the egg groups table to retrieve the pokemon's egg groups
     *
     * @param nationalID nationalID in the pokemon table
     * @return the list of egg groups that the pokemon belongs to
     */
    private List<EggGroup> getEggGroups(int nationalID) {
        List<EggGroup> eggGroups = new LinkedList<>();

        // move the cursor to the egg group mapping table
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor mappingCursor = database.query(POKEMON_EGG_GROUPS_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);
        mappingCursor.moveToFirst();

        // Get the cursors for the egg Groups tables:
        Cursor cursor = database.query(EGG_GROUPS_TABLE, null, null, null, null, null, null);

        while (!mappingCursor.isAfterLast()) {
            selectionArg[0] = String.valueOf(mappingCursor.getInt(mappingCursor.getColumnIndex(EGG_GROUP_ID)));
            cursor = database.query(EGG_GROUPS_TABLE, null, EGG_GROUP_ID + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();

            eggGroups.add(new EggGroup(cursor.getString(cursor.getColumnIndex(NAME))));

            // go to the next egg group for this pokemonID
            mappingCursor.moveToNext();
        }

        // close the cursor
        cursor.close();
        mappingCursor.close();

        return eggGroups;
    }

    /**
     * Goes into the evolution table to retrieve the pokemon's evolutions
     *
     * @param pokemonID pokemonID in the pokemon table (not the nationalID)
     * @return the list of evolutions of the pokemon
     */
    private Evolution getEvolutions(int pokemonID) {
//        List<Evolution> evolutions = new LinkedList<>();
//
//        // move the cursor to the evolutions mapping table
//        String[] selectionArg = {String.valueOf(pokemonID)};
//        Cursor mappingCursor = database.query(POKEMON_EVOLUTIONS_TABLE, null, FROM_POKEMON_ID + "=? OR "+TO_POKEMON_ID + "=?", selectionArg, null, null, null);
//        mappingCursor.moveToFirst();
//
//        //By having Evolutions point to a full Pokemon object, Building a single instance of a Pokemon
//        // will result in a DFS to build all of its evolutions. Therefore, with good reason, this
//        // should only be done once!
//        while (!mappingCursor.isAfterLast()) {
//            int evolvesToPokemonID = mappingCursor.getInt(mappingCursor.getColumnIndex(TO_POKEMON_ID));
//            String condition = mappingCursor.getString(mappingCursor.getColumnIndex(CONDITION));
//            evolutions.add(new Evolution(condition, getPokemonByPokemonID(evolvesToPokemonID)));
//
//            // go to the next evolution for this pokemonID
//            mappingCursor.moveToNext();
//        }
//
//        // close the cursor
//        mappingCursor.close();
//
//        return evolutions;
        //TODO: Optimize so only need to be done once per evolution chain
        //TODO turn this list into a tree
        List<Evolution> evolutions = new LinkedList<>();
        Queue<Evolution> evolutionQueue = new LinkedList<>();
        int currentPokemonID = pokemonID;

        // From pokemonID drill down to first evolution by first finding if any evolve into this
        String[] selectionArg = {String.valueOf(currentPokemonID)};
        Cursor mappingCursor = database.query(POKEMON_EVOLUTIONS_TABLE, null, TO_POKEMON_ID + "=?", selectionArg, null, null, null);

        //while something still evolves into this pokemon, go lower
        while (mappingCursor.moveToFirst()) {
            currentPokemonID = mappingCursor.getInt(mappingCursor.getColumnIndex(FROM_POKEMON_ID));
            String[] currentSelectionArg = {String.valueOf(currentPokemonID)};
            mappingCursor = database.query(POKEMON_EVOLUTIONS_TABLE, null, TO_POKEMON_ID + "=?", currentSelectionArg, null, null, null);
        }
        int rootPokemonID=currentPokemonID;
        int rootNatID = allNationalIDMappedUnique[currentPokemonID];
        Evolution rootEvolutionChain = new Evolution("Base Pokemon", rootPokemonID, allMinimalPokemon[rootNatID]);
        evolutions.add(rootEvolutionChain);
        evolutionQueue.add(rootEvolutionChain);

        // close the cursor
        mappingCursor.close();

        // Now start traversing upwards once you have root and add evo objects

        while (!evolutionQueue.isEmpty()) {
            // Add first layer of evos, and pop the base evo off queue
            Evolution pokemonEvolutionTobuildFrom = evolutionQueue.remove();

            // From pokemonID drill down to first evolution by first finding if any evolve into this
            String[] evoSelectionArg = {String.valueOf(pokemonEvolutionTobuildFrom.getUniquepokemonID())};
            Cursor evoMappingCursor = database.query(POKEMON_EVOLUTIONS_TABLE, null, FROM_POKEMON_ID + "=?", evoSelectionArg, null, null, null);
            evoMappingCursor.moveToFirst();

            while (!evoMappingCursor.isAfterLast()) {
                int pokemonEvolutionToAddID = evoMappingCursor.getInt(mappingCursor.getColumnIndex(TO_POKEMON_ID));
                String condition = evoMappingCursor.getString(mappingCursor.getColumnIndex(CONDITION));

                // If condition is MegaStone/Primal then don't add to evo list
                // TODO: Alt form refactoring
                if(!condition.equals("MegaStone/Primal")) {
                    int pokemonEvolutionToAddNatID = allNationalIDMappedUnique[pokemonEvolutionToAddID];
                    Evolution pokemonEvolutionToAdd = new Evolution(condition, pokemonEvolutionToAddID, allMinimalPokemon[pokemonEvolutionToAddNatID]);
                    pokemonEvolutionTobuildFrom.getEvolvesInto().add(pokemonEvolutionToAdd);
                    evolutionQueue.add(pokemonEvolutionToAdd);
                }

                // go to the next evolution for this pokemonID
                evoMappingCursor.moveToNext();
            }
        }


        return rootEvolutionChain;

    }

    /**
     * From the typeID, goes into the types table to construct the {@link Type} object.
     * @param typeID Unique type ID
     * @return  {@link Type} from the database corresponding to that typeID
     */
    private Type getType(int typeID) {
        Type type = types[typeID];

        if(PRINT_DEBUG)
            Log.v("Database Access", "From typeID " + String.valueOf(typeID) + " type obtained was " + type.getName());

        /* This loads a new Type object each time.
        String[] selectionArg = {String.valueOf(typeID)};
        Cursor cursor = database.query(TYPES_TABLE, null, TYPE_ID + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();

        Type type = new Type(cursor.getString(cursor.getColumnIndex(NAME)), cursor.getString(cursor.getColumnIndex(DESCRIPTION)));

        // close the cursor
        cursor.close();
        */
        return type;
    }

    /**
     * Maps the categoryID to the name of the category.
     *
     * @param categoryID from the moves table
     * @return String of the name of the category
     */
    private String getCategory(int categoryID) {
        String[] selectionArg = {String.valueOf(categoryID)};
        Cursor cursor = database.query(CATEGORY_TABLE, null, CATEGORY_ID + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();

        String name = cursor.getString(cursor.getColumnIndex(NAME));

        // close the cursor
        cursor.close();

        return name;
    }

    /**
     * Checks whether the pokemon has been caught or not.
     *
     * @param nationalID national ID of the pokemon
     * @return true if the pokemon has been caught, false otherwise
     */
    private boolean isCaught(int nationalID) {
        // TODO: Implement this method with correct names when database is changed
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor cursor = database.query(POKEMON_CAUGHT_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();

        // assume not caught
        boolean caught = false;
        // verify if it is caught
        if (cursor.getInt(cursor.getColumnIndex(CAUGHT)) == 1) {
            caught = true;
        }

        // close the cursor
        cursor.close();
        if(PRINT_DEBUG)
            Log.v("Database Access", "T/F: Pokemon with nationalID " + String.valueOf(nationalID) + " was caught: " + String.valueOf(caught));

        return caught;
    }

    /**
     * Used to toggle whether the pokemon has been caught or not.
     * If the pokemon was caught, it will change the database to say that it is not caught.
     * The opposite is also true.
     *
     * @param nationalID national ID of the pokemon
     */
    public void toggleCaught(int nationalID) {
        // TODO: Implement this method with correct names when database is changed
        String[] selectionArg = {String.valueOf(nationalID)};
        Cursor cursor = database.query(POKEMON_CAUGHT_TABLE, null, POKEMON_NATIONAL_ID + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();

        // assume not caught
        int caught = 0;
        // verify if it is caught, change it to not caught.
        if (cursor.getInt(cursor.getColumnIndex(CAUGHT)) == 0) {
            caught = 1;
        }
        // close the cursor
        cursor.close();

        // create new row content
        ContentValues content = new ContentValues();
        content.put(POKEMON_NATIONAL_ID, nationalID);
        content.put(CAUGHT, caught);

        // replace the row with the nationalID with the new row
        database.beginTransaction();
        database.replaceOrThrow(POKEMON_CAUGHT_TABLE, null, content);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    /**
     * Sets the value of caught to caught or not in the database.
     *
     * @param nationalID nationalID of the pokemon that is caught
     * @param value true if changing to caught, false if changing to not caught
     */
    public void setCaught(int nationalID, boolean value){
        if (nationalID>0 && nationalID<MAX_UNIQUE_ID) {
            int caught = value? 1 : 0;

            //Update the MinimalPokemon
            if(allMinimalPokemon!=null){
                if(allMinimalPokemon[nationalID].isCaught()!=value)
                    allMinimalPokemon[nationalID].toggleCaught();
            }

            // create new row content
            ContentValues content = new ContentValues();
            content.put(POKEMON_NATIONAL_ID, nationalID);
            content.put(CAUGHT, caught);

            // replace the row with the nationalID with the new row
            database.beginTransaction();
            database.replaceOrThrow(POKEMON_CAUGHT_TABLE, null, content);
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    /**
     * Determines the current generation of the games.
     *
     * @return current generation
     */
    public int getCurrentGeneration() {
        /*
        TODO: test this method, and the loadMaxNationalID, to make sure that this selection command
                 works to get the max. If not, let @Nicole know :)
        */
        Cursor cursor = database.query(GAMES_TABLE, null, null, null, null, null, null);
        cursor.moveToLast();

        int generationID = cursor.getInt(cursor.getColumnIndex(GENERATION_ID));

        // close the cursor:
        cursor.close();

        return generationID;
    }

    /**
     * Determines the current number of pokemon by national ID
     *
     * @return max national ID
     */
    public int loadMaxNationalID() {
        Cursor cursor = database.query(POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE, null, null, null, null, null, null);
        cursor.moveToLast();

        int maxNationalID = cursor.getInt(cursor.getColumnIndex(POKEMON_NATIONAL_ID));

        // close the cursor:
        cursor.close();

        return maxNationalID;
    }

    /**
     * Determines the current number of pokemon by their unique ID
     *
     * @return max national ID
     */
    public int loadMaxUniqueID() {
        String[] colToGet = {POKEMON_UNIQUE_ID} ;
        Cursor cursor = database.query(POKEMON_UNIQUE_INFO_TABLE, colToGet, null, null, null, null, null);
        cursor.moveToLast();

        int maxUniqueID = cursor.getInt(cursor.getColumnIndex(POKEMON_UNIQUE_ID));

        // close the cursor:
        cursor.close();

        return maxUniqueID;
    }

    /**
     * Determines the current maximum number of generations
     *
     * @return max generations
     */
    public int getMaxGeneration() {
        Cursor cursor = database.query(POKEMON_COMMON_INFO_TABLE, null, null, null, null, null, GENERATION_FIRST_APPEARED);
        cursor.moveToLast();

        int maxGeneration = cursor.getInt(cursor.getColumnIndex(GENERATION_FIRST_APPEARED));

        // close the cursor:
        cursor.close();

        return maxGeneration;
    }
    /**
     * Determines the current number of types
     *
     * @return max typeID
     */
    public int getMaxTypeID() {
        Cursor cursor = database.query(TYPES_TABLE, null, null, null, null, null, null);
        cursor.moveToLast();

        int maxTypeID = cursor.getInt(cursor.getColumnIndex(TYPE_ID));
        if(PRINT_DEBUG)
            Log.v("Database Access", "Max typeID is: " + String.valueOf(maxTypeID));
        // close the cursor:
        cursor.close();

        return maxTypeID;
    }

    /**
     * Determines the current number of abilities
     *
     * @return max abilityID
     */
    public int getMaxAbilityID() {
        Cursor cursor = database.query(ABILITIES_TABLE, null, null, null, null, null, null);
        cursor.moveToLast();

        int maxAbilityID = cursor.getInt(cursor.getColumnIndex(ABILITIY_ID));
        if(PRINT_DEBUG)
            Log.v("Database Access", "Max abilityID is: " + String.valueOf(maxAbilityID));
        // close the cursor:
        cursor.close();

        return maxAbilityID;
    }

    /**
     * Determines the current number of eggGroups.
     *
     * @return max maxEggGroupID+1 since eggGroup ID's start at 0
     */
    public int getMaxEggGroupID() {
        Cursor cursor = database.query(EGG_GROUPS_TABLE, null, null, null, null, null, null);
        cursor.moveToLast();

        int maxEggGroupID = cursor.getInt(cursor.getColumnIndex(EGG_GROUP_ID));
        if(PRINT_DEBUG)
            Log.v("Database Access", "Max eggGroupID is: " + String.valueOf(maxEggGroupID));
        // close the cursor:
        cursor.close();

        return maxEggGroupID+1;
    }

    /**
     * Get the party including all of the {@link Pokemon} and their respective {@link MoveSet}
     *
     * @return the {@link Party} of the user
     */
    public Party getParty() {
        // grab the whole party table
        Cursor cursor = database.query(PARTY_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();

        Party myParty = new Party();

        Cursor moveCursor = database.query(PARTY_MOVESET_TABLE, null, null, null, null, null, null);

        // go through the party table making the pokemon and associated moveSets for each one
        while (!(cursor.isAfterLast())) {
            int pokemonID = cursor.getInt(cursor.getColumnIndex(POKEMON_UNIQUE_ID));
            int partyID = cursor.getInt(cursor.getColumnIndex(PARTY_ID));

            List<Move> moves = new LinkedList<>();

            String[] selectionArg = {String.valueOf(partyID)};
            moveCursor = database.query(PARTY_MOVESET_TABLE, null, PARTY_ID + "=?", selectionArg, null, null, null);

            while (!(moveCursor.isAfterLast())) {
                moves.add(getMove(moveCursor.getInt(moveCursor.getColumnIndex(MOVE_ID))));

                moveCursor.moveToNext();
            }

            try {
                myParty.addPokemonToParty(getPokemonByPokemonID(pokemonID), new MoveSet(moves));
            } catch (PartyFullException e) {
                // this should not happen in this context since there should not be more than
                // 6 pokemon in the party stored in the database.
            }

            cursor.moveToNext();
        }

        cursor.close();
        moveCursor.close();

        return myParty;
    }

    /**
     * Removes a pokemon and their associated moveSet from the database.
     *
     * @param partyID the array index in the {@link Party} associated with the pokemon to be removed.
     */
    public void removePokemonFromParty(int partyID) {
        String[] selectionArg = {String.valueOf(partyID)};

        database.beginTransaction();
        // delete the pokemon from the party table
        database.delete(PARTY_TABLE, PARTY_ID + "=?", selectionArg);
        // delete the pokemon's moves from the moveSet table
        database.delete(PARTY_MOVESET_TABLE, PARTY_ID + "=?", selectionArg);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    /**
     * Adds the information associated to the pokemon to the party table.
     *
     * Note: Assumes that the partyID that is being passed is valid (i.e. not already occupied by
     *       another pokemon, and less than 6, which is the max number of pokemon in a party,
     *       since verification should have happened at previous step)
     *
     * @param partyPokemon the pokemon and moveset to be added (includes the partyID)
     */
    public void addPokemonToParty(PartyPokemon partyPokemon) throws Exception {
        // TODO: Should we verify here as well that there are not already 6 pokemon in the database?
        // It would be easy enough to just go in the database and make sure that the partyID
        // is not there

        // firstly, add the party
        // create new row content for party table
        ContentValues partyContent = new ContentValues();
        partyContent.put(PARTY_ID, partyPokemon.getPartyID());
        partyContent.put(POKEMON_UNIQUE_ID, partyPokemon.getPokemon().getPokemonUniqueID());

        int numberOfMoves = partyPokemon.getMoveSet().getNumberOfMoves();
        List<Move> moves = partyPokemon.getMoveSet().getMoves();

        ContentValues[] moveContent = new ContentValues[numberOfMoves];
        for (int i = 0; i < numberOfMoves; i++) {
            moveContent[i].put(MOVE_ID, getMoveID(moves.get(i)));
        }


        // insert everything into the table at once, and if there is an exception, do not finalize
        // the transaction and throw an error
        try {
            database.beginTransaction();
            database.insertOrThrow(PARTY_TABLE, null, partyContent);
            // insert the moves
            for (int i = 0; i < numberOfMoves; i++) {
                database.insertOrThrow(PARTY_MOVESET_TABLE, null, moveContent[i]);
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception e) {
            throw new Exception("Database Error: Pokemon not successfully added to party.");
        }

    }

    /**
     * Adds a move to a pokemon in the party.
     *
     * @param partyID index in the array in party that the pokemon is associated to
     * @param move move to be added.
     */
    public void addMoveToPartyPokemon(int partyID, Move move) throws Exception {
        ContentValues moveContent = new ContentValues();
        moveContent.put(PARTY_ID, partyID);
        // Get the cursor for the moves tables to determine the moveID associated with the move

        moveContent.put(MOVE_ID, getMoveID(move));

        try {
            database.beginTransaction();
            database.insertOrThrow(PARTY_MOVESET_TABLE, null, moveContent);
            database.setTransactionSuccessful();
            database.endTransaction();
        } catch (Exception e) {
            throw new Exception("Database Error: Move not added to party pokemon.");
        }
    }

    /**
     * Removes a move from the party_moveSet table associated with a specific pokemon
     * @param partyID partyID of the pokemon to remove the move from
     * @param move move to be removed
     */
    public void removeMoveFromPokemonInParty(int partyID, Move move) {
        String[] selectionArgs = {String.valueOf(partyID), String.valueOf(getMoveID(move))};
        database.delete(PARTY_MOVESET_TABLE, PARTY_ID + "=? AND " + MOVE_ID + "=?", selectionArgs);
    }

    /**
     * Loads all the Types into memory. Should be done at program start (during splash screen)
     */
    public void loadAllTypes() {
        // fail safe 0th index of types:
        types[0] = new Type("Types", 0);

        // get the real types from the database:
        Cursor cursor = database.query(TYPES_TABLE, null, null, null, null, null, null);
        int typeID = 0;
        cursor.moveToFirst();
        do{
            typeID = cursor.getInt(cursor.getColumnIndex(TYPE_ID));
            types[typeID] = new Type(cursor.getString(cursor.getColumnIndex(NAME)), typeID);
        }while(cursor.moveToNext());

        cursor.close();
    }

    /**
     * Loads all the Abilities into memory. Should be done at program start (during splash screen)
     */
    public void loadAllAbilities() {
        // fail safe 0th index of types:
        abilities[0] = new Ability("Ability","None");
        // get the real abilities from the database:
        Cursor cursor = database.query(ABILITIES_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        int i = 1;

        int nameColumnIndex = cursor.getColumnIndex(NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(DESCRIPTION);

        do {
            abilities[i] = new Ability(cursor.getString(nameColumnIndex), cursor.getString(descriptionColumnIndex));
            i++;
        }while(cursor.moveToNext());
        // close the cursor
        cursor.close();
    }

    /**
     * Loads all the EggGroups into memory. Should be done at program start (during splash screen)
     */
    public void loadAllEggGroups() {
        // fail safe 0th index of types:
        eggGroups[0] = new EggGroup("Egg Group");
        Cursor cursor = database.query(EGG_GROUPS_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        int eggGroupNameColumn = cursor.getColumnIndex(NAME);
        int i = 1;
        // get the real abilities from the database:
        do {
            // eggGroupId's start at 0
            eggGroups[i] = new EggGroup(cursor.getString(eggGroupNameColumn));
            i++;

        }while(cursor.moveToNext());

        // close cursor
        cursor.close();
    }

    /**
     * Loads all the Generations into memory. Should be done at program start (during splash screen)
     */
    public void loadAllGenerations() {

        // fail safe 0th index of generations:
        generations[0] = "Generation Appeared";
        // get the real generations, no need to access database, since it's just integers:
        for (int i = 1; i < getMaxGeneration() + 1; i++) {
            generations[i]= Integer.toString(i);
        }
    }

    /**
     * Loads all the National Id's into memory. The position+1 would be the unique ID
     * Should be done at program start (during splash screen)
     */
    public void loadAllUniqueToNationalID() {

        // get the real national IDs from the database:

        Cursor cursor = database.query(POKEMON_NATIONAL_ID_TO_UNIQUE_ID_TABLE, null, null, null, null,null, POKEMON_UNIQUE_ID);
        cursor.moveToFirst();

        // Map for missingno
        allNationalIDMappedUnique[0] = 0;

        for (int i=1; i <= MAX_UNIQUE_ID; i++) {
            allNationalIDMappedUnique[i] = cursor.getInt(cursor.getColumnIndex(POKEMON_NATIONAL_ID));
            cursor.moveToNext();
        }

        // close the cursor
        cursor.close();

    }

    /**
     * Retrieves the moveID of the move.
     *
     * @param move move to get ID of
     * @return int moveID of the move
     */
    private int getMoveID(Move move) {
        String[] selectionArg = {move.getName()};
        String[] columnArg = {MOVE_ID};
        Cursor cursor = database.query(MOVES_TABLE, columnArg, NAME + "=?", selectionArg, null, null, null);
        cursor.moveToFirst();
        int moveID = cursor.getInt(cursor.getColumnIndex(MOVE_ID));
        cursor.close();

        return moveID;
    }

    /**
     * Getter to access all valid abilities in PokemonFactory
     *
     * @return Ability[] array types of all valid abilities
     */
    public Ability[] getAllAbilities() {

        return abilities;
    }

    /**
     * Getter to access all valid eggGroups in PokemonFactory
     *
     * @return EggGroup[] array types of all valid eggGroups
     */
    public EggGroup[] getAllEggGroups() {

        return eggGroups;
    }

    /**
     * Getter to access all valid generations in PokemonFactory
     *
     * @return String[] array types of all valid generartions
     */
    public String[] getAllGenerations() {

        return generations;
    }
    /**
     * Database query, returns all uniqueIDs of pokemon with the abilityID in question
     *
     * @param abilityID of ability queried
     * @return ArrayList of Integers containing uniqueIDs
     */

    public ArrayList<Integer> getAllUniqueIDsFromAbility(int abilityID){

        ArrayList<Integer> filteredUniqueids = new ArrayList<>();

        if(abilityID > -1) {
            String[] selectionArg = {String.valueOf(abilityID)};
            Cursor cursor = database.query(POKEMON_ABILITIES_TABLE, null, ABILITIY_ID + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int uniqueID = cursor.getInt(cursor.getColumnIndex(POKEMON_UNIQUE_ID));
                filteredUniqueids.add(uniqueID);
                cursor.moveToNext();
            }
            cursor.close();

        }
        return filteredUniqueids;

    }

    /**
     * Filters all unique ID's that are of the type requested
     *
     * @param typeID of the type in question
     * @return array list of ints corresponding to unique ids
     */
    public ArrayList<Integer> getAllUniqueIDsFromType(int typeID){

        ArrayList<Integer> filteredUniqueids = new ArrayList<>();

        if(typeID > 0) {
            String[] selectionArg = {String.valueOf(typeID)};
            Cursor cursor = database.query(POKEMON_TYPES_TABLE, null, TYPE_ID + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int uniqueID = cursor.getInt(cursor.getColumnIndex(POKEMON_UNIQUE_ID));
                filteredUniqueids.add(uniqueID);
                cursor.moveToNext();
            }
            cursor.close();

        }
        return filteredUniqueids;

    }

    /**
     * Filters all unique ID's that are of the bewteen the range of stats or the stat requested
     * @param lowerLimit lower integer value limit
     * @param upperLimit upper integer value
     * @param stat the stat being filtered for, where stat is either: HP, ATTACK, DEFENCE, spattack, spdefence or SPEED
     *             TODO stat can also be basestat, which will search for TOTAL_BASE STATS
     * @return array of integers of the unique id's that correspond
     */
    public ArrayList<Integer> getAllUniqueIDsFromStat(int lowerLimit, int upperLimit, String stat){
        ArrayList<Integer> filteredUniqueids = new ArrayList<>();

        //TODO: Consider using enum
        if(stat.equals(HP) || stat.equals(ATTACK) || stat.equals(DEFENCE) || stat.equals(SPECIAL_ATTACK)
                || stat.equals(SPECIAL_DEFENCE) || stat.equals(SPEED) || stat.equals(TOTAL_BASE_STATS)) {

            String[] columnsToReturn = {POKEMON_UNIQUE_ID,};
            String[] selectionArg = {String.valueOf(lowerLimit), String.valueOf(upperLimit)};

            Cursor cursor = database.query(POKEMON_UNIQUE_INFO_TABLE, columnsToReturn, stat + ">=? AND " + stat + "<=?", selectionArg, null, null, null);
            int uniqueIDColIndex = cursor.getColumnIndex(POKEMON_UNIQUE_ID);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                int uniqueID = cursor.getInt(uniqueIDColIndex);
                filteredUniqueids.add(uniqueID);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return filteredUniqueids;

    }


    /**
     * Filters all national ID's that are of the eggGroup requested
     *
     * @param eggGroupID of the type in question
     * @return array list of ints corresponding to national id
     */
    public ArrayList<Integer> getAllNationalIdsFromEggGroup(int eggGroupID){

        ArrayList<Integer> filteredNationalids = new ArrayList<>();

        if(eggGroupID > 0) {
            //EggGroupID's start at 0
            String[] selectionArg = {String.valueOf(eggGroupID - 1 )};
            Cursor cursor = database.query(POKEMON_EGG_GROUPS_TABLE, null, EGG_GROUP_ID + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int nationalID = cursor.getInt(cursor.getColumnIndex(POKEMON_NATIONAL_ID));
                filteredNationalids.add(nationalID);
                cursor.moveToNext();
            }
            cursor.close();

        }
        return filteredNationalids;

    }

    /**
     * Filters all national ID's that are of the Generation first appeared requested
     *
     * @param generationID of the generation in question
     * @return array list of ints corresponding to national id
     */
    public ArrayList<Integer> getAllNationalIdsFromGenerationFirstAppeared(int generationID){

        ArrayList<Integer> filteredNationalids = new ArrayList<>();

        if(generationID > 0) {
            String[] selectionArg = {String.valueOf(generationID)};
            Cursor cursor = database.query(POKEMON_COMMON_INFO_TABLE, null, GENERATION_FIRST_APPEARED + "=?", selectionArg, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int nationalID = cursor.getInt(cursor.getColumnIndex(POKEMON_NATIONAL_ID));
                filteredNationalids.add(nationalID);
                cursor.moveToNext();
            }
            cursor.close();

        }
        return filteredNationalids;

    }

    /**
     * Takes list of unique id's and returns the corresponding non-repeating national ids
     *
     * @param uniqueIDList in an array list
     * @return nationalid;s in array list
     */
    public ArrayList<Integer> convertUniqueToNational(ArrayList<Integer> uniqueIDList){
        long startTime = System.nanoTime();
        ArrayList<Integer> nationalIDList = new ArrayList<>();
        for(int uniqueid : uniqueIDList){
            if(!nationalIDList.contains(allNationalIDMappedUnique[uniqueid])) {
                nationalIDList.add(allNationalIDMappedUnique[uniqueid]);
            }
        }

        Collections.sort(nationalIDList);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        Log.d("QPDex", String.format("Converting Unique ID's to National IDs took %s ns", duration));

        return nationalIDList;


    }

    public boolean isDetailedNationalIDBuiltAndReady(int nationalID){
        return detailedPokemonShortList!=null && detailedPokemonShortList[nationalID]!=null;
    }

    /**
     * Constructs a sparse matrix of the type effectivenesses.
     *
     * This should be done on initiation of the {@link PokemonFactory}
     *
     * @return sparse matrix of the types effectiveness against each other
     */
    private double[][] buildTypeEffectivenessTable() {
        int maxTypeID = getMaxTypeID();
        double[][] typeEffectivenessTable = new double[maxTypeID + 1][maxTypeID + 1];

        // get the whole table from the database
        Cursor cursor = database.query(TYPE_EFFECTIVENESS_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();

        // read the database and set the values for all real types
        for (int i = 0; i < maxTypeID * maxTypeID; i++) {
            typeEffectivenessTable[cursor.getInt(cursor.getColumnIndex(FROM_TYPE_ID))][cursor.getInt(cursor.getColumnIndex(TO_TYPE_ID))] = cursor.getDouble(cursor.getColumnIndex(EFFECTIVE_LEVEL));
            cursor.moveToNext();
        }

        // set the values for none/bird type
        for (int i = 0; i < maxTypeID + 1; i++) {
            typeEffectivenessTable[0][i] = 1;
            typeEffectivenessTable[i][0] = 1;
        }

        return typeEffectivenessTable;
    }

    /**
     * Getter for typeEffectiveness sparse matrix.
     *
     * @return sparse matrix of the types effectiveness against each other
     */
    public double[][] getTypeEffectivenessTable() {
        return typeEffectiveness;
    }

    /**
     * getter for the list of types
     * @return array of types
     */
    public Type[] getAllTypes() {
        return types;
    }

    /**
     * getter for max unique id
     * @return int of maximum unique id
     */
    public int getMAX_UNIQUE_ID() { return MAX_UNIQUE_ID; }

    /**
     * getter for max national id
     * @return int of maximum national id
     */
    public int getMAX_NATIONAL_ID() { return MAX_NATIONAL_ID;}


}



