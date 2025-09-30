import java.io.*;
import java.util.*;

public class Simulator {
    static final int NUMMEMORY = 65536; // maximum number of words in memory
    static final int NUMREGS   = 8;     // number of machine registers

    static class State {
        int pc;
        int[] mem = new int[NUMMEMORY];
        int[] reg = new int[NUMREGS];
        int numMemory; // number of words actually loaded from file
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.printf("error: usage: %s <machine-code file>\n", "java Simulator");
            System.exit(1);
        }

        State state = new State();

        // ---- Read machine-code file into memory (exactly like the C version) ----
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                // DO NOT skip empty lines; treat any non-integer as an error at this address
                int value;
                try {
                    // trim like sscanf would ignore leading spaces/tabs/newlines
                    value = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    System.out.printf("error in reading address %d\n", state.numMemory);
                    System.exit(1);
                    return; // unreachable, but keeps the compiler happy
                }
                state.mem[state.numMemory] = value;
                System.out.printf("memory[%d]=%d\n", state.numMemory, state.mem[state.numMemory]);
                state.numMemory++;
                if (state.numMemory >= NUMMEMORY) break;
            }
        } catch (FileNotFoundException e) {
            System.out.printf("error: can't open file %s", args[0]);
            System.out.println();
            System.out.flush();
            System.err.println("fopen");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("error: IO exception while reading file");
            System.exit(1);
        }

        // ---- Initialize registers and pc ----
        state.pc = 0;
        Arrays.fill(state.reg, 0);

        int instrCount = 0;
        boolean running = true;

        // ---- Main execute loop ----
        while (running) {
            printState(state);

            // pc bounds check (the C version effectively allows any 0..NUMMEMORY-1)
            if (state.pc < 0 || state.pc >= NUMMEMORY) {
                System.out.println("error: pc out of bounds");
                System.exit(1);
            }

            int inst = state.mem[state.pc];
            int opcode = getOpcode(inst);
            int regA   = getRegA(inst);
            int regB   = getRegB(inst);
            int offset = getOffset(inst); // sign-extended 16-bit

            instrCount++;

            switch (opcode) {
                case 0: { // add
                    int dest = getDestReg(inst);
                    state.reg[dest] = state.reg[regA] + state.reg[regB];
                    state.pc++;
                    break;
                }
                case 1: { // nand
                    int dest = getDestReg(inst);
                    state.reg[dest] = ~(state.reg[regA] & state.reg[regB]);
                    state.pc++;
                    break;
                }
                case 2: { // lw
                    int addr = state.reg[regA] + offset;
                    if (addr < 0 || addr >= NUMMEMORY) {
                        System.out.println("error: lw out of bounds");
                        System.exit(1);
                    }
                    state.reg[regB] = state.mem[addr];
                    state.pc++;
                    break;
                }
                case 3: { // sw
                    int addr = state.reg[regA] + offset;
                    if (addr < 0 || addr >= NUMMEMORY) {
                        System.out.println("error: sw out of bounds");
                        System.exit(1);
                    }
                    state.mem[addr] = state.reg[regB];
                    state.pc++;
                    break;
                }
                case 4: { // beq
                    if (state.reg[regA] == state.reg[regB]) {
                        state.pc = state.pc + 1 + offset; // PC-relative to next
                    } else {
                        state.pc++;
                    }
                    break;
                }
                case 5: { // jalr
                    int nextPC = state.pc + 1;
                    state.pc = state.reg[regA];
                    state.reg[regB] = nextPC;
                    break;
                }
                case 6: { // halt
                    System.out.println("machine halted");
                    System.out.printf("total of %d instructions executed\n", instrCount);
                    System.out.println("final state of machine:");
                    printState(state);
                    running = false;
                    break;
                }
                case 7: { // noop
                    state.pc++;
                    break;
                }
                default:
                    System.out.printf("error: illegal opcode %d\n", opcode);
                    System.exit(1);
            }
        }
    }

    // ---- Decode helpers (match the C bit layout) ----
    static int getOpcode(int inst) { return (inst >>> 22) & 0x7; }
    static int getRegA  (int inst) { return (inst >>> 19) & 0x7; }
    static int getRegB  (int inst) { return (inst >>> 16) & 0x7; }
    static int getDestReg(int inst){ return  inst & 0x7; }

    static int getOffset(int inst) {
        int off = inst & 0xFFFF;        // low 16 bits
        if ((off & 0x8000) != 0) {
            off -= (1 << 16);           // sign-extend to negative
        }
        return off;
    }

    // ---- Pretty printer (exactly like the C version) ----
    static void printState(State s) {
        System.out.println("\n@@@");
        System.out.println("state:");
        System.out.printf("\tpc %d\n", s.pc);
        System.out.println("\tmemory:");
        for (int i = 0; i < s.numMemory; i++) {
            System.out.printf("\t\tmem[ %d ] %d\n", i, s.mem[i]);
        }
        System.out.println("\tregisters:");
        for (int i = 0; i < NUMREGS; i++) {
            System.out.printf("\t\treg[ %d ] %d\n", i, s.reg[i]);
        }
        System.out.println("end state");
    }
}
