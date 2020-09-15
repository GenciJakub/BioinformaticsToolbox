/**
 * Class used to store small data about the molecules in the file. Sequence is not stored internally because of memory burden.
 */
public class FastaData {
    private String description = null;
    private int sequenceLength = -1;
    private String sequence = null;

    public void setDescription(String description) { this.description = description; }
    public String getDescription() {return description;}
    public void setSequenceLength(int sequenceLength) { this.sequenceLength = sequenceLength; }
    public int getSequenceLength() {return sequenceLength;}
    public void setSequence(String sequence) { this.sequence = sequence; }
    public String getSequence() { return sequence; }
}
