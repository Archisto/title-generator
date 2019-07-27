import java.util.*;

/**
 * Title Generator
 *
 * @author Lauri Kosonen
 * @version 2019-05-30
 */
public class TitleGenerator {
    private static final String PROGRAM_VERSION = "v0.45, 2019-05-30";
    private static final boolean SHUFFLE_DECK_FOR_EACH_HAND = false;
    private static final boolean PRINT_CATEGORIES = false;
    private static List<Card> deck;
    private static List<Card> deckFeature;
    private static List<Card> deckConcept;
    private static List<Card> deckThing;
    private static List<Card> deckAction;
    private static List<Card> deckPlaceTime;
    private static List<List<Card>> decks;
    private static Hand[] hands;
    private static List<Integer> categorySizes;
    private static List<Integer> categoryFirstCardIndexes;
    private static int handAmount = 0;
    private static int cardsInHand = 0;
    private static int drawnCardAmount = 0;
    private static boolean specialEndCommand = false;
    private static boolean showAll = false;
    private static boolean printPartSeparators = false;
    private static int shownCategory = -1;
    private static int customNameLength = 0;

    // Ä, ä, Ö and ö characters, respectively,
    // by their ASCII numbers
    private static char a1 = (char) 196; // 'Ä'
    private static char a2 = (char) 228; // 'ä'
    private static char o1 = (char) 214; // 'Ö'
    private static char o2 = (char) 246; // 'ö'

   /**
    * Runs the program.
    */
    public static void main(String[] args)
    {
        // Prints the title of the program
        String title = "TITLE GENERATOR";
        System.out.println();
        System.out.println(title);
        System.out.println();

        boolean runMainProgram = true;

        // Creates the deck
        initDeck();

        if (deck.size() == 0) {
            System.out.println("There are no cards!");
            runMainProgram = false;
        }
        else {

            // Parses special commands
            runMainProgram = parseSpecialCommands(args);

            // If the last command line argument is a
            // special command, it is removed from the array
            if (specialEndCommand) {
                args = getCmdArgsWithoutLastArg(args);
            }
        }

        if (runMainProgram) {

            // Initializes the number of hands generated
            // and how many cards are in a hand
            initHandAmountAndSize(args);

            // Continues if the number of hands and hand size are positive
            if (handAmount > 0 && (cardsInHand > 0 || showAll)) {
                boolean emptyDeck = false;
                boolean incompleteHand = false;

                if (!showAll && shownCategory < 0) {
                    shuffleDeck();
                }

                // If custom name length is used, prints
                // a message of what's happening
                if (customNameLength > 0) {
                    System.out.format("Special mode: title%s with %d part%s\n\n",
                        plural(cardsInHand),
                        customNameLength,
                        plural(customNameLength));
                }

                // Creates and checks the hands
                hands = new Hand[handAmount];
                for (int i = 0; i < handAmount; i++) {
                    if (!emptyDeck) {

                        // Initializes the current hand
                        hands[i] = new Hand(cardsInHand);

                        // Adds cards to the hand
                        for (int j = 0; j < cardsInHand; j++) {
                            if (shownCategory < 0) {
                                drawCard(i, j);
                            }
                            else {
                                drawCardInCategory(i, j, shownCategory);
                            }

                            // Prints the name's number
                            System.out.print(formatCardIndex(j, cardsInHand));

                            // Prints a name
                            if (customNameLength > 0) {
                                System.out.println(generateCustomTitle());
                            }
                            else {
                                System.out.println(generateFullTitle());
                            }

                            // Prevents further card adding and
                            // hand checking if the deck is empty
                            if (drawnCardAmount == deck.size()) {
                                emptyDeck = true;
                                incompleteHand = (j < cardsInHand - 1);

                                if (!showAll && (cardsInHand < deck.size()) &&
                                     (incompleteHand || SHUFFLE_DECK_FOR_EACH_HAND)) {
                                    System.out.println("No more cards!");
                                }

                                break;
                            }
                        }

                        System.out.println("------");

                        if (SHUFFLE_DECK_FOR_EACH_HAND) {
                            shuffleDeck();
                            emptyDeck = false;
                        }
                    }
                }
            }
        }
    }

   /**
    * Parses the user input for any special commands.
    * The keywords include "all", "category", "stats" and "help".
    *
    * @param cmdArgs the arguments given in command line
    * @returns will the main program be run
    */
    private static boolean parseSpecialCommands(String[] cmdArgs) {
        if (cmdArgs.length > 0) {
            String firstCommand = cmdArgs[0].toLowerCase();

            // Show part separators
            if (parseShowSeparatorsCommand(cmdArgs)) {
                printPartSeparators = true;
                specialEndCommand = true;
                return true;
            }
            // Show stats
            else if (firstCommand.equals("stats") || firstCommand.equals("info")) {
                printStats();
                return false;
            }
            // Show instructions
            else if (firstCommand.equals("help") || firstCommand.equals("?")) {
                printInstructions();
                return false;
            }
            else {
                // Show only cards that belong to a certain category
                int categoryNumber = categoryNumber(firstCommand);
                if (categoryNumber >= 0) {
                    shownCategory = categoryNumber;
                    return true;
                }

                // Custom name length
                boolean runProgram = parseCustomNameLengthCommand(cmdArgs);
                if (!runProgram) {
                    return false;
                }
                else if (customNameLength > 0) {
                    specialEndCommand = true;
                    return true;
                }
            }
        }

        return true;
    }

   /**
    * Returns "s" if the given number is not 1.
    *
    * @param number a number
    */
    private static String plural(int number) {
        if (number == 1)
            return "";
        else
            return "s";
    }

   /**
    * Returns the given string if the given number is not 1.
    *
    * @param number a number
    * @param ending an ending string
    */
    private static String plural(int number, String ending) {
        if (number == 1)
            return "";
        else
            return ending;
    }

   /**
    * Returns one string if the given number is 1 and the other if not.
    *
    * @param number         a number
    * @param singularEnding a singular ending string
    * @param pluralEnding   a plural ending string
    */
    private static String plural(int number,
                                 String singularEnding,
                                 String pluralEnding) {
        if (number == 1)
            return singularEnding;
        else
            return pluralEnding;
    }

   /**
    * Prints information about Card Archive:
    * - how many cards there are
    * - what categories there are
    * - how many cards there are in each category
    * - current program version and credits.
    */
    private static void printStats() {
        System.out.println("All available words: " + deck.size());
        System.out.println("Word types: " + categorySizes.size());
        for (int i = 0; i < categorySizes.size(); i++) {
            System.out.format("[%d. %s] size: %d\n",
                i, categoryName(i), decks.get(i).size());
        }

        System.out.println("\nCurrent program version: " + PROGRAM_VERSION);
        System.out.println("Created by Lauri Kosonen");
    }

   /**
    * Prints instructions on how to use this program.
    */
    private static void printInstructions() {
        System.out.println("How to use this program:");
        System.out.println("- Execute in command line by writing the command in this " +
                           "format:\n  java NameGenerator argument1 argument2 argument3");
        System.out.println("- Leave arguments out to run the program using the default settings");
        System.out.println("- Possible arguments:");
        System.out.println("  - Input one number to generate that many names");
        System.out.println("  - Input two numbers to generate that many hands and cards in each hand");
        System.out.println("  - Input a category's name to view the cards in it");
        System.out.println("  - Input \"category\" or \"cat\" followed by a category number to view the cards in it");
        System.out.println("  - Input \"all\" to view all cards");
        System.out.println("  - Input \"stats\" or \"info\" to see how many cards and what categories there are");
        System.out.println("  - Input \"help\" or \"?\" to see these instructions");
        System.out.println("- Press the Enter key to run the program");
        System.out.println("- Press the Up arrow key to insert the previous command and run the program again");
        System.out.println("- With each run you get different results depending on the arguments");
    }

    private static boolean parseCustomNameLengthCommand(String[] cmdArgs) {
        String lastCommand = cmdArgs[cmdArgs.length - 1];
        if (lastCommand.length() >= 2 && lastCommand.substring(0, 1).equals("+")) {
            try {
                customNameLength = Integer.parseInt(lastCommand.substring(1));
            }
            catch (NumberFormatException e) {
                System.out.println("Please input \"+\" immediately followed by a " +
                                   "number to generate titles with that many parts.");
                return false;
            }
        }

        return true;
    }

    private static boolean parseShowSeparatorsCommand(String[] cmdArgs) {
        String lastCommand = cmdArgs[cmdArgs.length - 1];
        if (lastCommand.equals("-") || (lastCommand.length() >= 3 &&
            lastCommand.substring(0, 3).equals("sep"))) {
            return true;
        }

        return false;
    }

    private static String[] getCmdArgsWithoutLastArg(String[] cmdArgs) {
        if (cmdArgs.length <= 1) {
            return new String[0];
        }
        else {
            String[] newCmdArgs = new String[cmdArgs.length - 1];
            for (int i = 0; i < newCmdArgs.length; i++) {
                newCmdArgs[i] = cmdArgs[i];
            }
            return newCmdArgs;
        }
    }

   /**
    * Parses the user input for which category's cards will be displayed.
    *
    * Showing all cards in a category depends on the deck
    * not being shuffled. Each category's first card's index
    * in an unshuffled deck has been recorded and will be used
    * in conjunction with the categories' sizes to get the
    * correct cards from the deck.
    *
    * @param cmdArgs the arguments given in command line
    * @returns will the main program be run
    */
    private static boolean parseShowCategoryCommand(String[] cmdArgs) {
        if (cmdArgs.length > 1) {
            try {
                int input = Integer.parseInt(cmdArgs[1]);
                if (input >= 0 && input < categorySizes.size()) {
                    shownCategory = input;
                    return true;
                }
                else {
                    System.out.format("The category number must be between 0 and %d (inclusive).\n",
                        categorySizes.size() - 1);
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Please input \"category\" or \"cat\" followed by " +
                                   "a category number to view the cards in it.");
            }
        }
        else {
            System.out.println("Please input also a category's " +
                               "number to view the cards in it.");
        }

        return false;
    }

   /**
    * Initializes the number of hands generated
    * and how many cards is in one hand.
    * Takes the command line arguments and parses
    * them into integers.
    * Giving no arguments defaults in the hand number of 1, hand size of 5.
    *
    * @param cmdArgs the arguments given in command line
    */
    private static void initHandAmountAndSize(String[] cmdArgs) {

        // Displays all cards of a certain category
        if (shownCategory >= 0) {
            handAmount = 1;
            cardsInHand = categorySizes.get(shownCategory);
            if (cardsInHand == 0) {
                System.out.println("The category is empty.");
            }
        }
        // Attempts to parse the input into two integers and
        // prints an error message if the input is invalid
        else if (cmdArgs.length > 0) {
            if (showAll) {
                handAmount = 1;
                cardsInHand = deck.size();
            }
            else {
                try {
                    // If there's only one command line argument,
                    // the argument is for the number of cards in hand
                    if (cmdArgs.length == 1) {
                        handAmount = 1;
                        cardsInHand = Integer.parseInt(cmdArgs[0]);
                    }
                    // If there's two or more, the first command
                    // line argument is for the number of hands and
                    // the second is for the number of cards in hand
                    else {
                        handAmount = Integer.parseInt(cmdArgs[0]);
                        cardsInHand = Integer.parseInt(cmdArgs[1]);
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("The given input is not valid.\n");
                    printInstructions();
                    handAmount = 0;
                    cardsInHand = 0;
                    return;
                }

                if (handAmount < 1 || cardsInHand < 1) {
                    System.out.println("Please input a positive integer.");
                    handAmount = 0;
                    cardsInHand = 0;
                }
                else if (cardsInHand > deck.size() ||
                         (!SHUFFLE_DECK_FOR_EACH_HAND &&
                          handAmount > deck.size())) {
                    System.out.format("Invalid input - the deck has %d cards.",
                        deck.size());
                    System.out.println();
                    handAmount = 0;
                    cardsInHand = 0;
                }
            }
        }
        // Default number of hands and cards in hand
        else {
            handAmount = 1;
            cardsInHand = 10;
        }
    }

   /**
    * Returns a category's number based on the given name.
    * Returns -1 if the name doesn't match with any category.
    * Only the first 3 letters are checked,
    * so the argument can be shortened.
    *
    * @param categoryName a category's name
    * @return the number of a category or -1 for error
    */
    public static int categoryNumber(String categoryName) {
        if (categoryName != null && categoryName.length() >= 3) {
            categoryName = categoryName.toLowerCase().substring(0, 3);

            for (int i = 0; i < categorySizes.size(); i++) {
                String catCandidate =
                    categoryName(i).toLowerCase().substring(0, 3);
                if (categoryName.equals(catCandidate)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static String generateCustomTitle() {
        String title = "";
        String article = "The ";

        double rand = Math.random();
        boolean addArticle = (rand <= 0.5f);
        if (addArticle) {
            title = article;
        }

        title += generateTitle(customNameLength, addArticle);
        return title;
    }

    private static String generateFullTitle() {
        double rand = Math.random();
        int firstNameCount = 1 + (int) (rand * 1.4f);
        boolean addSubtitle;
        String article = "The ";

        // Generates the main title
        int mainTitleWords;
        String mainTitle = "";
        boolean addArticle;

        rand = Math.random();
        addArticle = (rand <= 0.25f);
        rand = Math.random();
        mainTitleWords = 1 + (int) (rand * 2.5f);
        rand = Math.random();
        addSubtitle = (rand <= 0.3f);

        if (addArticle) {
            mainTitle = article;
        }

        mainTitle += generateTitle(mainTitleWords, addArticle);

        // Generates the subtitle
        if (addSubtitle) {
            String subtitle = "";
            rand = Math.random();
            addArticle = (!addArticle && rand <= 0.25f);
            rand = Math.random();
            int subtitleWords = 1 + (int) (rand * 2);

            if (addArticle) {
                subtitle = article;
            }

            subtitle +=
                generateTitle(subtitleWords, addArticle);

            return mainTitle + ": " + subtitle;
        }
        else {
            return mainTitle;
        }
    }

    private static String generateTitle(int words, boolean articleInFront) {
        String name = "";
        boolean capitalizeNextLetter = true;
        boolean usedMidWords = false;

        if (words > 0) {
            for (int i = 0; i < words; i++) {
                String word = getRandomWord(i, words, false);
                if (capitalizeNextLetter) {
                    word = capitalizeFirstLetter(word);
                    capitalizeNextLetter = false;
                }

                if (i > 0) {
                    String midWords = getMidWords(
                        usedMidWords,
                        articleInFront,
                        name.charAt(name.length() - 1));

                    if (!usedMidWords && midWords.length() > 1 &&
                          midWords.charAt(0) != '\'') {
                        usedMidWords = true;
                    }

                    name += midWords;
                }

                name += word;

                // Adds a separator to make it clear
                // where one word ends and another begins
                if (printPartSeparators) {
                    name += "|";
                }
            }
        }

        return name;
    }

    private static String getMidWords(boolean usedMidWords,
                                      boolean articleInFront,
                                      char lastLetter) {
        String result = " ";
        boolean usePossessive = false;
        double rand = Math.random();

        String possessive1 = "'s ";
        String possessive2 = "' ";

        if (!usedMidWords) {
            String midWords1 = " of the ";
            String midWords2 = " of ";
            String midWords3 = " and the ";
            String midWords4 = " and ";
            String midWords5 = " the ";
            String midWords6 = " vs ";
            // String midWords = " for the ";
            // String midWords = " for ";
            // String midWords = " against ";
            // String midWords = " over ";

            if (rand <= 0.5f) {
                if (rand <= 0.1f) {
                    result = midWords1;
                }
                else if (rand <= 0.2f) {
                    result = midWords2;
                }
                else if (rand <= 0.28f) {
                    result = midWords3;
                }
                else if (rand <= 0.36f) {
                    result = midWords4;
                }
                else if (rand <= 0.4f) {
                    result = midWords6;
                }
                else {
                    usePossessive = true;
                }
                /*else if (!articleInFront) {
                    // TODO: Use this only with a verb as the first word
                    // Example 1: Seize the Day
                    // Example 2: Taking the Lead
                    result = midWords5;
                }*/
            }
        }
        else if (rand <= 0.2f) {
            usePossessive = true;
        }

        if (usePossessive) {
            if (lastLetter == 's' || lastLetter == 'x') {
                result = possessive2;
            }
            else {
                result = possessive1;
            }
        }

        return result;
    }

    private static String getRandomWord(int partIndex,
                                        int mainPartCount,
                                        boolean endPart) {

        // Returns an error string if there are no title parts
        if (deck.size() == 0) {
            return "[EMPTY]";
        }

        boolean firstPart = (partIndex == 0);
        boolean lastMainPart = (partIndex == mainPartCount - 1);
        int randNamePartIndex = (int) (Math.random() * deck.size());

        /*// Sets the used word deck
        List<Card> usedDeck = deckFeatures;
        int totalDeckSize = usedDeck.size();
        if (endPart) {
            usedDeck = deckThings;
            totalDeckSize = usedDeck.size();
        } else if (!firstPart) {
            totalDeckSize += deckConcepts.size();
        }

        // TODO: Determine here which words can be used in
        // the beginning, middle and end of the title

        if (!specialCharacterUsed) {
            // Gets a random name part index.
            // If the index would be out of the bounds
            // of usedDeck, deckThings is used instead.
            randNamePartIndex = (int) (Math.random() * totalDeckSize);
            if (!firstPart && !endPart &&
                randNamePartIndex >= usedDeck.size()) {
                randNamePartIndex -= usedDeck.size();
                usedDeck = deckThings;
            }
        }*/

        return deck.get(randNamePartIndex).getName();
    }

    private static String capitalizeFirstLetter(String part) {
        if (part.length() >= 1) {
            String firstLetter = part.substring(0, 1).toUpperCase();
            if (part.length() >= 2) {
                part = firstLetter + part.substring(1);
            }
            else {
                part = firstLetter;
            }
        }

        return part;
    }

   /**
    * Returns a category's name based on the given number.
    *
    * @param categoryNumber a category's number
    * @return the name of a category or an error string
    */
    public static String categoryName(int categoryNumber) {
        switch (categoryNumber) {
            case 0: {
                return "Feature";
            }
            case 1: {
                return "Concept";
            }
            case 2: {
                return "Thing";
            }
            case 3: {
                return "Action";
            }
            case 4: {
                return "Place & Time";
            }

            // Prints an error message if the
            // category's number is out of limits
            default: {
                return "ERROR";
            }
        }
    }

   /**
    * Creates the deck.
    */
    private static void initDeck() {
        deck = new ArrayList<Card>();
        deckFeature = new ArrayList<Card>();
        deckConcept = new ArrayList<Card>();
        deckThing = new ArrayList<Card>();
        deckAction = new ArrayList<Card>();
        deckPlaceTime = new ArrayList<Card>();
        decks = new ArrayList<List<Card>>();
        decks.add(deckFeature);
        decks.add(deckConcept);
        decks.add(deckThing);
        decks.add(deckAction);
        decks.add(deckPlaceTime);

        categorySizes = new ArrayList<Integer>();
        categoryFirstCardIndexes = new ArrayList<Integer>();

        /*
        // Feature
        initCard(0, "");
        // Concept
        initCard(1, "");
        // Thing
        initCard(2, "");
        // Action
        initCard(3, "");
        // Place & Time
        initCard(4, "");
        */

        // Feature
        initCard(0, "Abyssal");
        initCard(0, "Aero");
        initCard(0, "Afflicted");
        initCard(0, "Alien");
        initCard(0, "All");
        initCard(0, "Alpha");
        initCard(0, "Altered");
        initCard(0, "Ancestral");
        initCard(0, "Ancient");
        initCard(0, "Angelic");
        initCard(0, "Anti");
        initCard(0, "Apex");
        initCard(0, "Arcane");
        initCard(0, "Ashen");
        initCard(0, "Astral");
        initCard(0, "Astro");
        initCard(0, "Auto");
        initCard(0, "Avenged");
        initCard(0, "Avian");
        initCard(0, "Awakened");
        initCard(0, "Azure");
        initCard(0, "Back");
        initCard(0, "Bad");
        initCard(0, "Banished");
        initCard(0, "Below");
        initCard(0, "Betrayed");
        initCard(0, "Big");
        initCard(0, "Bio");
        initCard(0, "Black");
        initCard(0, "Blind");
        initCard(0, "Blue");
        initCard(0, "Bound");
        initCard(0, "Brass");
        initCard(0, "Bright");
        initCard(0, "Brilliant");
        initCard(0, "Broken");
        initCard(0, "Bronze");
        initCard(0, "Brutal");
        initCard(0, "Built");
        initCard(0, "Burning");
        initCard(0, "Burnt");
        initCard(0, "Carven");
        initCard(0, "Caustic");
        initCard(0, "Center");
        initCard(0, "Charred");
        initCard(0, "Chemical");
        initCard(0, "Chrome");
        initCard(0, "Clandestine");
        initCard(0, "Clear");
        initCard(0, "Close");
        initCard(0, "Closed");
        initCard(0, "Cold");
        initCard(0, "Complete");
        initCard(0, "Corrosive");
        initCard(0, "Corrupt");
        initCard(0, "Cosmic");
        initCard(0, "Crawling");
        initCard(0, "Criminal");
        initCard(0, "Crimson");
        initCard(0, "Crooked");
        initCard(0, "Cruel");
        initCard(0, "Cursed");
        initCard(0, "Cyber");
        initCard(0, "Damaged");
        initCard(0, "Dark");
        initCard(0, "Darker");
        initCard(0, "Darkest");
        initCard(0, "Dead");
        initCard(0, "Deep");
        initCard(0, "Delirious");
        initCard(0, "Delta");
        initCard(0, "Deluxe");
        initCard(0, "Demonic");
        initCard(0, "Depths");
        initCard(0, "Deviant");
        initCard(0, "Digital");
        initCard(0, "Direct");
        initCard(0, "Dismal");
        initCard(0, "Divine");
        initCard(0, "Double");
        initCard(0, "Down");
        initCard(0, "Drowned");
        initCard(0, "Dry");
        initCard(0, "Dying");
        initCard(0, "Eastern");
        initCard(0, "Ebony");
        initCard(0, "Elder");
        initCard(0, "Eldritch");
        initCard(0, "Electric");
        initCard(0, "Electro");
        initCard(0, "Electronic");
        initCard(0, "Elusive");
        initCard(0, "Empty");
        initCard(0, "Endless");
        initCard(0, "Epic");
        initCard(0, "Eternal");
        initCard(0, "Evil");
        initCard(0, "Evolved");
        initCard(0, "Extraordinary");
        initCard(0, "Faded");
        initCard(0, "Fallen");
        initCard(0, "Falling");
        initCard(0, "False");
        initCard(0, "Familiar");
        initCard(0, "Far");
        initCard(0, "Fatal");
        initCard(0, "Faulty");
        initCard(0, "Final");
        initCard(0, "First");
        initCard(0, "Five");
        initCard(0, "Forgotten");
        initCard(0, "Forlorn");
        initCard(0, "Forsaken");
        initCard(0, "Fortunate");
        initCard(0, "Fractal");
        initCard(0, "Free");
        initCard(0, "Freeze");
        initCard(0, "Fresh");
        initCard(0, "Front");
        initCard(0, "Frost");
        initCard(0, "Frozen");
        initCard(0, "Full");
        initCard(0, "Fun");
        initCard(0, "Future");
        initCard(0, "Galactic");
        initCard(0, "Gamma");
        initCard(0, "Geo");
        initCard(0, "Giant");
        initCard(0, "Gilded");
        initCard(0, "Glass");
        initCard(0, "Gold");
        initCard(0, "Golden");
        initCard(0, "Good");
        initCard(0, "Grand");
        initCard(0, "Granite");
        initCard(0, "Gray");
        initCard(0, "Green");
        initCard(0, "Grim");
        initCard(0, "Guilty");
        initCard(0, "Half");
        initCard(0, "Haunted");
        initCard(0, "Heavy");
        initCard(0, "Heroic");
        initCard(0, "Hidden");
        initCard(0, "High");
        initCard(0, "Hollow");
        initCard(0, "Holy");
        initCard(0, "Horned");
        initCard(0, "Horrible");
        initCard(0, "Howling");
        initCard(0, "Huge");
        initCard(0, "Hundred");
        initCard(0, "Hunted");
        initCard(0, "Hydro");
        initCard(0, "Hyper");
        initCard(0, "Immortal");
        initCard(0, "In");
        initCard(0, "Incredible");
        initCard(0, "Infected");
        initCard(0, "Infernal");
        initCard(0, "Infinite");
        initCard(0, "Invisible");
        initCard(0, "Iron");
        initCard(0, "Ivory");
        initCard(0, "Just");
        initCard(0, "Last");
        initCard(0, "Leaden");
        initCard(0, "Left");
        initCard(0, "Legendary");
        initCard(0, "Light");
        initCard(0, "Little");
        initCard(0, "Living");
        initCard(0, "Locked");
        initCard(0, "Lost");
        initCard(0, "Low");
        initCard(0, "Lunar");
        initCard(0, "Mad");
        initCard(0, "Major");
        initCard(0, "Malevolent");
        initCard(0, "Masked");
        initCard(0, "Mech");
        initCard(0, "Mechanical");
        initCard(0, "Mega");
        initCard(0, "Metal");
        initCard(0, "Million");
        initCard(0, "Molten");
        initCard(0, "Mortal");
        initCard(0, "My");
        initCard(0, "Near");
        initCard(0, "Necro");
        initCard(0, "Neural");
        initCard(0, "Neuro");
        initCard(0, "Neutral");
        initCard(0, "New");
        initCard(0, "Next");
        initCard(0, "No");
        initCard(0, "Northern");
        initCard(0, "Nova");
        initCard(0, "Nuclear");
        initCard(0, "Null");
        initCard(0, "Odd");
        initCard(0, "Off");
        initCard(0, "Old");
        initCard(0, "Omega");
        initCard(0, "On");
        initCard(0, "One");
        initCard(0, "Open");
        initCard(0, "Opened");
        initCard(0, "Original");
        initCard(0, "Our");
        initCard(0, "Out");
        initCard(0, "Outer");
        initCard(0, "Over");
        initCard(0, "Pale");
        initCard(0, "Paper");
        initCard(0, "Past");
        initCard(0, "Perfect");
        initCard(0, "Perished");
        initCard(0, "Pierced");
        initCard(0, "Platinum");
        initCard(0, "Prime");
        initCard(0, "Proto");
        initCard(0, "Psycho");
        initCard(0, "Pure");
        initCard(0, "Pyro");
        initCard(0, "Quad");
        initCard(0, "Quantum");
        initCard(0, "Ravenous");
        initCard(0, "Raw");
        initCard(0, "Real");
        initCard(0, "Red");
        initCard(0, "Reverse");
        initCard(0, "Right");
        initCard(0, "Righteous");
        initCard(0, "Rising");
        initCard(0, "Rival");
        initCard(0, "Rotten");
        initCard(0, "Royal");
        initCard(0, "Sacred");
        initCard(0, "Sad");
        initCard(0, "Savage");
        initCard(0, "Scarred");
        initCard(0, "Scorched");
        initCard(0, "Scorned");
        initCard(0, "Screaming");
        initCard(0, "Second");
        initCard(0, "Secret");
        initCard(0, "Seething");
        initCard(0, "Serious");
        initCard(0, "Seven");
        initCard(0, "Shallow");
        initCard(0, "Shattered");
        initCard(0, "Shredded");
        initCard(0, "Silent");
        initCard(0, "Silver");
        initCard(0, "Sinister");
        initCard(0, "Slain");
        initCard(0, "Small");
        initCard(0, "Solar");
        initCard(0, "Southern");
        initCard(0, "Sovereign");
        initCard(0, "Static");
        initCard(0, "Steel");
        initCard(0, "Stellar");
        initCard(0, "Still");
        initCard(0, "Stolen");
        initCard(0, "Stone");
        initCard(0, "Strange");
        initCard(0, "Strong");
        initCard(0, "Stygian");
        initCard(0, "Sub");
        initCard(0, "Sunken");
        initCard(0, "Super");
        initCard(0, "Taken");
        initCard(0, "Taking");
        initCard(0, "Terrible");
        initCard(0, "This");
        initCard(0, "Thousand");
        initCard(0, "Tiny");
        initCard(0, "Torn");
        initCard(0, "Total");
        initCard(0, "Toxic");
        initCard(0, "Tribal");
        initCard(0, "Triple");
        initCard(0, "Triumphant");
        initCard(0, "True");
        initCard(0, "Turbo");
        initCard(0, "Twisted");
        initCard(0, "Ultimate");
        initCard(0, "Ultra");
        initCard(0, "Unbelievable");
        initCard(0, "Unbroken");
        initCard(0, "Undead");
        initCard(0, "Under");
        initCard(0, "Undying");
        initCard(0, "Unfamiliar");
        initCard(0, "Unfortunate");
        initCard(0, "Unholy");
        initCard(0, "Unknown");
        initCard(0, "Unlimited");
        initCard(0, "Unseen");
        initCard(0, "Up");
        initCard(0, "Valiant");
        initCard(0, "Vigilant");
        initCard(0, "Vile");
        initCard(0, "Violent");
        initCard(0, "Virtual");
        initCard(0, "Wailing");
        initCard(0, "Weak");
        initCard(0, "Western");
        initCard(0, "Wet");
        initCard(0, "White");
        initCard(0, "Wicked");
        initCard(0, "Wild");
        initCard(0, "Withered");
        initCard(0, "Wooden");
        initCard(0, "Wretched");
        initCard(0, "X");
        initCard(0, "Xeno");
        initCard(0, "Yellow");
        initCard(0, "Zero");
        // Concept
        initCard(1, "Absolution");
        initCard(1, "Adventure");
        initCard(1, "Adventures");
        initCard(1, "Affliction");
        initCard(1, "Amnesia");
        initCard(1, "Ancestors");
        initCard(1, "Anger");
        initCard(1, "Annihilation");
        initCard(1, "Anomaly");
        initCard(1, "Anthem");
        initCard(1, "Apocalypse");
        initCard(1, "Art");
        initCard(1, "Ascendant");
        initCard(1, "Atom");
        initCard(1, "Band");
        initCard(1, "Bane");
        initCard(1, "Battalion");
        initCard(1, "Battle");
        initCard(1, "Beacon");
        initCard(1, "Beginning");
        initCard(1, "Belief");
        initCard(1, "Betrayal");
        initCard(1, "Birth");
        initCard(1, "Brotherhood");
        initCard(1, "Challenge");
        initCard(1, "Chaos");
        initCard(1, "Choir");
        initCard(1, "Chrysalis");
        initCard(1, "Clan");
        initCard(1, "Company");
        initCard(1, "Conflict");
        initCard(1, "Conspiracy");
        initCard(1, "Contract");
        initCard(1, "Core");
        initCard(1, "Covenant");
        initCard(1, "Creed");
        initCard(1, "Crew");
        initCard(1, "Crime");
        initCard(1, "Crimes");
        initCard(1, "Crisis");
        initCard(1, "Cross");
        initCard(1, "Cruelty");
        initCard(1, "Crux");
        initCard(1, "Cult");
        initCard(1, "Cure");
        initCard(1, "Danger");
        initCard(1, "Darkness");
        initCard(1, "Data");
        initCard(1, "Death");
        initCard(1, "Defeat");
        initCard(1, "Defence");
        initCard(1, "Descendant");
        initCard(1, "Destination");
        initCard(1, "Destiny");
        initCard(1, "Destruction");
        initCard(1, "Dimension");
        initCard(1, "Disaster");
        initCard(1, "Disease");
        initCard(1, "Distance");
        initCard(1, "Divinity");
        initCard(1, "Doom");
        initCard(1, "Doubt");
        initCard(1, "Dream");
        initCard(1, "Dreams");
        initCard(1, "Drome");
        initCard(1, "Echo");
        initCard(1, "Echoes");
        initCard(1, "Edge");
        initCard(1, "Effect");
        initCard(1, "Electron");
        initCard(1, "Enclave");
        initCard(1, "End");
        initCard(1, "Ends");
        initCard(1, "Energy");
        initCard(1, "Epidemic");
        initCard(1, "Error");
        initCard(1, "Essence");
        initCard(1, "Event");
        initCard(1, "Events");
        initCard(1, "Exile");
        initCard(1, "Factor");
        initCard(1, "Faith");
        initCard(1, "Family");
        initCard(1, "Fate");
        initCard(1, "Fault");
        initCard(1, "Fear");
        initCard(1, "Filth");
        initCard(1, "Fleet");
        initCard(1, "Force");
        initCard(1, "Fortune");
        initCard(1, "Forward");
        initCard(1, "Fragment");
        initCard(1, "Frame");
        initCard(1, "Freedom");
        initCard(1, "Funeral");
        initCard(1, "Fury");
        initCard(1, "Gale");
        initCard(1, "Gang");
        initCard(1, "Genesis");
        initCard(1, "Ghost");
        initCard(1, "Ghosts");
        initCard(1, "Glitch");
        initCard(1, "Glory");
        initCard(1, "Greed");
        initCard(1, "Grid");
        initCard(1, "Guild");
        initCard(1, "Guilt");
        initCard(1, "Harm");
        initCard(1, "Harmony");
        initCard(1, "Harvest");
        initCard(1, "Hate");
        initCard(1, "Hatred");
        initCard(1, "Havoc");
        initCard(1, "Hazard");
        initCard(1, "Heap");
        initCard(1, "Heresy");
        initCard(1, "History");
        initCard(1, "Honor");
        initCard(1, "Hope");
        initCard(1, "Horizon");
        initCard(1, "Horror");
        initCard(1, "Hospitality");
        initCard(1, "Hunger");
        initCard(1, "Identity");
        initCard(1, "Immortality");
        initCard(1, "Infinity");
        initCard(1, "Instinct");
        initCard(1, "Joy");
        initCard(1, "Justice");
        initCard(1, "Law");
        initCard(1, "Layer");
        initCard(1, "League");
        initCard(1, "Legacy");
        initCard(1, "Legend");
        initCard(1, "Legends");
        initCard(1, "Legion");
        initCard(1, "Liberty");
        initCard(1, "Lie");
        initCard(1, "Lies");
        initCard(1, "Life");
        initCard(1, "Line");
        initCard(1, "Love");
        initCard(1, "Luminescence");
        initCard(1, "Madness");
        initCard(1, "Madness");
        initCard(1, "Maelstrom");
        initCard(1, "Malevolence");
        initCard(1, "Mark");
        initCard(1, "Marvel");
        initCard(1, "Matrix");
        initCard(1, "Matter");
        initCard(1, "Mayhem");
        initCard(1, "Mercy");
        initCard(1, "Might");
        initCard(1, "Mind");
        initCard(1, "Minds");
        initCard(1, "Minute");
        initCard(1, "Misadventure");
        initCard(1, "Misadventures");
        initCard(1, "Misery");
        initCard(1, "Misfortune");
        initCard(1, "Mission");
        initCard(1, "Mistake");
        initCard(1, "Mob");
        initCard(1, "Mod");
        initCard(1, "Mode");
        initCard(1, "Myth");
        initCard(1, "Myths");
        initCard(1, "Name");
        initCard(1, "Names");
        initCard(1, "Narrative");
        initCard(1, "Nature");
        initCard(1, "Neutrality");
        initCard(1, "Neutron");
        initCard(1, "Nexus");
        initCard(1, "Nothing");
        initCard(1, "Oath");
        initCard(1, "Oblivion");
        initCard(1, "Obscurity");
        initCard(1, "Octane");
        initCard(1, "Odyssey");
        initCard(1, "Omen");
        initCard(1, "Onward");
        initCard(1, "Order");
        initCard(1, "Organization");
        initCard(1, "Outcast");
        initCard(1, "Outsider");
        initCard(1, "Pact");
        initCard(1, "Pain");
        initCard(1, "Pandemic");
        initCard(1, "Panic");
        initCard(1, "Pantheon");
        initCard(1, "Parasite");
        initCard(1, "Party");
        initCard(1, "Passage");
        initCard(1, "Peace");
        initCard(1, "Peak");
        initCard(1, "Perimeter");
        initCard(1, "Phantom");
        initCard(1, "Phenomenon");
        initCard(1, "Pinnacle");
        initCard(1, "Pity");
        initCard(1, "Plague");
        initCard(1, "Plane");
        initCard(1, "Planes");
        initCard(1, "Point");
        initCard(1, "Power");
        initCard(1, "Prayer");
        initCard(1, "Prelude");
        initCard(1, "Prestige");
        initCard(1, "Pride");
        initCard(1, "Prophecy");
        initCard(1, "Proton");
        initCard(1, "Pulse");
        initCard(1, "Punk");
        initCard(1, "Purpose");
        initCard(1, "Quest");
        initCard(1, "Radian");
        initCard(1, "Rage");
        initCard(1, "Ransom");
        initCard(1, "Ray");
        initCard(1, "Reality");
        initCard(1, "Remedy");
        initCard(1, "Rift");
        initCard(1, "Rim");
        initCard(1, "Ripple");
        initCard(1, "Risk");
        initCard(1, "Rite");
        initCard(1, "Ritual");
        initCard(1, "Ruin");
        initCard(1, "Ruse");
        initCard(1, "Sacrifice");
        initCard(1, "Scape");
        initCard(1, "Scorn");
        initCard(1, "Scourge");
        initCard(1, "Seal");
        initCard(1, "Series");
        initCard(1, "Shade");
        initCard(1, "Shadow");
        initCard(1, "Shadows");
        initCard(1, "Shape");
        initCard(1, "Side");
        initCard(1, "Signal");
        initCard(1, "Silence");
        initCard(1, "Sin");
        initCard(1, "Sins");
        initCard(1, "Skirmish");
        initCard(1, "Solace");
        initCard(1, "Solstice");
        initCard(1, "Soul");
        initCard(1, "Souls");
        initCard(1, "Source");
        initCard(1, "Spirit");
        initCard(1, "Spirits");
        initCard(1, "Spite");
        initCard(1, "Splendor");
        initCard(1, "State");
        initCard(1, "Stories");
        initCard(1, "Storm");
        initCard(1, "Story");
        initCard(1, "Strength");
        initCard(1, "Strife");
        initCard(1, "Supremacy");
        initCard(1, "Tale");
        initCard(1, "Tales");
        initCard(1, "Team");
        initCard(1, "Tech");
        initCard(1, "Tempest");
        initCard(1, "Terror");
        initCard(1, "Theory");
        initCard(1, "Threshold");
        initCard(1, "Thunder");
        initCard(1, "Tide");
        initCard(1, "Time");
        initCard(1, "Torment");
        initCard(1, "Trail");
        initCard(1, "Trials");
        initCard(1, "Tribute");
        initCard(1, "Triumph");
        initCard(1, "Truth");
        initCard(1, "Type");
        initCard(1, "Tyranny");
        initCard(1, "Ultimatum");
        initCard(1, "Umbra");
        initCard(1, "Us");
        initCard(1, "Valor");
        initCard(1, "Vantage");
        initCard(1, "Vector");
        initCard(1, "Verse");
        initCard(1, "Victory");
        initCard(1, "Vigilance");
        initCard(1, "Violence");
        initCard(1, "Virtue");
        initCard(1, "Virtues");
        initCard(1, "Volt");
        initCard(1, "Vortex");
        initCard(1, "War");
        initCard(1, "Ward");
        initCard(1, "Warning");
        initCard(1, "Wave");
        initCard(1, "Way");
        initCard(1, "We");
        initCard(1, "Weakness");
        initCard(1, "What");
        initCard(1, "When");
        initCard(1, "Whisper");
        initCard(1, "Whispers");
        initCard(1, "Why");
        initCard(1, "Wisdom");
        initCard(1, "Wonder");
        initCard(1, "Wrath");
        initCard(1, "You");
        initCard(1, "Zodiac");
        // Thing
        initCard(2, "Acid");
        initCard(2, "Air");
        initCard(2, "Amulet");
        initCard(2, "Android");
        initCard(2, "Angel");
        initCard(2, "Angels");
        initCard(2, "Anvil");
        initCard(2, "Arc");
        initCard(2, "Arch");
        initCard(2, "Armor");
        initCard(2, "Ash");
        initCard(2, "Axe");
        initCard(2, "Banner");
        initCard(2, "Bass");
        initCard(2, "Beast");
        initCard(2, "Bird");
        initCard(2, "Bishop");
        initCard(2, "Blade");
        initCard(2, "Blaze");
        initCard(2, "Blizzard");
        initCard(2, "Blood");
        initCard(2, "Board");
        initCard(2, "Bomb");
        initCard(2, "Bone");
        initCard(2, "Book");
        initCard(2, "Box");
        initCard(2, "Brain");
        initCard(2, "Brains");
        initCard(2, "Bride");
        initCard(2, "Brother");
        initCard(2, "Brothers");
        initCard(2, "Bullet");
        initCard(2, "Bullets");
        initCard(2, "Cage");
        initCard(2, "Candle");
        initCard(2, "Cannon");
        initCard(2, "Canvas");
        initCard(2, "Cape");
        initCard(2, "Case");
        initCard(2, "Chain");
        initCard(2, "Chains");
        initCard(2, "Chainsaw");
        initCard(2, "Child");
        initCard(2, "Children");
        initCard(2, "Chronicles");
        initCard(2, "Circle");
        initCard(2, "Claw");
        initCard(2, "Claws");
        initCard(2, "Cloak");
        initCard(2, "Cobra");
        initCard(2, "Code");
        initCard(2, "Codex");
        initCard(2, "Coil");
        initCard(2, "Compass");
        initCard(2, "Corpse");
        initCard(2, "Court");
        initCard(2, "Coyote");
        initCard(2, "Creature");
        initCard(2, "Crow");
        initCard(2, "Crown");
        initCard(2, "Crystal");
        initCard(2, "Cube");
        initCard(2, "Dagger");
        initCard(2, "Daughter");
        initCard(2, "Daughters");
        initCard(2, "Demon");
        initCard(2, "Demons");
        initCard(2, "Devil");
        initCard(2, "Diamond");
        initCard(2, "Disc");
        initCard(2, "Doll");
        initCard(2, "Dome");
        initCard(2, "Door");
        initCard(2, "Dragon");
        initCard(2, "Drone");
        initCard(2, "Drum");
        initCard(2, "Drums");
        initCard(2, "Dust");
        initCard(2, "Ember");
        initCard(2, "Embers");
        initCard(2, "Engine");
        initCard(2, "Eye");
        initCard(2, "Eyes");
        initCard(2, "Falcon");
        initCard(2, "Father");
        initCard(2, "Feather");
        initCard(2, "Files");
        initCard(2, "Fire");
        initCard(2, "Fist");
        initCard(2, "Flame");
        initCard(2, "Flesh");
        initCard(2, "Flood");
        initCard(2, "Flower");
        initCard(2, "Flute");
        initCard(2, "Fog");
        initCard(2, "Forge");
        initCard(2, "Fuel");
        initCard(2, "Furnace");
        initCard(2, "Galleon");
        initCard(2, "Gallows");
        initCard(2, "Gate");
        initCard(2, "Gates");
        initCard(2, "Gauntlet");
        initCard(2, "Gear");
        initCard(2, "Gears");
        initCard(2, "Giants");
        initCard(2, "Glaive");
        initCard(2, "God");
        initCard(2, "Goddess");
        initCard(2, "Guide");
        initCard(2, "Guitar");
        initCard(2, "Gun");
        initCard(2, "Halo");
        initCard(2, "Hammer");
        initCard(2, "Hand");
        initCard(2, "Harp");
        initCard(2, "Hawk");
        initCard(2, "Head");
        initCard(2, "Heart");
        initCard(2, "Hearts");
        initCard(2, "Hero");
        initCard(2, "Heroes");
        initCard(2, "Hood");
        initCard(2, "Horn");
        initCard(2, "Horns");
        initCard(2, "Horse");
        initCard(2, "Human");
        initCard(2, "Hurricane");
        initCard(2, "Hydra");
        initCard(2, "Hyena");
        initCard(2, "Ice");
        initCard(2, "Illusion");
        initCard(2, "Ink");
        initCard(2, "Insignia");
        initCard(2, "Ion");
        initCard(2, "Jackal");
        initCard(2, "Jet");
        initCard(2, "Jewel");
        initCard(2, "Lady");
        initCard(2, "Lance");
        initCard(2, "Laser");
        initCard(2, "Leech");
        initCard(2, "Leviathan");
        initCard(2, "Lightning");
        initCard(2, "Lion");
        initCard(2, "Lisk");
        initCard(2, "Lizard");
        initCard(2, "Lock");
        initCard(2, "Lord");
        initCard(2, "Machine");
        initCard(2, "Machines");
        initCard(2, "Magma");
        initCard(2, "Magnet");
        initCard(2, "Man");
        initCard(2, "Mantis");
        initCard(2, "Mask");
        initCard(2, "Maw");
        initCard(2, "Meteor");
        initCard(2, "Mirage");
        initCard(2, "Mirror");
        initCard(2, "Mist");
        initCard(2, "Monster");
        initCard(2, "Mother");
        initCard(2, "Neon");
        initCard(2, "Nitro");
        initCard(2, "Note");
        initCard(2, "Oil");
        initCard(2, "Orb");
        initCard(2, "Pariah");
        initCard(2, "Pearl");
        initCard(2, "Phoenix");
        initCard(2, "Phone");
        initCard(2, "Pillar");
        initCard(2, "Pillars");
        initCard(2, "Piranha");
        initCard(2, "Planet");
        initCard(2, "Plasma");
        initCard(2, "Poison");
        initCard(2, "Rain");
        initCard(2, "Rat");
        initCard(2, "Rats");
        initCard(2, "Raven");
        initCard(2, "Ravens");
        initCard(2, "Rig");
        initCard(2, "Rigs");
        initCard(2, "Ring");
        initCard(2, "Robe");
        initCard(2, "Robot");
        initCard(2, "Robots");
        initCard(2, "Rock");
        initCard(2, "Rocket");
        initCard(2, "Rook");
        initCard(2, "Rookie");
        initCard(2, "Rooster");
        initCard(2, "Rose");
        initCard(2, "Rune");
        initCard(2, "Runes");
        initCard(2, "Salt");
        initCard(2, "Saw");
        initCard(2, "Scar");
        initCard(2, "Scarab");
        initCard(2, "Scars");
        initCard(2, "Scorpion");
        initCard(2, "Scrap");
        initCard(2, "Script");
        initCard(2, "Scroll");
        initCard(2, "Scythe");
        initCard(2, "Seed");
        initCard(2, "Seeds");
        initCard(2, "Shark");
        initCard(2, "Shell");
        initCard(2, "Shield");
        initCard(2, "Shot");
        initCard(2, "Shreds");
        initCard(2, "Sign");
        initCard(2, "Signs");
        initCard(2, "Sister");
        initCard(2, "Sisters");
        initCard(2, "Skin");
        initCard(2, "Skull");
        initCard(2, "Smoke");
        initCard(2, "Snake");
        initCard(2, "Snow");
        initCard(2, "Son");
        initCard(2, "Song");
        initCard(2, "Sons");
        initCard(2, "Spear");
        initCard(2, "Spell");
        initCard(2, "Sphere");
        initCard(2, "Sphinx");
        initCard(2, "Spider");
        initCard(2, "Spire");
        initCard(2, "Square");
        initCard(2, "Steam");
        initCard(2, "Swan");
        initCard(2, "Sword");
        initCard(2, "Symbol");
        initCard(2, "Symphony");
        initCard(2, "Talon");
        initCard(2, "Tank");
        initCard(2, "Tether");
        initCard(2, "Thorn");
        initCard(2, "Throne");
        initCard(2, "Tiger");
        initCard(2, "Titan");
        initCard(2, "Tome");
        initCard(2, "Tooth");
        initCard(2, "Tooth");
        initCard(2, "Torch");
        initCard(2, "Trap");
        initCard(2, "Tree");
        initCard(2, "Triangle");
        initCard(2, "Tribe");
        initCard(2, "Valkyrie");
        initCard(2, "Venom");
        initCard(2, "Vermin");
        initCard(2, "Villain");
        initCard(2, "Villains");
        initCard(2, "Violin");
        initCard(2, "Viper");
        initCard(2, "Walls");
        initCard(2, "Water");
        initCard(2, "Web");
        initCard(2, "Wheel");
        initCard(2, "Wind");
        initCard(2, "Window");
        initCard(2, "Winds");
        initCard(2, "Wing");
        initCard(2, "Wings");
        initCard(2, "Witch");
        initCard(2, "Wolf");
        initCard(2, "Woman");
        initCard(2, "Word");
        initCard(2, "Words");
        initCard(2, "Zephyr");
        // Action
        initCard(3, "Access");
        initCard(3, "Apostate");
        initCard(3, "Architect");
        initCard(3, "Army");
        initCard(3, "Armies");
        initCard(3, "Artist");
        initCard(3, "Ascend");
        initCard(3, "Ascension");
        initCard(3, "Assault");
        initCard(3, "Attack");
        initCard(3, "Avenger");
        initCard(3, "Awaken");
        initCard(3, "Awakening");
        initCard(3, "Bandit");
        initCard(3, "Bandits");
        initCard(3, "Banishment");
        initCard(3, "Beggar");
        initCard(3, "Believe");
        initCard(3, "Blast");
        initCard(3, "Blaster");
        initCard(3, "Bleed");
        initCard(3, "Blink");
        initCard(3, "Born");
        initCard(3, "Borne");
        initCard(3, "Break");
        initCard(3, "Burn");
        initCard(3, "Call");
        initCard(3, "Caller");
        initCard(3, "Captain");
        initCard(3, "Cast");
        initCard(3, "Caster");
        initCard(3, "Champion");
        initCard(3, "Champions");
        initCard(3, "Citizen");
        initCard(3, "Citizens");
        initCard(3, "Combat");
        initCard(3, "Combine");
        initCard(3, "Command");
        initCard(3, "Commander");
        initCard(3, "Conquest");
        initCard(3, "Control");
        initCard(3, "Corrosion");
        initCard(3, "Corruption");
        initCard(3, "Craft");
        initCard(3, "Crash");
        initCard(3, "Creation");
        initCard(3, "Crook");
        initCard(3, "Crusade");
        initCard(3, "Cry");
        initCard(3, "Curse");
        initCard(3, "Damage");
        initCard(3, "Deception");
        initCard(3, "Descend");
        initCard(3, "Descent");
        initCard(3, "Design");
        initCard(3, "Destroyer");
        initCard(3, "Devotion");
        initCard(3, "Devourer");
        initCard(3, "Discovery");
        initCard(3, "Dive");
        initCard(3, "Division");
        initCard(3, "Doctor");
        initCard(3, "Dominion");
        initCard(3, "Drift");
        initCard(3, "Drifter");
        initCard(3, "Drop");
        initCard(3, "Duel");
        initCard(3, "Embrace");
        initCard(3, "Emperor");
        initCard(3, "Empress");
        initCard(3, "Encounter");
        initCard(3, "Enter");
        initCard(3, "Escape");
        initCard(3, "Evolution");
        initCard(3, "Evolve");
        initCard(3, "Existence");
        initCard(3, "Expedition");
        initCard(3, "Fall");
        initCard(3, "Fell");
        initCard(3, "Fighter");
        initCard(3, "Fighters");
        initCard(3, "Flash");
        initCard(3, "Flight");
        initCard(3, "Fold");
        initCard(3, "Follower");
        initCard(3, "Fool");
        initCard(3, "Fuse");
        initCard(3, "Fusion");
        initCard(3, "Glow");
        initCard(3, "Grip");
        initCard(3, "Guard");
        initCard(3, "Guardian");
        initCard(3, "Guardians");
        initCard(3, "Guillotine");
        initCard(3, "Has");
        initCard(3, "Have");
        initCard(3, "Heal");
        initCard(3, "Healing");
        initCard(3, "Heist");
        initCard(3, "Herald");
        initCard(3, "Heretic");
        initCard(3, "Hex");
        initCard(3, "Hit");
        initCard(3, "Hold");
        initCard(3, "Howl");
        initCard(3, "Hunt");
        initCard(3, "Hunter");
        initCard(3, "Hurt");
        initCard(3, "Infection");
        initCard(3, "Journey");
        initCard(3, "Keeper");
        initCard(3, "Kill");
        initCard(3, "Killer");
        initCard(3, "Killers");
        initCard(3, "King");
        initCard(3, "Kings");
        initCard(3, "Knave");
        initCard(3, "Knight");
        initCard(3, "Knights");
        initCard(3, "Knowing");
        initCard(3, "Lancer");
        initCard(3, "Lead");
        initCard(3, "Leader");
        initCard(3, "Leaders");
        initCard(3, "Legions");
        initCard(3, "Link");
        initCard(3, "Maker");
        initCard(3, "Mancer");
        initCard(3, "March");
        initCard(3, "Marine");
        initCard(3, "Master");
        initCard(3, "Masters");
        initCard(3, "Miss");
        initCard(3, "Mister");
        initCard(3, "Morph");
        initCard(3, "Mourn");
        initCard(3, "Mourning");
        initCard(3, "Mrs");
        initCard(3, "Nemesis");
        initCard(3, "Nomad");
        initCard(3, "Operation");
        initCard(3, "Pass");
        initCard(3, "Perish");
        initCard(3, "Pierce");
        initCard(3, "Piercer");
        initCard(3, "Pilgrim");
        initCard(3, "Pilgrimage");
        initCard(3, "Pilgrims");
        initCard(3, "Pioneer");
        initCard(3, "Pioneers");
        initCard(3, "Plot");
        initCard(3, "Predator");
        initCard(3, "Prey");
        initCard(3, "Prey");
        initCard(3, "Prince");
        initCard(3, "Prophet");
        initCard(3, "Punishment");
        initCard(3, "Purge");
        initCard(3, "Queen");
        initCard(3, "Raid");
        initCard(3, "Raider");
        initCard(3, "Razor");
        initCard(3, "Reactor");
        initCard(3, "Reaper");
        initCard(3, "Rebellion");
        initCard(3, "Recluse");
        initCard(3, "Redemption");
        initCard(3, "Reign");
        initCard(3, "Rest");
        initCard(3, "Retribution");
        initCard(3, "Return");
        initCard(3, "Revelation");
        initCard(3, "Revelations");
        initCard(3, "Revenge");
        initCard(3, "Revolution");
        initCard(3, "Rider");
        initCard(3, "Riot");
        initCard(3, "Rip");
        initCard(3, "Rise");
        initCard(3, "Rising");
        initCard(3, "Rule");
        initCard(3, "Run");
        initCard(3, "Saint");
        initCard(3, "Saints");
        initCard(3, "Salvation");
        initCard(3, "Savior");
        initCard(3, "Scavenger");
        initCard(3, "Scorch");
        initCard(3, "Scream");
        initCard(3, "Seeker");
        initCard(3, "Seer");
        initCard(3, "Seize");
        initCard(3, "Seizing");
        initCard(3, "Servant");
        initCard(3, "Servants");
        initCard(3, "Servitor");
        initCard(3, "Servitors");
        initCard(3, "Servitude");
        initCard(3, "Shift");
        initCard(3, "Shifter");
        initCard(3, "Shock");
        initCard(3, "Siege");
        initCard(3, "Sinner");
        initCard(3, "Sinners");
        initCard(3, "Slayer");
        initCard(3, "Slinger");
        initCard(3, "Soldier");
        initCard(3, "Soldiers");
        initCard(3, "Spark");
        initCard(3, "Spawn");
        initCard(3, "Spitter");
        initCard(3, "Splash");
        initCard(3, "Stalker");
        initCard(3, "Stand");
        initCard(3, "Sting");
        initCard(3, "Stop");
        initCard(3, "Stranger");
        initCard(3, "Strangle");
        initCard(3, "Strike");
        initCard(3, "Study");
        initCard(3, "Surge");
        initCard(3, "Take");
        initCard(3, "Taker");
        initCard(3, "Thief");
        initCard(3, "Thrower");
        initCard(3, "Thug");
        initCard(3, "Traitor");
        initCard(3, "Traveler");
        initCard(3, "Tyrant");
        initCard(3, "Vandal");
        initCard(3, "Vanguard");
        initCard(3, "Vengeance");
        initCard(3, "Versus");
        initCard(3, "Victor");
        initCard(3, "Victors");
        initCard(3, "Viking");
        initCard(3, "Vision");
        initCard(3, "Visitor");
        initCard(3, "Walk");
        initCard(3, "Walker");
        initCard(3, "Warlord");
        initCard(3, "Warlords");
        initCard(3, "Warrior");
        initCard(3, "Warriors");
        initCard(3, "Waste");
        initCard(3, "Watch");
        initCard(3, "Widow");
        initCard(3, "Wither");
        initCard(3, "Witness");
        initCard(3, "Woods");
        initCard(3, "Work");
        initCard(3, "Worship");
        // Place & Time
        initCard(4, "Abyss");
        initCard(4, "Age");
        initCard(4, "Altar");
        initCard(4, "Always");
        initCard(4, "Archive");
        initCard(4, "Archives");
        initCard(4, "Autumn");
        initCard(4, "Base");
        initCard(4, "Bastion");
        initCard(4, "Bay");
        initCard(4, "Bog");
        initCard(4, "Border");
        initCard(4, "Canyon");
        initCard(4, "Castle");
        initCard(4, "Cave");
        initCard(4, "Cavern");
        initCard(4, "Century");
        initCard(4, "Chamber");
        initCard(4, "Chasm");
        initCard(4, "Church");
        initCard(4, "Citadel");
        initCard(4, "City");
        initCard(4, "Coast");
        initCard(4, "Colony");
        initCard(4, "Cosmos");
        initCard(4, "Cove");
        initCard(4, "Crater");
        initCard(4, "Crypt");
        initCard(4, "Dawn");
        initCard(4, "Day");
        initCard(4, "Days");
        initCard(4, "Den");
        initCard(4, "Desert");
        initCard(4, "Domain");
        initCard(4, "Dungeon");
        initCard(4, "Earth");
        initCard(4, "East");
        initCard(4, "Eden");
        initCard(4, "Empire");
        initCard(4, "Estate");
        initCard(4, "Eternity");
        initCard(4, "Eve");
        initCard(4, "Evening");
        initCard(4, "Field");
        initCard(4, "Fields");
        initCard(4, "Forest");
        initCard(4, "Forever");
        initCard(4, "Fort");
        initCard(4, "Fortress");
        initCard(4, "Frontier");
        initCard(4, "Galaxy");
        initCard(4, "Gallery");
        initCard(4, "Garden");
        initCard(4, "Grave");
        initCard(4, "Ground");
        initCard(4, "Grove");
        initCard(4, "Hall");
        initCard(4, "Halls");
        initCard(4, "Haven");
        initCard(4, "Heaven");
        initCard(4, "Hell");
        initCard(4, "Hive");
        initCard(4, "Home");
        initCard(4, "Hour");
        initCard(4, "House");
        initCard(4, "Inferno");
        initCard(4, "Island");
        initCard(4, "Keep");
        initCard(4, "Kingdom");
        initCard(4, "Laboratory");
        initCard(4, "Labyrinth");
        initCard(4, "Lair");
        initCard(4, "Lake");
        initCard(4, "Land");
        initCard(4, "Lands");
        initCard(4, "Library");
        initCard(4, "Luna");
        initCard(4, "Marsh");
        initCard(4, "Maze");
        initCard(4, "Midnight");
        initCard(4, "Millennium");
        initCard(4, "Mine");
        initCard(4, "Mines");
        initCard(4, "Month");
        initCard(4, "Moon");
        initCard(4, "Morning");
        initCard(4, "Mountain");
        initCard(4, "Nest");
        initCard(4, "Nether");
        initCard(4, "Never");
        initCard(4, "Night");
        initCard(4, "Nights");
        initCard(4, "Noon");
        initCard(4, "North");
        initCard(4, "Now");
        initCard(4, "Nowhere");
        initCard(4, "Ocean");
        initCard(4, "Olympus");
        initCard(4, "Orbit");
        initCard(4, "Palace");
        initCard(4, "Paradise");
        initCard(4, "Path");
        initCard(4, "Pit");
        initCard(4, "Plaza");
        initCard(4, "Pool");
        initCard(4, "Realm");
        initCard(4, "Ridge");
        initCard(4, "River");
        initCard(4, "Road");
        initCard(4, "Route");
        initCard(4, "Ruins");
        initCard(4, "Sanctuary");
        initCard(4, "Sea");
        initCard(4, "Seas");
        initCard(4, "Season");
        initCard(4, "Sector");
        initCard(4, "Shelter");
        initCard(4, "Ship");
        initCard(4, "Shrine");
        initCard(4, "Skies");
        initCard(4, "Sky");
        initCard(4, "Sol");
        initCard(4, "South");
        initCard(4, "Space");
        initCard(4, "Spring");
        initCard(4, "Star");
        initCard(4, "Stars");
        initCard(4, "Station");
        initCard(4, "Summer");
        initCard(4, "Sun");
        initCard(4, "Surface");
        initCard(4, "Swamp");
        initCard(4, "Temple");
        initCard(4, "Terra");
        initCard(4, "Today");
        initCard(4, "Tomb");
        initCard(4, "Tomorrow");
        initCard(4, "Tower");
        initCard(4, "Town");
        initCard(4, "Universe");
        initCard(4, "Valley");
        initCard(4, "Vault");
        initCard(4, "Village");
        initCard(4, "Void");
        initCard(4, "Well");
        initCard(4, "West");
        initCard(4, "Where");
        initCard(4, "Winter");
        initCard(4, "World");
        initCard(4, "Yard");
        initCard(4, "Year");
        initCard(4, "Yesterday");
        initCard(4, "Zone");
    }

   /**
    * Initializes multiple cards.
    *
    * @param category    a category's index
    * @param names       cards' names
    */
    private static void initCard(int category, String... names) {
        for (String name : names) {
            initCard(category, name);
        }
    }

   /**
    * Initializes a card.
    *
    * @param category    a category's index
    * @param name        the card's name
    */
    private static void initCard(int category, String name) {

        // Creates the card
        Card card = new Card(category, name);
        deck.add(card);
        decks.get(category).add(card);

        // Adds the current category and all previous
        // missing ones to the category size list; also
        // records the index of the category's first card.
        if (categorySizes.size() <= category) {

            // Missing categories
            int categoryCount = categorySizes.size();
            for (int i = 0; i < category - categoryCount; i++) {
                categorySizes.add(0);
                categoryFirstCardIndexes.add(-1);
            }

            // Current category
            categorySizes.add(1);
            categoryFirstCardIndexes.add(deck.size() - 1);
        }
        // Increases the size of the current category and, if the category
        // was empty, records the index of the category's first card
        else {
            int startingCategorySize = categorySizes.get(category);
            categorySizes.set(category, startingCategorySize + 1);

            if (startingCategorySize == 0) {
                categoryFirstCardIndexes.set(category, deck.size() - 1);
            }
        }
    }

   /**
    * Shuffles the deck.
    * Does nothing if every card is shown.
    */
    private static void shuffleDeck() {
        double rand;
        Card temp;
        for (int i = 0; i < deck.size(); i++) {
            rand = Math.random();
            int randCardIndex = (int) (rand * deck.size());
            temp = deck.get(randCardIndex);
            deck.set(randCardIndex, deck.get(i));
            deck.set(i, temp);
        }

        drawnCardAmount = 0;
    }

   /**
    * Takes a card from the deck and adds it to a hand.
    *
    * @param handIndex  a hand's index
    * @param cardIndex  a card's index
    */
    private static void drawCard(int handIndex, int cardIndex) {
        if (handIndex < handAmount && cardIndex < cardsInHand) {
            hands[handIndex].addCard(cardIndex, deck.get(drawnCardAmount));
            drawnCardAmount++;
        }
    }

   /**
    * Takes a card which belongs a given category and adds it to a hand.
    *
    * @param handIndex  a hand's index
    * @param cardIndex  a card's index
    * @param category   a category's index
    */
    private static void drawCardInCategory(int handIndex, int cardIndex, int category) {
        if (category >= 0 && category < categorySizes.size() &&
             cardIndex < cardsInHand) {
            hands[handIndex].addCard(cardIndex,
                deck.get(categoryFirstCardIndexes.get(category) + cardIndex));
            drawnCardAmount++;
        }
    }

   /**
    * Sorts the given hand by the cards' categories.
    *
    * @param hand   a hand
    */
    private static void sortHandByCategory(Hand hand) {
        for (int i = 0; i < cardsInHand; i++) {
            int smallest = i;
            for (int j = i + 1; j < cardsInHand; j++) {
                if (hand.getCard(j).getCategory() <
                      hand.getCard(smallest).getCategory()) {
                    smallest = j;
                }
            }

            Card temp = hand.getCard(i);
            hand.setCard(i, hand.getCard(smallest));
            hand.setCard(smallest, temp);
        }
    }

   /**
    * Returns the given card's category and name as a string.
    *
    * @param card   a card
    * @return the card's category's and its own name
    */
    public static String cardCatAndName(Card card) {
        return categoryName(card.getCategory()) +
               " - " + card.getName();
    }

    private static String formatCardIndex(int cardIndex, int maxIndex) {
        String formattedCardIndex = "";

        // Presented list numbering starts from 1
        cardIndex++;

        int figuresInCardIndex = 1;
        for (int i = cardIndex; i / 10 >= 1; i = i / 10) {
            figuresInCardIndex++;
        }

        int figuresInMaxIndex = 1;
        for (int j = maxIndex; j / 10 >= 1; j = j / 10) {
            figuresInMaxIndex++;
        }

        for (int k = figuresInCardIndex; k < figuresInMaxIndex; k++) {
            formattedCardIndex += " ";
        }

        formattedCardIndex += cardIndex + " - ";

        return formattedCardIndex;
    }
}

/**
 * An object which holds card information:
 * category (integer) and name (string).
 */
class Card {
    private int category;
    private String name;

   /**
    * Gets the card's category.
    *
    * @return the card's category
    */
    public int getCategory() {
        return category;
    }

   /**
    * Gets the card's name.
    *
    * @return the card's name
    */
    public String getName() {
        return name;
    }

   /**
    * Class constructor.
    *
    * @param category   a category
    * @param name       a name
    */
    public Card(int category, String name) {
        this.category = category;
        this.name = name;
    }
}

/**
 * An object which stores drawn cards.
 */
class Hand {
    private Card[] cards;

   /**
    * Gets the amount of cards in the hand.
    *
    * @return the length of the cards array
    */
    public int getCardAmount() {
        return cards.length;
    }

   /**
    * Gets a card.
    *
    * @param cardIndex  the index of the card
    * @return the card in the given index
    */
    public Card getCard(int cardIndex) {
        return cards[cardIndex];
    }

   /**
    * Sets a card.
    *
    * @param cardIndex  the index of the card
    * @param card       the card to be set
    */
    public void setCard(int cardIndex, Card card) {
        cards[cardIndex] = card;
    }

   /**
    * Class constructor.
    *
    * @param cardAmount the number of cards in the hand
    */
    public Hand(int cardAmount) {
        cards = new Card[cardAmount];
    }

   /**
    * Adds a card to the hand.
    *
    * @param cardIndex  the index of the card
    * @param card       the card to be added
    */
    public void addCard(int cardIndex, Card card) {
        if (cardIndex >= 0 && cardIndex < cards.length) {
            cards[cardIndex] = card;
        }
    }
}
