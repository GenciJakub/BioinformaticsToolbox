import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Assumes correct structure of the pdb file, according to documentation version 3.30.
 */
public class PdbProcessor {

    static PDBStructure structure = new PDBStructure();

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
     * Wrapper for loading the file. Used from task 7.
     * @param fileName Name of the pdb file.
     * @return Representation of the structure.
     */
    public static PDBStructure loadAndGetStructure(String fileName) {
        processFile(fileName);
        return structure;
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
     * Manages preprocessing of the file. Reads it and decides what to do based on type of PDB record.
     * Records taken into account are ATOM, HETATM, MODEL, ENDMDL
     * @param fileName Name of the file containing data in pdb format (does not check for it).
     */
    private static void processFile(String fileName) {
        System.out.println();
        System.out.println("Structure in file " + fileName + " will be loaded.");

        //Reading the file
        try (BufferedReader reader = new BufferedReader(new FileReader("InputFiles/" + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {

                //Cases where records have less than 7 characters (MODEL and ENDMDL).
                if (line.contentEquals("MODEL")) {
                    structure.process(line, 3);
                    continue;
                }
                if (line.contentEquals("ENDMDL")) {
                    structure.process(line, 4);
                    continue;
                }

                //If records have at least 7 characters
                switch (line.substring(0,6)) {
                    case "ATOM  ":
                        structure.process(line,1);
                        break;
                    case "HETATM":
                        structure.process(line, 2);
                        break;
                    case "MODEL ":
                        structure.process(line, 3);
                        break;
                    default:
                        break;
                }
            }

            //Manual ENDMDL if only one model was in the file
            structure.process("ENDMDL", 4);
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("An error occured. Please make sure that file is in the \"InputFiles\" directory.");
        }
    }

    /**
     * Used to read and decide the function ID.
     * @return ID of the function (values 0 - 4). Invalid inputs (bigger function ID or missing identifier) are handled in the 'runFunction' method.
     */
    private static int decideFunction() {
        System.out.println();
        System.out.println("Please write the ID of the function which you want to run.");
        System.out.println("Available functions are:");
        System.out.println("0 - Stop this task.");
        System.out.println("1 - Get information about the structure.");
        System.out.println("2 - Calculate width of the structure.");
        System.out.println("3 - Get list of atoms being in given distance from given HETATM.");
        System.out.println("4 - Get list of residues being in given distance from given HETATM.");

        //Parsing function ID
        int functionID = 0;
        try {
            functionID = Integer.parseInt(Controller.consoleInputReader.nextLine());
        }
        catch (StringIndexOutOfBoundsException e) {
            System.out.println();
            System.out.println("Invalid ID was passed.");
            return 5;
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
                getInformation();
                break;
            case 2:
                calculateWidth();
                break;
            case 3:
                atomListFromHetAtm();
                break;
            case 4:
                residueListFromHetAtm();
                break;
            case 5:
                break;
            default:
                System.out.println();
                System.out.println("Invalid function ID");
                break;
        }
        return true;
    }

    /**
     * Function 1. Presents information about the structure.
     */
    private static void getInformation() {
        structure.printInfo();
    }

    /**
     * Function 2. Start for calculating the width of the macromolecule.
     */
    private static void calculateWidth() {
        structure.getWidth();
    }

    /**
     * Function 3. Presents list of atoms in certain distance from given ligand (residue containing hetatm records).
     */
    private static void atomListFromHetAtm() {
        structure.hetatmDistance(true);
    }

    /**
     * Function 4. Presents list of residues in certain distance from given ligand (residue containing hetatm records).
     */
    private static void residueListFromHetAtm() {
        structure.hetatmDistance(false);
    }

}
