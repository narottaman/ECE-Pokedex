package me.quadphase.qpdex.pokemon;

import android.content.Context;

import java.util.List;

import me.quadphase.qpdex.databaseAccess.PokemonFactory;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : QPdex (C) 2015
//  @ File Name : Pokemon.java
//  @ Date : 21-Jul-15
//  @ Author : Nicole
//
//


/**
 * Used to describe the base characteristics of each Pokemon
 */
public class Pokemon {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //NOTE:
    // PokemonUniqueID: one-to-one mapping to a specific pokemon, including Mega Evolution and gendered
    // PokemonNationalID: one-to-many mapping used to identify pokemon by a common name, but without
    //                  relation to specific stats. This the number shown in the Pokedex.
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * List of the abilities that the Pokemon has
     * MAX: 3
     */
    public List<Ability> abilities;
    /**
     * Pokemon ID as the guaranteed Unique Key in our application
     */
    private int pokemonUniqueID;

    /**
     * Pokemon National ID as registered in the Pokedex
     */
    private int pokemonNationalID;

    /**
     * Name of the Pokemon in English
     */
    private String name;
    /**
     * Description of the Pokemon from the latest generation
     */
    private String description;
    /**
     * Height of the Pokemon
     */
    private double height;
    /**
     * Weight of the Pokemon
     */
    private double weight;
    /**
     * Stats: Attack
     */
    private int attack;
    /**
     * Stats: Defence
     */
    private int defence;
    /**
     * Stats: HP
     */
    private int hp;
    /**
     * Stats: Special Attack
     */
    private int spAttack;
    /**
     * Stats: Special Defence
     */
    private int spDefence;
    /**
     * Stats: Speed
     */
    private int speed;
    /**
     * true if Pokemon is caught, false if not caught
     */
    private Boolean caught;
    /**
     * generation that the Pokemon first appeared
     */
    private int genFirstAppeared;
    /**
     * Number of steps to hatch the egg
     */
    private int hatchTime;
    /**
     * Base catch rate percentage
     */
    private int catchRate;
    /**
     * Percentage of males
     * to find number of females, do (100 - <code>genderRatioMale</code> )
     */
    private int genderRatioMale;
    /**
     * List of the locations where the Pokemon can be found
     */
    private List<Location> locations;
    /**
     * List of the moves that the Pokemon can learn
     */
    private List<Move> moves;

    /**
     * List of the types that the Pokemon has
     * MAX: 2
     */
    private List<Type> types;

    /**
     * List of the egg groups that the Pokemon belongs to
     * MAX: 2
     */
    private List<EggGroup> eggGroups;

    /**
     * List of the evolutions that the pokemon can have
     */
    private List<Evolution> evolutions;


    /**
     * Constructor
     */
    public Pokemon(int pokemonUniqueID, int pokemonNationalID, String name, String description, double height, double weight,
                   int attack, int defence, int hp, int spAttack, int spDefence, int speed,
                   boolean caught, int genFirstAppeared, int hatchTime, int catchRate,
                   int genderRatioMale, List<Location> locations, List<Ability> abilities,
                   List<Move> moves, List<Type> types, List<EggGroup> eggGroups,
                   List<Evolution> evolutions) {
        this.pokemonUniqueID = pokemonUniqueID;
        this.pokemonNationalID = pokemonNationalID;
        this.name = name;
        this.description = description;
        this.height = height;
        this.weight = weight;
        this.attack = attack;
        this.defence = defence;
        this.hp = hp;
        this.spAttack = spAttack;
        this.spDefence = spDefence;
        this.speed = speed;
        this.caught = caught;
        this.genFirstAppeared = genFirstAppeared;
        this.hatchTime = hatchTime;
        this.catchRate = catchRate;
        this.genderRatioMale = genderRatioMale;
        this.locations = locations;
        this.abilities = abilities;
        this.moves = moves;
        this.types = types;
        this.eggGroups = eggGroups;
        this.evolutions = evolutions;
    }

    /**
     * Constructor with {@link MinimalPokemon}, which has basic pokemon information nationalID,
     * name, description, and type(s)
     *
     */
    public Pokemon(MinimalPokemon MinimalPokemon, int pokemonUniqueID, double height, double weight, int attack,
                   int defence, int hp, int spAttack, int spDefence, int speed, boolean caught,
                   int genFirstAppeared, int hatchTime, int catchRate, int genderRatioMale,
                   List<Location> locations, List<Ability> abilities, List<Move> moves,
                   List<EggGroup> eggGroups, List<Evolution> evolutions) {
        this.pokemonNationalID = MinimalPokemon.getPokemonNationalID();
        this.pokemonUniqueID = pokemonUniqueID;
        this.name = MinimalPokemon.getName();
        this.description = MinimalPokemon.getDescription();
        this.height = height;
        this.weight = weight;
        this.attack = attack;
        this.defence = defence;
        this.hp = hp;
        this.spAttack = spAttack;
        this.spDefence = spDefence;
        this.speed = speed;
        this.caught = caught;
        this.genFirstAppeared = genFirstAppeared;
        this.hatchTime = hatchTime;
        this.catchRate = catchRate;
        this.genderRatioMale = genderRatioMale;
        this.locations = locations;
        this.abilities = abilities;
        this.moves = moves;
        this.types = MinimalPokemon.getTypes();
        this.eggGroups = eggGroups;
        this.evolutions = evolutions;
    }

    /**
     * Getters for the Pokemon information:
     */
    public List<Ability> getAbilities() {
        return abilities;
    }

    public int getPokemonUniqueID() {
        return pokemonUniqueID;
    }

    public int getPokemonNationalID() {
        return pokemonNationalID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefence() {
        return defence;
    }

    public int getHp() {
        return hp;
    }

    public int getSpAttack() {
        return spAttack;
    }

    public int getSpDefence() {
        return spDefence;
    }

    public int getSpeed() {
        return speed;
    }

    public Boolean getCaught() {
        return caught;
    }

    public int getGenFirstAppeared() {
        return genFirstAppeared;
    }

    public int getHatchTime() {
        return hatchTime;
    }

    public int getCatchRate() {
        return catchRate;
    }

    public int getGenderRatioMale() {
        return genderRatioMale;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public List<Type> getTypes() {
        return types;
    }

    public List<EggGroup> getEggGroups() {
        return eggGroups;
    }

    public List<Evolution> getEvolutions() {
        return evolutions;
    }

    /**
     * toggles whether or not a pokemon has been caught
     */
    public void toggleCaught(Context currentContext) {
        caught = !caught;
        PokemonFactory.getPokemonFactory(currentContext).setCaught(this.pokemonNationalID,this.caught);
    }

    @Override
    public String toString(){
        return String.format("%s. %s (unique:%s)",pokemonNationalID,name,pokemonUniqueID);
    }

    public int retrieveStatFromString(String specific){
        int statVal = 0;
        if(specific.equals("hp")){
            statVal = this.hp;
        }
        else if(specific.equals("attack")){
            statVal = this.attack;
        }
        else if(specific.equals("defense")){
            statVal = this.defence;
        }
        else if(specific.equals("spatk")){
            statVal = this.spAttack;
        }
        else if(specific.equals("spdef")){
            statVal = this.spDefence;
        }
        else if(specific.equals("speed")){
            statVal = this.speed;
        }
        return statVal;
    }

}
