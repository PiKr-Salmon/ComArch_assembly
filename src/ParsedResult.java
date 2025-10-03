import java.util.List;

public class ParsedResult {
    public final List<ParsedLine> parsedLines;
    public final boolean hasError;

    public ParsedResult(List<ParsedLine> parsedLines, boolean hasError) {
        this.parsedLines = parsedLines;
        this.hasError = hasError;
    }
}