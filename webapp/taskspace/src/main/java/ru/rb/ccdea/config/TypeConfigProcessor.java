package ru.rb.ccdea.config;

import com.documentum.web.form.control.Option;
import com.documentum.web.formext.config.ConfigFile;
import com.documentum.web.formext.config.IConfigElement;
import ru.rb.ccdea.search.filter.FilterProcessorStub;
import ru.rb.ccdea.search.filter.IFilterProcessor;
import ru.rb.ccdea.search.filter.IFilterProcessorInternal;

import java.util.*;

/**
 * Класс, работающий с xml-конфигом типов и фильтров. Синглетон.
 * Created by ER21595 on 29.05.2015.
 */
public final class TypeConfigProcessor {
    private final static String configPath ="/ccdea/config/ccdea_types.xml";
    private final Map<String, TypeDescription> types = new LinkedHashMap<String, TypeDescription>();
    private final Map<String, Map<String,String>> filtersTypes = new HashMap<String, Map<String, String>>();
    private final String multitypeSelectFields;
    private final String multitypeComponent;
    private final Set<String> multitypeJoinTypes;
    private final Set<String> multitypeJoinConditions;

    private static class LazyHolder{
        private static final TypeConfigProcessor INSTANCE = new TypeConfigProcessor();
    }

    /**
     * Получение экземпляра класса.
     * @return
     */
    public static TypeConfigProcessor getInstance(){
        return LazyHolder.INSTANCE;
    }

    private class TypeDescription{
        final String name;
        final String nls;
        final String docbaseType;
        final String selectFields;
        final String multiTypeSelectFields;
        final String componentName;
        final Map<String,IFilterProcessor> filterStorage= new HashMap<String, IFilterProcessor>();
        final Set<String> conditions = new HashSet<String>();

        TypeDescription(IConfigElement el){
            name = el.getAttributeValue("name");
            nls = el.getChildValue("nls");
            docbaseType = el.getChildValue("docbase_type");
            selectFields = el.getChildValue("select_fields");
            multiTypeSelectFields =el.getChildValue("multitype_select_fields");
            componentName = el.getChildValue("component");
            IConfigElement filters = el.getChildElement("search_filters");
            for(Iterator filtersIt = filters.getChildElements(); filtersIt.hasNext();){
                IConfigElement filter = (IConfigElement) filtersIt.next();
                String filterName = filter.getAttributeValue("filter_type");
                Map<String,String> filterDescription = new HashMap<String, String>( filtersTypes.get(filterName));
                String attr = filter.getAttributeValue("class");
                if( attr!=null &&attr.length()>0){
                    filterDescription.put("class", attr);
                }
                attr =filter.getAttributeValue("field");
                if( attr!=null &&attr.length()>0){
                    filterDescription.put("field", attr);
                }
                filterDescription.put("type", docbaseType);
                filterStorage.put(filterName, createFilterProcessor(filterDescription));
            }
            filters = el.getChildElement("multitype_conditions");
            if(filters!=null){
                for(Iterator filtersIt = filters.getChildElements(); filtersIt.hasNext();){
                    IConfigElement filter = (IConfigElement) filtersIt.next();
                    conditions.add(filter.getValue());
                }
            }
        }
    }

    private TypeConfigProcessor(){
        ConfigFile cfg = new ConfigFile(configPath, "ccdea");
        IConfigElement elem =cfg.getDescendantElement("scope.filter_types");
        for(Iterator filterEl = elem.getChildElements(); filterEl.hasNext();){
            IConfigElement el = (IConfigElement) filterEl.next();
            Map<String,String> filterDescr = new HashMap<String, String>();
            filterDescr.put("name",el.getAttributeValue("name"));
            filterDescr.put("class",el.getAttributeValue("class"));
            filterDescr.put("field",el.getAttributeValue("field"));
            filtersTypes.put(el.getAttributeValue("name"), filterDescr);
        }
        elem =cfg.getDescendantElement("scope.types");
        for(Iterator typeEl = elem.getChildElements(); typeEl.hasNext();){
            TypeDescription descr = new TypeDescription((IConfigElement) typeEl.next());
            types.put(descr.name, descr);
        }
        multitypeSelectFields = cfg.getDescendantElement("scope.multitype_select_fields").getValue();
        multitypeComponent = cfg.getDescendantElement("scope.multitype_component").getValue();
        multitypeJoinTypes = new HashSet<String>();
        elem = cfg.getDescendantElement("scope.multitype_types");
        for(Iterator typeEl = elem.getChildElements(); typeEl.hasNext();){
            IConfigElement el = (IConfigElement) typeEl.next();
            multitypeJoinTypes.add(el.getValue());
        }
        multitypeJoinConditions = new HashSet<String>();
        elem = cfg.getDescendantElement("scope.multitype_conditions");
        for(Iterator typeEl = elem.getChildElements(); typeEl.hasNext();){
            IConfigElement el = (IConfigElement) typeEl.next();
            multitypeJoinConditions.add(el.getValue());
        }
    }

    private IFilterProcessor createFilterProcessor(Map<String,String> description){
        IFilterProcessor fp = null;
        if("???".equals(description.get("field"))){
            fp= new FilterProcessorStub();
        }else {
            try {
                Class<IFilterProcessorInternal> cls =
                        (Class<IFilterProcessorInternal>) Class.forName(description.get("class"));
                IFilterProcessorInternal fpi = cls.newInstance();
                fpi.setField(description.get("field"));
                fpi.setControlName(description.get("name"));
                fpi.setType(description.get("type"));
                if(description.containsKey("repeating")){
                    fpi.setRepeating(Boolean.parseBoolean(description.get("repeating")));
                }
                fp = fpi;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return fp;
    }

    /**
     * Получение списка типов документов которые можно искать в системе
     * @return список {@link Option}'ов для контрола
     */
    public List<Option> getTypeFilterOptions(){
        List<Option> lst =new ArrayList<Option>();
        for(TypeDescription td: types.values()){
            Option o = new Option();
            o.setValue(td.name);
            o.setName(td.name);
            o.setLabel(td.nls);
            lst.add(o);
        }
        return lst;
    }

    /**
     * Получение списка фильтров, которые можно использовать для
     * данного множества типов
     * @param selectedTypes список выбранных пользователем типов документов
     * @return
     */
    public Set<String> getAllowedFilters(List<String> selectedTypes){
        Set<String> filters = new HashSet<String>();
        for(String type:selectedTypes){
            filters.addAll(types.get(type).filterStorage.keySet());
        }
        return filters;
    }

    /**
     * Получение списка {@link IFilterProcessor}'ов для обработки значений,
     * введённых пользователем в фильтры
     * @param selectedTypes список выбранных пользователем типов документов
     * @return
     */
    public List<IFilterProcessor> getFilterChain(List<String> selectedTypes){
        List<IFilterProcessor> processors = new ArrayList<IFilterProcessor>();
        for (String type : selectedTypes) {
            processors.addAll(types.get(type).filterStorage.values());
        }
        return processors;
    }

    /**
     * Получение списка полей в запросе, соответствующего множеству
     * выбранных пользователем типов документов
     * @param selectedTypes
     * @return
     */
    public String getSelectFields(List<String> selectedTypes){
        if(selectedTypes.size()==1){
            return types.get(selectedTypes.get(0)).selectFields;
        }else{
            return multitypeSelectFields;
        }
    }

    public Collection<IFilterProcessor> getFilters(String selectedType){
        return types.get(selectedType).filterStorage.values();
    }


    public String getResultsComponentName(List<String> selectedTypes){
        if(selectedTypes.size()==1){
            return types.get(selectedTypes.get(0)).componentName;
        }else{
            return multitypeComponent;
        }
    }

    public Set<String> getDocbaseTypes(List<String> selectedTypes){
        Set<String> docbaseTypes = new HashSet<String>();
        for (String typeFilter:selectedTypes) {
            docbaseTypes.add(types.get(typeFilter).docbaseType);
        }
        return docbaseTypes;
    }

    public String getSelectFields(String selectedType, boolean isMultiType){
        if(isMultiType){
            return types.get(selectedType).multiTypeSelectFields;
        }else{
            return types.get(selectedType).selectFields;
        }
    }
    
    public Set<String> getFromTypes(String typeFilterName, boolean isMultiType){
        Set<String> docbaseTypes = new LinkedHashSet<String>();
        docbaseTypes.add(types.get(typeFilterName).docbaseType);
        if(isMultiType){
            docbaseTypes.addAll(multitypeJoinTypes);
        }
        return docbaseTypes;
    }

    public Set<String> getConditions(String typeFilterName, boolean isMultiType){
        Set<String> conditions = new HashSet<String>();
        conditions.addAll(types.get(typeFilterName).conditions);
        if(isMultiType){
            conditions.addAll(multitypeJoinConditions);
        }
        return conditions;
    }

    /**
     * Получение уникальной коллекции фильтров
     *
     * @param selectedTypes
     * @return Коллекция, в которой есть все отображаемые фильтры, но каждому контролу на компоненте соответствует только один элемент
     */
    public Collection<IFilterProcessor> getUniqueFilters(List<String> selectedTypes){
        Map<String, IFilterProcessor> filters = new HashMap<String, IFilterProcessor>();
        for(String typeName:selectedTypes){
            filters.putAll(types.get(typeName).filterStorage);
        }
        return filters.values();
    }

    /**
     * Получение списка типов документов, которые можно искать в системе.
     * Используется для запросов по всем типам сразу.
     * @return
     */
    public List<String> getTypeNames(){
        List<String> lst = new ArrayList<String>();
        for(TypeDescription td: types.values()){
            lst.add(td.name);
        }
        return lst;
    }

	/**
	 * Получить множество типов документов, на которые накладывается указанный
	 * фильтр
	 * 
	 * @param filter
	 *            - фильтр
	 * @return множество типов документов, на которые накладывается указанный
	 *         фильтр
	 */
	public Set<String> getTypeNames(IFilterProcessor filter) {
		Set<String> typeNames = new HashSet<String>();
		for (Map.Entry<String, TypeDescription> entry : types.entrySet()) {
			if (entry.getValue().filterStorage.containsValue(filter)) {
				typeNames.add(entry.getKey());
			}
		}
		return typeNames;
	}
}
