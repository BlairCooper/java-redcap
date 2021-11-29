package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiLogType
	implements RedCapApiEnum
{
	EXPORT("export"),
	MANAGE("manage"),
	USER("user"),
	RECORD("record"),
	RECORD_ADD("record_add"),
    RECORD_EDIT("record_edit"),
    RECORD_DELETE("record_delete"),
    LOCK_RECORD("lock_record"),
    PAGE_VIEW("page_view");

    public final String label;

    private RedCapApiLogType(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
