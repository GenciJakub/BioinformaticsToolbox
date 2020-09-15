import java.util.ArrayList;
import java.util.List;

/**
 * Main class for representing the hierarchy of the molecular structure. Contains 4 inner classes.
 */
public class PDBStructure {

    /**
     * List of models. One structure may contain more models.
     */
    List<Model> models = new ArrayList<>();

    /**
     * Method used to decide action based on type of record.
     * @param fileLine Line from to file. Will be parsed in called methods.
     * @param recordType Shortcut for parsing substring (record type) from the file line.
     */
    public void process(String fileLine, int recordType) {
        //If only one model is in the file - MODEL record does not have to be there
        if (models.size() == 0) { models.add(new Model()); }

        //Passing the loading logic
        switch (recordType) {
            //ATOM
            case 1:
                models.get(models.size() - 1).addAtom(false, fileLine);
                break;
            //HETATM
            case 2:
                models.get(models.size() - 1).addAtom(true, fileLine);
                break;
            //MODEL
            case 3:
                if (models.get(models.size() - 1).isComplete()) { models.add(new Model()); }
                break;
            //ENDMDL
            case 4:
                models.get(models.size() - 1).completed();
                break;
            //Should not happen
            default:
                break;
        }
    }

    /**
     * Method used for printing information about the macromolecule. Calls method on the model level.
     */
    public void printInfo() {
        System.out.println();
        System.out.println("This structure contains " + models.size() + " models.");
        if (models.size() > 1) {
            System.out.println("All models should contain the same atom records, so all of the information will be obtained from the first model.");
        }
        models.get(0).printInfo();
    }

    /**
     * Method used for calling method on the model level. First level of calculating width of the macromolecule.
     */
    public void getWidth() {
        if (models.size() > 0) {
            //Action based on amount of models. If structure contains more models, width is calculated for each one.
            if (models.size() == 1) {
                System.out.println();
                System.out.println("Width of the loaded structure is " + models.get(0).getWidth() + " Angstroms.");
            }
            else {
                System.out.println();
                System.out.println("This structure contains more than one model. Calculation may take some time.");
                //Loop through each model
                for (int i = 0; i < models.size(); i++) {
                    System.out.println("Width of the structure in the model " + (i+1) + " is " + models.get(i).getWidth() + " Angstroms.");
                }
            }
        } else {
            System.out.println();
            System.out.println("No model has been loaded.");
        }
    }

    /**
     * Used for getting the diameter of the protein in task 7.
     * @return Diameter of the protein.
     */
    public float getWidthWithoutText() {
        if (models.size() > 0) {
            return models.get(0).getWidth();
        } else {return 0;}
    }

    /**
     * Method used for calling method on the model level. First level of finding atoms / residues in a given distance from hetatm residue.
     * @param atoms True if atoms should be listed, false if residues.
     */
    public void hetatmDistance(boolean atoms) {
        if (models.size() > 1) {
            System.out.println();
            System.out.println("Structure contains " + models.size() + " models. Which one do you want to use? [ 1 for the first one, 2 for second one, etc.]");

            //Detecting ID (order) of model in which method should work.
            int modelNumber = Integer.parseInt(Controller.consoleInputReader.nextLine());
            models.get(modelNumber - 1).hetatmDistance(atoms);
        }
        else {
            models.get(0).hetatmDistance(atoms);
        }
    }

    /**
     * Gets names of residues in the structure. Used mainly in task 7.
     * @return ArrayList of names.
     */
    public List<String> getResNames() {
        return models.get(0).getResNames();
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * First inner class. Represents the first stage of pdb hierarchy.
     */
    private class Model {
        /**
         * Boolean which is indicator for fully loaded model.
         */
        private boolean complete = false;

        /**
         * List containing chains in this model.
         */
        List<Chain> chains = new ArrayList<>();

        public void completed() {complete = true;}
        public boolean isComplete() {return complete;}

        /**
         * Method used for loading the structure. Calls method on the chain level.
         * @param isHetAtm True if HETATM record, false if ATOM.
         * @param line Line from file.
         */
        public void addAtom(boolean isHetAtm, String line) {

            //For the first record
            if (chains.size() == 0) {chains.add(new Chain(line.charAt(21)));}

            //If new chain is in the model
            Chain currChain = null;
            if (chains.get(chains.size() - 1).getID() != line.charAt(21)) {
                for (Chain chain : chains) {
                    if (chain.getID() == line.charAt(21)) {
                        currChain = chain;
                        break;
                    }
                }
                if (currChain == null) {
                    chains.add(new Chain(line.charAt(21)));
                }
            }

            //Add atom to the chain
            if (currChain == null) {
                chains.get(chains.size() - 1).addAtom(isHetAtm,line);
            } else {
                currChain.addAtom(isHetAtm, line);
            }
        }

        /**
         * Method used for printing information about the macromolecule. Calls method on the chain level.
         */
        private void printInfo() {
            System.out.println("Models contain " + chains.size()  + " chains.");
            System.out.println("------------------------------------------------------------");
            for (Chain chain : chains) {
                chain.printInfo();
            }
        }

        /**
         * Loops through every atom and sets it as the current to calculate distance from other atoms.
         * @return Width of the molecule in this model.
         */
        private float getWidth() {
            int chainSeqNr;
            int residueOrder;
            int atomOrder;
            Atom currAtom;
            float width = 0;
            float toCompare = 0;

            //Chain level
            for (int i = 0; i < chains.size(); i++) {
                chainSeqNr = i;
                //Residue level
                for (int j = 0; j < chains.get(i).atomResidues.size(); j++) {
                    residueOrder = j;
                    //Atom level
                    for (int k = 0; k < chains.get(i).atomResidues.get(j).atoms.size(); k++) {
                        atomOrder = k;

                        //Setting the starting atom
                        currAtom = chains.get(i).atomResidues.get(j).atoms.get(k);

                        //Getting the biggest distance from atom in currAtom variable
                        toCompare = calculateWidth(chainSeqNr, residueOrder, atomOrder, currAtom);

                        //Comparison to get the largest distance
                        if (toCompare > width) {width = toCompare;}
                    }
                }
            }

            //Sqrt is done only once
            return (float) Math.sqrt(width);
        }

        /**
         * Loops through every chain in the model and calculates the distance between current atom and current chain.
         * @param chainSeqNr Chain sequence number of current atom.
         * @param residueOrder Residue order in chain representation.
         * @param atomOrder Atom order in its residue.
         * @param currAtom Current atom.
         * @return Squared width.
         */
        private float calculateWidth(int chainSeqNr, int residueOrder, int atomOrder, Atom currAtom) {
            float maxSquaredWidth = 0;
            float toCompare = 0;

            //Loop through every chain
            for (int i = chainSeqNr; i < chains.size(); i++) {
                if (i == chainSeqNr) {
                    toCompare = chains.get(i).getWidth(true, residueOrder, atomOrder, currAtom);
                } else {
                    toCompare =  chains.get(i).getWidth(false, residueOrder, atomOrder, currAtom);
                }

                //Comparison
                if (toCompare > maxSquaredWidth) {maxSquaredWidth = toCompare;}
            }
            return maxSquaredWidth;
        }

        /**
         * Second level logic for computing distance from hetatm residue.
         * @param atoms True if atoms should be listed, false if residues.
         */
        private void hetatmDistance(boolean atoms) {
            //Interaction with user - choosing the ligand (hetatm residue)
            System.out.println();
            System.out.println("This model contains next hetatm records (showing only residue level):");
            for (int i = 0; i < chains.size(); i++) {
                chains.get(i).printHetAtm();
            }
            System.out.println("Which of them do you want to use? Write chain identifier and the residue sequence number. [ e.g. \"A 23\"] ");
            String line = Controller.consoleInputReader.nextLine();
            char chainID = line.charAt(0);

            //Getting reference to used ligand (hetatm residue)
            Residue hetatm = null;
            for (int i = 0; i < chains.size(); i++) {
                if (chains.get(i).getID() == chainID) {
                    hetatm = chains.get(i).getHetAtm(line.substring(2));
                    break;
                }
            }

            //Different message based on printing atoms / residues.
            if (atoms) {
                System.out.println("How far should atoms be from your hetatm record (in Angstroms)?");
            } else {
                System.out.println("How far should residues be from your hetatm record (in Angstroms)?");
            }

            //Interaction with user - getting desired distance
            float distanceThreshold = Float.parseFloat(Controller.consoleInputReader.nextLine());
            System.out.println();
            System.out.println("List of atoms / residues in distance smaller or equal to chosen hetatm residue:");

            //Call to next level of hierarchy
            for (int i = 0; i < chains.size(); i++) {
                chains.get(i).printCloseToHetAtm(atoms, hetatm, distanceThreshold*distanceThreshold);
            }

            //Message to visually end the list
            System.out.println("End of list.");

        }

        /**
         * Gets names of residues in this model.
         * @return ArrayList of residue names.
         */
        private List<String> getResNames() {
            //Getting residues in chains
            List<String> resNames = new ArrayList<>();
            for (int i = 0; i < chains.size(); i++) {
                resNames.addAll(chains.get(i).getResNames());
            }

            return resNames;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Second inner class. Represents the second stage of pdb hierarchy.
     */
    private class Chain {

        /**
         * ID which is used in ATOM / HETATM records.
         */
        private char id;

        /**
         * List of ATOM residues.
         */
        List<Residue> atomResidues = new ArrayList<>();

        /**
         * List of HETATM residues.
         */
        List<Residue> hetAtmResidues = new ArrayList<>();

        /**
         * Constructor with ID parameter.
         * @param id ID of the chain.
         */
        public Chain(char id) {this.id = id;}

        public char getID() {return id;}

        /**
         * Method used as switch to add new atom into the structure (loaded from file).
         * @param isHetAtm True if HETATM record, false if ATOM record.
         * @param line Line from file.
         */
        public void addAtom(boolean isHetAtm, String line) {
            if (isHetAtm) {addHetAtm(line);}
            else {addNormalAtom(line);}
        }

        /**
         * Method used to store residue from the ATOM record.
         * @param line Line from file.
         */
        private void addNormalAtom(String line) {
            if (atomResidues.size() == 0) {
                atomResidues.add(new Residue(line.substring(22,26), line.substring(17,20)));
            }

            if (atomResidues.get(atomResidues.size() - 1).getSequenceNumber().contentEquals(line.substring(22,26)) == false) {
                atomResidues.add(new Residue(line.substring(22,26), line.substring(17,20)));
            }
            atomResidues.get(atomResidues.size() - 1).createAtom(line);
        }

        /**
         * Method used to store residue from the HETATM record.
         * @param line Line from file.
         */
        private void addHetAtm(String line) {
            if (hetAtmResidues.size() == 0) {
                hetAtmResidues.add(new Residue(line.substring(22,26), line.substring(17,20)));
            }

            if (hetAtmResidues.get(hetAtmResidues.size() - 1).getSequenceNumber().contentEquals(line.substring(22,26)) == false) {
                hetAtmResidues.add(new Residue(line.substring(22,26), line.substring(17,20)));
            }
            hetAtmResidues.get(hetAtmResidues.size() - 1).createAtom(line);
        }

        /**
         * Method used to print information about this chain. Calls method on the residue level.
         */
        private void printInfo() {
            System.out.println("This chain ID is " + id + ". It contains " + atomResidues.size() +
                    " residues in the main structure and " + hetAtmResidues.size() + " other residues.");
            System.out.println("Atom records:");
            for (Residue res : atomResidues) {
                res.printInfo();
            }
            if (hetAtmResidues.size() > 0) {
                System.out.println("Hetatm records:");
                for (Residue res : hetAtmResidues) {
                    res.printInfo();
                }
            }
            System.out.println("------------------------------------------------------------");
        }

        /**
         * Method for calculating width of the structure on the level of one chain.
         * @param chainContainsAtom True if this chain contains current atom (atom parameter).
         * @param residueOrder Residue order in chain representation.
         * @param atomOrder Atom order in its residue.
         * @param atom Current atom.
         * @return Squared maximum distance in this chain to current atom.
         */
        private float getWidth(boolean chainContainsAtom, int residueOrder, int atomOrder, Atom atom) {
            float maxSquaredWidth = 0;
            float toCompare = 0;

            if (chainContainsAtom) {
                //Loop through each residue (beginning on the one containing current atom)
                for (int i = residueOrder; i < atomResidues.size(); i++) {
                    if (i == residueOrder) {
                        toCompare = atomResidues.get(i).getWidth(true, atomOrder, atom);
                    } else {
                        toCompare = atomResidues.get(i).getWidth(false, atomOrder, atom);
                    }
                    if (toCompare > maxSquaredWidth) {maxSquaredWidth = toCompare;}
                }
            } else {
                for (int i = 0; i < atomResidues.size(); i++) {
                    toCompare = atomResidues.get(i).getWidth(false, atomOrder, atom);
                }
                if (toCompare > maxSquaredWidth) {maxSquaredWidth = toCompare;}
            }

            return maxSquaredWidth;
        }

        /**
         * Used for printing list of hetatm residues in this chain. Used in interaction with user.
         */
        private void printHetAtm() {
            for (int i = 0; i < hetAtmResidues.size(); i++) {
                System.out.println("Chain " + id + ", residue " + hetAtmResidues.get(i).getName() + " (" + hetAtmResidues.get(i).getSequenceNumber() + ")");
            }
        }

        /**
         * Method for getting the hetatm residue requested by the user.
         * @param hetatmID ID of requested hetatm residue.
         * @return Residue requested by the user.
         */
        private Residue getHetAtm(String hetatmID) {
            for (int i = 0; i < hetAtmResidues.size(); i++) {
                if (hetAtmResidues.get(i).getSequenceNumber().contains(hetatmID)) {return hetAtmResidues.get(i);}
            }
            System.out.println("Hetatm record not found.");
            return null;
        }

        /**
         * Calls next level of printing atoms / residues in a given distance from a ligand.
         * @param atoms True if atoms should be printed, false if residues.
         * @param hetatm Ligand.
         * @param distance Squared distance given by the player.
         */
        private void printCloseToHetAtm (boolean atoms, Residue hetatm, float distance) {
            for (int i = 0; i < atomResidues.size(); i++) {
                atomResidues.get(i).printCloseToHetAtm(atoms, hetatm, distance, id);
            }
        }

        /**
         * Gets names of residues in this chain.
         * @return ArrayList of residue names.
         */
        private List<String> getResNames() {
            List<String> resNames = new ArrayList<>();
            for (int i = 0; i < atomResidues.size(); i++) {
                resNames.add(atomResidues.get(i).name);
            }
            return resNames;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Third inner class. Represents the Third stage of pdb hierarchy.
     */
    private class Residue {

        private String sequenceNumber;
        private String name;
        /**
         * List of atoms in one residue.
         */
        List<Atom> atoms = new ArrayList<>();

        /**
         * Constructor with information obtainable from pdb file.
         * @param seqNumber Sequence number of the residue.
         * @param name Name of the residue.
         */
        public Residue(String seqNumber, String name) {
            sequenceNumber = seqNumber;
            this.name = name;
        }

        public String getSequenceNumber() {return sequenceNumber;}

        public String getName() {return name;}

        /**
         * Creating atom instance and adding it to the list of atoms.
         * @param line Line from file.
         */
        public void createAtom(String line) {
            atoms.add(new Atom(line.substring(6,11), line.substring(12,16), line.substring(30,38), line.substring(38,46), line.substring(46,54)));
        }

        /**
         * Prints information about residues (sequence number, name and number of atoms)
         */
        private void printInfo() {
            System.out.println("Residue " + sequenceNumber + " is " + name + " and contains " + atoms.size() + " atoms.");
        }

        /**
         * Calls calculation of distance between two atoms.
         * @param residueContainsAtom True if this residue contains current atom, false if not.
         * @param atomOrder Order of current atom in its residue.
         * @param atom Current atom.
         * @return Squared maximum distance of current atom and atoms in this residue.
         */
        private float getWidth(boolean residueContainsAtom, int atomOrder, Atom atom) {
            float maxSquaredWidth = 0;
            float toCompare = 0;

            if (residueContainsAtom) {
                for (int i = atomOrder + 1; i < atoms.size(); i++) {
                    toCompare = atoms.get(i).calculateSquaredWidth(atom);
                }
                if (toCompare > maxSquaredWidth) {maxSquaredWidth = toCompare;}
            } else {
                for (int i = 0; i < atoms.size(); i++) {
                    toCompare = atoms.get(i).calculateSquaredWidth(atom);
                }
                if (toCompare > maxSquaredWidth) {maxSquaredWidth = toCompare;}
            }
            return maxSquaredWidth;
        }

        /**
         * Method responsible for printing atoms / residues in a given distance from a ligand.
         * @param atomsBool True if atoms should be printed, false if residues.
         * @param hetatm Hetatm chosen by user. Distance is calculated from its every atom.
         * @param distance Squared distance threshold.
         * @param chainID ID of the chain containing this residue (uset for printing the user message).
         */
        private void printCloseToHetAtm(boolean atomsBool, Residue hetatm, float distance, char chainID) {
            for (int i = 0; i < atoms.size(); i++) {
                //Loop through every atom in hetatm residue.
                for (int j = 0; j < hetatm.atoms.size(); j++) {
                    if (atoms.get(i).calculateSquaredWidth(hetatm.atoms.get(j)) <= distance) {
                        if (atomsBool) {
                            System.out.println("Chain " + chainID + ", residue " + name + " (" + sequenceNumber + "), atom " + atoms.get(i).name + " (" + atoms.get(i).serialNumber + ")");
                            //Only one message per atom
                            break;
                        } else {
                            System.out.println("Chain " + chainID + ", residue " + name + " (" + sequenceNumber + ")");
                            //Only one message per residue
                            return;
                        }
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Fourth inner class. Represents the fourth stage of pdb hierarchy.
     */
    private class Atom {
        String serialNumber;
        String name;
        float xCoor;
        float yCoor;
        float zCoor;

        /**
         * Constructor with important information contained in ATOM record in pdb file.
         * @param serialNO Serial number of the atom.
         * @param name Name of the atom.
         * @param xCoor X coordinate in Angstroms.
         * @param yCoor Y coordinate in Angstroms.
         * @param zCoor Z coordinate in Angstroms.
         */
        public Atom(String serialNO, String name, String xCoor, String yCoor, String zCoor) {
            this.serialNumber = serialNO;
            this.name = name;
            this.xCoor = Float.parseFloat(xCoor);
            this.yCoor = Float.parseFloat(yCoor);
            this.zCoor = Float.parseFloat(zCoor);
        }

        /**
         * Calculates squared distance between this atom and one passed as parameter (current atom).
         * @param atom Atom from which distance is calculated.
         * @return Squared distance of two atoms.
         */
        private float calculateSquaredWidth(Atom atom) {
            float xDistSquared = (atom.xCoor - xCoor)*(atom.xCoor - xCoor);
            float yDistSquared = (atom.yCoor - yCoor)*(atom.yCoor - yCoor);
            float zDistSquared = (atom.zCoor - zCoor)*(atom.zCoor - zCoor);
            return (xDistSquared + yDistSquared + zDistSquared);
        }

    }
}