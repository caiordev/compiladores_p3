package br.ufma.p3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter writer;
    private String currentFunction;
    private int labelCounter;
    private String filename;

    public CodeWriter(String outputFile) throws IOException {
        writer = new PrintWriter(new FileWriter(outputFile));
        labelCounter = 0;
        currentFunction = "";
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void writeArithmetic(String command) {
        switch (command) {
            case "add":
                writeBinaryOperation("+");
                break;
            case "sub":
                writeBinaryOperation("-");
                break;
            case "neg":
                writeUnaryOperation("-");
                break;
            case "eq":
                writeComparisonOperation("JEQ");
                break;
            case "gt":
                writeComparisonOperation("JGT");
                break;
            case "lt":
                writeComparisonOperation("JLT");
                break;
            case "and":
                writeBinaryOperation("&");
                break;
            case "or":
                writeBinaryOperation("|");
                break;
            case "not":
                writeUnaryOperation("!");
                break;
        }
    }

    private void writeBinaryOperation(String operator) {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("M=M" + operator + "D");
    }

    private void writeUnaryOperation(String operator) {
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=" + operator + "M");
    }

    private void writeComparisonOperation(String jump) {
        String label = "COMP" + labelCounter++;
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("A=A-1");
        writer.println("D=M-D");
        writer.println("M=-1");
        writer.println("@" + label);
        writer.println("D;" + jump);
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("(" + label + ")");
    }

    public void writePushPop(CommandType command, String segment, int index) {
        if (command == CommandType.C_PUSH) {
            writePush(segment, index);
        } else if (command == CommandType.C_POP) {
            writePop(segment, index);
        }
    }

    private void writePush(String segment, int index) {
        switch (segment) {
            case "constant":
                writer.println("@" + index);
                writer.println("D=A");
                break;
            case "local":
                writePushSegment("LCL", index);
                break;
            case "argument":
                writePushSegment("ARG", index);
                break;
            case "this":
                writePushSegment("THIS", index);
                break;
            case "that":
                writePushSegment("THAT", index);
                break;
            case "temp":
                writer.println("@" + (5 + index));
                writer.println("D=M");
                break;
            case "pointer":
                writer.println("@" + (3 + index));
                writer.println("D=M");
                break;
            case "static":
                writer.println("@" + filename + "." + index);
                writer.println("D=M");
                break;
        }
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");
    }

    private void writePushSegment(String segment, int index) {
        writer.println("@" + segment);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("A=D+A");
        writer.println("D=M");
    }

    private void writePop(String segment, int index) {
        if (segment.equals("temp")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("@" + (5 + index));
            writer.println("M=D");
            return;
        }
        if (segment.equals("pointer")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("@" + (3 + index));
            writer.println("M=D");
            return;
        }
        if (segment.equals("static")) {
            writer.println("@SP");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("@" + filename + "." + index);
            writer.println("M=D");
            return;
        }

        String segmentPointer = getSegmentPointer(segment);
        writer.println("@" + segmentPointer);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("D=D+A");
        writer.println("@R13");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    private String getSegmentPointer(String segment) {
        switch (segment) {
            case "local":
                return "LCL";
            case "argument":
                return "ARG";
            case "this":
                return "THIS";
            case "that":
                return "THAT";
            default:
                throw new IllegalArgumentException("Invalid segment: " + segment);
        }
    }

    public void writeLabel(String label) {
        writer.println("(" + currentFunction + "$" + label + ")");
    }

    public void writeGoto(String label) {
        writer.println("@" + currentFunction + "$" + label);
        writer.println("0;JMP");
    }

    public void writeIf(String label) {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@" + currentFunction + "$" + label);
        writer.println("D;JNE");
    }

    public void writeFunction(String functionName, int nVars) {
        currentFunction = functionName;
        writer.println("(" + functionName + ")");
        for (int i = 0; i < nVars; i++) {
            writePush("constant", 0);
        }
    }

    public void writeCall(String functionName, int nArgs) {
        String returnLabel = currentFunction + "$ret." + labelCounter++;
        
        // Push return address
        writer.println("@" + returnLabel);
        writer.println("D=A");
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");

        // Push LCL, ARG, THIS, THAT
        for (String segment : new String[]{"LCL", "ARG", "THIS", "THAT"}) {
            writer.println("@" + segment);
            writer.println("D=M");
            writer.println("@SP");
            writer.println("A=M");
            writer.println("M=D");
            writer.println("@SP");
            writer.println("M=M+1");
        }

        // ARG = SP - 5 - nArgs
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@5");
        writer.println("D=D-A");
        writer.println("@" + nArgs);
        writer.println("D=D-A");
        writer.println("@ARG");
        writer.println("M=D");

        // LCL = SP
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");

        // goto function
        writer.println("@" + functionName);
        writer.println("0;JMP");

        // return label
        writer.println("(" + returnLabel + ")");
    }

    public void writeReturn() {
        // FRAME = LCL
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@R13"); // R13 = FRAME
        writer.println("M=D");

        // RET = *(FRAME-5)
        writer.println("@5");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@R14"); // R14 = RET
        writer.println("M=D");

        // *ARG = pop()
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
        writer.println("@ARG");
        writer.println("A=M");
        writer.println("M=D");

        // SP = ARG + 1
        writer.println("@ARG");
        writer.println("D=M+1");
        writer.println("@SP");
        writer.println("M=D");

        // Restore THAT, THIS, ARG, LCL
        String[] segments = {"THAT", "THIS", "ARG", "LCL"};
        for (int i = 0; i < segments.length; i++) {
            writer.println("@R13");
            writer.println("AM=M-1");
            writer.println("D=M");
            writer.println("@" + segments[i]);
            writer.println("M=D");
        }

        // goto RET
        writer.println("@R14");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    public void close() {
        writer.close();
    }
}
