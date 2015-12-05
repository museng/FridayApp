package com.boardgame.friday.cards;

//TODO: Do we need this useless parent class?

/**
 * Card
 *
 * Represents a Card, which is further broken down into HazardCard (a
 * card describing a hazard the player must fight), RobinsonCard (a
 * fighting card the player uses to fight hazards), AgingCard (a
 * special type of RobinsonCard which has a negative effect), and
 * PirateCard (a hazard-like card which acts as the boss of the game).
 *
 * This class really only exists so we can refer to any of the child
 * class types using the name 'Card'. Cards don't handle their own
 * drawing to the screen (GameActivity does that) and the different
 * sub-types don't even really have all that much in common. As such,
 * a Card has just two characteristics:
 *  cardName        the name of the card
 *  cardAbility     the ability the card has (if none, then "NO_ABILITY")
 *  cardImage       the image of the front of the card
 *
 * @author  Corey Marchetti
 */
public class Card {
    // Abilities a card can have
    public enum Ability {
        NO_ABILITY,         // 0
        PLUS_ONE_LIFE,      // 1
        PLUS_TWO_LIFE,      // 2
        PLUS_ONE_CARD,      // 3
        PLUS_TWO_CARD,      // 4
        DESTROY_ONE,        // 5
        DOUBLE_ONE,         // 6
        COPY_ONE,           // 7
        PHASE_MINUS_ONE,    // 8
        SORT_THREE,         // 9
        EXCHANGE_ONE,       // 10
        EXCHANGE_TWO,       // 11
        ONE_BELOW_STACK,    // 12
        MINUS_ONE_LIFE,     // 13
        MINUS_TWO_LIFE,     // 14
        HIGHEST_CARD_ZERO,  // 15
        STOP_DRAWING        // 16
    }

    private String cardName;
    private Ability cardAbility;
    private int cardImage;

    /**
     * Create a new Card.
     *
     * @param   cardName        the name of the card
     * @param   cardAbility     the ability the card has
     * @param   cardImage       the image of the front of the card
     */
    public Card(String cardName, Ability cardAbility, int cardImage){
        this.cardName = cardName;
        this.cardAbility = cardAbility;
        this.cardImage = cardImage;
    }

    public String getCardName(){ return cardName; }
    public Ability getCardAbility() { return cardAbility; }
    public int getCardImage(){ return cardImage; }
}
