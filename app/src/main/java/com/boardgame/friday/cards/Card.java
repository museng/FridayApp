package com.boardgame.friday.cards;

/**
 * Card
 *
 * Represents a Card, which is further broken down into HazardCard (a
 * card describing a hazard the player must fight), RobinsonCard (a
 * fighting card the player uses to fight hazards), and AgingCard (a
 * special type of RobinsonCard which has a negative effect).
 *
 * A Card has just a handful of characteristics:
 *  cardName        a name
 *  attackStrength  its attack strength when used as a fighting card
 *  costToRemove    a cost to remove from the Robinson deck
 *  cardAbility     id of the ability the card has, described by the
 *                  Ability enum. Note that there is a slot in the
 *                  enum representing "No Ability"! All cards must have
 *                  an Ability
 *  isTapped        true if the card can be tapped (that is, clicked
 *                  to turn card 270 degrees, and activate the card's
 *                  ability) - cards always begin not tapped
 *  canBeTapped     only cards with abilities can be tapped
 *  defaultCardPosition     the default position for a card - most default
 *                          to vertical, but pirates are always horizontal
 *  cardImage               an image for the front of the card
 *
 * This class is abstract because a Card should never exist on its
 * own. A new Card should always have a very specific type.
 *
 * Card objects don't have to handle their own animation - the main
 * class will simply grab each card's image and draw it himself.
 *
 * @author  Corey Marchetti
 */
public abstract class Card {
    // Abilities a card can have
    public static enum Ability {
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
        STOP_DRAWING,       // 16
    }

    // Position of a card
    //  Horizontal for Pirate cards and "tapped" cards
    //  Vertical for everything else
    public static enum Position {
        HORIZONTAL,         // 0
        VERTICAL            // 1
    }

    private String cardName;
    private int attackStrength;
    private int costToRemove;
    private Ability cardAbility;
    private boolean isTapped;
    private boolean canBeTapped;
    private Position defaultCardPosition;
    private int cardImage;

    /**
     * Create a new Card. This really just exists so every sub-type of
     * Card can call super's constructor.
     *
     * @param   cardName            the name of the card
     * @param   attackStrength      attack strength
     * @param   costToRemove        life point cost to remove
     * @param   cardAbility         card ability
     * @param   cardImage           card image
     */
    public Card(String cardName, int attackStrength, int costToRemove,
                Ability cardAbility, Position defaultCardPosition,
                int cardImage){
        this.cardName = cardName;
        this.attackStrength = attackStrength;
        this.costToRemove = costToRemove;

        this.cardAbility = cardAbility;
        if (getCardAbility() != Ability.NO_ABILITY)
            canBeTapped = true;     // card with abilities can be tapped!

        this.defaultCardPosition = defaultCardPosition;
        if (defaultCardPosition == Position.HORIZONTAL)
            canBeTapped = false;    // horizontal cards can't be tapped!

        this.cardImage = cardImage;
        isTapped = false;                   // cards always untapped to start
        //isFaceDown = true;                  // cards always face down to start
    }

    // Getters for everybody! We'll only set once, so no setters...
    public String getCardName(){
        return cardName;
    }
    public int getAttackStrength(){
        return attackStrength;
    }
    public int getCostToRemove(){
        return costToRemove;
    }
    public Ability getCardAbility(){ return cardAbility; }
    // TODO: Do other stuff? Maybe error message if card can't be tapped?
    public void tapCard() { if (canBeTapped) isTapped = true; }
    public boolean cardIsTapped() {
        return isTapped;
    }
    public int getCardImage() { return cardImage; }
}
