package culminatingproject;

/** **********
 * Project: CulminatingProject
 * Programmer: Amro Issa
 * Due Date: Feb 4, 2022
 * File: GameManager.java
 ************ */


import java.util.Scanner;
import java.util.Collections;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

enum FramesEnum {
    MainMenu,
    Game,
    HowToPlay,
    Highscores,
}

enum PlayerEnum{
    LocalPlayer,
    Computer
}

public class GameManager {
    static Hashtable<PlayerEnum, ArrayList<Card>> Hands = new Hashtable<>();
    static ArrayList<Card> DrawDeck = new ArrayList<>(), DiscardPile = new ArrayList<>();

    static ArrayList<PlayerEnum> playerTurnOrder; //the turn order in which the game rotates
    static PlayerEnum turn;
    static int turnIndex = -1;

    static Card cardInDiscardPile; //top card in the discard pile
    static boolean gameOver; //keeps track of game state
    static String username; //the username the player entered (if they did)
    static int roundNumber; //keeps track of round number

    /**
    BeginNewRound
    Begins a new round by moving the turn to the next player
    */
    public static void BeginNewRound() {
        MoveTurnToNextPlayer();
        roundNumber++;
        UI.roundNumberLabel.setText("Round: " + roundNumber);

        ArrayList<Integer> indicesOfPlayableCards = GetIndicesOfPlayableCards(turn);

        if (turn != PlayerEnum.LocalPlayer) {
            //A SwingWorker is just a separate thread that runs a specific method
            //Creating a SwingWorker in order to freeze the computer's turn (so that the computer appears to be "thinking" instead of instantaneously playing a card)
            //Since this SwingWorker runs in a separate thread, it won't affect the GUI and the GUI will be able to update regardless of whether this thread is frozen or not
            new SwingWorker(){
                //code to execute MUST be in this overriden method
                @Override
                protected Object doInBackground() throws Exception {
                    FreezeThread(2000); //freezing thread to make computer appear to be "thinking" about a move to play

                    int choice = GetBestPlayIndexForComputer();

                    if (choice == -1) //Drawing a card if computer can't place down anything
                    {
                        Card card = DrawCard(PlayerEnum.Computer);

                        if (IsCardPlayable(card))
                        {
                            //playing the drawn card if it is playable
                            UI.AddToGameFeed("Computer drew a " + card.GetCardName() + " and played it"); //adding this action to the game feed
                            PlaceCard(PlayerEnum.Computer, card); //places card and begins a new round
                        } else //if the card is not playable
                        {
                            UI.AddToGameFeed("Computer drew a card"); //adding this action to the game feed
                            BeginNewRound(); //begins a new round
                        }
                    }
                    else //Placing down a card from hand
                    {
                        PlaceCard(PlayerEnum.Computer, choice);
                    }

                    return null;
                }
            }.execute(); //must call execution of this method straight away
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Initializing Methods">
    /**
    Initialize
    Initializes the game by resetting values, prompting username input, creating draw deck and dealing hands
    */
    public static void Initialize() {
        username = UI.DisplayInputDialog(FramesEnum.Game, "Enter a name to be entered into the highscore system:"); //prompting username input
        
        if (username == null || username.isEmpty()) {
            return;
        }
        
        ResetGame(); //resets game values

        UI.ShowFrame(FramesEnum.Game);
        
        //Initializing hands
        for (PlayerEnum playerEnum : PlayerEnum.values()) {
            Hands.put(playerEnum, new ArrayList<Card>());
        }

        InitializeDrawDeck();
        DealHands();

        //shuffling turn order to see who starts
        Collections.shuffle(playerTurnOrder);
        UI.AddToGameFeed(playerTurnOrder.get(0) + " starts!"); //adding this action to the game feed

        Card startingCard = DrawDeck.get(Utilities.GetRandomInt(0, DrawDeck.size(), true, false));
        while (Utilities.IsWildCard(startingCard.type)) {
            //draws another card to be the starting card if the current one is a wild card (cannot start on a wild card)
            startingCard = DrawDeck.get(Utilities.GetRandomInt(0, DrawDeck.size(), true, false));
        }
        startingCard.hand = DrawDeck;
        startingCard.MoveToDiscardPile();

        //aligning card labels for the first time
        UI.AlignCardLabels(PlayerEnum.LocalPlayer);
        UI.AlignCardLabels(PlayerEnum.Computer);

        //begins first round
        BeginNewRound();
    }
    public static void InitializeDrawDeck(){
        /*              DECK BREAKDOWN
        4 different colors, each containing:
        1 "Zero"
        2 of each from "One" to "Nine"
        2 of each of "Skip", "Reverse" and "Plus 2"
        Wild cards:
        4 of "Change Color"
        4 of "Plus 4"
        To simplify the game a bit, there won't be any customizable or swap hands wild cards
        */

        for (CardColorEnum color : CardColorEnum.values()) {
            if (color != CardColorEnum.Any) {
                //creating number and action cards
                for (CardTypeEnum type : Utilities.GetNonWildCardTypes()) {
                    if (type == CardTypeEnum.Zero) {
                        DrawDeck.add(new Card(color, CardTypeEnum.Zero, null, DrawDeck));
                    }
                    else {
                        //generating the rest of the number and action cards
                        for (int i = 0; i < 2; i++) //for loop needed since there are 2 of each card
                        {
                            DrawDeck.add(new Card(color, type, null, DrawDeck));
                        }
                    }

                }
            }
            else{
                //creating wild cards
                for (int i = 0; i < 4; i++) {
                    for (CardTypeEnum wildCard : Utilities.GetWildCardTypes()) {
                        DrawDeck.add(new Card(CardColorEnum.Any, wildCard, null, DrawDeck));
                    }
                }
            }
        }

        //shuffling draw deck
        Collections.shuffle(DrawDeck);
    }
    public static void DealHands(){
        for (PlayerEnum value : PlayerEnum.values()) {
            DrawCard(value, 7);
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="DrawCard">
    public static Card DrawCard(PlayerEnum player) {
        //checks to see if draw deck is empty and restocks it before trying to draw another card
        if (DrawDeck.isEmpty()) {
            RestockDrawDeck();
        }

        //gets the first element in draw deck (P.S. deck is already shuffled from the initialization)
        Card card = DrawDeck.get(0);
        card.MoveToHand(player);

        return card;
    }
    public static void DrawCard(PlayerEnum player, int count) {
        for (int i = 0; i < count; i++) {
            DrawCard(player);
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="PlaceCard">
    public static Card PlaceCard(PlayerEnum player, Card card) {
        if (Utilities.IsActionOrWildCard(card.type)) {
            //If the card is an action or wild card, we call the cast method to cast its specific action
            CastCard(player, GetNextPlayer(), card);
        }
        card.MoveToDiscardPile();
        BeginNewRound();
        IsGameOver(); //checks if the game is over (if the player got rid of all their cards)

        return card;
    }
    public static Card PlaceCard(PlayerEnum player, int index) {
        Card card = Hands.get(player).get(index);
        return PlaceCard(player, card);
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Casting Methods">
    /**
    CastCard
    Calls a specific action based on what the card type entered is
    @param castingPlayer - the player casting this card
    @param playerToCastTo - the player receiving the cast
    @param card - the card to cast
    */
    public static void CastCard(PlayerEnum castingPlayer, PlayerEnum playerToCastTo, Card card){
        switch (card.type) //calling different methods based on the type of card placed down
        {
            case Skip:
                Skip();
                break;
            case Reverse:
                Reverse();
                break;
            case Plus2:
                Plus(2, playerToCastTo);
                break;
            case Plus4:
                ChangeColor(castingPlayer, card);
                Plus(4, playerToCastTo);
                break;
            case ChangeColor:
                ChangeColor(castingPlayer, card);
                break;
        }
    }
    /**
    Skip
    Skips the turn of the next player
    */
    public static void Skip() {
        MoveTurnToNextPlayer(); //although this looks like it does the opposite by moving the turn to the next player, the "BeginNextTurn" method will move the turn once again, causing the player's turn to be skipped
        UI.AddToGameFeed(turn + "'s turn has been skipped!");
    }
    /**
    Reverse
    Reverses the turn order of the game (skips the next turn if it is a 2-player game
    */
    public static void Reverse() {
        if (playerTurnOrder.size() == 2) {
            //if the game has only two players
            Skip();
        }
        else{
            Collections.reverse(playerTurnOrder);
            turnIndex = playerTurnOrder.size() - 1 - turnIndex; //adjusts index back to the one that holds the casting player
        }
        UI.AddToGameFeed("Play order has been reversed!");
    }
    /**
    Plus
    Adds the number of cards specified to the specified player's hand
    @param numOfCards - # of cards to add
    @param playerToCastTo - the player receiving the cards
    */
    public static void Plus(int numOfCards, PlayerEnum playerToCastTo) {
        DrawCard(playerToCastTo, numOfCards);
        Skip();
        UI.AddToGameFeed(String.format("Plus %s! %s drew %s cards!", numOfCards, playerToCastTo, numOfCards));
    }
    /**
    PromptChangeColor
    Changes the color to the one specified by the user or to a random one for the computer
    @param player - player changing color
    @param card - the card that is allowing the color to be changed
    */
    public static void ChangeColor(PlayerEnum player, Card card) {
        if (player == PlayerEnum.LocalPlayer) //player
        {
            int choice;
            do{
                Object[] options = {CardColorEnum.Red, CardColorEnum.Green, CardColorEnum.Blue, CardColorEnum.Yellow};
                choice = UI.DisplayOptionDialog(FramesEnum.Game, "Change Color", options);
            }
            while (choice == -1); //if player closed the window (didn't choose a color), it opens again until they do

            card.color = CardColorEnum.values()[choice];
        }
        else //computer
        {
            card.color = Utilities.GetRandomColor();
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Playable Cards">
    /**
    IsCardPlayable
    Checks if the card is playable by comparing type or color
    @return - boolean - whether the card is playable or not
    */
    public static boolean IsCardPlayable(Card card) {
        if (card.color == cardInDiscardPile.color || card.type == cardInDiscardPile.type || Utilities.IsWildCard(card.type)) {
            return true;
        }

        return false;
    }
    /**
    GetIndicesOfPlayableCards
    Gets all the indices of all the playable cards in the hand of the specified player
    */
    public static ArrayList<Integer> GetIndicesOfPlayableCards(PlayerEnum player) {
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Card> hand = Hands.get(player);

        for (int i = 0; i < hand.size(); i++) {
            if (IsCardPlayable(hand.get(i))) {
                indices.add(i);
            }
        }

        return indices;
    }
    /**
    GetPlayableCards
    Gets all the playable cards in the hand of the specified player
    */
    public static ArrayList<Card> GetPlayableCards(PlayerEnum player) {
        ArrayList<Card> hand = Hands.get(player);
        ArrayList<Card> playableCards = new ArrayList<>();

        for (Card card : hand) {
            if (IsCardPlayable(card)) {
                playableCards.add(card);
            }
        }

        playableCards.add(null);

        return playableCards;
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="GetPlayer">
    /**
    Gets the next player in the turn order without actually moving to the next turn
    @return - the player next in turn
    */
    public static PlayerEnum GetNextPlayer() {
        //using try catch to see if the index to get the next player is out of bounds, so we catch the exception and return the player at index 0 (because we looped back around)
        try {
            return playerTurnOrder.get(turnIndex + 1);
        }
        catch (IndexOutOfBoundsException e) {
            return playerTurnOrder.get(0);
        }
    }
    /**
    Gets a player from their hand that was passed in, or returns null if that hand doesn't belong to any player
    @param hand - the hand of the player
    @return - PlayerEnum or null if the hand belongs to no player
    */
    public static PlayerEnum GetPlayerEnum(ArrayList<Card> hand){
        for (PlayerEnum player : PlayerEnum.values()) {
            if (Hands.get(player) == hand) {
                return player;
            }
        }

        return null;
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="GetBestPlayableCard">
    /**
    GetBestPlayCardForComputer
    Gets the index of the best card to play for the computer or -1 if the computer can't play anything and needs to draw
    @return - index of card or -1 signifying card draw
    */
    public static int GetBestPlayIndexForComputer() {
        ArrayList<Integer> playableCards = GetIndicesOfPlayableCards(PlayerEnum.Computer);

        if (playableCards.isEmpty()) {
            //if no card can be played
            return -1;
        }

        ArrayList<Card> computerHand = Hands.get(PlayerEnum.Computer);

        //If player has 1 card left, computer will try to play the best card to stop the player from winning
        if (Hands.get(GetNextPlayer()).size() == 1) {
            ArrayList<CardTypeEnum> actionAndWildCardTypes = (ArrayList<CardTypeEnum>) Arrays.asList(Utilities.GetActionAndWildCardTypes());
            Collections.reverse(actionAndWildCardTypes); //reversing the collection to get them in order of strongest to weakest action or wild card
            for (CardTypeEnum type : actionAndWildCardTypes) { //iterating over the action and wild card types (from best (Plus4) to worst (reverse))
                for (int index : playableCards) { //going through each playable card to see if it matches the current type
                    if (computerHand.get(index).type == type) {
                        return index;
                    }
                }
            }
        }
        //make it so that the computer doesn't play high value cards when it's not urgent to do so
        //Computer randomly chooses a card to play
        return playableCards.get(Utilities.GetRandomInt(0, playableCards.size(), true, false));
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Misc">
    /**
    MoveTurnToNextPlayer
    Moves the current turn to the next player in the turn order
    */
    public static void MoveTurnToNextPlayer() {
        //if index to get the next player in turn is out of bounds, we catch the exception and get the turn at index 0 (looped back around)
        try {
            turn = playerTurnOrder.get(++turnIndex); //incrementing turn index then getting the turn at that new index
        }
        catch (IndexOutOfBoundsException e) {
            turnIndex = 0;
            turn = playerTurnOrder.get(0);
        }
    }
    /**
    IsGameOver
    Checks if the game is over and if so, applies necessary actions, like adding player who won to the highscores system
    @return - boolean - whether the game is over or not
    */
    public static boolean IsGameOver(){
        if (!GameManager.gameOver) {
            for (ArrayList<Card> hand : GameManager.Hands.values()) {
                if (hand.isEmpty())
                {
                    UI.AddToGameFeed("GAME OVER!");

                    if (GameManager.GetPlayerEnum(hand) == PlayerEnum.LocalPlayer) {
                        UI.DisplayGameOver(true);

                        //if the username isn't null or empty, that means the player entered a name so as to be entered into the highscores if they win
                        if (username != null && !username.isEmpty()) {
                            UI.AddToHighscores(username + " " + roundNumber);
                        }
                    }
                    else{
                        UI.DisplayGameOver(false);
                        UI.AddToHighscores("computer " + roundNumber);
                    }

                    GameManager.gameOver = true;
                    return true;
                }
            }
            return false;
        }
        else{
            return true;
        }
    }
    public static void FreezeThread(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void RestockDrawDeck() {
        DrawDeck.addAll(DiscardPile); //adding all the cards in discard pile to draw deck
        Collections.shuffle(DrawDeck); //shuffling draw deck
        DiscardPile.clear(); //clearing discard pile

        UI.AddToGameFeed("Draw deck has been restocked...");
    }
    public static void ResetGame(){
        UI.ResetGameUI();

        username = null;
        roundNumber = 0;
        gameOver = false;
        Hands.clear();
        DrawDeck.clear();
        DiscardPile.clear();
        cardInDiscardPile = null;
        playerTurnOrder = new ArrayList<>(Arrays.asList(PlayerEnum.values()));
        turn = null;
        turnIndex = -1;
    }
    //</editor-fold>
}
