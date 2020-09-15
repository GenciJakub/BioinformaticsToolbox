# Bioinformatics Toolbox
Semester project for NDBI044 course at Charles University, Prague

## Goal of the program
Program implements first 7 tasks from bioinformatics toolbox in the Charles University bioinformatics repository. Details of the assignments can be found on the address: 

http://bioinformatika.mff.cuni.cz/repository/#/stories/detail?id=bioinformatics_toolbox

It is advised that the user acquaints with the assignments before the use of this program. Only basic level of knowledge in the field of bioinformatics is required from the user.

## How to start and run the program
Program was developed in Java SE 13 (JDK 13) and only this version of JDK should be used to run it. Project uses Maven as a build tool. You can run the project from your favorite IDE for Java or from the command line. If you want to run the project from the command line, firstly download and install Maven. Instructions can be found here: https://maven.apache.org/install.html

After installing Maven, download this repository with ``` git clone https://github.com/GenciJakub/BioinformaticsToolbox ``` command. Then you can run the program with:
```
cd .../BioinformaticsToolbox
mvn compile
mvn clean install
```
Last command depends on whether you want to use command line arguments of not.
```
mvn exec:java -Dexec.mainClass=Controller
mvn exec:java -Dexec.mainClass=Controller -Dexec.args="name of file with extension"
```
After starting the program, check whether InputFiles and OutputFiles directories are located (correct location will be printed by the program).

## How to work with the program
Program requires only keyboard input and for most of the tasks files (text format) stored in the InputFiles directory. After the start, user is asked whether he / she want to run a task. Valid responses are ‘n’ for stopping the program or ‘y’ for running a task (case does not matter).

If user wants to run a task, he / she decides which file(s) should be used in this task (Note: working with more than one file is possible if assignment of the task specifies it, other files will be ignored). Priority have files passed as command line arguments to the program, but user may choose not to use them. In that case, he is asked to specify the files which contain data for the program. (Note: when writing name of file, write its full name – e. g. test.txt)

After handling files serving as input, user is asked which task he / she wants to run. After choosing the task, user will be presented with multiple functions according to the assignment. User may choose to run a function or stop execution of the task (program returns to the start) by writing the ID of the function. Running a function may require additional data and user has to provide them when asked.

## Guide to the program with examples

### Task 1 – Processing FASTA files
Program assumes that files used as input have correct structure (FASTA format). When choosing the function ID, user has to include an identifier of one of the molecules. Identifier does not have to be unique, but it must be in the description of the molecule.

For example, if description of the molecule is “>2MKG_1|Chain A|BRCA1-A complex subunit RAP80|Homo sapiens (9606)” you can use 2MKG_1, BRCA1-A, Homo sapiens or anything else present in the description as the molecule identifier. This means that “1 2MKG_1” would be valid for running a function. File FastaProc.txt contains some examples of the data in FASTA format.

### Task 2 – Measuring sequence similarity using Hamming distance
Program will use data from the first task. Only if first task has not been started yet, molecules from the file will be loaded (this is done in order to preserve loaded structure in the program memory).

If you want to know how to work with this task, look at the example in the task 1.

### Task 3 – Sequence alignment using edit distance
File for this task should contain two sequences with residues represented by their one-letter code. Valid structure of the file can be seen in AlignmentSame.txt and AlignmentDifferent.txt files.

This task does not require function ID, because it contains only one function. It reads the file provided and calculates edit distance of the sequences. If you want to see all optimal alignments, you can choose so when the program asks you. Optimal alignments will be printed in .txt file in the OutputFiles directory. Name of the file is based on time and date in order to create different file names each time.

### Task 4 – Processing PDB files
Program assumes that valid pdb file has been passed. If not, almost certainly nothing at all will be loaded and program will not work as intended. Example of valid pdb file can be seen in 1yih.pdb and pdbtest.txt files. Any file downloaded from PDB (Protein Data Bank) in pdb format will be valid. Note that calculating distance or width of the structure can run for a longer time if big amounts of atoms are in the passed file. In case that you do not know much about structure of the pdb file, look at this page: http://www.wwpdb.org/documentation/file-format. Program uses standards from version 3.30.

### Task 5 – Processing multiple sequence alignment
Program requires file with structure identical to the msaLong.txt file (blocks of sequences delimited by an empty line). File msaTest.txt contains good example for computing sum of pairs. After choosing the function you want to run, you will be asked which sequence do you want to work with (functions 1 and 2). In case that multiple sequences share the same identifier, program will output the results for every sequence with that identifier.

In case that you want to compute sum of pairs, you will be asked for a scoring matrix and a gap penalty. There are few different scoring matrices already stored in the InputFiles/ScoringMatrices directory. In case you want to add your own scoring matrix, please use the same format of the data. All scoring matrices have to be in the .txt format.

### Task 6 – Conservation determination from multiple aligned sequences
File with the same structure as in the previous task is required. Because of that, you can choose to use MSA file from the previous task or load a new one. If you want to try this task, I recommend using the msaTest.txt file.

Program tells you how many times residues in chosen sequence appear at the same position in the other sequences (function 1) or gives you positions where same residue appears more than N times (you specify N).

### Task 7 – Computing structure related properties
This task is mainly about detecting surface and buried amino acids in the protein. This is done by BioJava library and requires a file in .pdb format (file with extension .pdb). I recommend trying the task with the 1yih.pdb file. If you want to know about theoretical background of this task, search for accessible solvent area (ASA).

Note: results of work with the hemoglobin and A2a receptor are in the HemoglobinAndA2a.txt file
