package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of possible formats for dates when importing records.
 *
 */
public enum REDCapApiDateFormat
	implements REDCapApiEnum
{
	/** Month Day Year */
	MDY("MDY"),
	/** Day Month Year */
	DMY("DMY"),
	/** Year Month Day */
	YMD("YMD");

	private final String label;

    private REDCapApiDateFormat(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
