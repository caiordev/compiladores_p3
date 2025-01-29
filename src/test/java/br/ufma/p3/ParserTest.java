package br.ufma.p3;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ParserTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private File testFile;

    @Before
    public void setUp() throws IOException {
        testFile = tempFolder.newFile("test.vm");
        try (FileWriter writer = new FileWriter(testFile)) {
            // Write test VM commands
            writer.write("// This is a comment\n");
            writer.write("push constant 10\n");
            writer.write("push local 2\n");
            writer.write("add // inline comment\n");
            writer.write("pop argument 1\n");
            writer.write("label LOOP\n");
            writer.write("goto LOOP\n");
            writer.write("if-goto END\n");
            writer.write("function sum 2\n");
            writer.write("call sum 2\n");
            writer.write("return\n");
        }
    }

    @Test
    public void testParserInitialization() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        assertTrue(parser.hasMoreCommands());
    }

    @Test
    public void testCommandNavigation() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        int commandCount = 0;
        while (parser.hasMoreCommands()) {
            parser.advance();
            commandCount++;
        }
        assertEquals(10, commandCount); // Number of non-comment commands in test file
    }

    @Test
    public void testPushCommand() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        parser.advance(); // First command is "push constant 10"
        assertEquals(CommandType.C_PUSH, parser.getCommandType());
        assertEquals("constant", parser.getArg1());
        assertEquals(10, parser.getArg2());
    }

    @Test
    public void testArithmeticCommand() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        parser.advance();
        parser.advance();
        parser.advance(); // Third command is "add"
        assertEquals(CommandType.C_ARITHMETIC, parser.getCommandType());
        assertEquals("add", parser.getArg1());
    }

    @Test
    public void testPopCommand() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        parser.advance();
        parser.advance();
        parser.advance();
        parser.advance(); // Fourth command is "pop argument 1"
        assertEquals(CommandType.C_POP, parser.getCommandType());
        assertEquals("argument", parser.getArg1());
        assertEquals(1, parser.getArg2());
    }

    @Test
    public void testLabelCommand() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        for (int i = 0; i < 4; i++) parser.advance();
        parser.advance(); // Fifth command is "label LOOP"
        assertEquals(CommandType.C_LABEL, parser.getCommandType());
        assertEquals("LOOP", parser.getArg1());
    }

    @Test
    public void testFunctionCommand() throws IOException {
        Parser parser = new Parser(testFile.getAbsolutePath());
        for (int i = 0; i < 8; i++) parser.advance();
        assertEquals(CommandType.C_FUNCTION, parser.getCommandType());
        assertEquals("sum", parser.getArg1());
        assertEquals(2, parser.getArg2());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidCommand() throws IOException {
        File invalidFile = tempFolder.newFile("invalid.vm");
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("invalid command\n");
        }
        Parser parser = new Parser(invalidFile.getAbsolutePath());
        parser.advance();
        parser.getCommandType(); // Should throw IllegalStateException
    }

    @Test
    public void testCommentHandling() throws IOException {
        File commentFile = tempFolder.newFile("comment.vm");
        try (FileWriter writer = new FileWriter(commentFile)) {
            writer.write("// Full line comment\n");
            writer.write("push constant 1 // Inline comment\n");
            writer.write("\n"); // Empty line
            writer.write("pop local 0\n");
        }
        Parser parser = new Parser(commentFile.getAbsolutePath());
        int commandCount = 0;
        while (parser.hasMoreCommands()) {
            parser.advance();
            commandCount++;
        }
        assertEquals(2, commandCount); // Should only count non-comment lines
    }
}
