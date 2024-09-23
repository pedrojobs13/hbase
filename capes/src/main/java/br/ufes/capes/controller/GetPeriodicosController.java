package br.ufes.capes.controller;

import br.ufes.capes.entity.Projeto;
import br.ufes.capes.service.GetDadosCapes;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.knowm.xchart.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/periodicos")
@RequiredArgsConstructor
public class GetPeriodicosController {
    private final GetDadosCapes getDadosCapes;


    @GetMapping("/list")
    public List<Projeto> listPeriodicos() throws IOException {
        return getDadosCapes.listDadosCapes();
    }

    @GetMapping("/grafico")
    public void getGrafico(HttpServletResponse response) throws IOException {
        List<Projeto> projetos = getDadosCapes.listDadosCapes();


        Map<String, Long> projetosProAno = projetos.stream()
                .collect(java.util.stream.Collectors.groupingBy(Projeto::getAno, java.util.stream.Collectors.counting()));

        PieChart chart = new PieChartBuilder().width(800).height(600).title("Artigos por Ano").build();

        projetosProAno.forEach(chart::addSeries);

        var image = BitmapEncoder.getBufferedImage(chart);

        ImageIO.write(image, "png", response.getOutputStream());
    }


    @GetMapping("/linhas-artigos-por-ano")
    public void getLineChart(HttpServletResponse response) throws IOException {

        List<Projeto> projetos = getDadosCapes.listDadosCapes();
        Map<String, Long> artigosPorAno = projetos.stream()
                .collect(Collectors.groupingBy(Projeto::getAno, Collectors.counting()));
        List<Integer> anos = artigosPorAno.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        XYChart chart = new XYChartBuilder().width(800).height(600).title("Tendência de Artigos por Ano").xAxisTitle("Ano").yAxisTitle("Número de Artigos").build();

        chart.addSeries("Artigos", anos, new ArrayList<>(artigosPorAno.values()));

        response.setContentType("image/png");
        ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", response.getOutputStream());
    }


    @GetMapping("/barras-artigos-por-ano")
    public void getBarChart(HttpServletResponse response) throws IOException {
        List<Projeto> projetos = getDadosCapes.listDadosCapes();

        Map<String, Long> artigosPorAno = projetos.stream()
                .collect(Collectors.groupingBy(Projeto::getAno, Collectors.counting()));


        CategoryChart chart = new CategoryChartBuilder().width(2500).height(600).title("Número de Artigos por Ano").xAxisTitle("Ano").yAxisTitle("Número de Artigos").build();


        chart.addSeries("Artigos", new ArrayList<>(artigosPorAno.keySet()), new ArrayList<>(artigosPorAno.values()));


        response.setContentType("image/png");
        ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", response.getOutputStream());
    }

    @GetMapping("/barras-artigos-por-universidade")
    public void getBarChartUni(HttpServletResponse response) throws IOException {
        List<Projeto> projetos = getDadosCapes.listDadosCapes();

        Map<String, Long> artigosPorAno = projetos.stream()
                .collect(Collectors.groupingBy(Projeto::getPublicacao, Collectors.counting()));

        List<Map.Entry<String, Long>> sorted = artigosPorAno.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toList());

        List<String> publicacoesTop = sorted.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Long> numerosArtigosTop = sorted.stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());


        CategoryChart chart = new CategoryChartBuilder()
                .width(1500).height(600)
                .title("Top 20 Publicações por Número de Artigos")
                .xAxisTitle("Publicação")
                .yAxisTitle("Número de Artigos")

                .build();
        chart.addSeries("Artigos", publicacoesTop, numerosArtigosTop);


        chart.getStyler().setXAxisLabelRotation(90);
        response.setContentType("image/png");
        ImageIO.write(BitmapEncoder.getBufferedImage(chart), "png", response.getOutputStream());
    }



}
