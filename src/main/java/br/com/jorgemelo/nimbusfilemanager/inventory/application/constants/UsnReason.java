package br.com.jorgemelo.nimbusfilemanager.inventory.application.constants;

/**
 * The subset of {@code USN_REASON_*} flags the change interpreter reacts to. A
 * single USN record's {@code Reason} is a bitmask that may combine several of
 * these (for example {@code FILE_CREATE | DATA_EXTEND | CLOSE}).
 *
 * <p>
 * Values are the documented Win32 constants (winioctl.h). Kept here, decoupled
 * from JNA, so the interpreter and its tests never touch native code.
 */
public final class UsnReason {

	public static final int DATA_OVERWRITE = 0x0000_0001;
	public static final int DATA_EXTEND = 0x0000_0002;
	public static final int DATA_TRUNCATION = 0x0000_0004;
	public static final int FILE_CREATE = 0x0000_0100;
	public static final int FILE_DELETE = 0x0000_0200;
	public static final int RENAME_OLD_NAME = 0x0000_1000;
	public static final int RENAME_NEW_NAME = 0x0000_2000;
	public static final int CLOSE = 0x8000_0000;

	private UsnReason() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	/** Whether {@code reason} carries any bit in {@code mask}. */
	public static boolean hasAny(int reason, int mask) {
		return (reason & mask) != 0;
	}
}