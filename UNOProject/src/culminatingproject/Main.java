/************
* Project: CulminatingProject
* Programmer: Amro Issa
* Due Date: Feb 4, 2022
* File: Main.java
*************/

package culminatingproject;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

public class Main {
    public static Main Instance;
    
    public Main(){
        System.out.println("Instance of Main class created");
    }
    
    public static void main(String[] args) {
        Instance = new Main();
        UI.Initialize();
    }
    
    public ImageIcon GetCardIcon(CardColorEnum color, CardTypeEnum type){
        URL path = getClass().getClassLoader().getResource("Images/Cards/" + color + "/" + type + ".png");
        System.out.println(path.toString());
        return new ImageIcon(path);

//        return new ImageIcon("Assets/Images/Cards/" + color + "/" + type + ".png");
    }
    public ImageIcon GetCardIcon(Card card){
        URL path = getClass().getClassLoader().getResource("Images/Cards/" + card.color + "/" + card.type + ".png");
        System.out.println(path.toString());
        return new ImageIcon(path);

//        return new ImageIcon("Assets/Images/Cards/" + card.color + "/" + card.type + ".png");
    }
    public ImageIcon GetCardIcon(String color, String type){
        return new ImageIcon(getClass().getClassLoader().getResource("Images/Cards/" + color + "/" + type + ".png"));
//        return new ImageIcon("Assets/Images/Cards/" + color + "/" + type + ".png");
    }
    public ImageIcon GetCardIcon(String path){
        return new ImageIcon(getClass().getClassLoader().getResource(path));
//        return new ImageIcon(path);
    }
    
    public URL GetURL(String path) {
      return getClass().getClassLoader().getResource(path);
   }
}