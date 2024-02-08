package culminatingproject;

/************
* Project: CulminatingProject
* Programmer: Amro Issa
* Due Date: Feb 4, 2022
* File: UI.java
*************/



import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UI {
    static Hashtable<FramesEnum, JFrame> framesDict = new Hashtable<>();
    static Hashtable<FramesEnum, ArrayList<JLabel>> labelsDict = new Hashtable<>(); //storing all labels to apply actions on them together
    static Hashtable<FramesEnum, ArrayList<JButton>> buttonsDict = new Hashtable<>(); //storing all buttons to apply actions on them together

    static LinkedHashMap<Card, JButton> localPlayerCardButtons = new LinkedHashMap<>();
    static LinkedHashMap<Card, JLabel> computerCardLabels = new LinkedHashMap<>(); //using linkedhashmap instead of normal hashmap (dictionary) so that we can iterate over the elements in a predictable order

    static JLabel computerOverflowLabel; //icon label to show when too much cards are in the computer's hand
    static JLabel computerOverflowLabelText;

    static JLabel discardPileCardLabel;
    static JButton drawDeckCardButton;

    static JLabel roundNumberLabel;

    static DefaultTableModel highscoresTableModel; //used to add a row to the highscores JTable
    static DefaultTableModel gameFeedTableModel; //used to add a row to the game feed JTable

    /**
    Initialize
    Initializes the game by assigning values to dictionaries and setting up frames
    */
    public static void Initialize(){
        //initializing dictionaries
        for (FramesEnum frameEnum : FramesEnum.values()) {
            labelsDict.put(frameEnum, new ArrayList<>());
            buttonsDict.put(frameEnum, new ArrayList<>());
        }

        //initializing frames
        SetUpMainMenuFrame();
        SetUpGameFrame();
        SetUpHowToPlayFrame();
        SetUpHighscoresFrame();
    }

    /**
    ResetGameUI
    Resets the game UI by removing any card labels from the previous game so that a new one can begin
    */
    public static void ResetGameUI(){
        //Setting up game to play again by removing the card labels of players' hands
        for (JButton button : localPlayerCardButtons.values()) {
            framesDict.get(FramesEnum.Game).remove(button);
        }
        for (JLabel label : computerCardLabels.values()) {
            framesDict.get(FramesEnum.Game).remove(label);
        }
        localPlayerCardButtons.clear();
        computerCardLabels.clear();
    }

    //<editor-fold defaultstate="collapsed" desc="Setting Up Frames">
    public static void SetUpMainMenuFrame(){
        JFrame frame = CreateFrame("UNO~Main Menu", 1920, 1080, 0, 0, false);
        frame.setVisible(true);
        framesDict.put(FramesEnum.MainMenu, frame);

        ImageIcon icon = Utilities.GetCardIcon("Images/Title.png");
        JLabel titleLabel = CreateLabel(icon, (frame.getWidth() / 2) - (icon.getIconWidth() / 2), 0, FramesEnum.MainMenu, true);

        int buttonWidth = 150;
        int buttonHeight = 75;
        int distanceBetweenButtons = 50; //code to make the buttons fit to screen: (mainMenuFrame.getHeight() - titleLabel.getHeight() - buttonHeight * array.length) / array.length;
        String[] array = {"New Game", "How To Play", "Highscores", "Exit"};

        for (int i = 0; i < array.length; i++) {
            //procedurally generating the buttons
            int posX = (frame.getWidth() / 2) - (buttonWidth / 2); //centering horizontally
            int posY = (titleLabel.getHeight() + 25) + (buttonHeight + distanceBetweenButtons) * i;

            CreateButton(array[i], buttonWidth, buttonHeight, posX, posY, FramesEnum.MainMenu, true);
        }

        //<editor-fold defaultstate="collapsed" desc="cardButton listeners">
        //Start new game cardButton
        buttonsDict.get(FramesEnum.MainMenu).get(0).addActionListener((ActionEvent e) -> {
            GameManager.Initialize(); //initializing game when it is clicked
        });
        //How to play button
        buttonsDict.get(FramesEnum.MainMenu).get(1).addActionListener((ActionEvent e) -> {
            ShowFrame(FramesEnum.HowToPlay);
        });
        //View highscores button
        buttonsDict.get(FramesEnum.MainMenu).get(2).addActionListener((ActionEvent e) -> {
            ShowFrame(FramesEnum.Highscores);
            UpdateHighscoresTable();
        });
        //Exit game cardButton
        buttonsDict.get(FramesEnum.MainMenu).get(3).addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Credits">
        JLabel lbl = CreateLabel("Made by: Amro Issa", 400, 200, frame.getWidth() - 400, frame.getHeight() - 200, FramesEnum.MainMenu, true);
        lbl.setFont(new Font("Serif", Font.PLAIN, 40));
        //</editor-fold>
    }
    public static void SetUpGameFrame(){
        JFrame frame = CreateFrame("UNO~Game", 1920, 1080, 0, 0, false);
        framesDict.put(FramesEnum.Game, frame);

        ImageIcon icon = Utilities.GetCardIcon("Misc", "BackOfCard"); //storing back of card icon for use later

        //creating draw deck button
        drawDeckCardButton = CreateButton(icon, frame.getWidth() / 2 + 200, frame.getHeight() / 2 - 135, FramesEnum.Game, true); //the actual draw deck button
        JLabel drawDeckTextLabel = CreateLabel("Draw Deck", 100, 25, drawDeckCardButton.getX() + 50, drawDeckCardButton.getY() - 25, FramesEnum.Game, true); //label to display "Draw Deck" above the draw deck

        //draw deck button functionality
        drawDeckCardButton.addActionListener((ActionEvent ae) -> {
            if (GameManager.turn == PlayerEnum.LocalPlayer) {
                //if it is local player's turn, then they draw a card
                Card drawnCard = GameManager.DrawCard(PlayerEnum.LocalPlayer);

                if (GameManager.IsCardPlayable(drawnCard)) {
                    //if the drawn card is playable, it gives the user the option of playing it by displaying a dialog
                    String message = "The card you just drew is playable. Would you like to play it?";
                    Object[] options = {"Yes", "No"};
                    int choice = JOptionPane.showOptionDialog(UI.framesDict.get(FramesEnum.Game), null, message, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);

                    if (choice == JOptionPane.YES_OPTION) {
                        GameManager.PlaceCard(PlayerEnum.LocalPlayer, drawnCard);
                    }
                }

                GameManager.BeginNewRound(); //moves on to the next round
            }
        });

        //creating discard pile label
        discardPileCardLabel = CreateLabel(icon, frame.getWidth() / 2 - icon.getIconWidth() / 2, frame.getHeight() / 2 - 135, FramesEnum.Game, true);

        computerOverflowLabel = CreateLabel(icon, frame.getWidth() / 2 - icon.getIconWidth() / 2, 20, FramesEnum.Game, true);
        computerOverflowLabelText = CreateLabel("Card Count", 100, 50, frame.getWidth() / 2 + icon.getIconWidth() / 2 + 25, icon.getIconHeight() / 2, FramesEnum.Game, false);
        computerOverflowLabelText.setFont(new Font(Font.SERIF, Font.PLAIN, 25));

        roundNumberLabel = CreateLabel(null, 200, 20, 700, framesDict.get(FramesEnum.Game).getHeight() / 2 - 50, FramesEnum.Game, true);
        roundNumberLabel.setFont(new Font(Font.SERIF, Font.PLAIN, 25));

        CreateGameFeed();
    }
    public static void CreateGameFeed(){
        //<editor-fold defaultstate="collapsed" desc="Feed">
        String[] columnNames = {"Order", "Description"};

        //making the actual table
        JTable table = new JTable(new DefaultTableModel(null, columnNames)){
            //disabling editing
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                return false;
            }
        };
        gameFeedTableModel = (DefaultTableModel) table.getModel(); //storing the table model so that we can add rows to it later (cant add directly to table)

        table.getColumnModel().getColumn(0).setPreferredWidth(15);
        table.getColumnModel().getColumn(1).setPreferredWidth(135);

        //allowing scrolling in the table using a scroll pane
        JScrollPane scrollPane = new JScrollPane(table); //assigning table to the scrollbar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); //always showing the scrollbar
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        framesDict.get(FramesEnum.Game).add(scrollPane);

        int width = 400;
        int height = 300;
        int posX = framesDict.get(FramesEnum.Game).getWidth() - (width) - 50;
        int posY = framesDict.get(FramesEnum.Game).getHeight() / 2 - (height / 2) - 25;
        scrollPane.setBounds(posX, posY, width, height);
        //</editor-fold>
    }
    public static void SetUpHowToPlayFrame(){
        JFrame frame = CreateFrame("UNO~How To Play", 1920, 1080, 0, 0, false);
        framesDict.put(FramesEnum.HowToPlay, frame);
        CreateBackButton(FramesEnum.HowToPlay, FramesEnum.MainMenu, true);

        //how to play
        String str = "\t\t\tHOW TO PLAY\n\n" +
                "7 cards are dealt to the player and computer each. A starting card begins the game\n\n" +
                "The objective of the game is to get rid of all your cards before your opponent\n\n" +
                "You can place a card if it:\n" +
                "   - matches the color of the most recent card in the discard pile\n" +
                "   - matches the type of the most recent card in the discard pile\n" +
                "   - is a wild card (wild cards have a black background)\n" +
                "\nThe Action Cards are:\n" +
                "   - Skip (skips the turn of the other person)\n" +
                "   - Reverse (since this is a two player game, reverse is the same as skip)\n" +
                "   - Plus 2 (forces opponent to draw 2 cards and skips their turn)\n" +
                "\nThe Wild Cards are\n" +
                "   - Plus 4 (forces opponent to draw 4 cards and skips their turn)\n" +
                "   - Change Color (allows the user to change the current color)\n";


        CreateTextPane(str, 500, 350, frame.getWidth() / 2 - 500 / 2, 0, FramesEnum.HowToPlay, true);
    }
    public static void SetUpHighscoresFrame(){
        JFrame frame = CreateFrame("UNO~Highscores", 1920, 1080, 0, 0, false);
        framesDict.put(FramesEnum.Highscores, frame);

        CreateLabel("HIGHSCORES", 100, 25, (frame.getWidth() / 2) - (100 / 2), 0, FramesEnum.Highscores, true);

        //making the highscores table with these columns
        String[] columnNames = {"Rank", "Name", "Rounds It Took To Win"};
        JTable table = new JTable(new DefaultTableModel(null, columnNames)){
            //disabling editing
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                return false;
            }
        };
        highscoresTableModel = (DefaultTableModel) table.getModel(); //storing table model to be able to add rows on to it later (cant add directly to JTable)

        //assigning the table to a scrollpane to be able to scroll through it if the data overflows the table size
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(frame.getWidth() / 2 - 200, 300, 400, 200);
        frame.add(scrollPane); //adding to frame

        CreateBackButton(FramesEnum.Highscores, FramesEnum.MainMenu, true);

        UpdateHighscoresTable(); //populating the table with past entries in the highscores text file
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Component Creation">
        //Methods to create components from a blueprint
        //Most take in a name, width and height (except icons, since their dimensions are taken from the icon itself), location,
        //and whether to make them visible or not
    public static JFrame CreateFrame(String name, int width, int height, int posX, int posY, boolean visible){
        JFrame frame = new JFrame(name);
        frame.setLayout(null);
        frame.setVisible(visible);
        frame.setBounds(posX, posY, width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stops application when window is closed
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //opens frame maximized
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());

        return frame;
    }
    public static JLabel CreateLabel(String text, int width, int height, int posX, int posY, FramesEnum frameToAddTo, boolean visible){
        JLabel label = new JLabel(text);
        label.setVisible(visible);
        label.setBounds(posX, posY, width, height);
        labelsDict.get(frameToAddTo).add(label); //adding to labels dictionary
        framesDict.get(frameToAddTo).add(label); //adding to specified frame

        return label;
    }
    public static JLabel CreateLabel(ImageIcon icon, int posX, int posY, FramesEnum frameToAddTo, boolean visible){
        JLabel label = new JLabel(icon);
        label.setVisible(visible);
        int widthScaleMultiplier = Toolkit.getDefaultToolkit().getScreenSize().width / 1920;
        int heightScaleMultiplier = Toolkit.getDefaultToolkit().getScreenSize().height / 1080;
        label.setBounds(posX, posY, icon.getIconWidth() * widthScaleMultiplier, icon.getIconHeight() * heightScaleMultiplier); //width and height inferred from icon
        labelsDict.get(frameToAddTo).add(label); //adding to labels dictionary
        framesDict.get(frameToAddTo).add(label); //adding to specified frame

        return label;
    }
    public static JTextPane CreateTextPane(String str, int width, int height, int posX, int posY, FramesEnum frameToAddTo, boolean visible){
        JTextPane textPane = new JTextPane();
        textPane.setText(str);
        textPane.setVisible(visible);
        textPane.setBounds(posX, posY, width, height);
        textPane.setEditable(false); //prevent user from editing

        framesDict.get(frameToAddTo).add(textPane); //adding to specified frame

        return textPane;
    }
    public static JButton CreateButton(String text, int width, int height, int posX, int posY, FramesEnum frameToAddTo, boolean visible){
        JButton button = new JButton(text);
        button.setVisible(visible);
        button.setBounds(posX, posY, width, height);
        buttonsDict.get(frameToAddTo).add(button); //adding to button dictionary
        framesDict.get(frameToAddTo).add(button); //adding to specified frame

        return button;
    }
    public static JButton CreateButton(ImageIcon icon, int posX, int posY, FramesEnum frameToAddTo, boolean visible){
        JButton button = new JButton(icon);
        button.setVisible(visible);
        button.setBounds(posX, posY, icon.getIconWidth(), icon.getIconHeight()); //dimensions inferred from icon
        buttonsDict.get(frameToAddTo).add(button); //adding to button dictionary
        framesDict.get(frameToAddTo).add(button); //adding to specified frame

        return button;
    }
    /**
    CreateBackButton
    Blueprint to create a back button whose location is the bottom middle of the specified frame
    @param frameToAddTo - the frame this button will show in
    @param frameToMoveBackToWhenClicked - the frame that will be transitioned to when this button is clicked
    */
    public static JButton CreateBackButton(FramesEnum frameToAddTo, FramesEnum frameToMoveBackToWhenClicked, boolean visible){
        int width = 150;
        int height = 75;
        int posX = (framesDict.get(frameToAddTo).getWidth() / 2) - (width / 2); //centering horizontally
        int posY = (framesDict.get(frameToAddTo).getHeight() - height) - 100; //100 is the distance from the bottom of the frame
        JButton btn = CreateButton("Back", width, height, posX, posY, frameToAddTo, visible);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ShowFrame(frameToMoveBackToWhenClicked);
            }
        });

        return btn;
    }
    /**
    DisplayOptionDialog
    Shows an dialog with options that the user can choose from
    @param frame - the frame to add the dialog to
    @param message - the question asked
    @param options - an array of objects representing the possible options to choose from
    @return - an integer representing the index of the choice in the "options" object array. returns -1 if the dialog was closed
    */
    public static int DisplayOptionDialog(FramesEnum frame, String message, Object[] options){
        return JOptionPane.showOptionDialog(UI.framesDict.get(frame), message, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    }
    /**
    DisplayInputDialog
    Shows an dialog with an input field to enter something
    @param frame - the frame to add the dialog to
    @param message - the question asked
    @return - the string entered. returns null if the dialog was closed
    */
    public static String DisplayInputDialog(FramesEnum frame, String message){
        String x = JOptionPane.showInputDialog(UI.framesDict.get(frame), message);
        
        return x;
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Card Labels/Buttons">
    /**
    CreateLocalPlayerCardButton
    Creates a clickable button for the specified card belonging to the local player
    @return the button
    */
    public static JButton CreateLocalPlayerCardButton(Card card){
        JButton button = CreateButton(Utilities.GetCardIcon(card), 0, 0, FramesEnum.Game, true); //correct posX will be set through "AlignCardLabels" method
        localPlayerCardButtons.put(card, button); //adding to the collection
        AlignCardLabels(PlayerEnum.LocalPlayer); //aligning the card buttons visually after creating a new one
        return button;
    }
    /**
    CreateComputerCardLabel
    Creates a label for the specified card belonging to the computer
    @return the label
    */
    public static JLabel CreateComputerCardLabel(Card card){
        //the icon must be the back of a card so that the player doesn't see what the cards in the computer's hand are
        JLabel label = CreateLabel(Utilities.GetCardIcon("Misc", "BackOfCard"), 0, 0, FramesEnum.Game, true); //correct posX will be set through "AlignCardLabels" method
        computerCardLabels.put(card, label); //adding to collection
        AlignCardLabels(PlayerEnum.Computer); //aligning card labels after creating a new one
        return label;
    }
    /**
    AlignCardLabels
    Aligns the card labels (player's card buttons or computer's card labels) of the specified player in a symmetrical manner
    */
    public static void AlignCardLabels(PlayerEnum player){
        int maxCardsUntilOverflow = 5; //the maximum cards allowed in the hand of the computer before the card labels are compressed into just 1 card label with a text label indicating how many cards there are
        int distanceBetweenCards = 50;
        int width = 170; //avg width of cards
        int height = 260; //avg height of cards
        int posY = player == PlayerEnum.LocalPlayer ? framesDict.get(FramesEnum.Game).getHeight() - height - 70 : 20; //assigning the vertical position depending on the player (using ternary operator)
        int count = player == PlayerEnum.LocalPlayer ? localPlayerCardButtons.size() : computerCardLabels.size(); //the number of card labels the player has

        int startX = (framesDict.get(FramesEnum.Game).getWidth() / 2) - ((width + distanceBetweenCards) * (count / 2)); //if card count is even
        if (count % 2 == 1) startX -= (width / 2); //if card count is odd, we need to adjust the startX so that there is a card directly in the middle (helps with symmetry)

        int i = 0; //counter
        if (player == PlayerEnum.LocalPlayer) {
            for (JButton btn : localPlayerCardButtons.values()){
                int posX = startX + ((width + distanceBetweenCards) * i);
                btn.setLocation(posX, posY);
                i++;
            }
        }
        else{
            for (JLabel lbl : computerCardLabels.values()){
                //if card count exceeds max, it is an overflow...and so we hide the card labels
                if (count > maxCardsUntilOverflow) {
                    lbl.setVisible(false);
                }
                else{
                    lbl.setVisible(true);
                    int x = startX + ((width + distanceBetweenCards) * i);
                    lbl.setLocation(x, posY);
                    i++;
                }
            }

            if (count > maxCardsUntilOverflow) {
                //showing the overflow labels if there is an overflow
                computerOverflowLabel.setVisible(true);
                computerOverflowLabelText.setVisible(true);
                computerOverflowLabelText.setText("x" + count); //setting card count on label
            }
            else{
                computerOverflowLabel.setVisible(false);
                computerOverflowLabelText.setVisible(false);
            }
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="Misc">
    public static void ShowFrame(FramesEnum frame){
        for(JFrame frm : framesDict.values()){
            if (frm == framesDict.get(frame)) {
                frm.setVisible(true);
            }
            else{
                frm.setVisible(false);
            }
        }
    }

    /**
    Updates the highscores table by first clearing it, then reading all the entries from the file again (bcuz there could be changes), sorting them and printing to table
    */
    public static void UpdateHighscoresTable(){
        highscoresTableModel.setRowCount(0);

        ArrayList<Object[]> fileData = new ArrayList<>();

        try {
            //reading the entries from the file
            File file = null;
            try {
                file = new File(Main.Instance.GetURL("Text Files/Highscores.txt").toURI());
            } catch (URISyntaxException ex) {
                fileData.add(new Object[]{"ERROR", 69});
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String str = null;
            while (true) {
                str = br.readLine();
                
                if (str == null) {
                    break;
                }
                
                fileData.add(new Object[]{str.split(" ")[0], Integer.parseInt(str.split(" ")[1])}); //splitting  data into names and rounds it took to win and storing it as an object array in fileData
            }

            br.close();
            fr.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //<editor-fold defaultstate="collapsed" desc="Sorting entries in descending order">
        for (int i = 0; i < fileData.size() - 1; i++) {
            int currentLowestNumberIndex = i;

            for (int j = i + 1; j < fileData.size(); j++) {
                if ((int)fileData.get(j)[1] < (int)fileData.get(currentLowestNumberIndex)[1]) {
                    currentLowestNumberIndex = j;
                }
            }

            Object[] temp = fileData.get(i);
            fileData.set(i, fileData.get(currentLowestNumberIndex));
            fileData.set(currentLowestNumberIndex, temp);

        }
        //</editor-fold>

        //Displaying entries in the table by adding rows to it for every entry
        for (int i = 0; i < fileData.size(); i++) {
            highscoresTableModel.addRow(new Object[]{i + 1, fileData.get(i)[0], fileData.get(i)[1]});
        }
    }
    /**
    AddToHighscores
    Adds the passed in string (entry) to the highscores file
    @param string - the entry to add
    */
    public static void AddToHighscores(String string){
        try {
            //Appending entry to the end of the highscores text file
            
            File file = null;
            try {
                file = new File(Main.Instance.GetURL("Text Files/Highscores.txt").toURI());
            } catch (URISyntaxException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\n" + string);

            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
    AddToGameFeed
    Adds the passed string to the game feed table
    */
    public static void AddToGameFeed(String string){
        Object[] data = new Object[]{gameFeedTableModel.getRowCount() + 1, string}; //order is just the number of rows there are plus 1
        gameFeedTableModel.addRow(data);
    }

    public static void DisplayGameOver(boolean playerWon){
        ShowFrame(FramesEnum.MainMenu);
        JOptionPane.showMessageDialog(UI.framesDict.get(FramesEnum.MainMenu), "GAME OVER! YOU " + (playerWon ? "WON!" : "LOST!"));
    }
    //</editor-fold>
}
