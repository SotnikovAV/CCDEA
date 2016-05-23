package ru.rb.ccdea.search;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.nls.NlsResourceBundle;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.LocaleService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.form.control.*;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.Container;
import ru.rb.ccdea.config.TypeConfigProcessor;
import ru.rb.ccdea.control.ListBoxFilter;
import ru.rb.ccdea.control.PrintControl;
import ru.rb.ccdea.dialog.ManageMultivalueComponent;
import ru.rb.ccdea.dialog.SelectSingleValueComponent;
import ru.rb.ccdea.search.filter.IFilterProcessor;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.utils.StringUtils;

import java.util.*;


/**
 * Класс для основного компонента, содержащего фильтры для поиска документов.
 * Created by ER21595 on 22.05.2015.
 */
public class SearchComponent extends Container {
	
	private static final String ARCHIVE_TYPES = "'rar','zip','arj','7z'";

    private static final String printDql= 
//    	    " select cont.r_object_id, cont.object_name, cont.r_content_size "
//		    + " from dm_sysobject#1 cont, dm_relation rel "
//		    + " where rel.relation_name='ccdea_content_relation' "
//		    + " and cont.i_chronicle_id = rel.child_id "
//		    + " and rel.parent_id in ('#2') "
//		    + " and cont.a_content_type = 'pdf' and cont.r_content_size > 0 "
    		" select distinct cont.r_object_id "
		    + " from ccdea_doc_content#1 cont, dm_relation rel "
		    + " where rel.relation_name='ccdea_content_relation' "
		    + " and cont.i_chronicle_id = rel.child_id "
		    + " and rel.parent_id in ('#2') "
		    + " and cont.b_is_original = true and cont.r_content_size > 0 "
    ;
    
    private static final String printDossierDql =
//    	    " select cont.r_object_id, cont.object_name, cont.r_content_size "
//    		+ " from dm_sysobject#1 cont, dm_relation rel, ccdea_base_doc doc1, ccdea_base_doc doc2 "
//            + " where rel.relation_name='ccdea_content_relation' "
//            + " and cont.i_chronicle_id = rel.child_id "
//            + " and rel.parent_id = doc1.r_object_id "
//            + " and doc1.id_dossier = doc2.id_dossier "
//            + " and doc2.r_object_id in ('#2') "
//            + " and cont.a_content_type = 'pdf' and cont.r_content_size > 0 "
    		" select distinct cont.r_object_id "
    		+ " from ccdea_doc_content#1 cont, dm_relation rel, ccdea_base_doc doc1, ccdea_base_doc doc2 "
            + " where rel.relation_name='ccdea_content_relation' "
            + " and cont.i_chronicle_id = rel.child_id "
            + " and rel.parent_id = doc1.r_object_id "
            + " and doc1.id_dossier = doc2.id_dossier "
            + " and doc2.r_object_id in ('#2') "
            + " and cont.b_is_original = true and cont.r_content_size > 0 "
	;

    protected final static NlsResourceBundle searchNlsBundle =
            new NlsResourceBundle("ru.rb.ccdea.search.VkSearchComponentNlsProp");

    private ListBoxFilter docType;
    private Set<String> allowedFilters = new HashSet<String>();
    private TypeConfigProcessor proc = TypeConfigProcessor.getInstance();

    @Override
    public void onInit(ArgumentList arg) {
        arg.replace("component", "ccdea_vk_search_no_results");
        super.onInit(arg);
        List<Option> types = proc.getTypeFilterOptions();
        for(Option o:types){
            o.setLabel(getString(o.getLabel()));
        }
        docType = (ListBoxFilter) getControl("doctype",ListBoxFilter.class);
        docType.setMutable(true);
        docType.setOptions(types);
        getControl("printButton", Button.class).setEnabled(false);
        getControl("printDossierButton", Button.class).setEnabled(false);
        ListBoxFilter branchFilter = (ListBoxFilter) getControl("processing_unit", ListBoxFilter.class);
        branchFilter.setMutable(true);
        branchFilter.setOptions(getBranchesOpts());

    }

    public void onClickSearch(Control c, ArgumentList arg ){
        if(getContainedComponent().getId().equals("ccdea_vk_search_no_results")){
            setContainedComponentId("ccdea_vk_search_results");
            setContainedComponentName("ccdea_vk_search_results");
            initCurrentComponent();
        }
        getControl("printButton", Button.class).setEnabled(true);
        getControl("printDossierButton", Button.class).setEnabled(true);
        final List<String> documentTypes = docType.getSelectedValues().size()>0?
                docType.getSelectedValues() : proc.getTypeNames();
        List<IFilterProcessor> filters = proc.getFilterChain(documentTypes);
        boolean allright = true;
        Set<String> errors = new HashSet<String>();
        Map<String,Set<String>> selectedTypes = new HashMap<String,Set<String>>();
        Set<String> filterNames = new HashSet<String>();
        for(IFilterProcessor filter:filters){
        	if(!filter.isEmpty(this)) {
        		filterNames.add(filter.getControlName());
        		for(String docType:proc.getTypeNames(filter)) {
        			Set<String> filterForType = selectedTypes.get(docType);
        			if(filterForType == null) {
        				filterForType = new HashSet<String>();
        				selectedTypes.put(docType, filterForType);
        			}
        			filterForType.add(filter.getControlName());
        		}
        	}
            StringBuilder newMessage = new StringBuilder();
            if(!filter.validate(this,newMessage)){
                allright=false;
                errors.add(newMessage.toString());
            }
        }
        
		if (filterNames.size() > 0) {
			for (String docType : documentTypes) {
				Set<String> filterForType = selectedTypes.get(docType);
				if (filterForType != null) {
					if (!filterForType.equals(filterNames)) {
						selectedTypes.remove(docType);
					}
				}
			}

			documentTypes.clear();
			documentTypes.addAll(selectedTypes.keySet());
		}
        
        if(documentTypes.size() == 0) {
        	allright=false;
        	errors.add("Невозможно найти ни один документ по указанным условиям.");
        }
        
        Label errorLbl = (Label) getControl("error_message");
        if(allright){
            errorLbl.setLabel("");
            String fullQueryStr = StringUtils.joinStrings(documentTypes, " UNION ALL ",
                    new StringUtils.IWorker<String>() {
                        public String doWork(String param) {
                            return createSingleTypeQuery(param, true);
                        }
                    });
            DfLogger.debug(this, fullQueryStr, null, null);
            SearchResultsComponent comp = (SearchResultsComponent) getContainedComponent();
            comp.refreshDatagrid(fullQueryStr);
        }else{
            StringBuilder errorMessages =new StringBuilder("Ошибки:\r\n");
            errorMessages.append(StringUtils.joinStrings(errors, ";\r\n"));
            errorMessages.append('.');
            errorLbl.setLabel(errorMessages.toString());
        }
    }

    public void typeSelectionChanged(Control c, ArgumentList arg){
        allowedFilters = proc.getAllowedFilters(docType.getSelectedValues());
        setContainedComponentId(proc.getResultsComponentName(docType.getSelectedValues()));
        setContainedComponentName(proc.getResultsComponentName(docType.getSelectedValues()));
        initCurrentComponent();
    }

    public Boolean isFilterAllowed(String filterName){
        return allowedFilters.contains(filterName);
    }

    private String createSingleTypeQuery(String typeName, boolean isSubquery) {
        DqlQueryBuilder builder = new DqlQueryBuilder();
        builder.addSelectField(proc.getSelectFields(typeName, isSubquery));
        builder.addJoinedTable(proc.getFromTypes(typeName, isSubquery));
        builder.addGroupBy(proc.getGroupBy(typeName, isSubquery));
        for (String cond : proc.getConditions(typeName, isSubquery)) {
            builder.addOnClause(cond);
        }
        Collection<IFilterProcessor> filters = proc.getFilters(typeName);
        for (IFilterProcessor filter : filters) {
            filter.addCondition(this, builder);
        }
        return builder.getLeftJoinQuery();
    }

    public void onClickClearConditions(Control c, ArgumentList arg){
        for(IFilterProcessor filter:proc.getUniqueFilters(proc.getTypeNames())){
            filter.clearFilter(this);
        }
        for(Object control:getContainedControlsList()) {
        	if (control instanceof ListBoxFilter) {
                ((ListBoxFilter)control).clear();
            }
        }
        Component comp = getContainedComponent();
        if (comp instanceof SearchResultsComponent) {
            ((SearchResultsComponent)comp).clearDatagrid();
        }
    }

    public void onClickPrint(Control c, ArgumentList arg){
        SearchResultsComponent comp = (SearchResultsComponent) getContainedComponent();
        Set<String> sel = comp.getSelectedDocuments();
        boolean printAllVersions = canPrintAllVersions();
        if(printAllVersions){
            Checkbox doPrintAll = (Checkbox) getControl("print_all_versions");
            printAllVersions = doPrintAll.getValue();
        }
        DfLogger.debug(this, sel.toString(), null, null);
        if(sel.size()>0) {
//            PrintControl print = (PrintControl) getControl("print");
//            print.setSession(getDfSession());
            setContentObjects(null, sel, printAllVersions,
                    "printDossierButton".equals(c.getName()));
//            print.setPrint(true);
        } else {
        	Label errorLbl = (Label) getControl("error_message", Label.class);
        	errorLbl.setLabel("Не выбраны документы для отправки на печать.");
        }
    }

    private List<Option> getBranchesOpts(){
        List<Option> result = new ArrayList<Option>();
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery("select s_code, s_name from ccdea_branch");
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            while (col.next()){
                Option op = new Option();
                op.setLabel(col.getString("s_name"));
                op.setValue(col.getString("s_code"));
                result.add(op);
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col!=null){
                try{
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
        return result;
    }
    
    

    private void setContentObjects(PrintControl control,Set<String> selectedDocuments,
                                          boolean allVersions, boolean dossier){
//        List<String> contentIds = new ArrayList<String>();
//        List<String> contentNames = new ArrayList<String>();
//        List<String> contentSizes = new ArrayList<String>();

        IDfCollection col = null;
        try {
            IDfQuery query = new DfQuery();
            String dql =dossier?printDossierDql:printDql;
            dql = dql.replace("#1", allVersions?"(all)":"");
            dql = dql.replace("#2", StringUtils.joinStrings(selectedDocuments, "', '"));
            dql = dql.replace("#3", ARCHIVE_TYPES);
            query.setDQL(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
//            final List<String> objIds = new ArrayList<String>();
			while (col.next()) {
				String objId = col.getString("r_object_id");
				ArgumentList actionArgs = new ArgumentList();
				actionArgs.add("objectId", objId);
				actionArgs.add("type", ContentPersistence.TYPE_NAME);
				ActionService.execute("ucfview", actionArgs, getContext(), this, null);
				// contentIds.add(col.getString("r_object_id"));
				// contentNames.add(col.getString("object_name"));
				// contentSizes.add(col.getString("r_content_size"));
				
			}
			
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
//        control.setObjectToPrint(StringUtils.joinStrings(contentIds, ","));
//        control.setContentNames(StringUtils.joinStrings(contentNames, ","));
//        control.setContentSizes(StringUtils.joinStrings(contentSizes, ","));
//        control.setContentCount(contentIds.size());
    }

    private final static Set<String> docNumberTypes= new HashSet<String>();
    private final static Set<String> printAllVersionTypes = new HashSet<String>();
    static{
        docNumberTypes.addAll(Arrays.asList("ccdea_spd", "ccdea_svo", "ccdea_pd"));
        printAllVersionTypes.addAll(Arrays.asList("ccdea_passport", "ccdea_vbk"));
    }

    public String getDocNumberDocbaseType(){
        Set<String> selected= proc.getDocbaseTypes(docType.getSelectedValues());
        selected.retainAll(docNumberTypes);
        if(selected.size()==1) {
            return (String) selected.toArray()[0];
        }else{
            return "";
        }
    }

    public String getDocNumberSpecialQuery(){
        Set<String> selected= proc.getDocbaseTypes(docType.getSelectedValues());
        selected.retainAll(docNumberTypes);
        String query = "";
        if(selected.size()>1){
            query=StringUtils.joinStrings(selected, " UNION ALL ",
                    new StringUtils.IWorker<String>() {
                        public String doWork(String param) {
                            return "SELECT s_doc_number as lbl, s_doc_number as val from " +
                                    param + " where lower(s_doc_number) like '<selectStr>%'";
                        }
                    });
        }
        return query;
    }

    public boolean canPrintAllVersions(){
        Set<String> selected= proc.getDocbaseTypes(docType.getSelectedValues());
        selected.retainAll(printAllVersionTypes);
        return selected.size()>0;
    }

    public String getSelectSingleValueDql(String valueType, String valueStart) {
        String dql = null;
        if (valueType.equalsIgnoreCase("contract_currency") ||
                valueType.equalsIgnoreCase("document_currency")) {
            dql = "select s_code as s_value from ccdea_currency where s_code like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (valueType.equalsIgnoreCase("customer_name")) {
            dql = "select s_name as s_value from ccdea_customer where s_name like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (valueType.equalsIgnoreCase("customer_number")) {
            dql = "select s_number as s_value from ccdea_customer where s_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (valueType.equalsIgnoreCase("document_number")) {
            dql = "select distinct s_doc_number as s_value from ccdea_pd where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%') +" UNION ALL " +
            "select distinct s_doc_number as s_value from ccdea_spd where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%') +" UNION ALL " +
            "select distinct s_doc_number as s_value from ccdea_svo_detail where s_doc_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (valueType.equalsIgnoreCase("contract_number")) {
            dql = "select distinct s_contract_number as s_value from ccdea_base_doc where s_contract_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        else if (valueType.equalsIgnoreCase("passport_number")) {
            dql = "select distinct s_passport_number as s_value from ccdea_base_doc where s_passport_number like " + DfUtil.toQuotedString(valueStart + '%') ;
        }
        return dql;
    }

    public String trySelectSingleValue(String valueType, String valueStart) {
        String fullValue = null;
        int valueCount = 0;
        IDfCollection col = null;
        try {
            IDfQuery query = new DfQuery();
            String dql = getSelectSingleValueDql(valueType, valueStart);
            query.setDQL(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            while (col.next() && valueCount < 2) {
                fullValue = col.getString("s_value");
                valueCount++;
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
        if (valueCount == 1) {
            return fullValue;
        }
        else {
            return null;
        }
    }

    public String getCustomerValue(String customerValueField, String oppositValue) {
        String value = null;
        IDfCollection col = null;
        String dql = null;
        if ("s_name".equalsIgnoreCase(customerValueField)) {
            dql = "select s_name from ccdea_customer where s_number = " + DfUtil.toQuotedString(oppositValue);
        }
        else {
            dql = "select s_number from ccdea_customer where s_name = " + DfUtil.toQuotedString(oppositValue);
        }
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if (col.next()) {
                value = col.getString(customerValueField);
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
        return value;
    }

    public void setFilterFullValue(Text textControl, Hidden valueControl, String fullValue) {
        textControl.setValue(fullValue);
        textControl.setToolTip(fullValue);
        if (valueControl != null) {
            String multivalue = valueControl.getValue();
            if (multivalue == null || multivalue.trim().isEmpty()) {
                multivalue = "'" + fullValue + "'";
            }
            else {
                multivalue += ",'" + fullValue + "'";
            }
            valueControl.setValue(multivalue);
        }
        else if (textControl.getName().equalsIgnoreCase("customer_name")) {
            Text customerNumberControl = (Text)getControl("customer_number");
            String customerValue = getCustomerValue("s_number", fullValue);
            customerNumberControl.setValue(customerValue);
            customerNumberControl.setToolTip(customerValue);
        }
        else if (textControl.getName().equalsIgnoreCase("customer_number")) {
            Text customerNumberControl = (Text)getControl("customer_name");
            String customerValue = getCustomerValue("s_name", fullValue);
            customerNumberControl.setValue(customerValue);
            customerNumberControl.setToolTip(customerValue);
        }
    }

    public void completeInputValue(final Control control, ArgumentList args) {
        String valueType = args.get("valueType");
        if (valueType == null || valueType.trim().isEmpty()) {
            throw new IllegalArgumentException("Argument valueType is empty");
        }
        else {
            final Text textControl = (Text) getControl(valueType);
            final Hidden valueControl = (Hidden) getControl(valueType + "_value");
            String valueStart = textControl.getValue();

            String fullValue = trySelectSingleValue(valueType, valueStart);
            if (fullValue != null) {
                setFilterFullValue(textControl, valueControl, fullValue);
            }
        }
    }

    public void searchInputValue(final Control control, ArgumentList args) {
        String valueType = args.get("valueType");
        if (valueType == null || valueType.trim().isEmpty()) {
            throw new IllegalArgumentException("Argument valueType is empty");
        }
        else {
            final Text textControl = (Text)getControl(valueType);
            final Hidden valueControl = (Hidden)getControl(valueType + "_value");
            String valueStart = textControl.getValue();

            ArgumentList nestedArgs = new ArgumentList();
            nestedArgs.add("component", "select_single_value_component");
            nestedArgs.add("valueType", valueType);
            nestedArgs.add("valueStart", valueStart);
            nestedArgs.add("title", searchNlsBundle.getString("MSG_SELECT_SINGLE_VALUE_TITLE_" + valueType, LocaleService.getLocale()));
            setComponentNested("dialogcontainer", nestedArgs, getContext(), new IReturnListener() {
                @Override
                public void onReturn(Form form, Map map) {
                    if ("true".equalsIgnoreCase((String)map.get("success"))) {
                        String fullValue = (String) map.get(SelectSingleValueComponent.SELECTED_VALUE);
                        if (fullValue != null) {
                            setFilterFullValue(textControl, valueControl, fullValue);
                        }
                    }
                }
            });
        }
    }

    public void manageMultipleValue(final Control control, ArgumentList args) {
        String valueType = args.get("valueType");
        if (valueType == null || valueType.trim().isEmpty()) {
            throw new IllegalArgumentException("Argument valueType is empty");
        } else {
            final Hidden valueControl = (Hidden)getControl(valueType + "_value");
            if (valueControl != null) {
                ArgumentList nestedArgs = new ArgumentList();
                nestedArgs.add("component", "manage_multivalue_component");
                nestedArgs.add("valueType", valueType);
                nestedArgs.add("multivalue", valueControl.getValue());
                nestedArgs.add("title", searchNlsBundle.getString("MSG_SELECT_SINGLE_VALUE_TITLE_" + valueType, LocaleService.getLocale()));
                setComponentNested("dialogcontainer", nestedArgs, getContext(), new IReturnListener() {
                    @Override
                    public void onReturn(Form form, Map map) {
                        if ("true".equalsIgnoreCase((String)map.get("success"))) {
                            String multivalue = (String) map.get(ManageMultivalueComponent.SELECTED_MULTIVALUE);
                            if (multivalue != null) {
                                valueControl.setValue(multivalue);
                            }
                            else {
                                valueControl.setValue("");
                            }
                        }
                    }
                });
            }
            else {
                throw new IllegalArgumentException("Cant find hidden control for value " + valueType);
            }
        }
    }

}
