package com.boardgame.friday.cards;

import com.boardgame.friday.GameActivity;

import java.util.logging.Logger;

/**
 * HazardCard
 *
 * Represents a Card, specifically one residing in the Hazard deck (at
 * least initially). HazardCard is special because if/when the player
 * "defeats" the hazard, the card is flipped 180 degrees and converted
 * to a RobinsonCard. That means we need the standard information
 * associated with a Card, plus some special info about the hazard
 * (three rounds worth of strength values and a number of free cards
 * the player gets to draw), plus some info about the fighting card
 * the player can win (new name, attack strength, ability).
 *
 * HazardCard objects should only ever live in one of three places
 * in the main class:
 *   o hazardDeck:
 *      - Before before being drawn in any given round
 *      - After being drawn but not chosen (in discard pile associated
 *        with deck)
 *      - After being chosen, but player opts to lose against hazard
 *        (in discard pile associated with deck)
 *   o drawnHazard, after having been chosen
 *   o robinsonDeck, after having been converted to a RobinsonCard
 *
 * Card objects don't have to handle their own animation - the main
 * class will simply grab each card's image and draw it himself.
 *
 * @author  Corey Marchetti
 */
public class HazardCard extends Card {
    private static final Logger LOGGER = Logger.getLogger(GameActivity.class.getName());

    private int numFreeDraws;           // number of cards user can draw for free to fight hazard
    private int[] hazardStrength;       // strength of hazard in green, yellow, red rounds
    private String robinsonCardName;    // name of card once converted to Robinson card

    /**
     * Create a new hazard card by setting hazard-specific fields manually before
     * invoking the Card constructor.
     *
     * @param   hazardCardName      the name of the hazard
     * @param   numFreeDraws        number of cards user can draw for free to fight hazard
     * @param   hazardStrength      strength of hazard in green, yellow, red rounds
     * @param   robinsonCardName    name of card once converted to RobinsonCard
     * @param   attackStrength      attack strength once converted to RobinsonCard
     * @param   costToRemove        life point cost to remove once converted to RobinsonCard
     * @param   cardAbility         card ability once converted to RobinsonCard
     * @param   cardImage           card image
     */
    public HazardCard(String hazardCardName, int numFreeDraws, int[] hazardStrength,
                      String robinsonCardName, int attackStrength, int costToRemove,
                      Ability cardAbility, int cardImage){
        super(hazardCardName, attackStrength, costToRemove, cardAbility,
                Position.VERTICAL, cardImage);

        this.numFreeDraws = numFreeDraws;
        this.hazardStrength = hazardStrength;   // expecting 3-length array (G,Y,R strengths)
        this.robinsonCardName = robinsonCardName;

        LOGGER.finer("HazardCard <" + hazardCardName + "> has been created");
        LOGGER.finer("Number of free draws:           " + numFreeDraws);
        LOGGER.finer("Strength in green round:        " + hazardStrength[0]);
        LOGGER.finer("Strength in yellow round:       " + hazardStrength[1]);
        LOGGER.finer("Strength in red round:          " + hazardStrength[2]);
        LOGGER.finer("Name as RobinsonCard:           " + robinsonCardName);
        LOGGER.finer("Strength as RobinsonCard:       " + attackStrength);
        LOGGER.finer("Cost to remove as RobinsonCard: " + costToRemove);
        LOGGER.finer("Ability as RobinsonCard:        " + cardAbility);
    }

    /**
     * convertToRobinsonCard
     *
     * Convert a HazardCard to a RobinsonCard. What that really means is, we'll
     * take the info given to us describing the characteristics of the RobinsonCard
     * the player should receive upon defeating this hazard, and we'll create a new
     * RobinsonCard object to return to our caller. The caller should add the new
     * card into the RobinsonDeck discard pile and just throw away the existing
     * HazardCard object, as it's no longer needed.
     *
     * @return  the RobinsonCard created from this HazardCard
     */
    public RobinsonCard convertToRobinsonCard(){
        RobinsonCard newCard = new RobinsonCard(
                robinsonCardName, getAttackStrength(), getCostToRemove(), getCardAbility(), getCardImage());

        LOGGER.finer("Hazard <" + getCardName() + "> has been converted into Robinson card <" +  newCard.getCardName() + ">");

        return newCard;
    }

    /**
     * getHazardStrength
     *
     * Return the strength of the hazard for the current round.
     * The round enum is defined in GameActivity.
     *
     * @param   round   current round (green, yellow, or red)
     */
    public int getHazardStrength(int round){
        return hazardStrength[round];
    }
}
