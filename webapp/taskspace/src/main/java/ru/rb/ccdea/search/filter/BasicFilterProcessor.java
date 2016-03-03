package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.search.DqlQueryBuilder;

/**
 * Created by ER21595 on 04.06.2015.
 */
public abstract class BasicFilterProcessor implements IFilterProcessorInternal {
    protected String controlName;
    
	protected String docbaseType;
    protected String field;
    protected boolean repeating = false;

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public void setType(String docbaseType) {
        this.docbaseType = docbaseType;
    }

    public void setField(String field) {
        this.field = field;
        repeating = (field!=null && field.endsWith("_r"));
    }

    public void setRepeating(boolean repeating){
        this.repeating = repeating;
    }

    public void addCondition(Component component, DqlQueryBuilder builder) {
        builder.addWhereClause(getCondition(component));
    }

    public void clearFilter(Component component) {
        StringInputControl control  = (StringInputControl) component.getControl(controlName);
        if(control !=null) {
            control.setValue("");
        }
    }
    
    public String getControlName() {
		return controlName;
	}
}
