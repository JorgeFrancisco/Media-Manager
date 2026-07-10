package br.com.jorgemelo.nimbusfilemanager.shared.domain.enums;

/**
 * Where a media file's capture date came from. Doubles as a confidence signal
 * (higher trust first), so downstream logic (e.g. the duplicate recommender)
 * can weight dates by reliability instead of comparing raw values:
 *
 * <ul>
 * <li>{@code EXIF}/{@code MEDIA_INFO} - embedded in the content, the true
 * capture instant.</li>
 * <li>{@code FILE_NAME_CONFIRMED} - the day came from the file name AND a
 * filesystem timestamp (modified/created) of the same day corroborated it, so
 * we adopted that timestamp for the real time-of-day. High trust.</li>
 * <li>{@code FILE_NAME}/{@code FOLDER_LAYOUT} - day parsed from the
 * name/folder, not corroborated (usually midnight). Medium trust.</li>
 * <li>{@code FILE_CREATED_AT}/{@code FILE_MODIFIED_AT} - filesystem timestamp
 * with nothing to corroborate it (a copy/sync date). Low trust.</li>
 * <li>{@code UNKNOWN} - none available.</li>
 * </ul>
 */
public enum DateSource {

	EXIF, MEDIA_INFO, FILE_NAME, FILE_NAME_CONFIRMED, FILE_CREATED_AT, FILE_MODIFIED_AT, FOLDER_LAYOUT, UNKNOWN
}