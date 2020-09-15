import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SequenceAlignment {

    private static char[] firstSeq;
    private static char[] secondSeq;
    /**
     * Matrix used for computing edit distance by dynamic programing.
     */
    private static int[][] dynamicDistanceMatrix;

    /**
     * Main method of this class. Responsible for its basic logic.
     * @param fileNames Name(s) of file(s) containing sequences which should be aligned.
     */
    public static void run(String[] fileNames) {

        //Load sequences
        if (filesPassed(fileNames)) {
            manageReadingSequences(fileNames);
        } else { return; }

        //This class logic
        calculateEditDistance();
        if (userWantsAlignment()) {
            printAlignments();
        } else {
            System.out.println();
            System.out.println("Edit distance of your sequences is: " + dynamicDistanceMatrix[firstSeq.length][secondSeq.length]);
        }

    }

    /**
     * Checks if at least one file was assigned to this task.
     * @param fileNames Name(s) of file(s) in question.
     * @return True if at least 1 file was passed, False if 0.
     */
    private static boolean filesPassed(String[] fileNames) {
        if (fileNames.length == 0) {
            System.out.println();
            System.out.println("No files assigned. Please use at least one file as an input.");
            return false;
        } else {return true;}
    }

    /**
     * Logic for reading sequences from file(s). This method can serve input from one or two files.
     * @param fileNames Name(s) of file(s).
     */
    private static void manageReadingSequences(String[] fileNames) {

        //One file
        if (fileNames.length == 1) {
            try (BufferedReader reader = new BufferedReader(new FileReader("InputFiles/" + fileNames[0]))) {
                readOneFile(reader);
            }
            catch (IOException e) {
                System.out.println();
                System.out.println("Some error occurred. Probably invalid file name.");
            }
        }
        //Two files
        else {
            try (BufferedReader reader1 = new BufferedReader(new FileReader("InputFiles/" + fileNames[0]));
                 BufferedReader reader2 = new BufferedReader(new FileReader("InputFiles/" + fileNames[1]))) {
                readFile(reader1, 1);
                readFile(reader2, 2);
            }
            catch (IOException e) {
                System.out.println();
                System.out.println("Some error occurred. Probably invalid file name.");
            }
        }
    }

    /**
     * Reads first sequence form a file. Loads input into variables firstSeq and secondSeq.
     * @param reader Reader assigned to the file.
     * @param order Order of the sequence. Intended values are 1 or 2.
     * @throws IOException Opening, reading or closing the file. Handled in calling method.
     */
    private static void readFile(BufferedReader reader, int order) throws IOException {
        //Reading from file
        StringBuilder sequence = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sequence.append(line);
        }

        //Conversion to char array
        switch (order) {
            case 1:
                firstSeq = sequence.toString().toUpperCase().toCharArray();
                break;
            case 2:
                secondSeq = sequence.toString().toUpperCase().toCharArray();
                break;
            default:
                System.out.println();
                System.out.println("Invalid number of sequences.");
                break;
        }
    }

    /**
     * Reads two sequences delimited by an empty line from one file. Also loads input variables as readFile method.
     * @param reader Reader assigned to the file.
     * @throws IOException Opening, reading or closing the file. Handled in calling method.
     */
    private static void readOneFile(BufferedReader reader) throws IOException {
        //Reading from file
        StringBuilder firstSeqString = new StringBuilder();
        StringBuilder secondSeqString = new StringBuilder();
        boolean readingFirst = true;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {readingFirst = false;}
            if (readingFirst) {
                firstSeqString.append(line);
            } else {
                secondSeqString.append(line);
            }
        }
        //Conversion to char array
        firstSeq = firstSeqString.toString().toUpperCase().toCharArray();
        secondSeq = secondSeqString.toString().toUpperCase().toCharArray();
    }

    /**
     * Checks if user wants to see all optimal alignments.
     * @return True if yes, False if not.
     */
    private static boolean userWantsAlignment() {
        while (true) {
            System.out.println();
            System.out.println("Do you want all optimal alignments for these sequences? [ Y / n ]");

            String consoleInput = Controller.consoleInputReader.nextLine();
            if (consoleInput.contentEquals("Y") || consoleInput.contentEquals("y")) {
                return true;
            }
            if (consoleInput.contentEquals("N") || consoleInput.contentEquals("n")) {
                return false;
            }

            System.out.println();
            System.out.println("Invalid response.");
        }
    }

    /**
     * Calculates matrix of edit distance (for two sequences) by dynamic programming algorithm.
     */
    private static void calculateEditDistance() {
        //Initialization
        dynamicDistanceMatrix = new int[firstSeq.length + 1][secondSeq.length + 1];
        for (int i = 0; i <= secondSeq.length; i++) {
            dynamicDistanceMatrix[0][i] = i;
        }
        for (int i = 0; i <= firstSeq.length; i++) {
            dynamicDistanceMatrix[i][0] = i;
        }

        //Computation of edit distance
        int posLeft = 0;
        int posUp = 0;
        int posUpLeft = 0;
        int sameSymbol = 0;
        for (int i = 1; i <= firstSeq.length; i++) {
            for (int j = 1; j <= secondSeq.length; j++) {

                //Getting values needed to compute next value
                posLeft = dynamicDistanceMatrix[i][j-1];
                posUp = dynamicDistanceMatrix[i-1][j];
                posUpLeft = dynamicDistanceMatrix[i-1][j-1];
                if (firstSeq[i-1] == secondSeq[j-1]) {
                    sameSymbol = 0;
                } else {sameSymbol = 1;}


                //Comparison and value assignment
                if (posLeft + 1 < posUp + 1 && posLeft + 1 < posUpLeft + sameSymbol) {
                    dynamicDistanceMatrix[i][j] = posLeft + 1;
                } else {
                    if (posUp + 1 < posLeft + 1 && posUp + 1 < posUpLeft + sameSymbol) {
                        dynamicDistanceMatrix[i][j] = posUp + 1;
                    } else {
                        dynamicDistanceMatrix[i][j] = posUpLeft + sameSymbol;
                    }
                }
            }
        }
    }

    /**
     * Logic behind printing all optimal alignments. Opens and closes the file for them.
     */
    private static void printAlignments() {
        //Deciding file name - base on date and time, so every file name is unique
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedTime = dtf.format(LocalDateTime.now());
        System.out.println();
        System.out.println("Alignments will be printed into file OutputFiles/EDAlignments" + formattedTime + ".txt");

        //Printing + recursion
        StringBuilder firstSeqSB = new StringBuilder();
        StringBuilder secondSeqSB = new StringBuilder();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("OutputFiles/EDAlignments" + formattedTime + ".txt"))) {
            writer.write("Edit distance of your sequences is: " + dynamicDistanceMatrix[firstSeq.length][secondSeq.length]);
            writer.newLine();
            recursiveBacktracking(writer, firstSeqSB, secondSeqSB, firstSeq.length, secondSeq.length);
        }
        catch (IOException e) {
            System.out.println();
            System.out.println("Some problem with the file.");
        }
    }

    /**
     * Recursive backtracking through the matrix of dynamic programming.
     * @param alignedFirstSeq StringBuilder for the first sequence.
     * @param alignedSecondSeq StringBuilder for the second sequence.
     * @param rowCoor First coordinate for the matrix.
     * @param colCoor Second coordinate form the matrix.
     * @throws IOException From calling printAlignmentIntoFile method. Handled by calling method.
     */
    private static void recursiveBacktracking(BufferedWriter writer, StringBuilder alignedFirstSeq, StringBuilder alignedSecondSeq, int rowCoor, int colCoor) throws IOException {
        //Upper left corner of matrix - alignment is complete, stops recursion
        if (rowCoor == 0 && colCoor == 0) {
            printAlignmentIntoFile(writer, alignedFirstSeq, alignedSecondSeq);
            return;
        }

        //First line of matrix - only adds '-' into the first sequence
        if (rowCoor == 0) {
            alignedFirstSeq.append('-');
            alignedSecondSeq.append(secondSeq[colCoor - 1]);
            recursiveBacktracking(writer, alignedFirstSeq, alignedSecondSeq, rowCoor, colCoor - 1);
            alignedFirstSeq.deleteCharAt(alignedFirstSeq.length() - 1);
            alignedSecondSeq.deleteCharAt(alignedSecondSeq.length() - 1);
            return;
        }

        //First column of matrix - only adds '-' into the second sequence
        if (colCoor == 0) {
            alignedFirstSeq.append(firstSeq[rowCoor - 1]);
            alignedSecondSeq.append('-');
            recursiveBacktracking(writer, alignedFirstSeq, alignedSecondSeq, rowCoor - 1, colCoor);
            alignedFirstSeq.deleteCharAt(alignedFirstSeq.length() - 1);
            alignedSecondSeq.deleteCharAt(alignedSecondSeq.length() - 1);
            return;
        }

        //If rowCoor and colCoor are > 0
        //Check if path to the left is possible
        if (dynamicDistanceMatrix[rowCoor][colCoor] == dynamicDistanceMatrix[rowCoor][colCoor - 1] + 1) {
            alignedFirstSeq.append('-');
            alignedSecondSeq.append(secondSeq[colCoor - 1]);
            recursiveBacktracking(writer, alignedFirstSeq, alignedSecondSeq, rowCoor, colCoor - 1);
            alignedFirstSeq.deleteCharAt(alignedFirstSeq.length() - 1);
            alignedSecondSeq.deleteCharAt(alignedSecondSeq.length() - 1);
        }
        //Check if two letters should be aligned
        if ((dynamicDistanceMatrix[rowCoor][colCoor] == dynamicDistanceMatrix[rowCoor - 1][colCoor - 1] && firstSeq[rowCoor - 1] == secondSeq[colCoor - 1])
            || (dynamicDistanceMatrix[rowCoor][colCoor] == dynamicDistanceMatrix[rowCoor - 1][colCoor - 1] + 1 && firstSeq[rowCoor - 1] != secondSeq[colCoor - 1])) {

            alignedFirstSeq.append(firstSeq[rowCoor - 1]);
            alignedSecondSeq.append(secondSeq[colCoor - 1]);
            recursiveBacktracking(writer, alignedFirstSeq, alignedSecondSeq, rowCoor - 1, colCoor - 1);
            alignedFirstSeq.deleteCharAt(alignedFirstSeq.length() - 1);
            alignedSecondSeq.deleteCharAt(alignedSecondSeq.length() - 1);
        }
        //Check if path up is possible
        if (dynamicDistanceMatrix[rowCoor][colCoor] == dynamicDistanceMatrix[rowCoor - 1][colCoor] + 1) {
            alignedFirstSeq.append(firstSeq[rowCoor - 1]);
            alignedSecondSeq.append('-');
            recursiveBacktracking(writer, alignedFirstSeq, alignedSecondSeq, rowCoor - 1, colCoor);
            alignedFirstSeq.deleteCharAt(alignedFirstSeq.length() - 1);
            alignedSecondSeq.deleteCharAt(alignedSecondSeq.length() - 1);
        }

    }

    /**
     * Prints an optimal alignment into file.
     * @param writer BufferedWriter used for writing into the file.
     * @param alignedFirstSeq StringBuilder containing the first sequence.
     * @param alignedSecondSeq StringBuilder containing the second sequence.
     * @throws IOException Writing into the file.
     */
    private static void printAlignmentIntoFile(BufferedWriter writer, StringBuilder alignedFirstSeq, StringBuilder alignedSecondSeq) throws IOException {
        writer.write("------------------------------------------------------------");
        writer.newLine();

        //Alignment is reversed in StringBuilder
        alignedFirstSeq = alignedFirstSeq.reverse();
        alignedSecondSeq = alignedSecondSeq.reverse();

        int i = 0;
        while (true) {
            writer.newLine();
            if (alignedFirstSeq.length() / (60*(i+1)) > 0) {
                writer.write(alignedFirstSeq.substring(i*60, (i+1)*60));
                writer.newLine();
                writer.write(alignedSecondSeq.substring(i*60, (i+1)*60));
                writer.newLine();
            } else {
                writer.write(alignedFirstSeq.substring(i*60));
                writer.newLine();
                writer.write(alignedSecondSeq.substring(i*60));
                writer.newLine();
                break;
            }
            i++;
        }

        //Needed for more alignments
        alignedFirstSeq = alignedFirstSeq.reverse();
        alignedSecondSeq = alignedSecondSeq.reverse();
    }
}
