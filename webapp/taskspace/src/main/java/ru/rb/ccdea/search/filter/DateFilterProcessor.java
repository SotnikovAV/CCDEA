package ru.rb.ccdea.search.filter;

import java.text.SimpleDateFormat;

import com.documentum.web.form.control.DateInput;
import com.documentum.web.formext.component.Component;

/**
 * Created by ER21595 on 04.06.2015.
 */
public class DateFilterProcessor extends BasicFilterProcessor {

    private static final String DQL_DATE_PATTERN = "dd.mm.yyyy";
    private static final String DQL_DATETIME_PATTERN = "dd.mm.yyyy HH:mi:ss";
    private static final SimpleDateFormat dqlDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public boolean validate(Component component, StringBuilder errorMessage) {
        boolean result = true;
        DateInput dateFrom = (DateInput) component.getControl(controlName + "_from");
        if (dateFrom != null) {
            if(!dateFrom.isUndefinedDate()&&!dateFrom.isValidDate()){
                result = false;
                errorMessage.append("Ошибка в фильтре \""+getControlName(component,controlName)+"\", поле \"с\"");
            }
        }
        DateInput dateTo = (DateInput) component.getControl(controlName + "_to");
		if (dateTo != null) {
			if (!dateTo.isUndefinedDate() && !dateTo.isValidDate()) {
				result = false;
				errorMessage.append("Ошибка в фильтре \"" + getControlName(component, controlName) + "\", поле \"по\"");
			}
		}
		if(result && dateFrom != null && dateTo != null && dateFrom.toDate() != null && dateTo.toDate() != null) {
			if(dateFrom.toDate().compareTo(dateTo.toDate()) > 0) {
				result = false;
				errorMessage.append("Ошибка в фильтре \"" + getControlName(component, controlName) + "\", поле \"с\" не должно быть больше поля \"по\"");
			}
		}
        return result;
    }

    public String getCondition(Component component) {
        String dateFrom = readControl(component, controlName + "_from");
        String dateTo = readControl(component, controlName + "_to");
        return constructClause(field,dateFrom,dateTo);
    }

    protected String readControl(Component component, String dateControlName){
        DateInput input = (DateInput) component.getControl(dateControlName);
        if(input!=null && !input.isUndefinedDate()){
            return dqlDateFormat.format(input.toDate());
        }
        else {
            return null;
        }
    }

    protected String constructClause(String docbaseField, String dateFrom, String dateTo){
        StringBuilder result = new StringBuilder();
        if(dateFrom!=null && dateTo!=null){
            result.append("(").append(docbaseField).append(" between ");
            result.append("DATE('").append(dateFrom).append(" 00:00:00','" + DQL_DATETIME_PATTERN + "')");
            result.append(" AND ");
            result.append("DATE('").append(dateTo).append(" 23:59:59','" + DQL_DATETIME_PATTERN + "'))");
        }else{
            if(dateFrom!=null){
                result.append(docbaseField).append(" >= ");
                result.append("DATE('").append(dateFrom).append(" 00:00:00','" + DQL_DATETIME_PATTERN + "')");
            }else if(dateTo!=null){
                result.append(docbaseField).append(" <= ");
                result.append("DATE('").append(dateTo).append(" 23:59:59','" + DQL_DATETIME_PATTERN + "')");
            }
        }
        if(result.length() > 0) {
        	result.append(" AND ").append(docbaseField).append(" IS NOT NULLDATE ");
        }
        return result.toString();
    }

    private String getControlName(Component component, String controlName){
        String nls = "MSG_"+controlName.toUpperCase();
        String localizedName = component.getString(nls);
        if(localizedName.startsWith("yy")) {
            return controlName;
        }else{
            return  localizedName;
        }
    }

    @Override
    public void clearFilter(Component component) {
        DateInput input = (DateInput) component.getControl(controlName + "_from");
        if(input!=null) {
            input.clear();
            input = (DateInput) component.getControl(controlName + "_to");
            input.clear();
        }
    }

	@Override
	public boolean isEmpty(Component component) {
		boolean result = true;
		String dateFrom = readControl(component, controlName + "_from");
		String dateTo = readControl(component, controlName + "_to");
		result = (dateFrom == null || dateFrom.trim().length() == 0) 
				&& (dateTo == null || dateTo.trim().length() == 0);
		return result;
	}
}
