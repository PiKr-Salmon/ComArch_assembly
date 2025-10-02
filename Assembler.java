import java.util.ArrayList;
import java.util.List;

//add AssemblerResult class so the rule condition of project can meet (exit(0) and exit(1))
public class Assembler {
    public static AssemblerResult assembler(List<ParsedLine> lines) {
        boolean errorOccurred = false;
        List<Integer> output= new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            ParsedLine line = lines.get(i);
            String instruction = line.instruction;

            if (instruction.equals(".fill")) {
                int value;
                if (isNumeric(line.field0)) {
                    value = Integer.parseInt(line.field0);
                } else {
                    value = getAddressForLabel(line.field0, lines);
                    if (value == -1) {
                        errorOccurred = true;
                        continue; //no return, exit(1)
                    }
                }

                output.add(value);
                String binaryString = String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0');
                String fullLine = String.join(" ", line.label.isEmpty() ? "" : line.label, instruction, line.field0).trim();
                System.out.printf("%-25s => %32s (%d)\n", fullLine, binaryString, value);
                continue;
            }

            int opcode = switch (instruction) {
                case "add" -> 0b000; //000
                case "nand" -> 0b001; //001
                case "lw" -> 0b010; //010
                case "sw" -> 0b011; //011
                case "beq" -> 0b100; //100
                case "jalr" -> 0b101; //101
                case "halt" -> 0b110; //110
                case "noop" -> 0b111; //111
                default -> -1; //other opcode
            };

            if (opcode == -1) {
                System.out.println("Invalid instruction: '" + instruction + "'");
                errorOccurred = true;
                continue; //no return, exit(1)
            }

            int[] bits = new int[32]; //keep 32 bits in array
            setBits(bits, 22, 3, opcode); //31-22 for opcode

            try { //set opcode in array
                switch (instruction) {
                    case "add", "nand" -> {
                        int regA = Integer.parseInt(line.field0);
                        int regB = Integer.parseInt(line.field1);
                        int destReg = Integer.parseInt(line.field2);
                        setBits(bits, 19, 3, regA);
                        setBits(bits, 16, 3, regB);
                        setBits(bits, 0, 3, destReg);
                    }
                    case "lw", "sw" -> {
                        int regA = Integer.parseInt(line.field0);
                        int regB = Integer.parseInt(line.field1);
                        int offset;

                        if (isNumeric(line.field2)) {
                            offset = Integer.parseInt(line.field2);
                        } else {
                            offset = getAddressForLabel(line.field2, lines);
                            if (offset == -1) {
                                errorOccurred = true;
                                continue;
                            }
                        }

                        if (offset < -32768 || offset > 32767) {
                            System.out.printf("Offset out of 16-bit signed range: %s %s %s %s\n",
                            instruction, line.field0, line.field1, line.field2);
                            errorOccurred = true;
                            continue;
                        }

                        setBits(bits, 19, 3, regA);
                        setBits(bits, 16, 3, regB);
                        setBits(bits, 0, 16, offset & 0xFFFF);
                    }
                    case "beq" -> {
                        int regA = Integer.parseInt(line.field0);
                        int regB = Integer.parseInt(line.field1);
                        int offset;

                        if (isNumeric(line.field2)) {
                            offset = Integer.parseInt(line.field2);
                        } else {
                            int labelAddress = getAddressForLabel(line.field2, lines);
                            if (labelAddress == -1) {
                                errorOccurred = true;
                                continue;
                            }
                            offset = labelAddress - (i + 1);
                        }

                        if (offset < -32768 || offset > 32767) {
                            System.out.printf("Offset out of 16-bit signed range: %s %s %s %s\n",
                                    instruction, line.field0, line.field1, line.field2);
                            errorOccurred = true;
                            continue;
                        }

                        setBits(bits, 19, 3, regA);
                        setBits(bits, 16, 3, regB);
                        setBits(bits, 0, 16, offset & 0xFFFF);
                    }
                    case "jalr" -> {
                        int regA = Integer.parseInt(line.field0);
                        int regB = Integer.parseInt(line.field1);
                        setBits(bits, 19, 3, regA);
                        setBits(bits, 16, 3, regB);
                    }
                    case "halt", "noop" -> {
                        //no op
                    }
                }

                int machineCode = bitsToInt(bits);
                output.add(machineCode);

                String fullLine = String.join(" ",
                        line.label.isEmpty() ? "" : line.label,
                        instruction,
                        line.field0,
                        line.field1,
                        line.field2
                ).trim();

                System.out.printf("%-25s => %32s (%d)\n",
                        fullLine,
                        bitsToString(bits),
                        machineCode);

            } catch (NumberFormatException e) {
                System.out.println("Invalid register or label in instruction: " + instruction);
                errorOccurred = true;
            }
        }

        if (errorOccurred) {
            return new AssemblerResult(null, true); //no output if found error
        }

        return new AssemblerResult(output, false);
    }

    //int to binary and write into bits[]
    private static void setBits(int[] bits, int startBit, int length, int value) {
        StringBuilder binary = new StringBuilder(Integer.toBinaryString(value));

        if (binary.length() > length) {
            binary = new StringBuilder(binary.substring(binary.length() - length)); //use lower bits
        }
        while (binary.length() < length) {
            binary.insert(0, "0");
        }
        for (int i = 0; i < length; i++) {
            bits[startBit + i] = binary.charAt(length - 1 - i) - '0';
        }
    }

    //change bit array to int
    private static int bitsToInt(int[] bits) {
        int result = 0;
        for (int i = 0; i < 32; i++) {
            if (bits[i] == 1) {
                result += (1 << i);
            }
        }
        return result;
    }

    //change bit array to binary string
    private static String bitsToString(int[] bits) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 31; i >= 0; i--) {
            sb.append(bits[i]);
        }
        return sb.toString();
    }

    //check if string is a number
    private static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //label to address (index in list)
    private static int getAddressForLabel(String label, List<ParsedLine> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (label.equals(lines.get(i).label)) {
                return i;
            }
        }
        System.out.println("Undefined label: " + label);
        return -1;
    }
}