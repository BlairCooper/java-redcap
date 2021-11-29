package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiCsvDelimiter
	implements RedCapApiEnum
{
	COMMA(","),
	SEMICOLON(";"),
	TAB("tab"),
	PIPE("|"),
	CARAT("^");

	public final String label;

    private RedCapApiCsvDelimiter(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
