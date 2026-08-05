package br.com.fiap.application.bdd.steps;

import br.com.fiap.application.adapters.AuthProxyController;
import br.com.fiap.application.adapters.VideoDownloadController;
import br.com.fiap.application.dtos.AuthLoginRequest;
import br.com.fiap.domain.exceptions.VideoZipNotFoundException;
import br.com.fiap.domain.model.PresignedUrlResult;
import br.com.fiap.domain.ports.in.GenerateDownloadUrlInputPort;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class VideoDownloadSteps {

    private UUID videoId;
    private String ownerUserId;
    private boolean zipExists;
    private ResponseEntity<?> response;

    @Given("a video with id {string} belonging to user {string}")
    public void aVideoWithIdBelongingToUser(String rawVideoId, String userId) {
        this.videoId = UUID.fromString(rawVideoId);
        this.ownerUserId = userId;
        this.zipExists = false;
        this.response = null;
    }

    @And("the ZIP file exists in storage")
    public void theZipFileExistsInStorage() {
        this.zipExists = true;
    }

    @And("the ZIP file does not exist in storage")
    public void theZipFileDoesNotExistInStorage() {
        this.zipExists = false;
    }

    @And("only the ZIP for user {string} exists")
    public void onlyTheZipForUserExists(String userId) {
        this.ownerUserId = userId;
        this.zipExists = true;
    }

    @When("a download URL request is made for videoId {string} and userId {string}")
    public void aDownloadUrlRequestIsMadeForVideoIdAndUserId(String rawVideoId, String userId) {
        this.videoId = UUID.fromString(rawVideoId);
        GenerateDownloadUrlInputPort stubUseCase = (requestedVideoId, requestedUser) -> {
            if (!zipExists || !ownerUserId.equals(requestedUser)) {
                throw new VideoZipNotFoundException(requestedVideoId, requestedUser);
            }
            return new PresignedUrlResult(
                    "https://s3.example.com/" + requestedVideoId,
                    Instant.now().plusSeconds(900),
                    requestedVideoId,
                    requestedUser
            );
        };

        VideoDownloadController controller = new VideoDownloadController(stubUseCase);
        try {
            response = controller.generateDownloadUrl(this.videoId, userId);
        } catch (VideoZipNotFoundException ex) {
            response = ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Then("the response should contain a presigned URL")
    public void theResponseShouldContainAPresignedUrl() {
        assertThat(response).isNotNull();
        assertThat(String.valueOf(response.getBody())).contains("https://s3.example.com/");
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    @When("a login request is made without AUTH_LAMBDA_URL configured")
    public void aLoginRequestIsMadeWithoutAuthLambdaUrlConfigured() {
        AuthProxyController controller = new AuthProxyController(new RestTemplate());
        response = controller.login(new AuthLoginRequest("admin", "admin123"));
    }
}
