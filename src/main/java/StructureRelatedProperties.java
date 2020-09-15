import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.asa.AsaCalculator;
import org.biojava.nbio.structure.asa.GroupAsa;
import org.biojava.nbio.structure.io.PDBFileReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses BioJava library, version 5.3.0.
 */
public class StructureRelatedProperties {

    /**
     * Representation made by this project.
     */
    static PDBStructure pdbStructure;

    /**
     * Calculated by BioJava library.
     */
    static double[] residueASA;

    /**
     * Main method of this class. Responsible for its basic logic.
     * @param fileNames Name(s) of file(s) containing sequences which should be aligned.
     */
    public static void run(String[] fileNames) {

        //File processing
        if (fileToProcess(fileNames)) {
            processFile(fileNames[0]);
            if (residueASA == null) {return;}
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
     * Processing of the file. File is read twice. First time for BioJava processing, second time for custom representation.
     * @param fileName NAme of the file.
     */
    public static void processFile(String fileName) {
        //Visual gap
        System.out.println();

        //Processing for BioJava library
        Atom[] structureAtoms = null;
        PDBFileReader fReader = new PDBFileReader();
        try {
            Structure pdbStructure = fReader.getStructure("InputFiles/" + fileName);
            structureAtoms = StructureTools.getAllNonHAtomArray(pdbStructure, false);
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("Some problem with the file occurred.");
        }

        //Processing for my own program
        pdbStructure = PdbProcessor.loadAndGetStructure(fileName);

        //Computing accessible solvent area for functions 2 - 4 -> done only once
        AsaCalculator asaCalc = new AsaCalculator(structureAtoms, 1.4, 1000, 1);
        GroupAsa[] groupAsas = asaCalc.getGroupAsas();
        residueASA = new double[groupAsas.length];
        for (int i = 0; i < groupAsas.length; i++) {
            residueASA[i] = groupAsas[i].getRelativeAsaU();
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
        System.out.println("1 - Compute the diameter of the protein.");
        System.out.println("2 - Compute ratio of surface and buried amino acids.");
        System.out.println("3 - Output data for a histogram.");
        System.out.println("4 - Get portion of polar amino acids on the surface and in the core of the protein.");

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
                getDiameter();
                break;
            case 2:
                getSurfaceBuriedRatio();
                break;
            case 3:
                getHistogramData();
                break;
            case 4:
                getPortionOfPolarAA();
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
     * Gets diameter of the structure. Only a wrapper using code for the function 2 (getting width) in task 4 (PDB processing).
     */
    private static void getDiameter() {
        //Getting width / diameter
        float f = pdbStructure.getWidthWithoutText();

        //Gives response only if structure is loaded correctly
        if (f <= 0) {
            System.out.println();
            System.out.println("File was not loaded properly. Check the file and rerun this task.");
        } else {
            System.out.println();
            System.out.println("Diameter was calculated for the first model. If you want to use another model, please run function 2 in task 4.");
            System.out.println("Diameter of the model is " + f + " Angstroms.");
        }
    }

    /**
     * Calculates ratio of surface and buried amino acids.
     */
    private static void getSurfaceBuriedRatio() {
        float relResArea = getResidualSurfaceProportion();

        //Count surface residues
        int surfaceResidues = 0;
        for (int i = 0; i < residueASA.length; i++) {
            if (residueASA[i] >= relResArea) {surfaceResidues++;}
        }

        //Calculation and outputting result
        double ratio = ((double) surfaceResidues) / (residueASA.length - surfaceResidues);
        System.out.println();
        System.out.println("Ratio of surface to buried residues is " + ratio);
        System.out.println("Structure contains " + surfaceResidues + " surface residues and " + (residueASA.length - surfaceResidues) + " buried residues.");
    }

    /**
     * Overhead for printing data hor histogram. Counts amount and type (surface / buried) of residues in the structure.
     */
    private static void getHistogramData() {
        //Getting names of the residues
        List<String> names = pdbStructure.getResNames();
        //Portion of residue area which should be accessible for solvent
        float relResArea = getResidualSurfaceProportion();

        //Counting residues
        Map<String, Integer> surface = new HashMap<>();
        Map<String, Integer> buried = new HashMap<>();
        //Loop through every residue
        for (int i = 0; i < residueASA.length; i++) {
            //Surface residue
            if (residueASA[i] >= relResArea) {
                if (surface.containsKey(names.get(i))) {
                    int help = surface.get(names.get(i));
                    surface.replace(names.get(i), (help + 1));
                } else {
                    surface.put(names.get(i), 1);
                }
            }
            //Buried residue
            else {
                if (buried.containsKey(names.get(i))) {
                    int help = buried.get(names.get(i));
                    buried.replace(names.get(i), (help + 1));
                } else {
                    buried.put(names.get(i), 1);
                }
            }
        }
        //Method to print data into file
        outputHistogramData(surface, buried);
    }

    /**
     * Prints data for the histogram in the csv format.
     * @param surface Map with residues on the surface and their counts.
     * @param buried Map with buried residues and their counts.
     */
    private static void outputHistogramData(Map<String, Integer> surface, Map<String, Integer> buried) {
        //Deciding file name - base on date and time, so every file name is unique
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedTime = dtf.format(LocalDateTime.now());
        System.out.println();
        System.out.println("Alignments will be printed into file OutputFiles/SRP" + formattedTime + ".csv");

        //Printing the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("OutputFiles/SRP" + formattedTime + ".csv"))) {
            //Header
            writer.write("Name,SurfaceAmount,BuriedAmount");
            writer.newLine();

            //Loop through every residue type in the surface map
            for (Map.Entry<String, Integer> entry: surface.entrySet()) {
                String name = entry.getKey();
                int value = entry.getValue();

                //If buried does not contain type of residue, line should be printed differently
                if (buried.containsKey(name)) {
                    writer.write(name + "," + value + "," + buried.get(name));
                    writer.newLine();
                } else {
                    writer.write(name + "," + value + ",0");
                    writer.newLine();
                }
            }

            //Loop through every buried residue type
            for (Map.Entry<String, Integer> entry: buried.entrySet()) {
                //Print only residues that have not been printed
                if (!surface.containsKey(entry.getKey())) {
                    writer.write(entry.getKey() + ",0," + entry.getValue());
                    writer.newLine();
                }
            }
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("Some error occurred.");
        }
    }

    /**
     * Gives ratio of polar amino acids in the core and on the surface of the protein.
     */
    private static void getPortionOfPolarAA() {
        //Getting names of the residues
        List<String> names = pdbStructure.getResNames();
        //Portion of residue area which should be accessible for solvent
        float relResArea = getResidualSurfaceProportion();

        int surfaceRes = 0;
        int polarSurfaceRes = 0;
        int buriedRes = 0;
        int polarBuriedRes = 0;
        //Loop through every residue
        for (int i = 0; i < names.size(); i++) {
            //Surface
            if (residueASA[i] >= relResArea) {
                surfaceRes++;
                if (isAAPolar(names.get(i))) {polarSurfaceRes++;}
            }
            //Buried
            else {
                buriedRes++;
                if (isAAPolar(names.get(i))) {polarBuriedRes++;}
            }
        }

        //Nice print
        System.out.println();
        System.out.println("There are " + surfaceRes + " surface residues and " + polarSurfaceRes + " out of them are polar.");
        System.out.println("There are " + buriedRes + " buried residues and " + polarBuriedRes + " out of them are polar.");
    }

    /**
     * Used for listing polar amino acids.
     * @param name Name of residue.
     * @return True if residue is polar amino acid, false if not.
     */
    private static boolean isAAPolar(String name) {
        switch (name) {
            case "ASN":
            case "CYS":
            case "GLN":
            case "HIS":
            case "SER":
            case "THR":
            case "TYR":
                return true;
            default:
                return false;
        }
    }

    /**
     * Asks user for relative solvent accessible area.
     * @return relative solvent accessible area.
     */
    private static float getResidualSurfaceProportion() {
        //Nice print
        System.out.println();
        System.out.println("How big portion of the residual surface should be exposed in order to be taken as surface residue? (Swiss PDB uses 0.3 as default)");

        //Parsing the value
        float relResArea = 0;
        while (true) {
            try {
                relResArea = Float.parseFloat(Controller.consoleInputReader.nextLine());
                break;
            }
            catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Please enter valid value.");
            }
        }
        return relResArea;
    }

}
