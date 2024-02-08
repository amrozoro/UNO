package culminatingproject;

/************
* Project: CulminatingProject
* Programmer: Amro Issa
* Due Date: Feb 4, 2022
* File: Utilities.java
*************/

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Utilities {
    //<editor-fold defaultstate="collapsed" desc="IsCard?">
    //Methods to check whether the passed in card type is a specific group of cards (number, action, or wild card) - returns a boolean
    public static boolean IsNumberCard(CardTypeEnum type){
        return !IsActionOrWildCard(type);
    }
    public static boolean IsActionCard(CardTypeEnum type){
        switch (type) {
            case Skip:
            case Reverse:
            case Plus2:
                return true;
            default:
                return false;
        }
    }
    public static boolean IsWildCard(CardTypeEnum type){
        switch(type){
            case Plus4:
            case ChangeColor:
                return true;
            default:
                return false;
        }
    }
    public static boolean IsActionOrWildCard(CardTypeEnum type){
        return IsActionCard(type) || IsWildCard(type);
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="GetTypes">
    //Methods to get all the card types of a specific card group, returns an array of CardTypeEnum
    public static CardTypeEnum[] GetNumberTypes() {
        CardTypeEnum[] arr = new CardTypeEnum[10];
        for (int i = 0; i < 10; i++) {
            arr[i] = CardTypeEnum.values()[i];
        }
        return arr;
    }
    public static CardTypeEnum[] GetActionCardTypes() {
        return new CardTypeEnum[]{CardTypeEnum.Skip, CardTypeEnum.Reverse, CardTypeEnum.Plus2};
    }
    public static CardTypeEnum[] GetWildCardTypes() {
        return new CardTypeEnum[]{CardTypeEnum.Plus4, CardTypeEnum.ChangeColor};
    }
    public static CardTypeEnum[] GetActionAndWildCardTypes() {
        return new CardTypeEnum[]{CardTypeEnum.Skip, CardTypeEnum.Reverse, CardTypeEnum.Plus2, CardTypeEnum.Plus4, CardTypeEnum.ChangeColor};
    }

    //Methods to get all the card types except that of a specific card group
    public static ArrayList<CardTypeEnum> GetNonActionCardTypes() {
        ArrayList<CardTypeEnum> arr = new ArrayList<>(Arrays.asList(CardTypeEnum.values()));
        arr.remove(CardTypeEnum.Reverse);
        arr.remove(CardTypeEnum.Skip);
        arr.remove(CardTypeEnum.Plus2);

        return arr;
    }
    public static ArrayList<CardTypeEnum> GetNonWildCardTypes() {
        ArrayList<CardTypeEnum> arr = new ArrayList<>(Arrays.asList(CardTypeEnum.values()));
        arr.remove(CardTypeEnum.Plus4);
        arr.remove(CardTypeEnum.ChangeColor);

        return arr;
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="GetRandom">
    //Methods to generate and return something random (int, card color, card type)
    /**
    GetRandomInt
    Generates a random integer between min and max and includes them in the generation if specified
    */
    public static int GetRandomInt(int min, int max, boolean inclusiveOfMin, boolean inclusiveOfMax){
        return (int) (min + Math.random() * (max - min + (inclusiveOfMax ? 1 : 0)));
    }
    public static CardColorEnum GetRandomColor(){
        CardColorEnum[] array = CardColorEnum.values();
        return array[GetRandomInt(0, array.length - 1, true, false)]; //we don't include last element because it is "Any" (used for cards that can change color)
    }
    public static CardTypeEnum GetRandomType(){
        CardTypeEnum[] array = CardTypeEnum.values();
        return array[GetRandomInt(0, array.length, true, false)];
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Misc">
    //Methods to get the FrameEnum of a JFrame. Useful when you want to know what frame is what
    public static FramesEnum GetFrameEnum(JFrame frame){
        for (FramesEnum frameEnum : FramesEnum.values()) {
            if (frame == UI.framesDict.get(frameEnum)) {
                return frameEnum;
            }
        }

        throw new IllegalArgumentException();
    }

    //Methods to get the ImageIcon of a specific card
    public static ImageIcon GetCardIcon(CardColorEnum color, CardTypeEnum type){
//        return new ImageIcon("Assets/Images/Cards/" + color + "/" + type + ".png");
//        return new ImageIcon(Utilities.class.getClassLoader().getResource("Assets/Images/Cards/" + color + "/" + type));
        return Main.Instance.GetCardIcon(color, type);
    }
    public static ImageIcon GetCardIcon(Card card){
//        return new ImageIcon("Assets/Images/Cards/" + card.color + "/" + card.type + ".png");
//        return new ImageIcon(Utilities.class.getClassLoader().getResource("Assets/Images/Cards/" + card.color + "/" + card.type));
        return Main.Instance.GetCardIcon(card);
    }
    
    public static ImageIcon GetCardIcon(String color, String type){
        return Main.Instance.GetCardIcon(color, type);
    }
    public static ImageIcon GetCardIcon(String path){
        return Main.Instance.GetCardIcon(path);
    }
    //</editor-fold>

}
