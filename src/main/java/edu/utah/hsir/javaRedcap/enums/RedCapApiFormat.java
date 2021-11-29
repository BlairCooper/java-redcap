package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiFormat
	implements RedCapApiEnum
{
	CSV("csv"),
	FILE("file"),
	JSON("json"),
	ODM("odm"),
	XML("xml");

    public final String label;

    private RedCapApiFormat(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
