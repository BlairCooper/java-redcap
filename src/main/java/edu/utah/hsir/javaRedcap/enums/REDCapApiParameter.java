package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of the parameters that can be used with the REDCap API.
 *
 */
public enum REDCapApiParameter
	implements REDCapApiEnum
{
	/** Action parameter */				ACTION("action"),
	/** All Records parameter */		ALL_RECORDS("allRecords"),
	/** Arm parameter */				ARM("arm"),
	/** Arms parameter */				ARMS("arms"),
	/** Begin Time parameter */			BEGIN_TIME("beginTime"),
	/** Compact Display parameter */	COMPACT_DISPLAY("compactDisplay"),
	/** Content parameter */			CONTENT("content"),
	/** CSV Delimiter parameter */		CSV_DELIMITER("csvDelimiter"),
	/** DAG parameter */				DAG("dag"),
	/** DAGs parameter */				DAGS("dags"),
	/** Data parameter */				DATA("data"),
	/** Date Format parameter */		DATE_FORMAT("dateFormat"),
	/** Date Range Begin parameter */	DATE_RANGE_BEGIN("dateRangeBegin"),
	/** Date Range End parameter */		DATE_RANGE_END("dateRangeEnd"),
	/** Decimal Characters parameter */	DECIMAL_CHARACTER("decimalCharacter"),
	/** End Time parameter */			END_TIME("endTime"),
	/** Event parameter */				EVENT("event"),
	/** Events parameter */				EVENTS("events"),
	/** Export Checkbox Label parameter */	EXPORT_CHECKBOX_LABEL("exportCheckboxLabel"),
	/** Export Data Access Groups parameter */	EXPORT_DATA_ACCESS_GROUPS("exportDataAccessGroups"),
	/** Export Files parameter */		EXPORT_FILES("exportFiles"),
	/** Export Survey Fields parameter */	EXPORT_SURVEY_FIELDS("exportSurveyFields"),
	/** Field parameter */				FIELD("field"),
	/** Fields parameter */				FIELDS("fields"),
	/** File parameter */				FILE("file"),
	/** Filter Logic parameter */		FILTER_LOGIC("filterLogic"),
	/** Force Auto Number parameter */	FORCE_AUTO_NUMBER("forceAutoNumber"),
	/** Format parameter */				FORMAT("format"),
	/** Forms parameter */				FORMS("forms"),
	/** Instrument parameter */			INSTRUMENT("instrument"),
	/** Log Type parameter */			LOGTYPE("logtype"),
	/** ODM parameter */				ODM("odm"),
	/** Override parameter */			OVERRIDE("override"),
	/** Overwrite Behavior parameter */	OVERWRITE_BEHAVIOR("overwriteBehavior"),
	/** Raw Or Label parameter */		RAW_OR_LABEL("rawOrLabel"),
	/** Raw Or Label Headers parameter */	RAW_OR_LABEL_HEADERS("rawOrLabelHeaders"),
	/** Record parameter */				RECORD("record"),
	/** Records parameter */			RECORDS("records"),
	/** Repeat Instance parameter */	REPEAT_INSTANCE("repeat_instance"),
	/** Record ID parameter */			REPORT_ID("report_id"),
	/** Return Content parameter */		RETURN_CONTENT("returnContent"),
	/** Return Format parameter */		RETURN_FORMAT("returnFormat"),
	/** Return Metadata Only parameter */	RETURN_METADATA_ONLY("returnMetadataOnly"),
	/** Token parameter */				TOKEN("token"),
	/** Type parameter */				TYPE("type"),
	/** User parameter */				USER("user");

    private final String label;

    private REDCapApiParameter(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
