package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiRawOrLabel
	implements RedCapApiEnum
{
	RAW("raw"),
	LABEL("label");

    public final String label;

    private RedCapApiRawOrLabel(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
