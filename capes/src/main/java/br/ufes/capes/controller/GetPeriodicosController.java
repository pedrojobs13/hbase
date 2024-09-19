package br.ufes.capes.controller;

import br.ufes.capes.entity.Projeto;
import br.ufes.capes.service.GetDadosCapes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/periodicos")
@RequiredArgsConstructor
public class GetPeriodicosController {
    private final GetDadosCapes getDadosCapes;

    @GetMapping("/get")
    public List<Projeto> getPeriodicos() throws IOException, InterruptedException {
        return getDadosCapes.getDadosCapes();
    }

}
