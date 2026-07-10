package br.com.jorgemelo.nimbusfilemanager.thumbnail.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * External-process glue for the video thumbnail: spawns ffmpeg to grab and
 * scale a single frame at {@code seek} seconds. Isolated in its own
 * {@code *ProcessRunner} class (excluded from coverage in pom.xml) because the
 * process spawn cannot be meaningfully unit-tested; the decode-free logic
 * (sizing, caching, atomic move) stays in {@link VideoThumbnailService}.
 */
@Component
public class FfmpegVideoThumbnailProcessRunner {

	public void run(String ffmpeg, Path source, Path target, int width, double seek)
			throws IOException, InterruptedException {
		List<String> command = List.of(ffmpeg, "-v", "error", "-y", "-ss", String.format(Locale.ROOT, "%.3f", seek),
				"-i", source.toString(), "-frames:v", "1", "-vf", "scale=" + width + ":-2", "-q:v", "3",
				target.toString());

		Process process = new ProcessBuilder(command).redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.redirectError(ProcessBuilder.Redirect.DISCARD).start();

		if (!process.waitFor(20, TimeUnit.SECONDS)) {
			process.destroyForcibly();

			throw new IOException("FFmpeg timed out while generating video thumbnail");
		}
		if (process.exitValue() != 0) {
			throw new IOException("FFmpeg could not generate video thumbnail");
		}
	}
}