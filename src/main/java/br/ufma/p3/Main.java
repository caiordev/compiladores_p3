package br.ufma.p3;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar vmtranslator.jar <input.vm>");
            System.exit(1);
        }
    }
}
