package br.ufes.capes.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Projeto {
    private String tipo;
    private String title;
    private String resumo;
    private String autor;
    private String url;
    private String AnoLocal;
}
