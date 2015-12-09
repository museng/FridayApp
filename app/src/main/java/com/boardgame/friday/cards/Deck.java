package com.boardgame.friday.cards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Deck
 *
 * Represents a "deck" of Card objects, which really exists as two separate
 * lists: the list of cards still able to be drawn (drawPile) and the list
 * of cards which have been discarded, and should eventually be re-shuffled
 * into the drawPile (discardPile).
 *
 * The caller is responsible for properly handling the drawn card. We just
 * sort of pass the Card object out by removing it from the drawPile. After
 * it leaves our custody we have no idea what happens to it. The caller is
 * responsible for keeping it safe and returning it to the discard pile
 * when he's done with it.
 *
 * We don't assume what the caller will want to fill the deck with, so
 * we just provide a simple interface for the caller to play with - pass
 * us a bunch of Card objects using the addCard method and then call
 * shuffleDeck to mix them up. The ArrayList class has a "shuffle" method
 * so we can really mix up the list and then just pop off the top. No
 * need to do anything too funky. Ultimately, this class functions sort
 * of like a queue. Add to the end, pop off from the front.
 *
 * @author  Corey Marchetti
 */
public class Deck implements Serializable{
    private ArrayList<Card> drawPile;
    private ArrayList<Card> discardPile;

    /**
     * Create a new Deck. This essentially just means initializing
     * the draw and discard piles. Easy peasy.
     */
    public Deck(){
        drawPile = new ArrayList<Card>();
        discardPile = new ArrayList<Card>();
    }

    /**
     * addCard
     *
     * Add a card to the bottom of the draw pile, i.e. the end
     * of the list.
     */
    public void addCard(Card newCard){ drawPile.add(newCard); }

    /**
     * shuffleDeck
     *
     * Mix up the draw pile, using the standard Collections.shuffle
     * method, some number of times between 10 and 50.
     *
     * This is an attempt at getting a reasonably randomized deck.
     * We're doing it like this, rather than just using a random index
     * into the static deck when we draw, because order within the deck
     * matters for certain cards (i.e. "re-order top three cards"; or
     * "put card under pile"). It's really just easier to "shuffle" the
     * actual list and then draw from the front.
     */
    public void shuffleDeck(){
        if (getDrawPileSize() == 0){
            drawPile = discardPile;
            discardPile = new ArrayList<Card>();
        }

        int numShuffles = new Random().nextInt(49) + 10;
        for (int i = 0; i < numShuffles; i++) {
            Collections.shuffle(drawPile, new Random());
        }
    }

    /**
     * drawCardOffTop
     *
     * Return the top card from the draw pile.
     *
     * Caller is returned a card, which is fully removed from the deck.
     * The caller is responsible for making sure there are cards left to
     * draw (so he could invoke some sort of "refill from discard" or "add
     * new card" animation, if so desired). Additionally, once they draw
     * it's no longer our responsibility to keep track of the card we gave
     * him. It's up to him to return it to the proper discard pile!
     */
    public Card drawCardOffTop(){
        return drawPile.remove(0);
    }

    /**
     * Return the card at a particular index.
     *
     * @param index
     * @return
     */
    public Card drawCardAtIndex(int index){ return drawPile.remove(index); }

    /**
     * peekCard
     *
     * Return a card from the deck without actually removing it.
     *
     * Useful for grabbing a specific card's image, etc.
     */
    public Card peekCard(int index){
        if (index > getDrawPileSize()-1) return null;
        return drawPile.get(index);
    }

    /**
     * discardCard
     *
     * Add a card to the top of the discard pile.
     *
     * This is different than adding to the draw pile, where the card
     * gets added to the bottom. In this case, we want to add to the
     * top so we have the option of viewing the most recently discarded
     * card first just by peeking at the top of the discard list.
     *
     * Just like real life, it's up to the caller to add their card to
     * the correct discard pile. We can't help you here.
     */
    public void discardCard(Card discard){ discardPile.add(0, discard); }

    // Get pile sizes
    public int getDrawPileSize(){ return drawPile.size(); }
    public boolean drawPileIsEmpty(){ return getDrawPileSize() == 0; }
    public int getDiscardPileSize(){ return discardPile.size(); }
}