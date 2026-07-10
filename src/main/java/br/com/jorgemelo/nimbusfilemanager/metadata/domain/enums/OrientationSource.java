package br.com.jorgemelo.nimbusfilemanager.metadata.domain.enums;

/**
 * Where the orientation applied by {@code PhotoDecoder} came from. The
 * precedence is single and testable: the file's own EXIF orientation wins; the
 * persisted {@code media_metadata.rotation} is used only when there is no EXIF
 * orientation; and when neither applies the image is left as decoded. The two
 * are <b>never</b> applied cumulatively, which is what prevents the
 * double-rotation the old thumbnail path could produce.
 */
public enum OrientationSource {

	/** Orientation came from the file's EXIF Orientation tag (values 1-8). */
	EXIF,

	/**
	 * No usable EXIF orientation; the persisted DB rotation was applied instead.
	 */
	DB_ROTATION,

	/** Neither EXIF nor DB rotation applied - the decoded pixels are used as-is. */
	NONE
}