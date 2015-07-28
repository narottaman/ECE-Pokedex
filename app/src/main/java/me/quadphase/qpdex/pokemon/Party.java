package me.quadphase.qpdex.pokemon;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : QPdex (C) 2015
//  @ File Name : Party.java
//  @ Date : 21-Jul-15
//  @ Author : Nicole
//
//


import java.util.LinkedList;
import java.util.List;

/**
 * These <code>Pokemon</code> are in your party. They each have a particular set of moves.
 */
public class Party {

    /**
     * Max: 6
     */
    private List<Pokemon> pokemon;

    /**
     * 1 move set per Pokemon in the <code>pokemon</code> list
     */
    private List<MoveSet> moveSets;

    /**
     * Constructor to make an empty party
     */
    public Party() {
        this.pokemon = new LinkedList<Pokemon>();
        this.moveSets = new LinkedList<MoveSet>();
    }

    /**
     * Adds a pokemon to the pokemon party.
     *
     * @param p - pokemon to be added
     * @param m - {@link MoveSet } that contains up to 4 moves that the pokemon knows
     * @return <code>true</code> if there was a spot available and the pokemon is successfully added
     * <code>false</code> if the party already had 6 pokemon, so the pokemon is not added
     */
    public boolean addPokemonToParty(Pokemon p, MoveSet m) {
        if (pokemon.size() < 6) {
            // TODO: Add the pokemon to the party with its moveset
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a pokemon from the pokemon party.
     *
     * @param p - pokemon to be removed from the party
     */
    public void removePokemonFromParty(Pokemon p) {
        // TODO: remove a pokemon
    }

    /**
     * Add a move to a pokemon in the pokemon party.
     *
     * @param p    - pokemon in the party to add the move to
     * @param move - move to be added to the pokemon
     * @return <code>true</code> if p is in party and move can be added to it and has been added
     * <code>false</code> if p is not in party or move is not valid for p or moveset is full
     */
    public boolean addMoveToPokemon(Pokemon p, Move move) {
        // TODO: verify that pokemon is in party
        // TODO: verify that move is allowed for pokemon
        // TODO: verify that moveset is not full
        // TODO: add move to moveset
        return true;
    }

    /**
     * Removes a move from a pokemon in the party.
     *
     * @param p    - pokemon to remove the move from
     * @param move - move to remove
     */
    public void removeMoveFromPokemon(Pokemon p, Move move) {
        // TODO: verify that the pokemon is in the party
        // TODO: verify that the pokemon has learned the move
        // TODO: remove the move
    }
}
