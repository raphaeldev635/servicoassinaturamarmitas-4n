package br.com.refeicoes;

import br.com.refeicoes.app.AppContext;
import br.com.refeicoes.view.ConsoleApp;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        String configuracao = System.getProperty("app.data", System.getenv().getOrDefault("APP_DATA_DIR", "dados"));
        Path pastaDados = Path.of(configuracao);
        AppContext context = new AppContext(pastaDados);
        new ConsoleApp(context).iniciar();
    }
}
