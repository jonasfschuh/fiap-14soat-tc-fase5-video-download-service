package br.com.fiap.domain.model;

import java.io.InputStream;

/** Conteúdo de um arquivo pronto para ser servido como download. */
public record DownloadedFile(InputStream content, String filename) {
}
