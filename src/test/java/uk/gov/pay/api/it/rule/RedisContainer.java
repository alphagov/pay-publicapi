package uk.gov.pay.api.it.rule;

import com.google.common.base.Stopwatch;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

class RedisContainer {

    private static final Logger logger = LoggerFactory.getLogger(RedisContainer.class);

    private final String containerId;
    private final int port;
    private DockerClient docker;
    private String host;
    private volatile boolean stopped = false;

    private static final int DB_TIMEOUT_SEC = 10;
    private static final String REDIS_IMAGE = "redis:latest";
    private static final String INTERNAL_PORT = "6379";

    RedisContainer(DockerClient docker, String host) throws DockerException, InterruptedException {

        this.docker = docker;
        this.host = host;

        failsafeDockerPull(docker);
        docker.listImages(DockerClient.ListImagesParam.create("name", REDIS_IMAGE));

        final HostConfig hostConfig = HostConfig.builder().logConfig(LogConfig.create("json-file")).publishAllPorts(true).build();
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(REDIS_IMAGE)
                .hostConfig(hostConfig)
                .build();
        containerId = docker.createContainer(containerConfig).id();
        docker.startContainer(containerId);
        port = hostPortNumber(docker.inspectContainer(containerId));
        registerShutdownHook();
        waitForRedisToStart();
    }

    private void failsafeDockerPull(DockerClient docker) {
        try {
            docker.pull(REDIS_IMAGE);
        } catch (Exception e) {
            logger.error("Docker image " + REDIS_IMAGE + " could not be pulled from DockerHub", e);
        }
    }

    String getConnectionUrl() {
        return host + ":" + port;
    }

    private static int hostPortNumber(ContainerInfo containerInfo) {
        String redisPortSpec = INTERNAL_PORT + "/tcp";
        PortBinding portBinding;
        try {
            portBinding = Objects.requireNonNull(containerInfo
                    .networkSettings()
                    .ports())
                    .get(redisPortSpec)
                    .get(0);
            logger.info("Redis host port: {}", portBinding.hostPort());
            return parseInt(portBinding.hostPort());
        } catch (NullPointerException e) {
            logger.error("Unable to find host port mapping for {} in container {}", redisPortSpec, containerInfo.id());
            throw e;
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void waitForRedisToStart() throws InterruptedException {
        Stopwatch timer = Stopwatch.createStarted();
        boolean succeeded = false;
        while (!succeeded && timer.elapsed(TimeUnit.SECONDS) < DB_TIMEOUT_SEC) {
            Thread.sleep(300);
            succeeded = checkRedisConnection();
        }
        if (!succeeded) {
            throw new RuntimeException("Redis did not start in " + DB_TIMEOUT_SEC + " seconds.");
        }
        logger.info("Redis docker container started in {}.", timer.elapsed(TimeUnit.MILLISECONDS));
    }

    private boolean checkRedisConnection() {
        try (Jedis ignored = new Jedis(host, port)) {
            return true;
        } catch (Exception except) {
            return false;
        }
    }

    void stop() {
        if (stopped) {
            return;
        }
        try {
            stopped = true;
            System.err.println("Killing redis container with ID: " + containerId);
            LogStream logs = docker.logs(containerId, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
            System.err.println("Killed container logs:\n");
            logs.attach(System.err, System.err);
            docker.stopContainer(containerId, 5);
            docker.removeContainer(containerId);
        } catch (DockerException | InterruptedException | IOException e) {
            System.err.println("Could not shutdown " + containerId);
            e.printStackTrace();
        }
    }

    public void clearRedisCache() {
        Jedis jedis = new Jedis(host, port);
        String response = jedis.flushAll();
        if (!response.equals("OK")) {
            logger.warn("Unexpected response from redis flushAll command: " + response);
        }
        jedis.disconnect();
    }
}
