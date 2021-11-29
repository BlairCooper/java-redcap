package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiAction
	implements RedCapApiEnum
{
	IMPORT("import"),
	EXPORT("export"),
	DELETE("delete");

    public final String label;

    private RedCapApiAction(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
