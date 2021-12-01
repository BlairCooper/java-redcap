package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of possible values for the overwrite behavior when importing
 * records.
 * 
 */
public enum REDCapApiOverwriteBehavior
	implements REDCapApiEnum
{
	/** Do not overwrite existing records */
	NORMAL("normal"),
	/** Overwrite existing records */
	OVERWRITE("overwrite");

    private final String label;

    private REDCapApiOverwriteBehavior(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
