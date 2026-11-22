package com.livrofacil.modulos.chat.controller;

import com.livrofacil.modulos.chat.service.DatasetExportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DatasetExportControllerTest {
    @Mock private DatasetExportService datasetExportService;
    private static final String TOKEN = "segredo-de-teste";

    @Test
    void ofereceArquivoJsonlParaDownload() throws Exception {
        when(datasetExportService.exportarTreinoJsonl()).thenReturn("{\"contents\":[]}");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        new DatasetExportController(datasetExportService, TOKEN, false))
                .build();

        mockMvc.perform(get("/api/chat/dataset.jsonl")
                        .header("X-Dataset-Export-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/x-ndjson"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"livrofacil-dataset-treino.jsonl\""));
    }

    @Test
    void ofereceArquivoJsonlDeValidacaoParaDownload() throws Exception {
        when(datasetExportService.exportarValidacaoJsonl()).thenReturn("{\"contents\":[]}");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        new DatasetExportController(datasetExportService, TOKEN, false))
                .build();

        mockMvc.perform(get("/api/chat/dataset/validation.jsonl")
                        .header("X-Dataset-Export-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/x-ndjson"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"livrofacil-dataset-validacao.jsonl\""));
    }

    @Test
    void bloqueiaExportacaoSemTokenCorretoOuConfigurado() throws Exception {
        MockMvc protegido = MockMvcBuilders.standaloneSetup(
                        new DatasetExportController(datasetExportService, TOKEN, false))
                .build();
        MockMvc semConfiguracao = MockMvcBuilders.standaloneSetup(
                        new DatasetExportController(datasetExportService, "", false))
                .build();

        protegido.perform(get("/api/chat/dataset.jsonl"))
                .andExpect(status().isForbidden());
        semConfiguracao.perform(get("/api/chat/dataset.jsonl")
                        .header("X-Dataset-Export-Token", TOKEN))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void liberaExportacaoSemTokenSomenteQuandoModoDemoHabilitado() throws Exception {
        when(datasetExportService.exportarTreinoJsonl()).thenReturn("{\"contents\":[]}");
        MockMvc demo = MockMvcBuilders.standaloneSetup(
                        new DatasetExportController(datasetExportService, "", true))
                .build();

        demo.perform(get("/api/chat/dataset.jsonl"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"livrofacil-dataset-treino.jsonl\""));
    }
}
