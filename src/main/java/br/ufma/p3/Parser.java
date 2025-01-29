package br.ufma.p3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<String> commands;
    private int currentCommand;

    public Parser(String filename) throws IOException {
        commands = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Remove comments and empty lines
                if (!line.isEmpty() && !line.startsWith("//")) {
                    int commentIndex = line.indexOf("//");
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex).trim();
                    }
                    commands.add(line);
                }
            }
        }
        currentCommand = -1;
    }

    public boolean hasMoreCommands() {
        return currentCommand < commands.size() - 1;
    }

    public void advance() {
        if (hasMoreCommands()) {
            currentCommand++;
        }
    }

    public String getCurrentCommand() {
        return commands.get(currentCommand);
    }

    public CommandType getCommandType() {
        String[] parts = getCurrentCommand().split("\\s+");
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "push":
                return CommandType.C_PUSH;
            case "pop":
                return CommandType.C_POP;
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                return CommandType.C_ARITHMETIC;
            case "label":
                return CommandType.C_LABEL;
            case "goto":
                return CommandType.C_GOTO;
            case "if-goto":
                return CommandType.C_IF;
            case "function":
                return CommandType.C_FUNCTION;
            case "call":
                return CommandType.C_CALL;
            case "return":
                return CommandType.C_RETURN;
            default:
                throw new IllegalStateException("Unknown command: " + command);
        }
    }

    public String getArg1() {
        String[] parts = getCurrentCommand().split("\\s+");
        if (getCommandType() == CommandType.C_ARITHMETIC) {
            return parts[0];
        }
        return parts[1];
    }

    public int getArg2() {
        String[] parts = getCurrentCommand().split("\\s+");
        return Integer.parseInt(parts[2]);
    }
}
