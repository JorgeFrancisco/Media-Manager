package br.com.jorgemelo.nimbusfilemanager.metadata.domain.enums;

/**
 * Which ImageIO reader actually decoded a photo. Reported by
 * {@code PhotoDecoder} so metrics can split the in-JVM cost between formats the
 * JDK reads natively (JPEG/PNG/BMP/GIF/TIFF) and formats served by the
 * TwelveMonkeys WEBP plugin, without any business code ever referencing a
 * TwelveMonkeys class.
 */
public enum DecoderType {

	/** A JDK-bundled ImageIO reader (JPEG/PNG/BMP/GIF/TIFF...). */
	IMAGEIO_NATIVE,

	/** The TwelveMonkeys WEBP reader, contributed through the ImageIO SPI. */
	WEBP_PLUGIN
}