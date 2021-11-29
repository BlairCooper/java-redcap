/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utah.hsir.javaRedcap.enums.RedCapApiAction;
import edu.utah.hsir.javaRedcap.enums.RedCapApiContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiCsvDelimiter;
import edu.utah.hsir.javaRedcap.enums.RedCapApiDateFormat;
import edu.utah.hsir.javaRedcap.enums.RedCapApiDecimalCharacter;
import edu.utah.hsir.javaRedcap.enums.RedCapApiEnum;
import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.RedCapApiLogType;
import edu.utah.hsir.javaRedcap.enums.RedCapApiOverwriteBehavior;
import edu.utah.hsir.javaRedcap.enums.RedCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.RedCapApiReturnContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiType;

public class RedCapApiParams
	extends HashMap<String, Object>
{
	private static final long serialVersionUID = 793940389053153015L;

	public static final String ACTION = "action";
	public static final String ALL_RECORDS = "allRecords";
	public static final String ARM = "arm";
	public static final String ARMS = "arms";
	public static final String BEGIN_TIME = "beginTime";
	public static final String COMPACT_DISPLAY = "compactDisplay";
	public static final String CONTENT = "content";
	public static final String CSV_DELIMITER = "csvDelimiter";
	public static final String DAG = "dag";
	public static final String DAGS = "dags";
	public static final String DATA = "data";
	public static final String DATE_FORMAT = "dateFormat";
	public static final String DATE_RANGE_BEGIN = "dateRangeBegin";
	public static final String DATE_RANGE_END = "dateRangeEnd";
	public static final String DECIMAL_CHARACTER = "decimalCharacter";
	public static final String END_TIME = "endTime";
	public static final String EVENT = "event";
	public static final String EVENTS = "events";
	public static final String EXPORT_CHECKBOX_LABEL = "exportCheckboxLabel";
	public static final String EXPORT_DATA_ACCESS_GROUPS = "exportDataAccessGroups";
	public static final String EXPORT_FILES = "exportFiles";
	public static final String EXPORT_SURVEY_FIELDS = "exportSurveyFields";
	public static final String FIELD = "field";
	public static final String FIELDS = "fields";
	public static final String FILE = "file";
	public static final String FILTER_LOGIC = "filterLogic";
	public static final String FORCE_AUTO_NUMBER = "forceAutoNumber";
	public static final String FORMAT = "format";
	public static final String FORMS = "forms";
	public static final String INSTRUMENT = "instrument";
	public static final String LOGTYPE = "logtype";
	public static final String ODM = "odm";
	public static final String OVERRIDE = "override";
	public static final String OVERWRITE_BEHAVIOR = "overwriteBehavior";
	public static final String RAW_OR_LABEL = "rawOrLabel";
	public static final String RAW_OR_LABEL_HEADERS = "rawOrLabelHeaders";
	public static final String RECORD = "record";
	public static final String RECORDS = "records";
	public static final String REPEAT_INSTANCE = "repeat_instance";
	public static final String REPORT_ID = "report_id";
	public static final String RETURN_CONTENT = "returnContent";
	public static final String RETURN_FORMAT = "returnFormat";
	public static final String RETURN_METADATA_ONLY = "returnMetadataOnly";
	public static final String TOKEN = "token";
	public static final String TYPE = "type";
	public static final String USER = "user";

	protected ErrorHandlerInterface errorHandler;
	protected Set<RedCapApiFormat> legalFormats;

	
	protected RedCapApiParams(Map<String, Object> dataMap, ErrorHandlerInterface errorHandler) throws JavaRedcapException {
		super(dataMap);

		// set the error handler first in case one of the set methods wants to throw an exception.
		if (null == errorHandler) {
			errorHandler = new ErrorHandler();
		}
		this.errorHandler = errorHandler;
		
		legalFormats = new HashSet<>(Arrays.asList(
				RedCapApiFormat.CSV,
				RedCapApiFormat.JSON,
				RedCapApiFormat.XML
				));
	}

	public RedCapApiParams(Map<String, Object> dataMap) throws JavaRedcapException {
		this(dataMap, null);

		if (!dataMap.containsKey(TOKEN) ||
			!dataMap.containsKey(CONTENT) ||
			!dataMap.containsKey(FORMAT)
			)
		{
			errorHandler.throwException(
					"Map missing one or more of the required keys: '"+TOKEN+"', '"+CONTENT+"', '"+FORMAT+"'",
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
		}
		
		// Set default for error responses
		setReturnFormat(RedCapApiFormat.JSON);
	}

	public RedCapApiParams (String apiToken, RedCapApiContent content, ErrorHandlerInterface errorHandler) throws JavaRedcapException {
		this(new HashMap<String, Object>(), errorHandler);

		setToken(apiToken);
		setContent(content);

		// Set default format
		setFormat(RedCapApiFormat.JSON);

		// Set default for error responses
		setReturnFormat(RedCapApiFormat.JSON);
	}

	public void addFormat(RedCapApiFormat format) throws JavaRedcapException {
		if (null == format) {
			errorHandler.throwException("Format cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		}

		legalFormats.add(format);
	}

	public void setAction(RedCapApiAction action) throws JavaRedcapException {
		if (null == action) {
			errorHandler.throwException("Action cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		}

		put(ACTION, action);
	}

	public void setAllRecords (boolean allRecords) {
		if (allRecords) {
			put(ALL_RECORDS, allRecords);
		}
	}

	public void setArm(Integer arm) throws JavaRedcapException {
        if (null == arm) {
            ;  // That's OK, no arm will be used in the request
        }
        else if (arm < 0) {
        	errorHandler.throwException(
        			"Arm number '" + arm + "' is a negative integer.",
                    ErrorHandlerInterface.INVALID_ARGUMENT
                    );
        }
        else {
        	put(ARM, arm);
        }
	}

	public void setArms(Set<Integer> arms, boolean required) throws JavaRedcapException {
		if (null == arms) {
			if (required) {
				errorHandler.throwException(
						"The arms argument was not set.",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
			}
        }
		else {
            if (required && 0 == arms.size()) {
            	errorHandler.throwException(
            			"No arms were specified in the arms argument; at least one must be specified.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            }

		    for (Integer arm : arms) {
		    	if (null == arm) {
		    		errorHandler.throwException("Arm cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		    	}
		    	else if (arm < 0) {
		    		errorHandler.throwException(
		    				"Arm number '" + arm + "' is a negative integer.",
		    				ErrorHandlerInterface.INVALID_ARGUMENT
		    				);
		    	}
		    }

		    if (0 != arms.size()) {
		    	put(ARMS, arms);
		    }
        }
	}

	public void setBeginTime(String beginTime) throws JavaRedcapException {
		beginTime = checkDateRangeArgument(beginTime);

		// checkDateRangeArgument will only return the date if it's valid
		put(BEGIN_TIME, beginTime);
	}

	public void setEndTime(String endTime) throws JavaRedcapException {
		endTime = checkDateRangeArgument(endTime);

		// checkDateRangeArgument will only return the date if it's valid
		put(END_TIME, endTime);
	}

	public void setCompactDisplay(boolean compactDisplay) {
		if (compactDisplay) {
			put(COMPACT_DISPLAY, compactDisplay);
		}
	}
	
	public void setContent(RedCapApiContent content) throws JavaRedcapException {
		if (null == content) {
			errorHandler.throwException("Content cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		}

		put(CONTENT, content);
	}

	public void setCsvDelimiter(RedCapApiCsvDelimiter delimiter) throws JavaRedcapException {
		if (null == delimiter) {
			delimiter = RedCapApiCsvDelimiter.COMMA;
		}

        put(CSV_DELIMITER, delimiter);
	}

	public void setDag(String dag) {
		if (null != dag && 0 != dag.trim().length()) {
			put(DAG, dag.trim());
		}
	}

	public void setDags(Set<String> dags, boolean required) throws JavaRedcapException {
		if (null == dags) {
			if (required) {
				errorHandler.throwException(
						"The dags argument was not set.",
	                    ErrorHandlerInterface.INVALID_ARGUMENT
	                    );
			}
		}
		else {
			if (required && 0 == dags.size()) {
				errorHandler.throwException(
						"No dags were specified in the dags argument; at least one must be specified.",
	                    ErrorHandlerInterface.INVALID_ARGUMENT
	                    );
			}

		    for (String dag : dags) {
		    	if (null == dag || 0 == dag.trim().length()) {
		    		errorHandler.throwException(
		    				"Dag is null or blank.",
		                    ErrorHandlerInterface.INVALID_ARGUMENT
		                );
		    	}
		    }

		    if (0 != dags.size()) {
		    	put(DAGS, dags);
		    }
		}
	}

	public void setData (Object data) throws JavaRedcapException {
		if (null == data) {
			errorHandler.throwException("Data cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		}

		put(DATA, data);
	}

	public void setDateFormat(RedCapApiDateFormat dateFormat) throws JavaRedcapException {
		if (null == dateFormat) {
            dateFormat = RedCapApiDateFormat.YMD;
        }

		put(DATE_FORMAT, dateFormat);
	}

	public void setDateRangeBegin(String dateRange) throws JavaRedcapException {
		dateRange = checkDateRangeArgument(dateRange);
		if (null != dateRange) {
			put(DATE_RANGE_BEGIN, dateRange);
		}
	}

	public void setDateRangeEnd(String dateRange) throws JavaRedcapException {
		dateRange = checkDateRangeArgument(dateRange);
		if (null != dateRange) {
			put(DATE_RANGE_END, dateRange);
		}
	}

	public void setDecimalCharacter(RedCapApiDecimalCharacter decimalChar) throws JavaRedcapException {
		if (null == decimalChar) {
			errorHandler.throwException("Decimal character cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		}
		
		put(DECIMAL_CHARACTER, decimalChar);
	}

	public void setEvent(String event) {
		if (null != event && 0 != event.trim().length()) {
			put (EVENT, event.trim());
		}
	}

	public void setEvents(Set<String> events, boolean required) throws JavaRedcapException {
		if (null == events) {
			if (required) {
				errorHandler.throwException(
						"The events argument was not set.",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
			}
        }
		else if (required && 0 == events.size()) {
        	errorHandler.throwException(
        			"No events were specified in the events argument; at least one must be specified.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }
		else {
    		for (String event : events) {
    			if (null == event || 0 == event.trim().length()) {
    				errorHandler.throwException(
    						"Blank or null event specified.",
    						ErrorHandlerInterface.INVALID_ARGUMENT
    						);
    			}
            }

    		if (0 != events.size()) {
    			put(EVENTS, events);
    		}
	    }
	}

	public void setExportCheckboxLabel(boolean exportCheckboxLabel) {
		put(EXPORT_CHECKBOX_LABEL, exportCheckboxLabel);
	}

	public void setExportDataAccessGroups(boolean exportDAGs) {
		put(EXPORT_DATA_ACCESS_GROUPS, exportDAGs);
	}

	public void setExportFiles(boolean exportFiles) {
    	put(EXPORT_FILES, exportFiles);
	}

	public void setExportSurveryFields(boolean exportSurveyFields) {
		put(EXPORT_SURVEY_FIELDS, exportSurveyFields);
	}

	public void setField(String field, boolean required) throws JavaRedcapException {
		if (null == field || 0 == field.trim().length()) {
			if (required) {
				errorHandler.throwException(
						"Field cannot be null or blank.",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
			}
			// else OK
		}
		else {
			put(FIELD, field.trim());
		}
	}

	public void setFields(Set<String> fields) throws JavaRedcapException {
		if (null != fields && 0 != fields.size()) {
    		for (String field : fields) {
    			if (null == field || 0 == field.trim().length()) {
    				errorHandler.throwException(
    						"Blank or null field specified.",
    						ErrorHandlerInterface.INVALID_ARGUMENT
    						);
    			}
            }

    		if (0 != fields.size()) {
    			put(FIELDS, fields);
    		}
		}
	}

	public void setFile(String filename) throws JavaRedcapException {
		if (null == filename || 0 == filename.trim().length()) {
			errorHandler.throwException(
					"No filename specified.",
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
		}
		else if (!Files.exists(Path.of(filename))) {
			errorHandler.throwException(
					"The input file '" + filename + "' could not be found.",
					ErrorHandlerInterface.INPUT_FILE_NOT_FOUND
					);
        }
		else if (!Files.isReadable(Path.of(filename))) {
        	errorHandler.throwException(
        			"The input file '" + filename + "' was unreadable.",
        			ErrorHandlerInterface.INPUT_FILE_UNREADABLE
        			);
        }
		else {
	        put(FILE, Paths.get(filename));
		}
	}

	public void setFilterLogic(String filterLogic) {
		if (null != filterLogic && 0 < filterLogic.trim().length()) {
			put(FILTER_LOGIC, filterLogic.trim());
		}
	}

	public void setForceAutoNumber(boolean forceAutoNumber) {
		put(FORCE_AUTO_NUMBER, forceAutoNumber);
	}

	public void setFormat(RedCapApiFormat format) throws JavaRedcapException {
		if (null == format) {
			format = RedCapApiFormat.JSON;
		}

		if (!legalFormats.contains(format)) {
			errorHandler.throwException(
					"Invalid format '" + format.label + "' specified. " +
					"The format should be one of the following: " + Arrays.toString(legalFormats.toArray()),
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
        }

		put(FORMAT, format);
	}

	public void setForms(Set<String> forms) throws JavaRedcapException {
		if (null != forms && 0 != forms.size()) {
    		for (String form : forms) {
    			if (null == form || 0 == form.trim().length()) {
    				errorHandler.throwException(
    						"Blank or null form specified.",
    						ErrorHandlerInterface.INVALID_ARGUMENT
    						);
    			}
            }

			put(FORMS, forms);
		}
	}

	public void setInstrument (String instrument, boolean required) throws JavaRedcapException {
		if (null == instrument || 0 == instrument.trim().length()) {
			if (required) {
				errorHandler.throwException(
						"The form argument was null or blank.",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
			}
		}
		else {
			put(INSTRUMENT, instrument);
	    }
	}

	public void setLogType (RedCapApiLogType logType) throws JavaRedcapException {
		if (null == logType) {
			errorHandler.throwException(
					"Log type cannot be null.",
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
		}

		put(LOGTYPE, logType);
	}

	public void setOdm(String odm) {
        if (null != odm && 0 != odm.trim().length()) {
        	put(ODM, odm.trim());
        }
	}

	public void setOverride(boolean override) {
        put(OVERRIDE, override ? 1 : 0);
	}

	public void setOverwriteBehavior(RedCapApiOverwriteBehavior overwriteBehavior) {
		if (null == overwriteBehavior) {
			overwriteBehavior = RedCapApiOverwriteBehavior.NORMAL;
        }

		put(OVERWRITE_BEHAVIOR, overwriteBehavior);
	}

	public void setRawOrLabel(RedCapApiRawOrLabel rawOrLabel) {
		if (null == rawOrLabel) {
			rawOrLabel = RedCapApiRawOrLabel.RAW;
		}

		put(RAW_OR_LABEL, rawOrLabel);
	}

	public void setRawOrLabelHeaders(RedCapApiRawOrLabel rawOrLabel) {
		if (null == rawOrLabel) {
			rawOrLabel = RedCapApiRawOrLabel.RAW;
		}

		put(RAW_OR_LABEL_HEADERS, rawOrLabel);
	}

	public void setRecord(String recordId, boolean required) throws JavaRedcapException {
		if (null == recordId || 0 == recordId.trim().length()) {
			if (required) {
				errorHandler.throwException(
						"No record ID specified.",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
            }
	    }
		else {
			put (RECORD, recordId.trim());
		}
	}

	public void setRecords(Set<String> recordIds) throws JavaRedcapException {
		if (null != recordIds && 0 != recordIds.size()) {
			for (String rcdId : recordIds) {
				if (null == rcdId || 0 == rcdId.trim().length()) {
					errorHandler.throwException(
							"Record ids cannot be null or blank",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
				}
			}
			
			put(RECORDS, recordIds);
		}
	}

	public void setRepeatInstance(Integer repeatInstance) throws JavaRedcapException {
		if (null != repeatInstance) {
			if (repeatInstance < 0) {
				errorHandler.throwException(
						"Repeat Instance cannot be negative",
						ErrorHandlerInterface.INVALID_ARGUMENT
						);
			}

			put(REPEAT_INSTANCE, repeatInstance);
		}
	}

	public void setReportId(Integer reportId) throws JavaRedcapException {
		if (null == reportId) {
			errorHandler.throwException(
					"Null report ID specified for export.",
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
        }

		if (reportId < 0) {
			errorHandler.throwException(
					"Report ID '" + reportId + "' is a negative integer.",
					ErrorHandlerInterface.INVALID_ARGUMENT
					);
		}

		put(REPORT_ID, reportId);

	}

	public void setReturnContent(RedCapApiReturnContent returnContent, boolean forceAutoNumber) throws JavaRedcapException {
		if (null == returnContent) {
			returnContent = RedCapApiReturnContent.COUNT;
        }
		else if (RedCapApiReturnContent.AUTO_IDS.equals(returnContent)) {
            if (!forceAutoNumber) {
            	errorHandler.throwException(
            			"'auto_ids' specified for returnContent, but forceAutoNumber was not set to true; " +
            			"'auto_ids' can only be used when forceAutoNumber is set to true.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            }
        }

		put(RETURN_CONTENT, returnContent);
	}

	protected void setReturnFormat (RedCapApiFormat format) {
		if (null == format) {
			format = RedCapApiFormat.JSON;
		}

		put(RETURN_FORMAT, format);
	}

	public void setReturnMetadataOnly(boolean metadataOnly) {
		put(RETURN_METADATA_ONLY, metadataOnly);
	}
	
	public void setToken(String token) throws JavaRedcapException {
		if (null != token) {
			put(TOKEN, token);
		}
	}

	public void setType(RedCapApiType type) {
		if (null == type) {
			type = RedCapApiType.FLAT;
		}

		put(TYPE, type);
	}

	public void setUser(String user) {
		if (null != user && user.trim().length() > 0) {
			put(USER, user);
		}
	}

	protected String checkDateRangeArgument(String date) throws JavaRedcapException
    {
		if (null == date || 0 == date.trim().length()) {
			errorHandler.throwException("Missing or blank date", ErrorHandlerInterface.INVALID_ARGUMENT);
		}
		else {
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sdf.setLenient(false);
			try {
				sdf.parse(date);
				// TODO: Check if resulting Date, when formatted, matches the input date?
				//		-- if (!($dt && $dt->format($legalFormat) == $date)) {
			} catch (ParseException e) {
				errorHandler.throwException(
						"Invalid date format. " +
						"The date format for export dates is YYYY-MM-DD HH:MM:SS, " +
						"e.g., 2020-01-31 00:00:00.",
						ErrorHandlerInterface.INVALID_ARGUMENT,
						e
						);
			}
        }

        return date;
    }

	public String getMediaType() {
		String mediaType = null;

		switch ((RedCapApiFormat)get(FORMAT)) {
			case JSON:	mediaType = "application/json";	break;
			case CSV:	mediaType = "text/csv";			break;
			case XML:
			case ODM:	mediaType = "application/xml";	break;
			default:	mediaType = "text/plain";		break;
		}
		
		return mediaType;
	}
	
	public String toFormData() {
    	StringBuilder builder = new StringBuilder();

    	for (Entry<String, Object> entry : entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            
            String field = entry.getKey().toString();
            Object value = entry.getValue();
            
            if (value instanceof RedCapApiEnum) {
            	builder.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
                builder.append("=");
                builder.append(URLEncoder.encode(((RedCapApiEnum)value).getLabel(), StandardCharsets.UTF_8));
            } else if (value instanceof String) {
            	builder.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
                builder.append("=");
                builder.append(URLEncoder.encode((String)value, StandardCharsets.UTF_8));
            }
            else if (value instanceof Set<?>) {
            	StringBuilder setBuilder = new StringBuilder();

            	field = field + "[]";
            	for (Object obj : ((Set<?>)value)) {
            		if (obj instanceof Integer) {
            			obj = ((Integer)obj).toString();
            		}

                    if (setBuilder.length() > 0) {
                        setBuilder.append("&");
                    }

            		setBuilder.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
                    setBuilder.append("=");
                    setBuilder.append(URLEncoder.encode((String)obj, StandardCharsets.UTF_8));
            	}

            	builder.append(setBuilder);
            }
        }

    	return builder.toString();
	}
}
