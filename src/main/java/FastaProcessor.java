import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class FastaProcessor {

    /**
     * Data structure used for storing the information about macro molecules in the fasta file.
     */
    private static List<FastaData> storedData = new ArrayList<>();
    private static String sequenceIdentifier = "";

    /**
     * Maim method of this class.
     * @param fileNames Contains name of the files used.
     */
    public static List<FastaData> run(String[] fileNames) {
        dataPreprocessor(fileNames);

        //Work inside this task
        while (true) {
            if (!runFunction(decideFunction())) {break;}
        }
        return storedData;
    }

    /**
     * Cycle for processing each file. Handles the IOException.
     * @param fileNames Array with names of the files.
     */
    public static List<FastaData> dataPreprocessor(String[] fileNames){
        for (String fileName : fileNames) {
            try (BufferedReader reader = new BufferedReader(new FileReader("InputFiles/" + fileName))) {
                preprocessFile(reader);
            }
            catch (IOException e) {
                System.out.println();
                System.out.println("Some error occured. Stopping this task.");
            }
        }
        return storedData;
    }

    /**
     * Used to process one file into the storedData list.
     * @param reader BufferedReader used to read the file.
     * @throws IOException Handled in the calling function.
     */
    private static void preprocessFile(BufferedReader reader) throws IOException {
        String line;
        int seqLength = 0;
        String sequence = "";
        FastaData lastMolecule = new FastaData();

        while ((line = reader.readLine()) != null) {
            //Empty line
            if (line.length() == 0) {
                lastMolecule.setSequenceLength(seqLength);
                lastMolecule.setSequence(sequence);
                if (seqLength > 0) {
                    storedData.add(lastMolecule);
                    seqLength = 0;
                    sequence = "";
                }
                continue;
            }
            //Description line
            if (line.substring(0,1).contentEquals(">")) {
                //If molecules are not delimited by an empty line
                if (seqLength > 0 && lastMolecule.getDescription() != null) {
                    lastMolecule.setSequenceLength(seqLength);
                    lastMolecule.setSequence(sequence);
                    storedData.add(lastMolecule);
                }

                //Memory assignment and initialization of new molecule data
                lastMolecule = new FastaData();
                lastMolecule.setDescription(line);
                seqLength = 0;
                sequence = "";
                continue;
            }
            //Sequence line
            seqLength += line.length();
            sequence = sequence + line;
        }

        //last line of file
        if (seqLength > 0 && lastMolecule.getDescription() != null) {
            lastMolecule.setSequenceLength(seqLength);
            lastMolecule.setSequence(sequence);
            storedData.add(lastMolecule);
        }
    }

    /**
     * Used to read and decide the function ID. Also stores identifier of the sequence in the static variable 'sequenceIdentifier'.
     * @return ID of the function (values 0 - 4). Invalid inputs (bigger function ID or missing identifier) are handled in the 'runFunction' method.
     */
    private static int decideFunction() {
        System.out.println();
        System.out.println("Please write the function ID and identifier of the molecule, which data you want to see.");
        System.out.println("Available functions are:");
        System.out.println("0 - Stop this task.");
        System.out.println("1 - Get the description of the given molecule.");
        System.out.println("2 - Get the sequence of the given molecule.");
        System.out.println("3 - Get the sequence length of the given molecule.");
        System.out.println("4 - Get the subsequence of the given molecule");

        //Reading and parsing user input
        String line = Controller.consoleInputReader.nextLine();
        int spaceIndex = line.indexOf(' ');
        if (line.contentEquals("0")) { return 0; }
        int chosenFunction;
        try {
            chosenFunction = Integer.parseInt(line.substring(0,spaceIndex));
            if (chosenFunction != 0) { sequenceIdentifier = line.substring(spaceIndex + 1); }
        }
        //No identifier
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("You did not write identifier of the sequence.");
            return 5;
        }
        //If structure of the command was correct
        return chosenFunction;
    }

    /**
     * Main switch of this class. Calls methods which work with data and handle invalid inputs in the 'decideFunction' method.
     * @param functionID ID of the function which should be started.
     * @return Boolean value indicating whether user wants to work with this class.
     */
    private static boolean runFunction(int functionID){
        switch (functionID) {
            //If user wants to stop work within this class
            case 0:
                return false;
            case 1:
                printDescription();
                break;
            case 2:
                printSequence();
                break;
            case 3:
                printSequenceLength();
                break;
            case 4:
                printSubsequence();
                break;
            //Invalid function ID - only break should be here
            case 5:
                break;
            default:
                System.out.println();
                System.out.println("Invalid function ID.");
                break;
        }
        //If user wants to continue work within this class
        return true;
    }

    /**
     * Prints the description (without '>' character) of each molecule which description contains substring specified in the static sequenceIdentifier variable.
     */
    private static void printDescription() {
        System.out.println();
        for (FastaData sequence : storedData) {
            if (sequence.getDescription().contains(sequenceIdentifier)) {
                System.out.println(sequence.getDescription().substring(1));
            }
        }
    }

    /**
     * Prints sequence of molecule if its description contains substring specified in the static sequenceIdentifier variable.
     */
    private static void printSequence(){
        System.out.println();
        for (FastaData sequence : storedData) {
            if (sequence.getDescription().contains(sequenceIdentifier)) {
                System.out.println(sequence.getSequence());
            }
        }
    }

    /**
     * Prints the sequence length of the molecule for each molecule with description containing substring specified in the static sequenceIdentifier variable.
     */
    private static void printSequenceLength() {
        System.out.println();
        for (FastaData sequence : storedData) {
            if (sequence.getDescription().contains(sequenceIdentifier)) {
                System.out.println("Sequence length: " + sequence.getSequenceLength());
            }
        }
    }

    /**
     * Gets the subsequence borders and prints the subsequence of the matching molecule sequence.
     */
    private static void printSubsequence(){
        //Get information from user which part of sequence should be printed.
        System.out.println();
        System.out.println("Specify (1-based) positions within the sequence. Subsequence between them will be printed (including those positions).");
        System.out.println("In case you want to print subsequence from one position to the end write only one number. Otherwise write space between them.");
        String input = Controller.consoleInputReader.nextLine();
        int leftBorder, rightBorder = -1;
        if (input.contains(" ")) {
            int spacePosition = input.indexOf(' ');
            leftBorder = Integer.parseInt(input.substring(0,spacePosition)) ;
            rightBorder = Integer.parseInt(input.substring(spacePosition + 1));
        } else {
            leftBorder = Integer.parseInt(input);
        }

        //Printing the subsequence
        for (FastaData sequence : storedData) {
            if (sequence.getDescription().contains(sequenceIdentifier)) {
                if (rightBorder == -1 ) {
                    System.out.println(sequence.getSequence().substring(leftBorder - 1));
                } else {
                    System.out.println(sequence.getSequence().substring(leftBorder - 1, rightBorder));
                }
            }
        }
    }
}
