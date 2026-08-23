package br.com.fiap.application.adapters;

import br.com.fiap.application.dtos.DownloadUrlResponse;
import br.com.fiap.domain.model.PresignedUrlResult;
import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@Tag(name = "Video Download", description = "Geração da URL de download do ZIP de frames")
public class VideoDownloadController {

    private final GenerateDownloadUrlInputPort generateDownloadUrl;

    public VideoDownloadController(GenerateDownloadUrlInputPort generateDownloadUrl) {
        this.generateDownloadUrl = generateDownloadUrl;
    }

    @GetMapping("/{videoId}/download")
    @Operation(
        summary = "Gerar URL de download do ZIP de frames",
        description = """
                Gera a URL de download do arquivo ZIP contendo os frames extraídos do vídeo a partir do volume persistente local.

                **Pré-requisitos:**
                - O vídeo deve ter sido processado com sucesso (`status = DONE`)
                - O `videoId` deve pertencer ao usuário identificado pelo header `X-User-Id`

                **Como usar a URL retornada:**
                ```bash
                curl -L "<url>" -o frames.zip
                ```
                """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL gerada com sucesso",
            content = @Content(schema = @Schema(implementation = DownloadUrlResponse.class))),
        @ApiResponse(responseCode = "404", description = "ZIP não encontrado — vídeo ainda processando, falhou ou userId incorreto"),
        @ApiResponse(responseCode = "400", description = "videoId inválido")
    })
    public ResponseEntity<DownloadUrlResponse> generateDownloadUrl(
            @PathVariable UUID videoId,
            @Parameter(description = "ID do usuário autenticado (injetado pelo API Gateway)", required = true)
            @RequestHeader("X-User-Id") String userId) {

        PresignedUrlResult result = generateDownloadUrl.generateDownloadUrl(videoId, userId);

        return ResponseEntity.ok(new DownloadUrlResponse(
                result.url(),
                result.expiresAt(),
                result.videoId(),
                result.userId()
        ));
    }
}
