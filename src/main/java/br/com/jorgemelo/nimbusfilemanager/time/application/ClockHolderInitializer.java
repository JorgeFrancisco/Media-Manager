package br.com.jorgemelo.nimbusfilemanager.time.application;

import java.time.Clock;

import org.springframework.stereotype.Component;

import br.com.jorgemelo.nimbusfilemanager.shared.domain.ClockHolder;

/**
 * Wires the application {@link Clock} ({@link ConfigurableClock}) into
 * {@link ClockHolder} at startup, so entity callbacks stamp timestamps in the
 * configured zone. Kept as a tiny dedicated component so {@link ClockHolder}
 * stays a plain static holder with no Spring annotations.
 */
@Component
public class ClockHolderInitializer {

	public ClockHolderInitializer(Clock clock) {
		ClockHolder.configure(clock);
	}
}