package me.quadphase.qpdex.pokemon;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : QPdex (C) 2015
//  @ File Name : Evolution.java
//  @ Date : 21-Jul-15
//  @ Author : Nicole
//
//


import java.util.LinkedList;
import java.util.List;

/** */
public class Evolution {
    /**
     * Condition needed to evolve into this pokemon
     */
    private String condition;

    /**
     * List of pokemon that the current pokemon evolves into
     */
    private List<Evolution> evolvesInto;

    /**
     * Unique ID for the pokemon in question
     */
    private int uniquepokemonID;

    /**
     * minimal pokemon that represent the pokemon
     */
    private MinimalPokemon minimalPokemon;


    /**
     * Constructor
     */
    public Evolution(String condition, int uniquepokemonID, MinimalPokemon minimalPokemon) {
        this.condition = condition;
        this.uniquepokemonID = uniquepokemonID;
        this.minimalPokemon = minimalPokemon;
        evolvesInto = new LinkedList<>();
    }


    /**
     * Getters
     */
    public String getCondition() {
        return condition;
    }

    public List<Evolution> getEvolvesInto() {
        return evolvesInto;
    }

    public int getUniquepokemonID() {
        return uniquepokemonID;
    }


    public MinimalPokemon getMinimalPokemon() {
        return minimalPokemon;
    }
}
