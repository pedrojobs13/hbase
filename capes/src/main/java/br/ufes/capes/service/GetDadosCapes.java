package br.ufes.capes.service;

import br.ufes.capes.entity.Projeto;
import br.ufes.capes.entity.dao.HbaseClientOperations;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.hbase.exceptions.IllegalArgumentIOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Log4j2
@EnableScheduling
public class GetDadosCapes {
    private final HbaseClientOperations hbaseClientOperations;
    private static final String TIME_ZONE = "America/Sao_Paulo";

    @Scheduled(cron = "0 0 * * * *", zone = TIME_ZONE)
    public void getDadosCapes() throws IOException, InterruptedException {
        hbaseClientOperations.createTable();

        WebDriver driver = new ChromeDriver();
        List<Projeto> todosProjetos = new ArrayList<>();

        String totalDePaginas = "https://www-periodicos-capes-gov-br.ezl.periodicos.capes.gov.br/index.php/acervo/buscador.html?q=intelig%C3%AAncia+artificial&source=&publishyear_min%5B%5D=1943&publishyear_max%5B%5D=2025&page=1";
        driver.get(totalDePaginas);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String pageSourceFirst = driver.getPageSource();
        Document docx = Jsoup.parse(pageSourceFirst);

        Elements getTotalPage = docx.select("#result-busca > div:nth-child(4) > div.col-sm-9 > div.row.mb-4 > div > p");
        String input = getTotalPage.text();
        log.info(input);

        Pattern pattern = Pattern.compile("para (\\d{1,3}(?:\\.\\d{3})*) \\(");
        Matcher matcher = pattern.matcher(input);

        if (!matcher.find()) {
            throw new IllegalArgumentIOException("Não foi possível encontrar o total de páginas");
        }
        String numero = matcher.group(1).replace(".", "");
        int total = Integer.parseInt(numero);
        total = (total / 30) + 1;

        for (int page = 1; page <= total; page++) {
            String url = "https://www-periodicos-capes-gov-br.ezl.periodicos.capes.gov.br/index.php/acervo/buscador.html?q=intelig%C3%AAncia+artificial&source=&publishyear_min%5B%5D=1943&publishyear_max%5B%5D=2025&page=" + page;

            driver.get(url);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            Elements title = doc.getElementsByClass("titulo-busca");
            Elements li = doc.getElementsByClass("link-default add-metrics");
            Elements resume = doc.getElementsByClass("blockquote blockquote-busca mt-3");
            Elements getContent = doc.select("[id^=conteudo-] > p:nth-child(3) > b");
            Elements ano = doc.select("[id^=conteudo-] > p:nth-child(5) > b:nth-child(1)");
            Elements universidade = doc.select("[id^=conteudo-] > p:nth-child(5) > b:nth-child(2)");
            Elements tipo = doc.select("[id^=conteudo-] > div:nth-child(1) > div > p > b");

            List<Projeto> projetos = new ArrayList<>();
            int minSize = Math.min(Math.min(title.size(), li.size()), Math.min(resume.size(), Math.min(getContent.size(), Math.min(ano.size(), Math.min(universidade.size(), tipo.size())))));

            for (int i = 0; i < minSize; i++) {
                String linkHref = li.get(i).attr("href");
                String titulo = title.get(i).text();
                String resumoTexto = resume.get(i).text();
                String autor = getContent.get(i).text();
                String anoPublic = ano.get(i).text();
                String localDePublicacao = universidade.get(i).text().replace("- ", "").replace("| ", "");
                String tipoDoRecurso = tipo.get(i).text();

                Projeto projeto = Projeto.builder()
                        .tipoDoRecurso(tipoDoRecurso)
                        .title(titulo)
                        .autor(autor)
                        .resumo(resumoTexto)
                        .url(linkHref)
                        .ano(anoPublic)
                        .publicacao(localDePublicacao)
                        .build();
                log.info(projeto);
                projetos.add(projeto);

                if (!hbaseClientOperations.dataExists(titulo, "artigos", "autor")) {
                    hbaseClientOperations.insertData(titulo, "artigos", "autor", autor);
                    hbaseClientOperations.insertData(titulo, "artigos", "resumo", resumoTexto);
                    hbaseClientOperations.insertData(titulo, "artigos", "url", linkHref);
                    hbaseClientOperations.insertData(titulo, "artigos", "ano", anoPublic);
                    hbaseClientOperations.insertData(titulo, "artigos", "publicacao", localDePublicacao);
                    hbaseClientOperations.insertData(titulo, "artigos", "tipoDoRecurso", tipoDoRecurso);
                }
            }

            todosProjetos.addAll(projetos);
            log.info("Total de projetos até agora: " + todosProjetos.size());
            Thread.sleep(1000);
        }

        driver.quit();
    }


    public List<Projeto> listDadosCapes() throws IOException {
        hbaseClientOperations.createTable();
        return hbaseClientOperations.listAllData();
    }
}