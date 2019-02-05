package uk.gov.pay.api.it.rule;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedisDockerRule implements TestRule {

    private static String host;
    private static RedisContainer container;

    static {
        try (DockerClient docker = DefaultDockerClient.fromEnv().build()) {

            String dockerHost = docker.getHost();
            URI dockerHostURI = new URI(dockerHost);
            boolean isDockerDaemonLocal =
                    "unix".equals(dockerHostURI.getHost())
                            || "localhost".equals(docker.getHost());

            host = isDockerDaemonLocal ? "localhost" : dockerHostURI.getHost();

        } catch (DockerCertificateException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public RedisDockerRule() throws DockerException {
        startRedisIfNecessary();
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return statement;
    }

    private void startRedisIfNecessary() throws DockerException {
        try {
            if (container == null) {
                DockerClient docker = DefaultDockerClient.fromEnv().build();
                container = new RedisContainer(docker, host);
            }
        } catch (DockerCertificateException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        container.stop();
        container = null;
    }

    public String getRedisUrl() {
        return container.getConnectionUrl();
    }

}
