package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of REDCap API request actions. The action along with a content identifies the
 * type of request being made.
 *
 */
public enum REDCapApiAction
	implements REDCapApiEnum
{
	/** An import data request */
	IMPORT("import"),
	/** An export data request */
	EXPORT("export"),
	/** A delete request */
	DELETE("delete");

    private final String label;

    private REDCapApiAction(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
