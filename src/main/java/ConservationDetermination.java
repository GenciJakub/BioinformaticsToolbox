import java.util.ArrayList;
import java.util.List;

public class ConservationDetermination {

    /**
     * List of sequences. Loaded from the file.
     */
    static List<MsaSequence> msa = new ArrayList<>();

    /**
     * Main method of this class. Responsible for its basic logic.
     * @param fileNames Name(s) of file(s) containing sequences which should be aligned.
     */
    public static void run(String[] fileNames) {

        //File processing
        if (fileToProcess(fileNames)) {
            processFile(fileNames[0]);
        } else {
            System.out.println();
            System.out.println("No file was passed.");
            return;
        }

        //Rest of the class logic
        while (true) {
            if (!runFunction(decideFunction())) {break;}
        }
    }

    /**
     * Checks whether file was passed to the run method.
     * @param fileNames Array containing names of files passed.
     * @return True if at least one file was passed, false if 0.
     */
    public static boolean fileToProcess(String[] fileNames) {
        if (fileNames.length > 0) {return true;}
        else {return false;}
    }

    /**
     * If MSA is loaded from task 5, asks user whether he / she wants to work with it (if not, loads one from passed file).
     * If MSA is not loaded, loads it (calls processFile method in the MsaProcessor class).
     * @param fileName Name of the file containing MSA.
     */
    public static void processFile(String fileName) {
        if (Controller.msaData != null && Controller.msaData.size() > 0) {
            //Text for user
            System.out.println();
            System.out.println("You have loaded MSA from the task 5. Do you want to use it? [ Y / n]");

            //Response reading.
            String response = Controller.consoleInputReader.nextLine();

            //Response processing.
            if (response.contentEquals("N") || response.contentEquals("n")) { msa = MsaProcessor.processFile(true, fileName); }
            if (response.contentEquals("Y") || response.contentEquals("y")) { return; }
        }
        //If MSA is not loaded from task 5
        else {
            msa = MsaProcessor.processFile(true,fileName);
        }
    }

    /**
     * Used to read and decide the function ID.
     * @return ID of the function (values 0 - 3). Invalid inputs (bigger function ID or missing identifier) are handled in the 'runFunction' method.
     */
    private static int decideFunction() {
        //Text for user
        System.out.println();
        System.out.println("Please write the ID of the function which you want to run.");
        System.out.println("Available functions are:");
        System.out.println("0 - Stop this task.");
        System.out.println("1 - Retrieve conservation scores for one sequence.");
        System.out.println("2 - Retrieve position scoring above threshold from whole MSA.");

        //Parsing function ID
        int functionID = 0;
        try {
            functionID = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid ID was passed.");
            return 3;
        }

        return functionID;
    }

    /**
     * Main switch of the class. Only calls methods corresponding to their function ID.
     * @param functionID ID of the function user wants to run.
     * @return True if work within this task should continue, false if otherwise.
     */
    private static boolean runFunction(int functionID) {
        switch (functionID){
            case 0:
                return false;
            case 1:
                oneSeqScores();
                break;
            case 2:
                getBestPositions();
                break;
            case 3:
                break;
            default:
                System.out.println();
                System.out.println("Invalid function ID");
                break;
        }
        return true;
    }

    /**
     * Overhead for getting conservation scores from one sequence. Does not do the calculation itself.
     */
    private static void oneSeqScores() {
        //Print names of sequences for the user
        System.out.println();
        System.out.println("You have loaded " + msa.size() + "sequences.");
        System.out.println("These sequences are loaded:");
        for (MsaSequence seq : msa) {
            System.out.println(seq.getName());
        }

        //Getting ID from user
        System.out.println("Which one of them do you want to retrieve? Write ID of the sequence.");
        String seqIdentifier = Controller.consoleInputReader.nextLine();

        //Calling method doing the calculation
        for (MsaSequence seq:msa) {
            if (seq.getName().contains(seqIdentifier)) {
                calculateOneSeqScore(seq);
            }
        }
    }

    /**
     * Calculating conservation score for one sequence.
     * @param chosenSequence Score for this sequence will be calculated.
     */
    private static void calculateOneSeqScore(MsaSequence chosenSequence) {
        //New line for visual gap
        System.out.println();

        //Loop through every position
        for (int i = 0; i < chosenSequence.getWholeSequence().length(); i++) {
            int sameResidues = 0;
            //Loop through every sequence
            for (int j = 0; j < msa.size(); j++) {
                //Incremented also for the chosen sequence
                if (msa.get(j).getSequencePosition(i+1).charAt(0) == chosenSequence.getSequencePosition(i+1).charAt(0)) {sameResidues++;}
            }
            System.out.println("Residue in position " + (i+1) + " is in " + sameResidues + " sequences.");
        }
    }

    /**
     * Gives positions on which residues appear in more sequences. Minimum number of sequences is specified by the user.
     */
    private static void getBestPositions() {
        //Nice text for the user
        System.out.println();
        System.out.println("MSA has length of " + msa.get(0).getWholeSequence().length() + ".");
        System.out.println("What is the score that positions you want to see should have (e.g. 7 if same residue should be in at least 7 sequences)?");

        //Parsing minimum number of sequences
        int countThreshold = 0;
        try {
            countThreshold = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid number.");
            return;
        }

        //Invalid value for number os sequences
        //Less than 1 or more than number of loaded sequences
        if (countThreshold > msa.size() || countThreshold < 1) {
            System.out.println();
            System.out.println("Invalid number.");
            return;
        }

        //Calculation
        int[] values = new int[27]; //Mentally addressed by uppercase letters and '-' symbol
        char residue;
        //Loop through every position
        for (int i = 0; i < msa.get(0).getWholeSequence().length(); i++) {
            //Counting residues at one position
            for (int j = 0; j < msa.size(); j++) {
                residue = msa.get(j).getSequencePosition(i+1).charAt(0);
                if (residue == '-') {values[26]++;}
                else {values[residue - 'A']++;}
            }

            //Printing the information
            for (int j = 0; j < values.length; j++) {
                if (values[j] >= countThreshold) {
                    if (j == 26) {
                        System.out.print("Position " + (i+1) + ", gap appears " + values[j]);
                        if (values[j] == 1) { System.out.println(" time."); }
                        else { System.out.println(" times."); }
                    } else {
                        System.out.print("Position " + (i+1) + ", residue " + (char)(j+'A') + " appears " + values[j]);
                        if (values[j] == 1) { System.out.println(" time."); }
                        else { System.out.println(" times."); }
                    }
                }
                values[j] = 0;
            }
        }
    }
}
