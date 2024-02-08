package culminatingproject;

/************
* Project: CulminatingProject
* Programmer: Amro Issa
* Due Date: Feb 4, 2022
* File: Card.java
*************/



import java.util.ArrayList; //used to make ArrayLists
import java.awt.event.*;

enum CardColorEnum {
    Red,
    Green,
    Blue,
    Yellow,
    Any
}

enum CardTypeEnum {
    Zero,
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Skip,
    Reverse,
    Plus2,
    Plus4,
    ChangeColor,;
}

public class Card {
    public CardColorEnum color;
    public final CardTypeEnum type;
    public PlayerEnum player; //player to which the card belongs
    public ArrayList<Card> hand; //hand (or even deck) to which the card belongs

    //Constructors
    public Card(){
        color = null;
        type = null;
        player = null;
        hand = null;
    }
    public Card(CardColorEnum color, CardTypeEnum type){
        this.color = color;
        this.type = type;
        player = null;
        hand = null;
    }
    public Card(CardColorEnum color, CardTypeEnum type, PlayerEnum player, ArrayList<Card> hand){
        this.color = color;
        this.type = type;
        this.player = player;
        this.hand = hand;
    }

    /**
    MoveToHand
    Moves this card to the specified player's hand and applies necessary actions
    @param player - the player of the hand the card will move to
    */
    public void MoveToHand(PlayerEnum player){
        this.player = player;
        if (hand != null) hand.remove(this); //if the card is already in a hand, we remove it from that hand first
        hand = GameManager.Hands.get(player); //getting hand of player
        hand.add(this); //adding to hand

        if (player == PlayerEnum.LocalPlayer) {
            //if player is local player, we first create a button for the card, then assign the click method to it so that it runs whenever the player clicks it
            UI.CreateLocalPlayerCardButton(this).addActionListener((ActionEvent ae) -> {
                ClickCard();
            });
        }
        else if (player == PlayerEnum.Computer){
            UI.CreateComputerCardLabel(this); //creating label for the card if it belongs to computer (not button since computer doesn't interact with the GUI
        }
    }
    /**
    MoveToDiscardPile
    Moves this card to the discard pile and applies necessary actions
    */
    public void MoveToDiscardPile(){
        //can't call UI.AlignCardLabels outside of the if block because the card might be in the draw deck, not the hand of a player
        if (player == PlayerEnum.LocalPlayer) {
            UI.localPlayerCardButtons.get(this).setVisible(false); //hiding the button label of this card
            UI.framesDict.get(FramesEnum.Game).remove(UI.localPlayerCardButtons.get(this)); //removing it from the frame
            UI.localPlayerCardButtons.remove(this); //removing it from the collection of labels
            UI.AlignCardLabels(player); //aligning cards after removal
        }
        else if (player == PlayerEnum.Computer) {
            UI.computerCardLabels.get(this).setVisible(false); //hiding the label of this card
            UI.framesDict.get(FramesEnum.Game).remove(UI.computerCardLabels.get(this)); //removing it from the frame
            UI.computerCardLabels.remove(this); //removing it from the collection of labels
            UI.AlignCardLabels(player); //aligning cards after removal
        }

        hand.remove(this); //removing card from the current hand
        //setting hand and player to null because it doesn't belong to anyone in the discard pile
        hand = null;
        player = null;

        GameManager.DiscardPile.add(this); //adding card to the discard pile collection
        GameManager.cardInDiscardPile = this; //updating the top card in discard pile to this one
        UI.discardPileCardLabel.setIcon(Utilities.GetCardIcon(this)); //updating the icon of the discard pile label
    }

    /**
    ClickCard
    The method called when this card's button is clicked by the local player
    */
    public void ClickCard(){
        if (GameManager.turn == PlayerEnum.LocalPlayer && GameManager.IsCardPlayable(this)) {
            //If it is the local player's turn and the card is playable, we play it
            GameManager.PlaceCard(player, this);
        }
    }

    /**
    GetCardName
    Gets the name (color and type) of this card in string form
    */
    public String GetCardName(){
        return String.format("[%s] %s", color.toString().toUpperCase(), type);
    }
}
