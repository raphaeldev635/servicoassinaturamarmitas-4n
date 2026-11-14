package br.com.refeicoes.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Utilitário pequeno para arquivos CSV separados por ponto e vírgula. */
public final class CsvFile {
    private CsvFile() { }

    public static List<String[]> read(Path path) {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(path)) return rows;
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // primeira linha é o cabeçalho
                String line = lines.get(i);
                if (!line.isBlank()) rows.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler " + path + ": " + e.getMessage(), e);
        }
        return rows;
    }

    public static void write(Path path, String header, List<String[]> rows) {
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            List<String> lines = new ArrayList<>();
            lines.add(header);
            for (String[] row : rows) {
                List<String> encoded = new ArrayList<>();
                for (String value : row) encoded.add(encode(value == null ? "" : value));
                lines.add(String.join(";", encoded));
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gravar " + path + ": " + e.getMessage(), e);
        }
    }

    private static String encode(String value) {
        return value.replace("\\", "\\\\").replace(";", "\\;").replace("\n", "\\n").replace("\r", "");
    }

    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                field.append(c == 'n' ? '\n' : c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == ';') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        if (escaped) field.append('\\');
        fields.add(field.toString());
        return fields.toArray(String[]::new);
    }
}
