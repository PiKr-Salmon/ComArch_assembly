import java.util.*;

/*
//AssemblerParser v1, can categorize instruction format (.fill not usable yet)
public class AssemblerParser {
    private static final List<String> INSTRUCTION = Arrays.asList("add","nand","lw","sw","beq","jalr","halt","noop");

    public static void parseInstruction(String line) {
        String[] parts = line.trim().split("\\s+"); //split whitespace

        if (parts.length == 0 || parts[0].startsWith("#")) {
            System.out.println("Invalid instruction");
            return;
        }

        String label = "";
        String instruction = "";
        String field0 = "";
        String field1 = "";
        String field2 = "";
        StringBuilder comment = new StringBuilder();
        int index;

        //check label
        if (INSTRUCTION.contains(parts[0])) {
            instruction = parts[0];
            index = 1;
        } else if (isValidLabel(parts[0])) {
            //valid label, check instruction
            label = parts[0];
            if (parts.length > 1 && INSTRUCTION.contains(parts[1])) {
                instruction = parts[1];
                index = 2;
            } else {
                System.out.println("Invalid instruction after label");
                return;
            }
        } else {
            //invalid label
            System.out.println("Invalid label or instruction");
            return;
        }

        String[] fields = new String[3];
        for (int i = 0; i < 3; i++) {
            if (parts.length > index) {
                fields[i] = parts[index++];
            } else {
                fields[i] = "";
            }
        }

        //check how many fields are used for each instruction
        int usedFields = switch (instruction) {
            case "add", "nand", "lw", "sw", "beq" -> 3; //R and I
            case "jalr" -> 2; //J
            case "noop", "halt" -> 0; //O
            default -> 3;
        };

        if (usedFields > 0) {
            field0 = fields[0];
        }
        if (usedFields > 1) {
            field1 = fields[1];
        }
        if (usedFields > 2) {
            field2 = fields[2];
        }

        //comment
        for (int i = usedFields; i < 3; i++) {
            if (!fields[i].isEmpty()) {
                comment.append(fields[i]).append(" ");
            }
        }

        //comment
        while (parts.length > index) {
            comment.append(parts[index++]).append(" ");
        }

        //print format
        System.out.println("Label = " + label);
        System.out.println("Instruction = " + instruction);
        System.out.println("field0 = " + field0);
        System.out.println("field1 = " + field1);
        System.out.println("field2 = " + field2);
        System.out.println("Comment = " + comment.toString().trim());
    }
    private static boolean isValidLabel(String word) { //check label condition
        return word.matches("^[a-zA-Z][a-zA-Z0-9]{0,5}$");
    }
}

 */

/*
//AssemblerParser v2, usable .fill instruction
public class AssemblerParser {
    private static final List<String> INSTRUCTION = Arrays.asList("add", "nand", "lw", "sw", "beq", "jalr", "halt", "noop", ".fill");

    public static void parseInstruction(String line) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length == 0 || parts[0].startsWith("#")) {
            System.out.println("Invalid instruction");
            return;
        }

        String label = "";
        String instruction = "";
        String field0 = "";
        String field1 = "";
        String field2 = "";
        StringBuilder comment = new StringBuilder();
        int index;

        if (INSTRUCTION.contains(parts[0])) {
            instruction = parts[0];
            index = 1;
        } else if (isValidLabel(parts[0])) {
            label = parts[0];
            if (parts.length > 1 && INSTRUCTION.contains(parts[1])) {
                instruction = parts[1];
                index = 2;
            } else {
                System.out.println("Invalid instruction after label");
                return;
            }
        } else {
            System.out.println("Invalid label or instruction");
            return;
        }

        //.fill uses only 1 field
        if (instruction.equals(".fill")) {
            //check if there is a label before .fill
            if (label.isEmpty()) {
                System.out.println("Invalid label before .fill");
                return;
            }

            if (parts.length > index) {
                field0 = parts[index++];
            } else {
                System.out.println("Invalid value or symbolic address after .fill");
                return;
            }

            while (parts.length > index) {
                comment.append(parts[index++]).append(" ");
            }

            //print .fill format
            System.out.println("Label = " + label);
            System.out.println("Instruction = " + instruction);
            System.out.println("field0 = " + field0);
            System.out.println("field1 = ");
            System.out.println("field2 = ");
            System.out.println("Comment = " + comment.toString().trim());
            System.out.println("(Fill: " + label + " will contain the address or value of '" + field0 + "')");
            return;
        }

        //if not instruction .fill
        String[] fields = new String[3];
        for (int i = 0; i < 3; i++) {
            if (parts.length > index) {
                fields[i] = parts[index++];
            } else {
                fields[i] = "";
            }
        }

        int usedFields = switch (instruction) {
            case "add", "nand", "lw", "sw", "beq" -> 3;
            case "jalr" -> 2;
            case "noop", "halt" -> 0;
            default -> 3;
        };

        if (usedFields > 0) {
            field0 = fields[0];
        }
        if (usedFields > 1) {
            field1 = fields[1];
        }
        if (usedFields > 2) {
            field2 = fields[2];
        }

        for (int i = usedFields; i < 3; i++) {
            if (!fields[i].isEmpty()) {
                comment.append(fields[i]).append(" ");
            }
        }

        while (parts.length > index) {
            comment.append(parts[index++]).append(" ");
        }

        //print non .fill format
        System.out.println("Label = " + label);
        System.out.println("Instruction = " + instruction);
        System.out.println("field0 = " + field0);
        System.out.println("field1 = " + field1);
        System.out.println("field2 = " + field2);
        System.out.println("Comment = " + comment.toString().trim());
    }

    private static boolean isValidLabel(String word) {
        return word.matches("^[a-zA-Z][a-zA-Z0-9]{0,5}$");
    }
}

 */





/*
//AssemblerParser v3, can detect undefined label, duplicated label, symbolic label longer than 6, separate label/instruction error correctly
public class AssemblerParser {
    private static final List<String> INSTRUCTION = Arrays.asList("add", "nand", "lw", "sw", "beq", "jalr", "halt", "noop", ".fill");
    private static final Set<String> definedLabel = new HashSet<>();
    private static final Set<String> usedLabel = new HashSet<>(); //collect labels
    private static final Set<String> duplicatedLabel = new HashSet<>();
    private static final List<ParsedLine> parsedLines = new ArrayList<>();


    public static ParsedResult parseAllInstructions(List<String> line) {
        boolean errorOccurred = false;
        parsedLines.clear(); // clear previous state if re-used
        definedLabel.clear();
        usedLabel.clear();
        duplicatedLabel.clear();

        //parse each instruction
        for (String instruction : line) {
            parseInstruction(instruction);
        }

        //check undefined label
        for (String label : usedLabel) {
            if (!definedLabel.contains(label) && !isNumber(label)) {
                System.out.println("Undefined address of '" + label + "'");
                errorOccurred = true;
            }
        }

        //check duplicated label
        for (String label : duplicatedLabel) {
            System.out.println("Duplicated label '" + label + "'");
            errorOccurred = true;
        }

        return new ParsedResult(parsedLines, errorOccurred || hasUndefinedLabels());
    }

    public static void parseInstruction(String line) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length == 0 || parts[0].startsWith("#")) {
            System.out.println("Invalid instruction");
            return;
        }

        String label = "";
        String instruction = "";
        String field0 = "";
        String field1 = "";
        String field2 = "";
        StringBuilder comment = new StringBuilder();
        int index;

        if (INSTRUCTION.contains(parts[0])) {
            instruction = parts[0];
            index = 1;
        } else if (isValidLabel(parts[0])) {
            label = parts[0];

            if (definedLabel.contains(label)) {
                duplicatedLabel.add(label);
            } else {
                definedLabel.add(label); //add defined label to set
            }

            if (parts.length > 1 && INSTRUCTION.contains(parts[1])) {
                instruction = parts[1];
                index = 2;
            } else {
                System.out.println("Invalid instruction '" + parts[1] + "'");
                return;
            }
        } else {
            System.out.println("Invalid label '" + parts[0] + "'");
            return;
        }

        if (instruction.equals(".fill")) {
            if (label.isEmpty()) {
                System.out.println("Invalid label '" + parts[0] + "'");
                return;
            }

            if (parts.length > index) {
                field0 = parts[index++];
                if (!isNumber(field0)) {
                    usedLabel.add(field0); //use symbolic address
                }
            } else {
                System.out.println("Invalid value or symbolic address after .fill");
                return;
            }

            while (parts.length > index) {
                comment.append(parts[index++]).append(" ");
            }

            //print .fill format
            System.out.println("Label = " + label);
            System.out.println("Instruction = " + instruction);
            System.out.println("field0 = " + field0);
            System.out.println("field1 = ");
            System.out.println("field2 = ");
            System.out.println("Comment = " + comment.toString().trim());
            System.out.println("(Fill: " + label + " will contain the address or value of '" + field0 + "')");

            parsedLines.add(new ParsedLine(label, instruction, field0, "", "")); //to assembler
            return;
        }

        //if not instruction .fill
        String[] fields = new String[3];
        for (int i = 0; i < 3; i++) {
            if (parts.length > index) {
                fields[i] = parts[index++];
            } else {
                fields[i] = "";
            }
        }

        int usedFields = switch (instruction) {
            case "add", "nand", "lw", "sw", "beq" -> 3;
            case "jalr" -> 2;
            case "noop", "halt" -> 0;
            default -> 3;
        };

        if (usedFields > 0) {
            field0 = fields[0];
        }
        if (usedFields > 1) {
            field1 = fields[1];
        }
        if (usedFields > 2) {
            field2 = fields[2];
        }

        //add usedLabel if symbolic labels are used in field and are not longer than 6
        if (!field0.isEmpty() && !isNumber(field0)) {
            if (isValidLabel(field0)) {
                usedLabel.add(field0);
            } else {
                System.out.println("Invalid symbolic label used in field0: '" + field0 + "'");
                return;
            }
        }

        if (!field1.isEmpty() && !isNumber(field1)) {
            if (isValidLabel(field1)) {
                usedLabel.add(field1);
            } else {
                System.out.println("Invalid symbolic label used in field1: '" + field1 + "'");
                return;
            }
        }

        if (!field2.isEmpty() && !isNumber(field2)) {
            if (isValidLabel(field2)) {
                usedLabel.add(field2);
            } else {
                System.out.println("Invalid symbolic label used in field2: '" + field2 + "'");
                return;
            }
        }

        for (int i = usedFields; i < 3; i++) {
            if (!fields[i].isEmpty()) {
                comment.append(fields[i]).append(" ");
            }
        }

        while (parts.length > index) {
            comment.append(parts[index++]).append(" ");
        }

        //print non .fill format
        System.out.println("Label = " + label);
        System.out.println("Instruction = " + instruction);
        System.out.println("field0 = " + field0);
        System.out.println("field1 = " + field1);
        System.out.println("field2 = " + field2);
        System.out.println("Comment = " + comment.toString().trim());

        parsedLines.add(new ParsedLine(label, instruction, field0, field1, field2)); //to assembler
    }

    private static boolean isValidLabel(String word) {
        return word.matches("^[a-zA-Z][a-zA-Z0-9]{0,5}$");
    }

    private static boolean isNumber(String word) {
        try {
            Integer.parseInt(word);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<ParsedLine> getParsedLines() {
        return parsedLines;
    }

    private static boolean hasUndefinedLabels() {
        for (String label : usedLabel) {
            if (!definedLabel.contains(label) && !isNumber(label)) {
                System.out.println("Undefined address of '" + label + "'");
                return true;
            }
        }
        return false;
    }
}
 */

//AssemblerParser v4, can stop the entire process when error is occurred, can handle with whitespace in text file,
//$0 can't be changed, and R-Type instruction can't use symbolic label in fields
public class AssemblerParser {
    private static final List<String> INSTRUCTION = Arrays.asList("add", "nand", "lw", "sw", "beq", "jalr", "halt", "noop", ".fill");
    private static final Set<String> definedLabel = new HashSet<>();
    private static final Set<String> usedLabel = new HashSet<>();
    private static final Set<String> duplicatedLabel = new HashSet<>();
    private static final List<ParsedLine> parsedLines = new ArrayList<>();

    public static ParsedResult parseAllInstructions(List<String> lines) {
        parsedLines.clear();
        definedLabel.clear();
        usedLabel.clear();
        duplicatedLabel.clear();

        boolean errorOccurred = false;

        for (String instruction : lines) {
            if (!parseInstruction(instruction)) {
                errorOccurred = true;
            }
        }

        for (String label : usedLabel) {
            if (!definedLabel.contains(label) && !isNumber(label)) {
                System.out.println("Undefined address of '" + label + "'");
                errorOccurred = true;
            }
        }

        for (String label : duplicatedLabel) {
            System.out.println("Duplicated label '" + label + "'");
            errorOccurred = true;
        }
        return new ParsedResult(parsedLines, errorOccurred);
    }

    public static boolean parseInstruction(String line) {

        String checkSpace = line.trim();
        if (checkSpace.isEmpty() || checkSpace.startsWith("#")) { //check whitespace in input file
            return true;
        }

        String[] parts = checkSpace.trim().split("\\s+");
        if (parts.length == 0 || parts[0].startsWith("#") || parts[0].isEmpty()) {
            System.out.println("Invalid instruction");
            return false;
        }

        String label = "";
        String instruction = "";
        String field0 = "";
        String field1 = "";
        String field2 = "";
        StringBuilder comment = new StringBuilder();
        int index;

        if (INSTRUCTION.contains(parts[0])) {
            instruction = parts[0];
            index = 1;
        } else if (isValidLabel(parts[0])) {
            label = parts[0];

            if (label.length() > 6) {
                System.out.println("Invalid label '" + label + "'");
                return false;
            }

            if (definedLabel.contains(label)) {
                duplicatedLabel.add(label);
            } else {
                definedLabel.add(label);
            }

            if (parts.length > 1 && INSTRUCTION.contains(parts[1])) {
                instruction = parts[1];
                index = 2;
            } else {
                System.out.println("Invalid instruction '" + (parts.length > 1 ? parts[1] : "") + "'");
                return false;
            }
        } else {
            System.out.println("Invalid label '" + parts[0] + "'");
            return false;
        }

        if (instruction.equals(".fill")) {
            if (label.isEmpty()) {
                System.out.println("Invalid label '" + (parts.length > 0 ? parts[0] : "") + "'");
                return false;
            }

            if (parts.length > index) {
                field0 = parts[index++];
                if (!isNumber(field0)) {
                    usedLabel.add(field0);
                }
            } else {
                System.out.println("Invalid value or symbolic address after .fill");
                return false;
            }

            while (parts.length > index) {
                comment.append(parts[index++]).append(" ");
            }

            //print .fill parsing format
            System.out.println("Label = " + label);
            System.out.println("Instruction = " + instruction);
            System.out.println("field0 = " + field0);
            System.out.println("field1 = ");
            System.out.println("field2 = ");
            System.out.println("Comment = " + comment.toString().trim());
            System.out.println("(Fill: " + label + " will contain the address or value of '" + field0 + "')");

            parsedLines.add(new ParsedLine(label, instruction, field0, "", ""));
            return true;
        }

        //if not .fill
        String[] fields = new String[3];
        for (int i = 0; i < 3; i++) {
            if (parts.length > index) {
                fields[i] = parts[index++];
            } else {
                fields[i] = "";
            }
        }

        int usedFields = switch (instruction) {
            case "add", "nand", "lw", "sw", "beq" -> 3;
            case "jalr" -> 2;
            case "noop", "halt" -> 0;
            default -> 3;
        };

        if (usedFields > 0) field0 = fields[0];
        if (usedFields > 1) field1 = fields[1];
        if (usedFields > 2) field2 = fields[2];

        if (instruction.equals("add") || instruction.equals("nand")) {
            if (!isNumber(field0) || !isNumber(field1) || !isNumber(field2)) { //check if fields 0-2 are numbers (no symbolic labels)
                System.out.println("add/nand instruction fields must be register numbers, not symbolic labels");
                return false;
            }
            if("0".equals(field2)) { //check if field2 is not zero
                System.out.println("Destination register can't be $0 with instruction '" + instruction + "'");
                return false;
            }
        }

        //check symbolic labels in fields and add to usedLabel
        if (!field0.isEmpty() && !isNumber(field0)) {
            if (isValidLabel(field0)) {
                usedLabel.add(field0);
            } else {
                System.out.println("Invalid symbolic label used in field0: '" + field0 + "'");
                return false;
            }
        }

        if (!field1.isEmpty() && !isNumber(field1)) {
            if (isValidLabel(field1)) {
                usedLabel.add(field1);
            } else {
                System.out.println("Invalid symbolic label used in field1: '" + field1 + "'");
                return false;
            }
        }

        if (!field2.isEmpty() && !isNumber(field2)) {
            if (isValidLabel(field2)) {
                usedLabel.add(field2);
            } else {
                System.out.println("Invalid symbolic label used in field2: '" + field2 + "'");
                return false;
            }
        }

        //comments
        for (int i = usedFields; i < 3; i++) {
            if (!fields[i].isEmpty()) {
                comment.append(fields[i]).append(" ");
            }
        }

        while (parts.length > index) {
            comment.append(parts[index++]).append(" ");
        }

        //print non .fill format
        System.out.println("Label = " + label);
        System.out.println("Instruction = " + instruction);
        System.out.println("field0 = " + field0);
        System.out.println("field1 = " + field1);
        System.out.println("field2 = " + field2);
        System.out.println("Comment = " + comment.toString().trim());

        parsedLines.add(new ParsedLine(label, instruction, field0, field1, field2));
        return true;
    }

    private static boolean isValidLabel(String word) {
        return word.matches("^[a-zA-Z][a-zA-Z0-9]{0,5}$");
    }

    private static boolean isNumber(String word) {
        try {
            Integer.parseInt(word);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<ParsedLine> getParsedLines() {
        return parsedLines;
    }
}
