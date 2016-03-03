package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;

/**
 * интерфейс, содержащий инициализационные и внутренние методы в дополниение к
 * открытым для клиентов
 * Created by ER21595 on 04.06.2015.
 */
public interface IFilterProcessorInternal extends IFilterProcessor {

    void setControlName(String name);

    void setType(String typeName);

    void setField(String field);

    void setRepeating(boolean repeating);

    /**
     * получение условия по данному фильтру.
     * @param component
     * @return
     */
    public String getCondition(Component component);

}
