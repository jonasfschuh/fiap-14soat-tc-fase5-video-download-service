package br.com.fiap.application.bdd;

import br.com.fiap.application.VideoDownloadApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = VideoDownloadApplication.class)
public class CucumberSpringConfiguration {
}