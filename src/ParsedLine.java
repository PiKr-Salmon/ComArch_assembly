public class ParsedLine {
    public String label;
    public String instruction;
    public String field0;
    public String field1;
    public String field2;

    //constructor
    public ParsedLine(String label, String instruction, String field0, String field1, String field2) {
        this.label = label;
        this.instruction = instruction;
        this.field0 = field0;
        this.field1 = field1;
        this.field2 = field2;
    }
}