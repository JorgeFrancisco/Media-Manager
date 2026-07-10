package br.com.jorgemelo.nimbusfilemanager.telemetry.application.dto;

public record PhotoHashCounters(long jvmDecodable, long ffmpegOnly, long failures) {
}