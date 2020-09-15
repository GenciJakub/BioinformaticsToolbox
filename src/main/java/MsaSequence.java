/**
 * Represents one macromolecule within the MSA file.
 */
public class MsaSequence {
    private String name;
    private String sequence = "";

    public MsaSequence(String name) {this.name = name;}

    public void appendSequence(String nextSequencePart) { sequence = sequence + nextSequencePart;}

    public String getName() {return name;}

    public String getWholeSequence() {return sequence;}

    /**
     * Gets one-letter code of residue at certain position in the sequence.
     * @param position Position of the residue in the sequence.
     * @return One-letter code of the residue.
     */
    public String getSequencePosition(int position) {return sequence.substring(position - 1, position);}
}
