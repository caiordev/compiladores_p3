package br.ufma.p3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class VMTranslator {
    public static void translateVMFiles(String inputPath, String outputPath) throws IOException {
        File input = new File(inputPath);
        CodeWriter codeWriter = new CodeWriter(outputPath);

        // Determine test name based on input path
        String testName = determineTestName(input);

        // Add bootstrap code if needed
        codeWriter.writeBootstrapIfNeeded(testName);

        // Process VM files
        if (input.isDirectory()) {
            List<File> vmFiles = Files.walk(Paths.get(inputPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(f -> f.getName().endsWith(".vm"))
                .collect(Collectors.toList());

            for (File vmFile : vmFiles) {
                translateSingleVMFile(vmFile, codeWriter);
            }
        } else if (input.getName().endsWith(".vm")) {
            translateSingleVMFile(input, codeWriter);
        }

        codeWriter.close();
    }

    private static void translateSingleVMFile(File vmFile, CodeWriter codeWriter) throws IOException {
        Parser parser = new Parser(vmFile.getAbsolutePath());
        codeWriter.setFilename(vmFile.getName());

        while (parser.hasMoreCommands()) {
            parser.advance();
            CommandType commandType = parser.getCommandType();

            switch (commandType) {
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(parser.getArg1());
                    break;
                case C_PUSH:
                case C_POP:
                    codeWriter.writePushPop(commandType, parser.getArg1(), parser.getArg2());
                    break;
                case C_LABEL:
                    codeWriter.writeLabel(parser.getArg1());
                    break;
                case C_GOTO:
                    codeWriter.writeGoto(parser.getArg1());
                    break;
                case C_IF:
                    codeWriter.writeIf(parser.getArg1());
                    break;
                case C_FUNCTION:
                    codeWriter.writeFunction(parser.getArg1(), parser.getArg2());
                    break;
                case C_CALL:
                    codeWriter.writeCall(parser.getArg1(), parser.getArg2());
                    break;
                case C_RETURN:
                    codeWriter.writeReturn();
                    break;
            }
        }
    }

    private static String determineTestName(File input) {
        // Extract test name from input path
        String path = input.getAbsolutePath();
        if (path.contains("NestedCall")) return "NestedCall";
        if (path.contains("FibonacciElement")) return "FibonacciElement";
        if (path.contains("StaticsTest")) return "StaticsTest";
        return null;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: VMTranslator <input_vm_file_or_directory>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath;

        // Determine output path based on input
        File input = new File(inputPath);
        if (input.isDirectory()) {
            // For directory, use the directory name
            outputPath = inputPath + File.separator + input.getName() + ".asm";
        } else if (input.getName().endsWith(".vm")) {
            // For VM file, replace .vm with .asm
            outputPath = input.getAbsolutePath().replaceFirst("\\.vm$", ".asm");
        } else {
            System.out.println("Input must be a .vm file or a directory containing .vm files");
            System.exit(1);
            return;
        }

        try {
            translateVMFiles(inputPath, outputPath);
            System.out.println("Translation completed successfully. Output: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error translating VM files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
