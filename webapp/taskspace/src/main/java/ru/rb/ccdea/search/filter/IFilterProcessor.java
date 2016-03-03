package ru.rb.ccdea.search.filter;

import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.search.DqlQueryBuilder;

/**
 * Обработчик фильтров на компоненте поиска документов.<br/>
 * Предназначен как для работы с контролами (проверка значений, очистка)
 * так и для создания dql-запросов (добавление условий в {@link DqlQueryBuilder})
 * <br/>
 * Created by ER21595 on 03.06.2015.
 */
public interface IFilterProcessor {

    /**
     *  проверка значения(ий), ведённого в контролы фильтра пользователем
     * @param component компонент, на котором должны находиться контролы
     * @param errorMessage осмысленное сообщение о проблеме (предполагается показ пользователю)
     * @return <b>true</b>/<b>false</b> в зависимости от того, всё ли в порядке
     */
    boolean validate(Component component, StringBuilder errorMessage);

    /**
     * получение условия по данному фильтру.
     * @param component
     * @return
     */
//    public String getCondition(Component component);

    /**
     * добавление в {@link DqlQueryBuilder} условий, учитывающих
     * данный фильтр. условия могут включать джойн дополнительных таблиц
     * @param component
     * @param builder
     */
    void addCondition(Component component, DqlQueryBuilder builder);

    /**
     * Очистка значений данного фильтра
     * @param component
     */
    void clearFilter(Component component);
    
	/**
	 * Этот фильтр не заполнен?
	 * 
	 * @param component - компонент, содержащий контрол фильтра
	 * 
	 * @return true, если фильтр не заполнен;иначе - false
	 */
	boolean isEmpty(Component component);
	
	/**
	 * 
	 * @return
	 */
	String getControlName();
}
