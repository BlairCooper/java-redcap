package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of possible log types when exporting logging information. 
 *
 */
public enum REDCapApiLogType
	implements REDCapApiEnum
{
	/** The export log */
	EXPORT("export"),
	/** The manage log */
	MANAGE("manage"),
	/** The user log */
	USER("user"),
	/** The record log */
	RECORD("record"),
	/** The record additions log */
	RECORD_ADD("record_add"),
	/** The record edits log */
    RECORD_EDIT("record_edit"),
    /** The record deletions log */
    RECORD_DELETE("record_delete"),
    /** The record locks log */
    LOCK_RECORD("lock_record"),
    /** The page view log */
    PAGE_VIEW("page_view");

    private final String label;

    private REDCapApiLogType(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
