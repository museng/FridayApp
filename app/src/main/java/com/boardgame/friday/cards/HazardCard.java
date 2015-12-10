package com.boardgame.friday.cards;

import com.boardgame.friday.GameActivity;

import java.util.logging.Logger;

/**
 * HazardCard
 *
 * Represents a Card, specifically one residing in the Hazard deck (at
 * least initially). HazardCard is special because if/when the player
 * "defeats" the hazard, the card is "converted" to a RobinsonCard.
 * That means we need the standard information associated with a Card,
 * plus some special info about the hazard (three rounds worth of strength
 * values and a number of free cards the player gets to draw), plus some
 * info about the fighting card the player can win (new name, attack
 * strength, ability).
 *
 * HazardCard objects should only ever live in one of three places
 * in the main class:
 *   o hazardDeck, in the main class:
 *      - Before before being drawn in any given round
 *      - After being drawn but not chosen (in discard pile associated
 *        with deck)
 *      - After being chosen, but player opts to lose against hazard
 *        (in discard pile associated with deck)
 *   o drawnHazard, in the main class, after having been chosen
 *   o robinsonDeck, in the main class, after having been converted to a RobinsonCard
 *
 * Card objects don't have to handle their own animation - the main
 * class will simply grab each card's image and draw it himself.
 *
 * @author  Corey Marchetti
 */
public class HazardCard extends Card {
    private static final Logger LOGGER = Logger.getLogger(GameActivity.class.getName());

    // Hazard cards can be chosen by the user (the other will be discarded)
    public enum Status {
        NONE,       // 0
        SELECTED    // 1
    }
    private Status cardStatus;

    // Abilities a card can have
    public enum HazardAbility {
        MINUS_ONE_LIFE,     // 0
        MINUS_TWO_LIFE,     // 1
        HIGHEST_CARD_ZERO,  // 2
        STOP_DRAWING,       // 3
    }

    private int numFreeDraws;           // number of cards user can draw for free to fight hazard
    private int[] hazardStrength;       // strength of hazard in green, yellow, red rounds
    private RobinsonCard converted;

    /**
     * Create a new hazard card.
     *
     * Invoke Card's constructor before setting the hazard-specific fields, including
     * creating a RobinsonCard so we have something to pass back to the player after
     * he converts this hazard to a fighting card.
     *
     * @param   hazardName              the name of the hazard
     * @param   hazardFreeDraws         number of cards user can draw for free to fight hazard
     * @param   hazardStrength          strength of hazard in green, yellow, red rounds
     * @param   hazardImage             image for hazard version of card
     * @param   robinsonName            name of card once converted to RobinsonCard
     * @param   robinsonAttackStrength  attack strength once converted to RobinsonCard
     * @param   robinsonCostToRemove    life point cost to remove once converted to RobinsonCard
     * @param   robinsonAbility         card ability once converted to RobinsonCard
     * @param   robinsonImage           card image once converted to RobinsonCard
     */
    public HazardCard(String hazardName, int hazardFreeDraws, int[] hazardStrength,
                      Ability hazardAbility, int hazardImage,
                      String robinsonName, int robinsonAttackStrength, int robinsonCostToRemove,
                      Ability robinsonAbility, int robinsonImage){
        super(hazardName, hazardAbility, hazardImage);

        this.numFreeDraws = hazardFreeDraws;
        this.hazardStrength = hazardStrength;   // expecting 3-length array (G,Y,R strengths)
        cardStatus = Status.NONE;

        converted = new RobinsonCard(
                robinsonName, robinsonAttackStrength, robinsonCostToRemove,
                robinsonAbility, robinsonImage);

        LOGGER.finer("HazardCard <" + hazardName + "> has been created");
        LOGGER.finer("Number of free draws:           " + numFreeDraws);
        LOGGER.finer("Strength in green round:        " + hazardStrength[0]);
        LOGGER.finer("Strength in yellow round:       " + hazardStrength[1]);
        LOGGER.finer("Strength in red round:          " + hazardStrength[2]);
        LOGGER.finer("Name as RobinsonCard:           " + robinsonName);
        LOGGER.finer("Strength as RobinsonCard:       " + robinsonAttackStrength);
        LOGGER.finer("Cost to remove as RobinsonCard: " + robinsonCostToRemove);
        LOGGER.finer("Ability as RobinsonCard:        " + robinsonAbility);
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
    public RobinsonCard convertToRobinsonCard(){ return converted; }

    public int getNumFreeDraws() { return numFreeDraws; }
    public int getHazardStrength(int round){ return hazardStrength[round]; }
    public void flagStatusSelected(){ cardStatus = Status.SELECTED; }
    public void flagStatusNone(){ cardStatus = Status.NONE; }
    public boolean isFlaggedForSelect() { return (cardStatus == Status.SELECTED); }
}
