package com.boardgame.friday.cards;

// TODO: Can this class be deleted, and the parent class Card can just describe a Robinson card by default?!

import com.boardgame.friday.GameActivity;

import java.util.logging.Logger;

/**
 * RobinsonCard
 *
 * Represents a Card, specifically one residing in the Robinson deck.
 *
 * RobinsonCard objects should only ever live in one of two places in
 * the main class:
 *   o robinsonDeck
 *      - Before before being drawn
 *      - After being discarded (in discard pile associated with deck)
 *   o playerHand, in the main class, when a card has been drawn
 *     during a turn
 *   o trashDeck, in the main class, after being removed from the game
 *
 * Card objects don't have to handle their own animation - the main
 * class will simply grab each card's image and draw it himself.
 *
 * @author  Corey Marchetti
 */
public class RobinsonCard extends Card{
    private static final Logger LOGGER = Logger.getLogger(GameActivity.class.getName());

    private int attackStrength;
    private int costToRemove;

    // Robinson cards can be flagged for trash (i.e. player chooses to remove them
    // from the game)
    public enum Status {
        NONE,       // 0
        TRASH       // 1
    }
    private Status cardStatus;

    /**
     * Create a new Robinson card by invoking the Card constructor.
     *
     * @param   cardName            the name of the card
     * @param   attackStrength      attack strength
     * @param   costToRemove        life point cost to remove
     * @param   cardAbility         card ability
     * @param   cardImage           card image
     */
    public RobinsonCard(String cardName, int attackStrength, int costToRemove,
                        Ability cardAbility, int cardImage){
        super(cardName, cardAbility, cardImage);

        this.attackStrength = attackStrength;
        this.costToRemove = costToRemove;
        cardStatus = Status.NONE;

        LOGGER.finer("RobinsonCard <" + cardName + "> has been created");
        LOGGER.finer("Strength:       " + attackStrength);
        LOGGER.finer("Cost to remove: " + costToRemove);
        LOGGER.finer("Ability:        " + cardAbility);
    }

    public int getAttackStrength(){ return attackStrength; }
    public int getCostToRemove(){ return costToRemove; }
    public void flagStatusTrash(){ cardStatus = Status.TRASH; }
    public void flagStatusNone(){ cardStatus = Status.NONE; }
    public boolean isFlaggedForTrash() { return (cardStatus == Status.TRASH); }
}
