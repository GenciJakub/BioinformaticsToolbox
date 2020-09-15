import java.util.ArrayList;
import java.util.List;

public class SequenceSimilarity {

    private static String firstMoleculeIdentifier = "";
    private static String secondMoleculeIdentifier = "";

    /**
     * Mani function if this class. Responsible for its basic logic.
     * @param fileNames Names of files which should be used.
     */
    public static void run(String[] fileNames) {

        //Check if it is needed to load molecules from file
        if (checkEmptyList()) {
            Controller.fastaData = FastaProcessor.dataPreprocessor(fileNames);
        } else {
            System.out.println();
            System.out.println("Previously loaded molecules will be available for this task.");
            System.out.println("If you want to change them, please rerun first task with desired file.");
        }

        //This class logic
        while (true) {
            printMoleculeDescriptions();
            if (!runFunction(decideFunction())) {break;}
        }

    }

    /**
     * Check if main list is empty or not.
     * @return True if empty, false if not.
     */
    private static boolean checkEmptyList() {
        if (Controller.fastaData.size() == 0) {return true;}
        else {return false;}
    }

    /**
     * Prints the descriptions of currently loaded molecules.
     */
    private static void printMoleculeDescriptions() {
        System.out.println();
        System.out.println("Do you want to view list of loaded molecules? [ Y / n ]");

        String consoleInput = Controller.consoleInputReader.nextLine();
        if (consoleInput.contentEquals("Y") || consoleInput.contentEquals("y")) {
            System.out.println();
            for (FastaData molecule : Controller.fastaData) {
                System.out.println(molecule.getDescription().substring(1));
            }
        }
    }

    /**
     * Used to read and decide the function ID. Also stores identifiers of the sequences.
     * @return ID of the function (values 0 - 1). Invalid inputs (bigger function ID or missing identifier) are handled in the 'runFunction' method.
     */
    private static int decideFunction() {
        System.out.println();
        System.out.println("Please write the function ID and unique identifier of two molecules into three separate lines.");
        System.out.println("Available functions are:");
        System.out.println("0 - Stop this task.");
        System.out.println("1 - Measure sequence similarity by Hamming distance.");

        //Parsing function ID
        int functionID = 0;
        try {
            functionID = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid ID was passed.");
            return 2;
        }

        //Parsing identifiers
        if (functionID == 0) {
            return 0;
        } else {
            firstMoleculeIdentifier = Controller.consoleInputReader.nextLine();
            secondMoleculeIdentifier = Controller.consoleInputReader.nextLine();
            return functionID;
        }
    }

    /**
     * Main switch of this class. Calls methods which work with data and handle invalid inputs in the 'decideFunction' method.
     * @param functionID ID of the function which sholud be run.
     * @return Boolean value indicating whether user wants to work with this class.
     */
    private static boolean runFunction(int functionID) {
        switch (functionID){
            case 0:
                return false;
            case 1:
                printHammingDistance();
                break;
            case 2:
                break;
            default:
                System.out.println();
                System.out.println("Invalid function ID");
                break;
        }
        return true;
    }

    /**
     * Finds the molecules according to their identifiers and does the logic behind Hamming distance (does not calculate it).
     */
    private static void printHammingDistance() {
        //Find the molecules
        FastaData firstMolecule = null;
        FastaData secondMolecule = null;
        for (FastaData molecule : Controller.fastaData ) {
            if (firstMolecule == null && molecule.getDescription().contains(firstMoleculeIdentifier)) {
                firstMolecule = molecule;
            }
            if (secondMolecule == null && molecule.getDescription().contains(secondMoleculeIdentifier)) {
                secondMolecule = molecule;
            }
        }

        //If identifier was not found
        if (firstMolecule == null || secondMolecule == null) {
            System.out.println();
            System.out.println("These identifiers do not exist in context of this task.");
            return;
        }

        //Check whether sequences have the same length
        if (firstMolecule.getSequenceLength() != secondMolecule.getSequenceLength()) {
            System.out.println();
            System.out.println("These two molecules do not have same sequence length. Please choose molecules with sequences of the same length.");
        } else {
            System.out.println();
            System.out.println(calculateHammingDistance(firstMolecule, secondMolecule));
        }
    }

    /**
     * Calculates Hamming distance of two molecules.
     * @param firstMolecule Stored data about first molecule.
     * @param secondMolecule Stored data about second molecule.
     * @return Hamming distance.
     */
    private static int calculateHammingDistance(FastaData firstMolecule, FastaData secondMolecule) {
        //Change to char[], so substring does not have to be called
        char[] first = firstMolecule.getSequence().toCharArray();
        char[] second = secondMolecule.getSequence().toCharArray();

        //Calculate Hamming distance
        int hammingDist = 0;
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {hammingDist++;}
        }
        return  hammingDist;
    }
}