import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MsaProcessor {

    /**
     * List of sequences. Loaded from the file.
     */
    static List<MsaSequence> msa = new ArrayList<>();

    /**
     * Main method of this class. Responsible for its basic logic.
     * @param fileNames Name(s) of file(s) containing sequences which should be aligned.
     */
    public static List<MsaSequence> run(String[] fileNames) {

        //File processing
        if (fileToProcess(fileNames)) {
            processFile(false, fileNames[0]);
        } else {
            System.out.println();
            System.out.println("No file was passed.");
            return null;
        }

        //Rest of the class logic
        while (true) {
            if (!runFunction(decideFunction())) {break;}
        }

        return msa;
    }

    /**
     * Checks whether file was passed to the run method.
     * @param fileNames Array containing names of files passed.
     * @return True if at least one file was passed, false if 0.
     */
    private static boolean fileToProcess(String[] fileNames) {
        if (fileNames.length > 0) {return true;}
        else {return false;}
    }

    /**
     * Processing of the file.
     * @param fileName Name of the file passed.
     */
    public static List<MsaSequence> processFile(boolean returnMSA, String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("InputFiles/" + fileName))) {
            //Index of current molecule in msa list
            int index = 0;
            String line;
            String help;
            String[] ar;
            line = reader.readLine();
            help = reader.readLine();

            //If MSA starts from the first line, otherwise reads two unimportant lines
            if (line.length() == help.length()) {
                ar = line.split(" +");
                msa.add(new MsaSequence(ar[0]));
                msa.get(0).appendSequence(ar[1]);

                ar = help.split(" +");
                msa.add(new MsaSequence(ar[0]));
                msa.get(1).appendSequence(ar[1]);
                index = 2;
            }

            //Reading every line in file
            while ((line = reader.readLine()) != null) {
                //Only sequence or special character lines
                if (line.length() > 0) {

                    //If line with special symbols is read
                    if (line.contains("*") || line.contains(":") || line.contains(".")) {continue;}

                    //MSA line
                    ar = line.split(" +");
                    //MSA line should be split only into two parts
                    if (ar.length == 2) {
                        if (msa.size() == index) {
                            msa.add(new MsaSequence(ar[0]));
                        }
                        msa.get(index).appendSequence(ar[1]);
                        index++;
                    }
                } else {
                    index = 0;
                }
            }
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("Some problem with the file occurred.");
        }

        if (returnMSA) {return msa;}
        else {return null;}
    }

    /**
     * Used to read and decide the function ID.
     * @return ID of the function (values 0 - 3). Invalid inputs (bigger function ID or missing identifier) are handled in the 'runFunction' method.
     */
    private static int decideFunction() {
        System.out.println();
        System.out.println("Please write the ID of the function which you want to run.");
        System.out.println("Available functions are:");
        System.out.println("0 - Stop this task.");
        System.out.println("1 - Retrieve sequence.");
        System.out.println("2 - Retrieve column from MSA.");
        System.out.println("3 - Retrieve sum of pairs scores.");

        //Parsing function ID
        int functionID = 0;
        try {
            functionID = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid ID was passed.");
            return 4;
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
                retrieveSequence();
                break;
            case 2:
                retrievePosition();
                break;
            case 3:
                getSumOfSquares();
                break;
            case 4:
                break;
            default:
                System.out.println();
                System.out.println("Invalid function ID");
                break;
        }
        return true;
    }

    /**
     * Displays sequence chosen by user.
     */
    private static void retrieveSequence() {
        //Print names of sequences for the user
        System.out.println();
        System.out.println("These sequence are loaded:");
        for (MsaSequence seq : msa) {
            System.out.println(seq.getName());
        }
        System.out.println("Which one of them do you want to retrieve? Write ID of the sequence or its order (1 for the first one, 2 for the second, etc.).");

        //Reading identifier of the sequence
        String seqIdentifier = Controller.consoleInputReader.nextLine();

        //Parsing in case of order of the sequence. ID has letter as first character always.
        int seqOrder;
        try {
            seqOrder = Integer.parseInt(seqIdentifier);
        }
        catch (NumberFormatException e) {
            seqOrder = -1;
        }

        //True if sequence ID was used, false if order.
        if (seqOrder == -1) {
            for (MsaSequence seq:msa) {
                //Recognition of the sequence
                if (seq.getName().contains(seqIdentifier)) {
                    String sequence = seq.getWholeSequence();
                    //Nice print
                    for (int i = 0; i < (sequence.length() / 70); i++) {
                        System.out.println(sequence.substring(i*70, (i+1)*70));
                    }
                    System.out.println(sequence.substring((sequence.length() / 70) * 70));
                    //Sequences can share modifier
                    System.out.println();
                }
            }
        } else {
            String sequence = msa.get(seqOrder - 1).getWholeSequence();
            //Nice print
            for (int i = 0; i < (sequence.length() / 70); i++) {
                System.out.println(sequence.substring(i*70, (i+1)*70));
            }
            System.out.println(sequence.substring((sequence.length() / 70) * 70));
        }
    }

    /**
     * Retrieves certain position of the MSA in all sequences.
     */
    private static void retrievePosition() {
        System.out.println();
        System.out.println("Aligned sequences have length of " + msa.get(0).getWholeSequence().length() + ". Which position do you want to see?");

        //Parsing position from user
        int position = -1;
        try {
            position = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid position number.");
        }

        //Printing position
        if (msa.get(0).getWholeSequence().length() >= position && position > 0 ) {
            for (MsaSequence seq : msa) {
                System.out.println(seq.getName() + " " + seq.getSequencePosition(position));
            }
        }
    }

    /**
     * Overhead for getting sum of squares. Loads scoring matrix.
     */
    private static void getSumOfSquares() {
        //Getting scoring matrix from user.
        System.out.println();
        System.out.println("Which scoring matrix do you want to use? Please have file containing it (.txt format) in the InputFiles/ScoringMatrices folder. [ e.g. PAM250]");

        String sMatrixName = Controller.consoleInputReader.nextLine();

        char[] header = null;
        int[][] scoringMatrix = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("InputFiles/ScoringMatrices/" + sMatrixName + ".txt"))) {
            String line;
            line = reader.readLine();
            line = line.replaceAll(" ", "");
            header = line.toCharArray();
            int index = 0;
            scoringMatrix = new int[20][20];
            //Parsing from file. Always should have structure for this.
            while ((line = reader.readLine()) != null) {
                String[] ar = line.split(" +");
                for (int i = 1; i < ar.length; i++) {
                    scoringMatrix[index][i - 1] = Integer.parseInt(ar[i]);
                }
                index++;
            }
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("Some error occurred. Make sure that you have file with scoring matrix in the right directory.");
        }

        //Call method to calculate sum of pairs
        calculateSumOfPairs(header, scoringMatrix);
    }

    /**
     * Calculates sum of pairs for every position and for whole MSA.
     * @param scorMatHeader Order of macromolecules in the scoring matrix.
     * @param scorMat Scoring matrix.
     */
    private static void calculateSumOfPairs(char[] scorMatHeader, int[][] scorMat) {
        //Check for fully loaded scoring matrix, just in case
        if (scorMatHeader == null || scorMat == null) {return;}

        //Getting gap penalty from user
        System.out.println();
        System.out.println("How big should be the gap penalty? Please write value as a positive integer (e.g. 1, not -1).");
        int penalty = Integer.parseInt(Controller.consoleInputReader.nextLine());

        //Calculation of sum of pairs
        char[] storage = new char[msa.size()];
        int indexFirst = 0;
        int indexSecond = 0;
        int postionSumOfSquares = 0;
        int totalSumOfPairs = 0;

        //Loop through every letter
        for (int i = 0; i < msa.get(0).getWholeSequence().length(); i++) {

            //Reading letters on certain position
            for (int j = 0; j < msa.size(); j++) {
                storage[j] = msa.get(j).getSequencePosition(i + 1).charAt(0);
            }

            //Calculation for position
            postionSumOfSquares = 0;
            for (int j = 0; j < (storage.length - 1); j++) {
                for (int k = j + 1; k < storage.length; k++) {

                    //Calculation for a pair
                    if (storage[j] == '-') {indexFirst = -1;}
                    if (storage[k] == '-') {indexSecond = -1;}
                    //If both are gaps - nothing is add to the position sum of squares
                    if (indexFirst == -1 && indexSecond == -1) {
                        continue;
                    }
                    //If one is gap - gap penalty is added to position sum of squares
                    if (indexFirst == -1 ^ indexSecond == -1) {
                        postionSumOfSquares += penalty;
                        continue;
                    }
                    //If none is gap - find out which residues are they and look into scoring matrix
                    //Does not require if, because other options end with continue
                    for (int l = 0; l < scorMatHeader.length; l++) {
                        if (scorMatHeader[l] == storage[j]) {indexFirst = l;}
                        if (scorMatHeader[l] == storage[k]) {indexSecond = l;}
                    }
                    postionSumOfSquares += scorMat[indexFirst][indexSecond];
                }
            }

            //Print for position
            System.out.println("Score for position " + (i+1) + " is " + postionSumOfSquares);
            totalSumOfPairs += postionSumOfSquares;
        }

        //Print for whole MSA
        System.out.println("Sum od pairs score for the whole MSA is " + totalSumOfPairs);
    }
}
