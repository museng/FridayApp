package com.boardgame.friday;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boardgame.friday.cards.Card;
import com.boardgame.friday.cards.Deck;
import com.boardgame.friday.cards.HazardCard;
import com.boardgame.friday.cards.RobinsonCard;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;


// ==========================================
//    Overall to dos, somewhat prioritized
// ==========================================
// TODO: BUG - when you click Robinson deck too fast, it sometimes breaks
// TODO: BUG - why are drawn cards painted so far apart?!
// ==========================================
// TODO: Add checks for death (player lifePoints < 0)
// TODO: Add choice to spend life points on extra cards (right now it's forced)
// TODO: Add ability to lose against hazards (and trash cards - "trash" deck so we keep 'em around?)
// TODO: Add ability for user to choose between two hazards at the start of each turn
// TODO: Add support for abilities!
// TODO: Add ability to view discard piles and count decks
// TODO: Make the UI pretty!
// TODO: Add pirates
// TODO: All classes use the GameActivity logger - each class should use its own?!
// TODO: Animation should be cleaner, the way it works right now it's sort of sloppy
// TODO: Need better artwork - maybe small very minimalistic cards and you click to see full text?
// TODO: Add more animation
// TODO: Add some "help" type stuff, explanation of abilities, etc - basically add the rule book!
// TODO: Add stat tracking and high scores, maybe back them up to a DB on AWS
// TODO: Add support for multiple levels
// TODO: Add music?

/**
 * GameActivity
 *
 * This class handles everything associated with the game board, including
 * but not limited to:
 *  o Starting the game
 *  o Allowing the player to choose
 */
public class GameActivity extends AppCompatActivity {

    private static final int CARD_TRASH_REQUEST = 0;

    public static final int CARD_SCALE = 150;  // scale cards to this size

    private static final Logger LOGGER = Logger.getLogger(GameActivity.class.getName());
    //private static final Level LOG_LEVEL = Level.SEVERE; // Turn off logging
    private static final Level LOG_LEVEL = Level.ALL;
    public static String LOG_NAME = "/friday.log";

    final static float ALPHA_HIDE = 0f;
    final static float ALPHA_SHOW = 1f;     // TODO: Is this needed?

    private static boolean canDrawRobinsonCard = false;

    public enum GameLevel {
        LEVEL_ONE,      // 0
        LEVEL_TWO,      // 1
        LEVEL_THREE,    // 2
        LEVEL_FOUR      // 3
    }
    public GameLevel currentLevel;

    public enum Round {
        GREEN,      // 0
        YELLOW,     // 1
        RED         // 2
    }
    public Round currentRound;

    public int numFreeDrawsLeft;

    Player player;

    // Various decks of cards...
    private Deck robinsonDeck;
    private Deck agingDeck;
    private Deck hazardDeck;
    private Deck trashDeck;
    private HazardCard drawnHazard; // The currently drawn hazard

    // TODO: There's probably a better way to do this
    private ImageView drawnHazardImage;
    private LinearLayout playerHandLayout;
    private ImageView drawnCardFront;
    private ImageView drawnCardBack;
    private TextView robinsonDrawCounter;
    private TextView robinsonStrength;
    private Button robinsonForfeit;

    private TextView freeDrawsCounter;
    private TextView lifePointsCounter;

    private int currentPlayerStrength;
    private int currentHazardStrength;

    // Animation sets
    private AnimatorSet flipRightOut;   // Flip
    private AnimatorSet flipLeftIn;
    private AnimatorSet slideCard;

    // TODO: We'll eventually get rid of these (or enhance them)
    Toast addedAgingCardToast;
    Toast defeatedHazardToast;
    Toast lostToHazardToast;
    Toast greenRoundToast;
    Toast yellowRoundToast;
    Toast redRoundToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        initLog();      // first thing's first - prepare the log

        // Set up all of those views
        drawnHazardImage = (ImageView)findViewById(R.id.drawn_hazard);
        playerHandLayout = (LinearLayout)findViewById(R.id.player_hand_layout);
        drawnCardFront = (ImageView)findViewById(R.id.drawn_card_front);
        drawnCardBack = (ImageView)findViewById(R.id.drawn_card_back);
        robinsonDrawCounter = (TextView)findViewById(R.id.robinson_counter);
        robinsonStrength = (TextView)findViewById(R.id.robinson_strength);
        robinsonForfeit = (Button) findViewById(R.id.robinson_forfeit);

        freeDrawsCounter = (TextView)findViewById(R.id.num_free_draws);
        lifePointsCounter = (TextView)findViewById(R.id.player_life);

        // Set up animators
        flipRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.animator.flip_right_out);
        flipLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.animator.flip_left_in);
        slideCard = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.animator.slide_card);

        // Init all decks
        player = new Player();  // this also inits player hand "deck"
        trashDeck = new Deck();
        initHazardDeck();
        initRobinsonDeck();
        initAgingDeck();

        createToasts();

        // TODO: This is a test, probably a better way to do all of this
        currentLevel = GameLevel.LEVEL_ONE;
        currentRound = Round.GREEN;
        greenRoundToast.show();

        currentPlayerStrength = 0;
        robinsonStrength.setText(Integer.toString(currentPlayerStrength));

        lifePointsCounter.setText(Integer.toString(player.getLifePoints()));

        // Drawing a hazard card signals the start of the game
        // TODO: Eventually player should click a button to draw hazard so this won't look as dumb
        drawHazardCard();
        canDrawRobinsonCard = true;
    }

    // Create all of the toasts for the entire game up front
    // TODO: GET RID OF THE TOASTS
    public void createToasts(){
        Context context =  getApplicationContext();
        String text = "Happy Birthday, Robinson! Added one aging card...";
        int duration = Toast.LENGTH_SHORT;
        addedAgingCardToast = Toast.makeText(context, text, duration);

        text = "Defeated hazard!";
        defeatedHazardToast = Toast.makeText(context, text, duration);

        text = "Lost to hazard!";
        lostToHazardToast = Toast.makeText(context, text, duration);

        text = "GREEN";
        greenRoundToast = Toast.makeText(context, text, duration);

        text = "YELLOW";
        yellowRoundToast = Toast.makeText(context, text, duration);

        text = "RED";
        redRoundToast = Toast.makeText(context, text, duration);
    }

    // Init the logger - I shamelessly stole this from a javacodegeeks article
    public void initLog(){
        Handler consoleHandler;     // for writing out to the console
        Handler fileHandler;        // for writing out to a file

        String logPath = getApplicationContext().getFilesDir().getPath().toString();
        String logName = LOG_NAME;
        LOG_NAME = logPath.concat(logName);

        try{
            // Creating consoleHandler and fileHandler
            consoleHandler = new ConsoleHandler();
            fileHandler  = new FileHandler(LOG_NAME);

            // Assigning handlers to LOGGER object
            LOGGER.addHandler(consoleHandler);
            LOGGER.addHandler(fileHandler);

            // Setting levels to handlers and LOGGER
            consoleHandler.setLevel(LOG_LEVEL);
            fileHandler.setLevel(LOG_LEVEL);
            LOGGER.setLevel(LOG_LEVEL);

            // All done!
            LOGGER.config("Log configured");
            LOGGER.config("Full log path is: " + LOG_NAME);
        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occurred in FileHandler", exception);
        }
    }

    // Create all of the Cards present in the initial Robinson deck
    // and add them to a new Deck object. Shuffle up the deck so
    // it's ready to go.
    public void initRobinsonDeck(){
        robinsonDeck = new Deck();

        LOGGER.fine("Initializing Robinson deck");

        robinsonDeck.addCard(
                new RobinsonCard("eating", 0, 1, Card.Ability.PLUS_TWO_LIFE, R.drawable.eating));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("weak", 0, 1, Card.Ability.NO_ABILITY, R.drawable.weak));
        robinsonDeck.addCard(
                new RobinsonCard("distracted", -1, 1, Card.Ability.NO_ABILITY, R.drawable.distracted));
        robinsonDeck.addCard(
                new RobinsonCard("distracted", -1, 1, Card.Ability.NO_ABILITY, R.drawable.distracted));
        robinsonDeck.addCard(
                new RobinsonCard("distracted", -1, 1, Card.Ability.NO_ABILITY, R.drawable.distracted));
        robinsonDeck.addCard(
                new RobinsonCard("distracted", -1, 1, Card.Ability.NO_ABILITY, R.drawable.distracted));
        robinsonDeck.addCard(
                new RobinsonCard("distracted", -1, 1, Card.Ability.NO_ABILITY, R.drawable.distracted));
        robinsonDeck.addCard(
                new RobinsonCard("focused", 1, 1, Card.Ability.NO_ABILITY, R.drawable.focused));
        robinsonDeck.addCard(
                new RobinsonCard("focused", 1, 1, Card.Ability.NO_ABILITY, R.drawable.focused));
        robinsonDeck.addCard(
                new RobinsonCard("focused", 1, 1, Card.Ability.NO_ABILITY, R.drawable.focused));
        robinsonDeck.addCard(
                new RobinsonCard("genius", 2, 1, Card.Ability.NO_ABILITY, R.drawable.genius));

        LOGGER.fine("Robinson deck created, contains " + robinsonDeck.getDrawPileSize() + " cards");

        robinsonDeck.shuffleDeck();

        // TODO: should this be done here?!
        robinsonDrawCounter.setText(Integer.toString(robinsonDeck.getDrawPileSize()));
    }

    // Create all Hazard Cards, create a Deck, and shuffle it up.
    public void initHazardDeck(){
        hazardDeck = new Deck();

        // All "with the raft to the wreck" hazard cards
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_food,
                        "food", 0, 1, Card.Ability.PLUS_ONE_LIFE,
                        R.drawable.food_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_food,
                        "food", 0, 1, Card.Ability.PLUS_ONE_LIFE,
                        R.drawable.food_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_equipment,
                        "equipment", 0, 1, Card.Ability.PLUS_TWO_CARD,
                        R.drawable.equipment_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_equipment,
                        "equipment", 0, 1, Card.Ability.PLUS_TWO_CARD,
                        R.drawable.equipment_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_books,
                        "books", 0, 1, Card.Ability.PHASE_MINUS_ONE,
                        R.drawable.books_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_strategy,
                        "strategy", 0, 1, Card.Ability.EXCHANGE_TWO,
                        R.drawable.strategy_raft));
        hazardDeck.addCard(
                new HazardCard("with the raft to the wreck", 1, new int[]{0, 1, 3},
                        Card.Ability.NO_ABILITY, R.drawable.raft_realization,
                        "realization", 0, 1, Card.Ability.DESTROY_ONE,
                        R.drawable.realization_raft));

        // All "exploring the island" hazard cards
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_food,
                        "food", 1, 1, Card.Ability.PLUS_ONE_LIFE,
                        R.drawable.food_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_food,
                        "food", 1, 1, Card.Ability.PLUS_ONE_LIFE,
                        R.drawable.food_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_mimicry,
                        "mimicry", 1, 1, Card.Ability.COPY_ONE,
                        R.drawable.mimicry_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_repeat,
                        "repeat", 1, 1, Card.Ability.DOUBLE_ONE,
                        R.drawable.repeat_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_deception,
                        "deception", 1, 1, Card.Ability.ONE_BELOW_STACK,
                        R.drawable.deception_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_realization,
                        "realization", 1, 1, Card.Ability.DESTROY_ONE,
                        R.drawable.realization_exploring));
        hazardDeck.addCard(
                new HazardCard("exploring the island", 2, new int[]{1, 3, 6},
                        Card.Ability.NO_ABILITY, R.drawable.exploring_weapon,
                        "weapon", 2, 1, Card.Ability.NO_ABILITY,
                        R.drawable.weapon_exploring));

        // All "further exploring the island" hazard cards
        hazardDeck.addCard(
                new HazardCard("further exploring the island", 3, new int[]{2, 5, 8},
                        Card.Ability.NO_ABILITY, R.drawable.further_experience,
                        "experience", 2, 1, Card.Ability.PLUS_ONE_CARD,
                        R.drawable.experience_further));
        hazardDeck.addCard(
                new HazardCard("further exploring the island", 3, new int[]{2, 5, 8},
                        Card.Ability.NO_ABILITY, R.drawable.further_food,
                        "food", 2, 1, Card.Ability.PLUS_ONE_LIFE,
                        R.drawable.food_further));
        hazardDeck.addCard(
                new HazardCard("further exploring the island", 3, new int[]{2, 5, 8},
                        Card.Ability.NO_ABILITY, R.drawable.further_strategy,
                        "strategy", 2, 1, Card.Ability.EXCHANGE_ONE,
                        R.drawable.strategy_further));
        hazardDeck.addCard(
                new HazardCard("further exploring the island", 3, new int[]{2, 5, 8},
                        Card.Ability.NO_ABILITY, R.drawable.further_repeat,
                        "repeat", 2, 1, Card.Ability.DOUBLE_ONE,
                        R.drawable.repeat_further));
        hazardDeck.addCard(
                new HazardCard("further exploring the island", 3, new int[]{2, 5, 8},
                        Card.Ability.NO_ABILITY, R.drawable.further_vision,
                        "vision", 2, 1, Card.Ability.SORT_THREE,
                        R.drawable.vision_further));

        // All "wild animals" hazard cards
        hazardDeck.addCard(
                new HazardCard("wild animals", 4, new int[]{4, 7, 11},
                        Card.Ability.NO_ABILITY, R.drawable.animals_realization,
                        "realization", 3, 1, Card.Ability.DESTROY_ONE,
                        R.drawable.realization_animals));
        hazardDeck.addCard(
                new HazardCard("wild animals", 4, new int[]{4, 7, 11},
                        Card.Ability.NO_ABILITY, R.drawable.animals_vision,
                        "vision", 3, 1, Card.Ability.SORT_THREE,
                        R.drawable.vision_animals));
        hazardDeck.addCard(
                new HazardCard("wild animals", 4, new int[]{4, 7, 11},
                        Card.Ability.NO_ABILITY, R.drawable.animals_strategy,
                        "strategy", 3, 1, Card.Ability.EXCHANGE_ONE,
                        R.drawable.strategy_animals));

        // All "cannibals" hazard cards
        hazardDeck.addCard(
                new HazardCard("cannibals", 5, new int[]{5, 9, 14},
                        Card.Ability.NO_ABILITY, R.drawable.cannibals_weapon,
                        "weapon", 4, 1, Card.Ability.NO_ABILITY,
                        R.drawable.weapon_cannibals));
        hazardDeck.addCard(
                new HazardCard("cannibals", 5, new int[]{5, 9, 14},
                        Card.Ability.NO_ABILITY, R.drawable.cannibals_weapon,
                        "weapon", 4, 1, Card.Ability.NO_ABILITY,
                        R.drawable.weapon_cannibals));

        hazardDeck.shuffleDeck();
    }

    // Create all Aging Cards, create a Deck, and shuffle it up. The Aging
    // deck is special because there are three cards which need to go on the
    // bottom (the "old" aging cards), so we'll actually create two decks
    // (white, gray), shuffle them up, then draw off the top of each to
    // build the final single Aging deck.
    public void initAgingDeck(){
        agingDeck = new Deck();

        Deck grayAging = new Deck();
        Deck whiteAging = new Deck();

        grayAging.addCard(
                new RobinsonCard("hungry", 0, 2, Card.Ability.MINUS_ONE_LIFE, R.drawable.hungry));
        grayAging.addCard(
                new RobinsonCard("scared", 0, 2, Card.Ability.HIGHEST_CARD_ZERO, R.drawable.scared));
        grayAging.addCard(
                new RobinsonCard("scared", 0, 2, Card.Ability.HIGHEST_CARD_ZERO, R.drawable.scared));
        grayAging.addCard(
                new RobinsonCard("very tired", 0, 2, Card.Ability.STOP_DRAWING, R.drawable.very_tired));
        grayAging.addCard(
                new RobinsonCard("distracted", -1, 2, Card.Ability.NO_ABILITY, R.drawable.aging_distracted));
        grayAging.addCard(
                new RobinsonCard("stupid", -2, 2, Card.Ability.NO_ABILITY, R.drawable.stupid));
        grayAging.addCard(
                new RobinsonCard("stupid", -2, 2, Card.Ability.NO_ABILITY, R.drawable.stupid));

        // Only add in the "very stupid" card on levels 3 or 4
        if (currentLevel == GameLevel.LEVEL_THREE || currentLevel == GameLevel.LEVEL_FOUR) {
            grayAging.addCard(
                    new RobinsonCard("very stupid", -3, 2, Card.Ability.NO_ABILITY, R.drawable.very_stupid));
        }

        whiteAging.addCard(
                new RobinsonCard("moronic", -4, 2, Card.Ability.NO_ABILITY, R.drawable.moronic));
        whiteAging.addCard(
                new RobinsonCard("very hungry", 0, 2, Card.Ability.MINUS_TWO_LIFE, R.drawable.very_hungry));
        whiteAging.addCard(
                new RobinsonCard("suicidal", -4, 2, Card.Ability.NO_ABILITY, R.drawable.suicidal));

        grayAging.shuffleDeck();
        whiteAging.shuffleDeck();

        // Add shuffled white cards to real aging deck
        int whiteSize = whiteAging.getDrawPileSize();
        for (int i = 0; i < whiteSize; i++){
            agingDeck.addCard(whiteAging.drawCardOffTop());
        }

        // Add shuffled gray cards to real aging deck
        int graySize = grayAging.getDrawPileSize();
        for (int j = 0; j < graySize; j++){
            agingDeck.addCard(grayAging.drawCardOffTop());
        }
    }

    public void drawHazardCard(){
        // If there are no cards left in the draw pile, add an aging card
        // and shuffle up the deck
        if (hazardDeck.getDrawPileSize() == 0) {
            LOGGER.fine("[drawHazardCard] Tried to draw HazardCard but deck was empty");

            // TODO: Incrementing round can be cleaner - do we really want to use an enum & toasts?

            if (currentRound == Round.GREEN){
                LOGGER.fine("[drawHazardCard] " + currentRound + " round has ended");

                currentRound = Round.YELLOW;
                yellowRoundToast.show();

                LOGGER.fine("[drawHazardCard] Moving to " + currentRound);
            }else if (currentRound == Round.YELLOW){
                LOGGER.fine("[drawHazardCard] " + currentRound + " round has ended");

                currentRound = Round.RED;
                redRoundToast.show();

                LOGGER.fine("[drawHazardCard] Moving to " + currentRound);
            }else if (currentRound == Round.RED) {
                // TODO: Handle going to pirates
            }

            hazardDeck.shuffleDeck();

            LOGGER.fine("[drawHazardCard] We shuffled the Hazard deck");
        }

        drawnHazard = (HazardCard) hazardDeck.drawCardOffTop();

        LOGGER.fine("[drawHazardCard] Drew hazard card = " + drawnHazard.getCardName());

        BitmapDecoder bd = new BitmapDecoder();
        Bitmap cardImage = bd.decodeResource(getApplicationContext(), drawnHazard.getCardImage(), CARD_SCALE);
        drawnHazardImage.setImageBitmap(cardImage);

        currentHazardStrength = drawnHazard.getHazardStrength(currentRound.ordinal());

        LOGGER.fine("[drawHazardCard] Painted hazard card = " + drawnHazard.getCardName() + " onto board");

        setNumFreeDraws(drawnHazard.getNumFreeDraws());
    }

    private void setNumFreeDraws(int numFreeDraws){
        numFreeDrawsLeft = numFreeDraws;
        freeDrawsCounter.setText(Integer.toString(numFreeDrawsLeft));
    }

    /**
     * drawRobinsonCard
     *
     * Draw the top card off the Robinson deck and add it to the player's hand.
     *
     * If the deck is empty, we'll add an aging card and shuffle the deck, which
     * will refill the draw pile from the discard pile in the process.
     *
     * Called when the Robinson deck is clicked by the player.
     */
    public void drawRobinsonCard(View view){
        if (canDrawRobinsonCard) {
            // Lock this action down! The player can't draw again until we're done
            canDrawRobinsonCard = false;

            LOGGER.fine("[drawRobinsonCard] canDrawRobinsonCard has been set to false");

            // If there are no cards left in the draw pile, add an aging card and shuffle the deck
            if (robinsonDeck.drawPileIsEmpty()) {
                LOGGER.fine("[drawRobinsonCard] Tried to draw RobinsonCard but deck was empty");

                Card addAgingCard = agingDeck.drawCardOffTop();

                LOGGER.fine("[drawRobinsonCard] Adding aging card <" + addAgingCard.getCardName() + ">");
                LOGGER.fine("[drawRobinsonCard] Aging deck has " + agingDeck.getDrawPileSize() + " cards left");

                robinsonDeck.discardCard(addAgingCard);
                robinsonDeck.shuffleDeck();

                // TODO: Is this how we want to handle notifications?
                addedAgingCardToast.show();

                LOGGER.fine("[drawRobinsonCard] Shuffled the Robinson deck");
            }

            // TODO: This should be the player's choice, whether to spend life points on more draws
            if (numFreeDrawsLeft <= 0){
                setPlayerLifePoints(-1);
            }

            // Draw a card and add it to the player's hand
            player.addCardToHand(robinsonDeck.drawCardOffTop());

            LOGGER.fine("[drawRobinsonCard] Robinson deck has " + robinsonDeck.getDrawPileSize() + " cards left");

            // TODO: Clean up the "cards left in the deck" display (when UI gets improved)
            robinsonDrawCounter.setText(Integer.toString(robinsonDeck.getDrawPileSize()));

            // Animate the newly drawn card into the player's hand
            animateRobinsonCardToHand(player.getSizeOfHand() - 1);

            // The player is now free to draw another card
            canDrawRobinsonCard = true;

            LOGGER.fine("[drawRobinsonCard] canDrawRobinsonCard has been set to true");

            setNumFreeDraws(--numFreeDrawsLeft);
        }else{
            LOGGER.fine("[drawRobinsonCard] canDrawRobinsonCard is false, can't draw Robinson card right now");
        }
    }

    private void setPlayerLifePoints(int lifePoints){
        player.updateLifePoints(lifePoints);
        lifePointsCounter.setText(Integer.toString(player.getLifePoints()));
    }



    // Animate the newly drawn RobinsonCard being added into the PlayerHand
    // The way this works is sort of sloppy and it should probably change eventually
    private void animateRobinsonCardToHand(int cardIndex) {
        // Get front image of new card
        // TODO: Remove debug line
        System.out.println("Called animateRobinsonCardToHand, index = " + cardIndex);
        RobinsonCard cardToAnim = (RobinsonCard)player.peekCardInHand(cardIndex);

        BitmapDecoder bd = new BitmapDecoder();
        Bitmap cardImage = bd.decodeResource(getApplicationContext(), cardToAnim.getCardImage(), CARD_SCALE);

        final Bitmap finalImage = cardImage;    // "final" within this scope, so we can pass to listener
        drawnCardFront.setImageBitmap(cardImage);
        drawnCardBack.setAlpha(1f);             // make the card back visible

        currentPlayerStrength += cardToAnim.getAttackStrength(); // will set in listener below

        // Animate the card flip - first, set up the AnimatorSets
        flipRightOut.setTarget(drawnCardBack);
        flipLeftIn.setTarget(drawnCardFront);
        // Move the card image once we're done with flipping the front over
        flipLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO: Remove debug line
                System.out.println("Called onAnimationEnd for flipLeftIn");
                animation.removeAllListeners();         // So we don't flip the card again
                drawnCardFront.setAlpha(ALPHA_HIDE);

                ImageView newCard = new ImageView(GameActivity.this);
                newCard.setImageBitmap(finalImage);

                // Using linear layout because that's what these images will live in
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER_VERTICAL;
                int sizeInDP = 2;
                int marginInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        sizeInDP, getResources().getDisplayMetrics());
                //lp.setMargins(marginInDP, 0, marginInDP, 0);
                newCard.setLayoutParams(lp);

                // Add card to player hand
                playerHandLayout.addView(newCard, 0);

                // Update the button which displays the current strength of the player's hand
                robinsonStrength.setText(Integer.toString(currentPlayerStrength));
            }

            public void onAnimationStart(Animator animation) {}
            public void onAnimationCancel(Animator animation) {}
            public void onAnimationRepeat(Animator animation) {}
        });
        // Start the animation
        flipRightOut.start();
        flipLeftIn.start();
    }

    // Discard player hand - this version would get called from the activity,
    // hence the view parameter, but I can't remember if this is ever needed...
    public void endPlayerTurn(View view){
        LOGGER.fine("[endPlayerTurn] Player clicked the end turn button");

        checkHazardDefeated();
    }

    // Check whether the current hazard has been defeated and, if so:
    //   1. Convert the current hazard to a RobinsonCard
    //   2. Add the new card to the Robinson deck's discard pile
    //   3. Draw a new hazard
    // TODO: Give the player a choice, right now this button is a death sentence
    private void checkHazardDefeated() {
        LOGGER.fine("[checkHazardDefeated] Check whether player defeated hazard");

        if (currentPlayerStrength >= currentHazardStrength){
            LOGGER.fine("[checkHazardDefeated] Player defeated hazard");

            // Add defeated Hazard to RobinsonDeck discard pile
            robinsonDeck.discardCard(drawnHazard.convertToRobinsonCard());
            discardHand();          // throw away current hand
            drawHazardCard();       // draw a new hazard
        }else{
            LOGGER.fine("[checkHazardDefeated] Player did not defeat hazard");

            // Start CardTrasherActivity, passing player's hand via the intent
            Intent intent = new Intent(this, CardTrasherActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(CardTrasherActivity.PLAYER_HAND, player.getHandDeck());
            intent.putExtras(bundle);
            startActivityForResult(intent, CARD_TRASH_REQUEST);

            // TODO: This should cost life points

            hazardDeck.discardCard(drawnHazard);    // discard unbeaten hazard
            //discardHand();                          // throw away current hand
            drawHazardCard();                       // draw a new hazard
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to

            /*
            if (requestCode == CARD_TRASH_REQUEST) {
                boolean[] trashArray = data.getBooleanArrayExtra(CardTrasherActivity.TRASH_ARRAY);

                // Reverse the array so when we pull cards out of the player's hand, the indexes
                // of the cards we have yet to remove don't get shifted to different indexes!
                for (int i = trashArray.length; i >= 0; i--){
                    if (trashArray[i]){
                        LOGGER.fine("Throwing away card " + player.getHandDeck().drawCardAtIndex(i) +
                                " which was flagged for removal.");

                        trashDeck.addCard(player.getHandDeck().drawCardAtIndex(i));
                    }
                }

                discardHand();                          // throw away what's left of hand
            }*/
        }
    }

    // Discard player hand - this version gets called from within this
    // class, i.e. when the player has beaten the hazard. Since we're not
    // calling from the activity, there's no view to pass in
    public void discardHand(){
        // Throw away in the player's hand onto the Robinson deck discard pile
        for (int i = 0; i < player.getSizeOfHand() - 1; i++){
            robinsonDeck.discardCard(player.takeTopCardFromHand());
        }

        // Remove all card images from the player hand scrollview
        playerHandLayout.removeAllViews();

        // Update the player hand strength counter
        currentPlayerStrength = 0;
        robinsonStrength.setText(Integer.toString(currentPlayerStrength));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
