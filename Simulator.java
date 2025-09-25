import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Simulator {

    // Constants
    private static final int NUMMEMORY = 65536; // maximum number of words in memory
    private static final int NUMREGS = 8;       // number of machine registers
    private static final int MAXLINELENGTH = 1000; // not strictly needed in Java, kept for parity

    // State struct equivalent
    static class StateType {
        int pc;
        int[] mem = new int[NUMMEMORY];
        int[] reg = new int[NUMREGS];
        int numMemory;
    }

    public static void main(String[] args) {
        String line;
        StateType state = new StateType();

        if (args.length != 1) {
            System.out.printf("error: usage: %s <machine-code file>\n", "Simulator");
            System.exit(1);
        }

        String filePath = args[0];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // read in the entire machine-code file into memory
            while ((line = br.readLine()) != null) {
                if (line.length() > MAXLINELENGTH) {
                    // Not in the C code, but we keep MAXLINELENGTH parity (optional)
                    line = line.substring(0, MAXLINELENGTH);
                }
                String trimmed = line.trim();
                // Mimic `sscanf(line, "%d", ...)`: parse the first integer on the line
                Integer parsed = tryParseLeadingInt(trimmed);
                if (parsed == null) {
                    System.out.printf("error in reading address %d\n", state.numMemory);
                    System.exit(1);
                }
                state.mem[state.numMemory] = parsed;
                System.out.printf("memory[%d]=%d\n", state.numMemory, state.mem[state.numMemory]);
                state.numMemory++;
                if (state.numMemory >= NUMMEMORY) {
                    // Prevent overflow if file has more than NUMMEMORY lines
                    break;
                }
            }
        } catch (IOException e) {
            System.out.printf("error: can't open file %s", filePath);
            System.out.println();
            // Mimic perror("fopen") behavior by printing the exception message
            System.out.println(e.getMessage());
            System.exit(1);
        }
        // The C code returns without calling printState; we keep the same behavior.
        // If you want to print the full state, uncomment the next line:
        // printState(state);
    }

    // Helper to mimic sscanf("%d", ...) — parse leading integer from a line
    private static Integer tryParseLeadingInt(String s) {
        if (s.isEmpty()) return null;
        // Allow lines like "123   ; comment" or "123 extra tokens"
        // Extract the first token that looks like an integer (with optional +/-)
        // Stop at first whitespace.
        int i = 0;
        int n = s.length();

        // Skip leading spaces
        while (i < n && Character.isWhitespace(s.charAt(i))) i++;

        // Optional sign
        boolean neg = false;
        if (i < n && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
            neg = s.charAt(i) == '-';
            i++;
        }

        // Must have at least one digit
        int startDigits = i;
        while (i < n && Character.isDigit(s.charAt(i))) i++;

        if (startDigits == i) {
            // No digits found
            return null;
        }

        String numStr = s.substring(neg ? startDigits - 1 : startDigits, i);
        // If negative, include the sign
        if (neg) numStr = "-" + s.substring(startDigits, i);

        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // printState equivalent
    private static void printState(StateType statePtr) {
        System.out.println("\n@@@");
        System.out.println("state:");
        System.out.printf("\tpc %d\n", statePtr.pc);
        System.out.println("\tmemory:");
        for (int i = 0; i < statePtr.numMemory; i++) {
            System.out.printf("\t\tmem[ %d ] %d\n", i, statePtr.mem[i]);
        }
        System.out.println("\tregisters:");
        for (int i = 0; i < NUMREGS; i++) {
            System.out.printf("\t\treg[ %d ] %d\n", i, statePtr.reg[i]);
        }
        System.out.println("end state");
    }
}
