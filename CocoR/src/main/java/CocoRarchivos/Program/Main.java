package CocoRarchivos.Program;

import CocoRarchivos.lib.Parser;
import CocoRarchivos.lib.Scanner;

public class Main {
    public static void main(String[] args) {
        String jsonArchivo = "E:\\Facultad\\Cuarto año, segundo semestre\\Compiladores\\CocoR\\src\\main\\java\\CocoRarchivos\\prueba.json";
        System.out.println("Parsing file: \"" + jsonArchivo + "\"" );
        Scanner scanner = new Scanner( jsonArchivo );
        Parser parser = new Parser( scanner );
        parser.Parse();
    }
}
