import java.util.List;

public class AssemblerResult {
    public List<Integer> machineCode;
    public boolean hasError;

    //constructor
    public AssemblerResult(List<Integer> machineCode, boolean hasError) {
        this.machineCode = machineCode;
        this.hasError = hasError;
    }
}
