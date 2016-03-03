package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.search.DqlQueryBuilder;

/**
 *
 * Заглушка для фильтров по нереализованным пока полям
 * Created by ER21595 on 04.06.2015.
 */
public class FilterProcessorStub implements IFilterProcessor {

    public boolean validate(Component component, StringBuilder errorMessage) {
        return true;
    }

    public String getCondition(Component component) {
        return null;
    }

    public void addCondition(Component component, DqlQueryBuilder builder) {

    }

    public void clearFilter(Component component) {
    }

	@Override
	public boolean isEmpty(Component component) {
		return true;
	}

	@Override
	public String getControlName() {
		return null;
	}
}
