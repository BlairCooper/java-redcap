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
import java.util.Map.Entry;
import java.util.Set;

import edu.utah.hsir.javaRedcap.enums.REDCapApiAction;
import edu.utah.hsir.javaRedcap.enums.REDCapApiContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiCsvDelimiter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDateFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDecimalCharacter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiEnum;
import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiLogType;
import edu.utah.hsir.javaRedcap.enums.REDCapApiOverwriteBehavior;
import edu.utah.hsir.javaRedcap.enums.REDCapApiParameter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.REDCapApiReturnContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiType;

/**
 * Represents the set of parameters to be sent to the REDCap server for a
 * request. 
 */
public class REDCapApiRequest
{
    protected Map<REDCapApiParameter, Object>paramMap = new HashMap<>();

    protected ErrorHandlerInterface errorHandler;
    protected Set<REDCapApiFormat> legalFormats;


    /**
     * Initializes an instance of the class. 
     * 
     * @param errorHandler An Error Handler to use in handling errors. If
     *         null, the default {@link ErrorHandler} will be used.
     */
    private void initializeInstance(ErrorHandlerInterface errorHandler)
    {
        // set the error handler first in case one of the set methods wants to throw an exception.
        if (null == errorHandler) {
            errorHandler = new ErrorHandler();
        }
        this.errorHandler = errorHandler;
        
        legalFormats = new HashSet<>(Arrays.asList(
                REDCapApiFormat.CSV,
                REDCapApiFormat.JSON,
                REDCapApiFormat.XML
                ));
    }

    /**
     * Constructs an instance using the values in the dataMap to initialize
     * parameters for the request. Any entries in the dataMap that are not
     * valid REDCap API parameters will be ignored.
     * <br><b>NOTE:</b> No validation is performed on the 
     * Constructs a new instance using parameters provided in a map.
     * 
     * @param dataMap A map of parameters to initialize the instance with.<br>
     *         {@link REDCapApiParameter#TOKEN},
     *         {@link REDCapApiParameter#CONTENT} and 
     *         {@link REDCapApiParameter#FORMAT} are required. 
     * 
     * @throws JavaREDCapException Thrown if one of the required parameters
     *         is missing.
     */
    public REDCapApiRequest(Map<REDCapApiParameter, Object> dataMap) throws JavaREDCapException {
    	initializeInstance(null);

        if (!dataMap.containsKey(REDCapApiParameter.TOKEN) ||
            !dataMap.containsKey(REDCapApiParameter.CONTENT) ||
            !dataMap.containsKey(REDCapApiParameter.FORMAT)
            )
        {
            errorHandler.throwException(
                    "Map missing one or more of the required keys: " +
                    "REDCapApiParameter.TOKEN, " +
                    "REDCapApiParameter.CONTENT, or " +
                    "REDCapApiParameter.FORMAT",
                    ErrorHandlerInterface.INVALID_ARGUMENT
                    );
        }
        
        // Set default for error responses
        setReturnFormat(REDCapApiFormat.JSON);
        
        paramMap.putAll(dataMap);
    }

    /**
     * Constructs a new instance using parameters provided.
     * 
     * @param apiToken A REDCap API Token
     * @param content The content field for the request
     * @param errorHandler An Error Handler. If null the default
     *         {@link ErrorHandler} will be used.
     * 
     * @throws JavaREDCapException Thrown if any of the input parameters are
     *         invalid.
     */
    public REDCapApiRequest (String apiToken, REDCapApiContent content, ErrorHandlerInterface errorHandler) throws JavaREDCapException {
    	initializeInstance(errorHandler);

        setToken(apiToken);
        setContent(content);

        // Set default format
        setFormat(REDCapApiFormat.JSON);

        // Set default for error responses
        setReturnFormat(REDCapApiFormat.JSON);
    }

    /**
     * Adds a format to the list of valid formats for the request. Initially
     * only {@link REDCapApiFormat#CSV}, {@link REDCapApiFormat#JSON} and
     * {@link REDCapApiFormat#XML} are set as valid format.
     * 
     * @param format The format to add to the list of valid format for the
     * 		request.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void addFormat(REDCapApiFormat format) throws JavaREDCapException {
        if (null == format) {
            errorHandler.throwException("Format cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
        }

        legalFormats.add(format);
    }

    /**
     * Sets the action type of the request. Used in conjunction with the
     * content to define the type of request being made to the REDCap server.
     * 
     * @param action The action type for the request.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setAction(REDCapApiAction action) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.ACTION, action);
    }

    /**
     * Sets whether all records should be included when exporting a PDF file
     * of the instruments.
     *   
     * @param allRecords Indicator whether to included all records in the
     * 		PDF. If set to true, all forms for all records will be
     * 		retrieved.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setAllRecords (boolean allRecords) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.ALL_RECORDS, allRecords);
    }

    /**
     * Sets the arm for a record to delete.
     * 
     * @param arm An arm number for a record being deleted.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setArm(Integer arm) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.ARM, arm);
    }

    /**
     * Sets the Arms to use when export or deleting arms, exporting events or
     * exporting instrument/event mappings.
     * 
     * @param arms A set of arm numbers to work with.
     * @param required A flag to indicate whether the list of Arms is required. 
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setArms(Set<Integer> arms, boolean required) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.ARMS, arms, required);
    }

    /**
     * Sets the start date/time for a period when exporting logging data. The
     * date must be in the format of YYYY-MM-DD HH:MM:SS 
     * (e.g. 2020-01-31 00:00:00)
     * 
     * @param beginTime The start date/time of a logging period.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setBeginTime(String beginTime) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.BEGIN_TIME, beginTime);
    }

    /**
     * Sets the end date/time for a period when exporting logging data. The
     * date must be in the format of YYYY-MM-DD HH:MM:SS 
     * (e.g. 2020-01-31 00:00:00)
     * 
     * @param endTime The end date/time of a logging period.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setEndTime(String endTime) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.END_TIME, endTime);
    }

    /**
     * Sets a flag indicating whether the fields should be compacted when
     * exporting a PDF file of an instrument.
     *  
     * @param compactDisplay Whether the PDF contents should be compacted.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setCompactDisplay(boolean compactDisplay) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.COMPACT_DISPLAY, compactDisplay);
    }
    
    /**
     * Sets the content type for the request. Used in conjunction with the
     * action to define the type of request being made to the REDCap server.
     * 
     * @param content The content type for the request.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setContent(REDCapApiContent content) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.CONTENT, content);
    }

    /**
     * Sets the CSV delimiter to use when exporting records or report, or importing records.
     * 
     * @param delimiter The CSV delimiter.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setCsvDelimiter(REDCapApiCsvDelimiter delimiter) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.CSV_DELIMITER, delimiter);
    }

    /**
     * Sets the Data Access Group (DAG) to use when exporting logging data.
     * 
     * @param dag A Data Access Group
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDag(String dag) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DAG, dag);
    }

    /**
     * Sets the Data Access Groups (DAGs) to be deleted.
     * 
     * @param dags A set of DAGs to be deleted.
     * @param required A flag to indicate whether the list of DAGs is required. 
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDags(Set<String> dags, boolean required) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DAGS, dags, required);
    }

    /**
     * Sets the data to be sent in the request. The data should be in the
     * format specified in the call to {@link #setFormat(REDCapApiFormat)}
     * or the default format which is JSON.
     * 
     * @param data The data for the request.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setData (Object data) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DATA, data);
    }

    /**
     * Sets the date format for records being imported.
     * 
     * @param dateFormat The date format for the import records.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDateFormat(REDCapApiDateFormat dateFormat) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DATE_FORMAT, dateFormat);
    }

    /**
     * Sets the beginning of a date range. The date must be in the format of
     * YYYY-MM-DD HH:MM:SS (e.g. 2020-01-31 00:00:00)
     *  
     * @param dateRange A date specifying the beginning of the range.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDateRangeBegin(String dateRange) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DATE_RANGE_BEGIN, dateRange);
    }

    /**
     * Sets the end of a date range. The date must be in the format of
     * YYYY-MM-DD HH:MM:SS (e.g. 2020-01-31 00:00:00)
     *  
     * @param dateRange A date specifying the end of the range.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDateRangeEnd(String dateRange) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DATE_RANGE_END, dateRange);
    }

    /**
     * Set the decimal character to use as a separator in numeric values when
     * exporting records or reports.
     * 
     * @param decimalChar The decimal character separator to use.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setDecimalCharacter(REDCapApiDecimalCharacter decimalChar) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.DECIMAL_CHARACTER, decimalChar);
    }

    /**
     * Set the event for a request.
     * 
     * @param event An event name.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setEvent(String event) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.EVENT, event);
    }

    /**
     * Sets the list of events to include when exporting records or project
     * XML, or deleting Events.
     * 
     * @param events A set of event names. If null or the set is empty all
     * 			events will be effective. Any event names included in the set
     * 			must be a non-null, not empty value.
     * @param required Flag indicating whether the event names are required
     * 			for the request.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setEvents(Set<String> events, boolean required) throws JavaREDCapException {
    	addParameter(REDCapApiParameter.EVENTS, events, required);
    }

    /**
     * Sets indicator for the format for exporting checkbox fields for the
     * case where format is {@link REDCapApiFormat#CSV}, type is
     * {@link REDCapApiType#FLAT} and RawOrLabel is
     * {@link REDCapApiRawOrLabel#RAW}. For other cases this parameter is
     * effectively ignored.
     * 
     * @param exportCheckboxLabel
     *        <ul>
     *        <li>if true - checked checkboxes will have a value equal to the
     *          checkbox option's label (e.g., 'Choice 1'), and unchecked
     *          checkboxes will have a blank value.
     *      </li>
     *      <li>if false - [default] checked checkboxes will have a value of
     *          'Checked', and unchecked checkboxes will have a value of
     *          'Unchecked'.
     *      </li>
     *      </ul>
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setExportCheckboxLabel(boolean exportCheckboxLabel) throws JavaREDCapException {
        addParameter(REDCapApiParameter.EXPORT_CHECKBOX_LABEL, exportCheckboxLabel);
    }

    /**
     * Sets whether to include Data Access Groups (DAGs) when exporting
     * records or project XML.
     * 
     * @param exportDAGs Whether to include DAGs in the export data.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setExportDataAccessGroups(boolean exportDAGs) throws JavaREDCapException {
        addParameter(REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS, exportDAGs);
    }

    /**
     * Sets whether to include file data when exporting project XML.
     *  
     * @param exportFiles Whether to include the data for file fields.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setExportFiles(boolean exportFiles) throws JavaREDCapException {
        addParameter(REDCapApiParameter.EXPORT_FILES, exportFiles);
    }

    /**
     * Sets indicator as to whether the survey fields should be included when
     * exporting records or project XML.
     *  
     * @param exportSurveyFields Whether to export the survey fields.
     *  <ul>
     *       <li>if true - export the following survey fields:
     *         <ul>
     *           <li> survey identifier field ('redcap_survey_identifier') </li>
     *           <li> survey timestamp fields (instrument+'_timestamp') </li>
     *         </ul>
     *       </li>
     *       <li>if false - [default] survey fields are not exported.</li>
     *     </ul>
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setExportSurveryFields(boolean exportSurveyFields) throws JavaREDCapException {
        addParameter(REDCapApiParameter.EXPORT_SURVEY_FIELDS, exportSurveyFields);
    }

    /**
     * Sets the field name to use when exporting field names, or importing,
     * exporting or delete files for a record.
     * 
     * @param field The name of the field to reference.
     * @param required Whether the field name is required, or not. If the
     *         field is required but not provided an exception will be thrown.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setField(String field, boolean required) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FIELD, field, required);
    }

    /**
     * Sets the fields to include when exporting metadata, records or project
     * XML.
     * 
     * @param fields A set for field names.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setFields(Set<String> fields) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FIELDS, fields);
    }

    /**
     * Sets the file name to use when importing a file into a record.
     * 
     * @param filename The name of the file to import.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setFile(String filename) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FILE, filename);
    }

    /**
     * Set the logic for filtering records when exporting records or
     * project XML.
     * 
     * @param filterLogic The filter logic.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setFilterLogic(String filterLogic) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FILTER_LOGIC, filterLogic);
    }

    /**
     * Sets whether to force auto number of record ids when importing
     * records.
     * 
     * @param forceAutoNumber Indicate whether to force auto numbering.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setForceAutoNumber(boolean forceAutoNumber) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FORCE_AUTO_NUMBER, forceAutoNumber);
    }

    /**
     * Sets the format of data sent in requests to REDCap.
     * 
     * @param format The format of in flowing data. If null,
     *         {@link REDCapApiFormat#JSON} is used. If the format is provided
     *         but not in the list of allowed formats an exception will be
     *         thrown. Additional formats can be added via the
     *         {@link #addFormat(REDCapApiFormat)} method.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setFormat(REDCapApiFormat format) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FORMAT, format);
    }

    /**
     * Sets the forms/instruments to include when exporting records or
     * metadata.
     * 
     * @param forms A set of forms to be included. If null or an empty set
     *         all forms will be included.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setForms(Set<String> forms) throws JavaREDCapException {
        addParameter(REDCapApiParameter.FORMS, forms);
    }

    /**
     * Sets the instrument to reference in fetching survey links,
     * participants or return code, or when exporting a PDF file of an
     * instrument.
     * 
     * @param instrument An instrument name 
     * @param required Indication if the instrument name is required for the
     *         request. If the instrument is required but not provided an
     *         exception will be thrown.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setInstrument (String instrument, boolean required) throws JavaREDCapException {
        addParameter(REDCapApiParameter.INSTRUMENT, instrument, required);
    }

    /**
     * Sets the log type to include when exporting logging.
     *  
     * @param logType The log type to export. If null, all log types will be
     *         exported.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setLogType (REDCapApiLogType logType) throws JavaREDCapException {
        addParameter(REDCapApiParameter.LOGTYPE, logType);
    }

    /**
     * Sets the ODM data representing the definition of a REDCap project.
     * Only applicable when creating a REDCap project which is only possible
     * if a Super API token is available.
     * 
     * @param odm A string containing the ODM representation of a project.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setOdm(String odm) throws JavaREDCapException {
        addParameter(REDCapApiParameter.ODM, odm);
    }

    /**
     * Sets whether existing arms or events should be overridden during an
     * import.
     * 
     * @param override Indication of whether existing arms or events should
     *         be overridden.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setOverride(boolean override) throws JavaREDCapException {
        addParameter(REDCapApiParameter.OVERRIDE, override);
    }

    /**
     * Sets the overwrite behavior when importing records.
     * 
     * @param overwriteBehavior Indication whether to overwrite existing
     *         records or not. Defaults to
     *         {@link REDCapApiOverwriteBehavior#NORMAL} if null is passed.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setOverwriteBehavior(REDCapApiOverwriteBehavior overwriteBehavior) throws JavaREDCapException {
        addParameter(REDCapApiParameter.OVERWRITE_BEHAVIOR, overwriteBehavior);
    }

    /**
     * Set what should be exported for options of multiple choice fields.
     * 
     * @param rawOrLabel Indicates whether the raw value or the label should
     *         be used. Defaults to {@link REDCapApiRawOrLabel#RAW} if null is
     *         passed. 
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setRawOrLabel(REDCapApiRawOrLabel rawOrLabel) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RAW_OR_LABEL, rawOrLabel);
    }

    /**
     * When exporting records in CSV/FLAT format sets what format should be
     * used for the CSV headers.
     *  
     * @param rawOrLabel Indicates whether the variable/field name should be
     *         used or the field label. Defaults to
     *         {@link REDCapApiRawOrLabel#RAW} if null is passed. 
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setRawOrLabelHeaders(REDCapApiRawOrLabel rawOrLabel) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RAW_OR_LABEL_HEADERS, rawOrLabel);
    }

    /**
     * Sets the record id to be used.
     * 
     * @param recordId The record id to use. If the record id is null or
     *         blank and is required an exception will be thrown. 
     * @param required Flag indicating whether the record id is required for
     *         the request.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setRecord(String recordId, boolean required) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RECORD, recordId, required);
    }

    /**
     * Sets the record ids to be used when exporting or deleting records.
     * 
     * @param recordIds A set of record ids. If null or the set is empty all
     *         records will be effective. Any record ids included in the set
     *         must be a non-null, not empty value.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setRecords(Set<String> recordIds) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RECORDS, recordIds);
    }

    /**
     * Sets the instance number for a repeating form. Used when:
     * <ul>
     * <li>importing, exporting or deleting a file on a repeating form</li>
     * <li>exporting the survey link or return code for a repeating form</li>
     * </ul>
     * 
     * @param repeatInstance The instance number of a repeating form. If null
     *         the parameter will not be set for the request.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setRepeatInstance(Integer repeatInstance) throws JavaREDCapException {
        addParameter(REDCapApiParameter.REPEAT_INSTANCE, repeatInstance);
    }

    /**
     * Sets the report id to be used when exporting reports.
     * 
     * @param reportId A report id.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setReportId(Integer reportId) throws JavaREDCapException {
        addParameter(REDCapApiParameter.REPORT_ID, reportId);
    }

    /**
     * Sets the format for the content returned when importing records.
     *  
     * @param returnContent The format for the returned content.
     * @param forceAutoNumber Whether to force auto numbering of the imported
     *         records.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setReturnContent(REDCapApiReturnContent returnContent, boolean forceAutoNumber) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RETURN_CONTENT, returnContent, forceAutoNumber);
    }

    /**
     * Sets the format for error responses by the REDCap server.
     * 
     * @param format The format to be used for error responses.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    protected void setReturnFormat (REDCapApiFormat format) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RETURN_FORMAT, format);
    }

    /**
     * Sets whether to only return metadata when exporting records as XML.
     * 
     * @param metadataOnly If true only metadata will be returned in the XML.
     *         Otherwise all data will be returned.
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setReturnMetadataOnly(boolean metadataOnly) throws JavaREDCapException {
        addParameter(REDCapApiParameter.RETURN_METADATA_ONLY, metadataOnly);
    }
    
    /**
     * Sets the API Token for the request.
     * 
     * @param token The API Token to send
     *  
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setToken(String token) throws JavaREDCapException {
        addParameter(REDCapApiParameter.TOKEN, token);
    }

    /**
     * Sets the format type for importing or exporting records.
     * 
     * @param type The format type.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setType(REDCapApiType type) throws JavaREDCapException {
        addParameter(REDCapApiParameter.TYPE, type);
    }

    /**
     * The user to be sent in the request.
     *  
     * @param user A user name. If null or blank the parameter will not be sent.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the parameter.
     */
    public void setUser(String user) throws JavaREDCapException {
        addParameter(REDCapApiParameter.USER, user);
    }

    protected String checkDateRangeArgument(String date) throws JavaREDCapException
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
                //        -- if (!($dt && $dt->format($legalFormat) == $date)) {
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

    /**
     * Adds a parameter value to the request.<p>
     * <b>Note:</b> As this method can only be called by this class or one
     * extending it the value parameter is assumed to be of the appropriate
     * type for the RedCapApiParamter. Callers should ensure this or risk a
     * {@link ClassCastException}.
     *  
     * @param param The parameter being added.
     * @param value The value for the parameter, of an appropriate type. 
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the
     *         parameter or it's value.
     */
    protected void addParameter(REDCapApiParameter param, Object value)
        throws JavaREDCapException
    {
        addParameter(param, value, false);
    }

    /**
     * Adds a parameter value to the request.<p>
     * <b>Note:</b> As this method can only be called by this class or one
     * extending it the value parameter is assumed to be of the appropriate
     * type for the RedCapApiParamter. Callers should ensure this or risk a
     * {@link ClassCastException}.
     *  
     * @param param The parameter being added.
     * @param value The value for the parameter, of an appropriate type. 
     * @param flag A flag which may mean something for validating the value.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the
     *         parameter or it's value.
     */
    protected void addParameter(REDCapApiParameter param, Object value, boolean flag)
        throws JavaREDCapException
    {
        if (null == param) {
            errorHandler.throwException("A REDCapApiParameter must be specified", ErrorHandlerInterface.INVALID_ARGUMENT);
        }

        // perform validation
        switch (param) {
        	case ACTION:
		        if (null == value) {
		            errorHandler.throwException("Action cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		        }
		
		        paramMap.put(param, value);
	        	break;

        	case ALL_RECORDS:
		        if ((Boolean)value) {
		        	paramMap.put(param, true);
		        }
		        break;

        	case ARM: {
        		Integer arm = (Integer)value;
		        if (null != arm) {
		        	if (arm < 0) {
			            errorHandler.throwException(
			                    "Arm number '" + arm + "' is a negative integer.",
			                    ErrorHandlerInterface.INVALID_ARGUMENT
			                    );
			        }

		        	paramMap.put(param, arm);
		        }
		        break;
        	}

        	case ARMS: {
        		@SuppressWarnings("unchecked")
				Set<Integer> arms = (Set<Integer>)value;
        		boolean required = flag;
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
		            	paramMap.put(param, arms);
		            }
		        }
		        break;
        	}

        	case COMPACT_DISPLAY:
		        if ((Boolean)value) {
		            paramMap.put(param, true);
		        }
		        break;

        	case CONTENT:
		        if (null == value) {
		            errorHandler.throwException("Content cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		        }
		
		        paramMap.put(param, value);
		        break;

        	case CSV_DELIMITER:
        		if (null == value) {
        			value = REDCapApiCsvDelimiter.COMMA;
        		}

        		paramMap.put(param, value);
        		break;

        	case DAG: {
        		String dag = (String)value;
		        if (null != dag && 0 != dag.trim().length()) {
		        	paramMap.put(param, dag.trim());
		        }
		        break;
        	}

        	case DAGS: {
        		@SuppressWarnings("unchecked")
				Set<String> dags = (Set<String>)value;
        		boolean required = flag;
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
		            	paramMap.put(param, dags);
		            }
		        }
		        break;
        	}

        	case DATA:
		        if (null == value) {
		            errorHandler.throwException("Data cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
		        }
		
		        paramMap.put(param, value);
		        break;

        	case DATE_FORMAT:
		        if (null == value) {
		            value = REDCapApiDateFormat.YMD;
		        }
		
		        paramMap.put(param, value);
		        break;

        	case BEGIN_TIME:
        	case END_TIME:
        	case DATE_RANGE_BEGIN:
        	case DATE_RANGE_END:
        		if (null != value) {
	        		String dateRange = checkDateRangeArgument((String)value);
	
	        		// checkDateRangeArgument will only return the date if it's valid
	    			paramMap.put(param, dateRange);
        		}
        		break;

        	case DECIMAL_CHARACTER:
        		if (null != value) {
        			paramMap.put(param, value);
        		}
        		break;

        	case EVENT: {
        		String event = (String)value;
        		if (null != event && 0 != event.trim().length()) {
        			paramMap.put(param, event.trim());
        		}
        		break;
        	}

        	case EVENTS: {
        		@SuppressWarnings("unchecked")
				Set<String> events = (Set<String>)value;
        		boolean required = flag;
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
		            	paramMap.put(param,  events);
		            }
		        }
		        break;
        	}

        	case EXPORT_CHECKBOX_LABEL:
                paramMap.put(param, value);
                break;
        
            case EXPORT_DATA_ACCESS_GROUPS:
                paramMap.put(param, value);
                break;

            case EXPORT_FILES:
                paramMap.put(param, value);
                break;

            case EXPORT_SURVEY_FIELDS:
                paramMap.put(param, value);
                break;

            case FIELD: {
                String field = (String)value;
                if (null == field || 0 == field.trim().length()) {
                    boolean required = flag;
                    if (required) {
                        errorHandler.throwException(
                                "Field cannot be null or blank.",
                                ErrorHandlerInterface.INVALID_ARGUMENT
                                );
                    }
                    // else OK
                }
                else {
                    paramMap.put(param, field.trim());
                }
                break;
            }

            case FIELDS: {
                @SuppressWarnings("unchecked")
				Set<String> fields = (Set<String>)value;
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
                        paramMap.put(param, fields);
                    }
                }
                break;
            }

            case FILE:
                String filename = (String)value;
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
                    paramMap.put(param, Paths.get(filename));
                }
                break;

            case FILTER_LOGIC:
                String filterLogic = (String)value;
                if (null != filterLogic && 0 < filterLogic.trim().length()) {
                    paramMap.put(param, filterLogic.trim());
                }
                break;

            case FORCE_AUTO_NUMBER:
                paramMap.put(param, value);
                break;

            case FORMAT:
                REDCapApiFormat format = (REDCapApiFormat)value;
                if (null == format) {
                    format = REDCapApiFormat.JSON;
                }
        
                if (!legalFormats.contains(format)) {
                    errorHandler.throwException(
                            "Invalid format '" + format.getLabel() + "' specified. " +
                            "The format should be one of the following: " + Arrays.toString(legalFormats.toArray()),
                            ErrorHandlerInterface.INVALID_ARGUMENT
                            );
                }

                paramMap.put(param, format);
                break;

            case FORMS:
                @SuppressWarnings("unchecked")
                Set<String> forms = (Set<String>)value;
                if (null != forms && 0 != forms.size()) {
                    for (String form : forms) {
                        if (null == form || 0 == form.trim().length()) {
                            errorHandler.throwException(
                                    "Blank or null form specified.",
                                    ErrorHandlerInterface.INVALID_ARGUMENT
                                    );
                        }
                    }
        
                    paramMap.put(param, forms);
                }
                break;
        
            case INSTRUMENT: {
                String instrument = (String)value;
                if (null == instrument || 0 == instrument.trim().length()) {
                    boolean required = flag;
                    if (required) {
                        errorHandler.throwException(
                                "The form argument was null or blank.",
                                ErrorHandlerInterface.INVALID_ARGUMENT
                                );
                    }
                }
                else {
                    paramMap.put(param, instrument.trim());
                }
                break;
            }

            case LOGTYPE:
                if (null != value) {
                    paramMap.put(param, value);
                }
                break;

            case ODM:
                String odm = (String)value;
                if (null != odm && 0 != odm.trim().length()) {
                    paramMap.put(param, odm.trim());
                }
                break;

            case OVERRIDE:
                paramMap.put(param, (Boolean)value ? 1 : 0);
                break;

            case OVERWRITE_BEHAVIOR:
                if (null == value) {
                    value = REDCapApiOverwriteBehavior.NORMAL;
                }
        
                paramMap.put(param, value);
                break;
        
            case RAW_OR_LABEL:
                if (null == value) {
                    value = REDCapApiRawOrLabel.RAW;
                }
        
                paramMap.put(param, value);
                break;

            case RAW_OR_LABEL_HEADERS:
                if (null == value) {
                    value = REDCapApiRawOrLabel.RAW;
                }
        
                paramMap.put(param, value);
                break;

            case RECORD: {
                String recordId = (String)value;
                if (null == recordId || 0 == recordId.trim().length()) {
                    boolean required = flag;
                    if (required) {
                        errorHandler.throwException(
                                "No record ID specified.",
                                ErrorHandlerInterface.INVALID_ARGUMENT
                                );
                    }
                }
                else {
                    paramMap.put(param, recordId.trim());
                }
                break;
            }

            case RECORDS:
                @SuppressWarnings("unchecked")
                Set<String> recordIds = (Set<String>)value;
                if (null != recordIds && 0 != recordIds.size()) {
                    for (String rcdId : recordIds) {
                        if (null == rcdId || 0 == rcdId.trim().length()) {
                            errorHandler.throwException(
                                    "Record ids cannot be null or blank",
                                    ErrorHandlerInterface.INVALID_ARGUMENT
                                    );
                        }
                    }

                    paramMap.put(param, recordIds);
                }
                break;

            case REPEAT_INSTANCE:
                if (null != value) {
                    Integer repeatInstance = (Integer)value;
                    if (repeatInstance < 0) {
                        errorHandler.throwException(
                                "Repeat Instance cannot be negative",
                                ErrorHandlerInterface.INVALID_ARGUMENT
                                );
                    }
        
                    paramMap.put(param, repeatInstance);
                }
                break;

            case REPORT_ID:
                if (null == value) {
                    errorHandler.throwException(
                            "Null report ID specified for export.",
                            ErrorHandlerInterface.INVALID_ARGUMENT
                            );
                }
            
                Integer reportId = (Integer)value;
                if (reportId < 0) {
                    errorHandler.throwException(
                            "Report ID '" + reportId + "' is a negative integer.",
                            ErrorHandlerInterface.INVALID_ARGUMENT
                            );
                }
            
                paramMap.put(param, reportId);
                break;

            case RETURN_CONTENT:        
                if (null == value) {
                    value = REDCapApiReturnContent.COUNT;
                }
                else if (REDCapApiReturnContent.AUTO_IDS.equals(value)) {
                    boolean forceAutoNumber = flag;
    
                    if (!forceAutoNumber) {
                        errorHandler.throwException(
                                "'auto_ids' specified for returnContent, but forceAutoNumber was not set to true; " +
                                "'auto_ids' can only be used when forceAutoNumber is set to true.",
                                ErrorHandlerInterface.INVALID_ARGUMENT
                                );
                    }
                }
    
                paramMap.put(param, value);
                break;

            case RETURN_FORMAT:
                if (null == value) {
                    value = REDCapApiFormat.JSON;
                }
                paramMap.put(param, value);
                break;

            case RETURN_METADATA_ONLY:
                paramMap.put(param, value);
                break;

            case TOKEN:
                String token = (String)value;
                if (null != value && token.trim().length() > 0) {
                    paramMap.put(param, token.trim());
                }
                break;

            case TYPE:
                if (null == value) {
                    value = REDCapApiType.FLAT;
                }
                paramMap.put(param, value);
                break;
                
            case USER:
                String user = (String)value;
                if (null != user && user.trim().length() > 0) {
                    paramMap.put(param, user.trim());
                }
                break;
                
            default:
                // Really shouldn't ever get here but for sanity's sake
                errorHandler.throwException("Unhandled parameter in addParameter()", ErrorHandlerInterface.INVALID_ARGUMENT);
                break;
        }
    }

    /**
     * Generates a string containing the parameters in www-form-urlencoded
     * format.
     * 
     * @return A string in www-form-urlencoded format.
     */
    public String toWwwFormUrlencoded() {
        StringBuilder builder = new StringBuilder();

        for (Entry<REDCapApiParameter, Object> entry : paramMap.entrySet()) {
            String field = entry.getKey().getLabel();
            Object value = entry.getValue();

            if (null != value) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                
	            if (value instanceof REDCapApiEnum) {
	                builder.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
	                builder.append("=");
	                builder.append(URLEncoder.encode(((REDCapApiEnum)value).getLabel(), StandardCharsets.UTF_8));
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
	            else if (value instanceof Boolean || value instanceof Integer) {
	                builder.append(URLEncoder.encode(field, StandardCharsets.UTF_8));
	                builder.append("=");
	                builder.append(URLEncoder.encode((String)value.toString(), StandardCharsets.UTF_8));
	            }
	            else {
	            	throw new java.lang.IllegalArgumentException("Unexpected REDCap parameter type: " + value.getClass());
	            }
            }
        }

        return builder.toString();
    }
}
