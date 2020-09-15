import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main class of the program. Used to determine which task do you want to run and the form of input.
 */
public class Controller {

    /**
     * Used to read answers from the user in the program.
     */
    public static Scanner consoleInputReader = new Scanner(System.in);
    public static List<FastaData> fastaData = new ArrayList<>();
    public static List<MsaSequence> msaData = new ArrayList<>();

    /**
     * Main function. Only calls others.
     * @param args Command line arguments.
     */
    public static void main(String[] args) { runJob(args); }

    /**
     * Control panel for the program. Manages functions for reading the task and file names.
     * @param args Command line arguments.
     */
    private static void runJob(String[] args) {
        String path = System.getProperty("user.dir");
        System.out.println("Make sure that input and output directories are located in " + path);

        String consoleInput;
        String[] fileNames;

        while (true) {
            System.out.println();
            System.out.println("Dou you want to run a task? [ Y / n ]");
            consoleInput = consoleInputReader.nextLine();

            //Ends the program.
            if (consoleInput.contentEquals("N") || consoleInput.contentEquals("n")) {
                break;
            }

            //Gets the task number, names of the input files (if true) or asks for valid input (if false). Also calls the callTask function.
            if (consoleInput.contentEquals("Y") || consoleInput.contentEquals("y")) {
                int taskNumber = getTaskNumberFromUser();
                boolean useArgsInput = useFilesInArgsArray(args);
                if (useArgsInput) {
                    fileNames = args;
                } else {
                    fileNames = getInputFileNames();
                }
                callTask(taskNumber, fileNames);
            } else {
                System.out.println();
                System.out.println("Invalid input. Please use only one character.");
            }
        }

        consoleInputReader.close();
    }

    /**
     * Determines which task should be run.
     * @return Number of the task.
     */
    private static int getTaskNumberFromUser() {

        int taskNumber = 0;

        while (true) {

            //Text for the user.
            System.out.println();
            System.out.println("Please choose which task do you want to run by writing its number.");
            System.out.println("1 - Processing FASTA files");
            System.out.println("2 - Measuring sequence similarity using Hamming distance");
            System.out.println("3 - Sequence alignment using edit distance");
            System.out.println("4 - Processing PDB files");
            System.out.println("5 - Processing multiple sequence alignment");
            System.out.println("6 - Conservation determination from multiple aligned sequences");
            System.out.println("7 - Computing structure related properties");

            //Handles parseInt exception and asks for valid input if it is thrown.
            try{
                taskNumber = Integer.parseInt(consoleInputReader.nextLine());
            }
            catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Please write a number.");
                continue;
            }

            //Stops the while cycle if valid number is given (if true) or asks for valid number (if false).
            if (taskNumber > 0 && taskNumber <= 7) { break; }
            else {
                System.out.println();
                System.out.println("Please write a valid task number.");
            }
        }

        return taskNumber;
    }

    /**
     * Determines whether user wants to use the files from the command line arguments (args array in the "main function").
     * @param args Program arguments.
     * @return Answer of the user.
     */
    private static boolean useFilesInArgsArray(String[] args) {
        if (args.length != 0) {

            //Text for the user.
            System.out.println();
            System.out.println("You passed these files as command line arguments:");
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
            System.out.println();
            System.out.println("Do you want to use these files as an input for the program? [ Y / n ]");

            while (true) {
                //Response reading.
                String response = consoleInputReader.nextLine();

                //Response processing.
                if (response.contentEquals("N") || response.contentEquals("n")) { return false; }
                if (response.contentEquals("Y") || response.contentEquals("y")) { return true; }

                //User prompt in case of invalid input.
                System.out.println();
                System.out.println("Invalid input. Please use ony 'Y' or 'N' characters.");

            }
        } else {
            return false;
        }
    }

    /**
     * Loads file names from the standard input.
     * @return Array with names of files.
     */
    private static String[] getInputFileNames() {

        //Getting number of files.
        int fileCount = 0;

        while (true) {
            try {
                System.out.println();
                System.out.println("How many files do you want to use?");
                String line = consoleInputReader.nextLine();

                fileCount = Integer.parseInt(line);

                break;
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Please write a number");
            }
        }

        //Getting the names of the files.
        String[] fileNames = new String[fileCount];
        System.out.println();
        System.out.println("Please enter the names of the files which you want to use as input (one per line).");
        System.out.println("Note that files must be in the 'InputFiles' directory.");
        for (int i = 0; i < fileCount; i++) {
            fileNames[i] = consoleInputReader.nextLine();
        }

        System.out.println();
        System.out.println("Selected task will run now.");

        return fileNames;

    }

    /**
     * Switch used to run tasks.
     * @param taskNumber ID of the task which will be run.
     * @param fileNames Names of files which will be used.
     */
    private static void callTask(int taskNumber, String[] fileNames) {
        switch (taskNumber) {
            case 1:
                fastaData = FastaProcessor.run(fileNames);
                break;
            case 2:
                SequenceSimilarity.run(fileNames);
                break;
            case 3:
                SequenceAlignment.run(fileNames);
                break;
            case 4:
                PdbProcessor.run(fileNames);
                break;
            case 5:
                msaData = MsaProcessor.run(fileNames);
                break;
            case 6:
                ConservationDetermination.run(fileNames);
                break;
            case 7:
                StructureRelatedProperties.run(fileNames);
                break;
            default:
                System.out.println();
                System.out.println("Some error occurred."); //should never happen
                break;
        }
    }
}
