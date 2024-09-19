package br.ufes.capes.service;

import br.ufes.capes.entity.Projeto;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Log4j2
public class GetDadosCapes {


    public List<Projeto> getDadosCapes() throws IOException, InterruptedException {
        WebDriver driver = new ChromeDriver();
        List<Projeto> todosProjetos = new ArrayList<>();

        for (int page = 1; page <= 10; page++) {
            String url = "https://www-periodicos-capes-gov-br.ezl.periodicos.capes.gov.br/index.php/acervo/buscador.html?q=intelig%C3%AAncia+artificial&source=&publishyear_min%5B%5D=1943&publishyear_max%5B%5D=2025&page=" + page;
            log.info("Navegando para: " + url);

            driver.get(url);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            String pageSource = driver.getPageSource();

            Document doc = Jsoup.parse(pageSource);

            Elements title = doc.getElementsByClass("titulo-busca");
            Elements li = doc.getElementsByClass("link-default add-metrics");
            Elements resume = doc.getElementsByClass("blockquote blockquote-busca mt-3");

            List<Projeto> projetos = new ArrayList<>();
            int minSize = Math.min(title.size(), Math.min(li.size(), resume.size()));

            for (int i = 0; i < minSize; i++) {
                String linkHref = li.get(i).attr("href");
                String titulo = title.get(i).text();
                String resumoTexto = resume.get(i).text();

                Projeto projeto = Projeto.builder()
                        .title(titulo)
                        .resumo(resumoTexto)
                        .url(linkHref)
                        .build();

                projetos.add(projeto);
            }

            todosProjetos.addAll(projetos);
            log.info("Total de projetos até agora: " + todosProjetos.size());

            Thread.sleep(1000);
        }

        driver.quit();
        return todosProjetos;
    }
}
