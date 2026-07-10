package br.com.jorgemelo.nimbusfilemanager.metadata.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.metadata.application.dto.FfprobeResult;

/**
 * External-process glue for ffprobe: spawns the probe and drains stdout (the
 * JSON) and stderr on separate threads. Isolated in its own
 * {@code *ProcessRunner} class (excluded from coverage in pom.xml) because
 * process spawning and concurrent stream draining cannot be meaningfully
 * unit-tested; the JSON parsing / metadata mapping stays in
 * {@link MediaInfoService}.
 */
@Component
public class FfprobeProcessRunner {

	public FfprobeResult run(String ffprobePath, Path file) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(ffprobePath, "-v", "error", "-print_format", "json", "-show_format",
				"-show_streams", file.toAbsolutePath().normalize().toString());

		Process process = builder.start();

		StringBuilder output = new StringBuilder();
		StringBuilder errorOutput = new StringBuilder();

		Thread outputReader = drainAsync(process.getInputStream(), output);
		Thread errorReader = drainAsync(process.getErrorStream(), errorOutput);

		try {
			boolean finished = process.waitFor(30, TimeUnit.SECONDS);

			outputReader.join(TimeUnit.SECONDS.toMillis(2));
			errorReader.join(TimeUnit.SECONDS.toMillis(2));

			return new FfprobeResult(finished, finished ? process.exitValue() : -1, output.toString(),
					errorOutput.toString());
		} finally {
			if (process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}

	private static Thread drainAsync(InputStream stream, StringBuilder sink) {
		Thread thread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
				String line;

				while ((line = reader.readLine()) != null) {
					sink.append(line);
				}
			} catch (IOException _) {
				// Stream closed because the process was destroyed after timing out.
			}
		});

		thread.setDaemon(true);
		thread.start();

		return thread;
	}
}