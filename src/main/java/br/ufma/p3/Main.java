package br.ufma.p3;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar vmtranslator.jar <input.vm>");
            System.exit(1);
        }

        try {
            String inputPath = args[0];
            File inputFile = new File(inputPath);
            String outputPath = inputPath.replace(".vm", ".asm");

            Parser parser = new Parser(inputPath);
            CodeWriter codeWriter = new CodeWriter(outputPath);
            
            // Set the filename for static variable handling
            codeWriter.setFilename(inputFile.getName().replace(".vm", ""));

            // Process each command
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
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                    case C_CALL:
                        codeWriter.writeCall(parser.getArg1(), parser.getArg2());
                        break;
                }
            }

            codeWriter.close();
            System.out.println("Translation completed successfully. Output written to: " + outputPath);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            System.exit(1);
        }
    }
}