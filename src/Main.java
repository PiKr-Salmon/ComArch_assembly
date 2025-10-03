import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filePath = "src\\assembly.txt";
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                System.out.println("File read: " + line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }
        System.out.println("---------------------------");

        //parsing
        ParsedResult parseResult = AssemblerParser.parseAllInstructions(lines);
        System.out.println("---------------------------");
        if (parseResult.hasError) {
            System.out.println("Error detected! (Parser)");
            System.exit(1);
        }

        //assembler
        AssemblerResult result = Assembler.assembler(AssemblerParser.getParsedLines());
        System.out.println("---------------------------");
        if (result.hasError) {
            System.out.println("Error detected! (Assembler)");
            System.exit(1); //exit(1)
        } else {
            String outFilePath = "src\\machinecode.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFilePath))) {
                for (int code : result.machineCode) {
                    writer.write(String.valueOf(code));
                    writer.newLine();
                }
                System.out.println("Machine code written!");
            } catch (IOException e) {
                System.out.println("Error writing file!");
                System.exit(1); //exit(1)
            }

            //simulator
            System.out.println("---------------------------");
            System.out.println("Simulator:");
            Simulator.main(new String[]{outFilePath});

            System.exit(0); //exit(0)
        }
    }
}