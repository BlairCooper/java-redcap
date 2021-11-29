package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiDecimalCharacter
	implements RedCapApiEnum
{
	COMMA(","),
	PERIOD(".");

	public final String label;

    private RedCapApiDecimalCharacter(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
