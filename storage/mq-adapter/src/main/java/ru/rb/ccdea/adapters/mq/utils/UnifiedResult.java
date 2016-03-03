package ru.rb.ccdea.adapters.mq.utils;

import java.util.Date;

public class UnifiedResult {

    public static String SUCCESS = "SUCCESS";
    public static String ERROR = "ERROR";
    public static String WARNING = "WARNING";

    public static String INTERNAL_ERROR_CODE = "500";
    public static String VALIDATE_ERROR_CODE = "501";
    public static String WRONG_ORDER_CODE = "510";

    private String result = SUCCESS;
    private String errorCode = "";
    private String errorDescription = "";
    private Date resultDate = null;

    public static UnifiedResult getSuccessResultInstance() {
        return new UnifiedResult();
    }

    public boolean isSuccess() {
        return SUCCESS.equalsIgnoreCase(result);
    }

    public boolean isWarning() {
        return WARNING.equalsIgnoreCase(result);
    }

    public boolean isError() {
        return ERROR.equalsIgnoreCase(result);
    }

    public void setWarning(String description) {
        this.result = WARNING;
        this.errorDescription = description;
    }

    public void setError(String errorCode, String errorDescription) {
        this.result = ERROR;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public void addError(String errorCode, String errorDescription) throws Exception {
        this.result = ERROR;
        if (!this.errorCode.isEmpty() && !this.errorCode.equalsIgnoreCase(errorCode)) {
            throw new Exception("Can not add new errorCode " + errorCode + " to existing " + this.errorCode);
        }
        this.errorCode = errorCode;
        if (!this.errorDescription.isEmpty()) {
            this.errorDescription += ", ";
        }
        this.errorDescription += errorDescription;
    }

    public String getResult() {
        return result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public Date generateResultDate() {
        resultDate = new Date();
        return resultDate;
    }

    public Date getResultDate() {
        return resultDate;
    }
}
