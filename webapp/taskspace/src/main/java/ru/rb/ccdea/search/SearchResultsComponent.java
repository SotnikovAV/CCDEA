package ru.rb.ccdea.search;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Hidden;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.ComponentColumnDescriptorList;

import javax.servlet.ServletRequest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Класс для компонента, отображающего результаты поиска чего бы то ни было.
 * Created by ER21595 on 05.06.2015.
 */
public class SearchResultsComponent extends Component {
    public static String OBJECT_LIST_GRID="search_results_grid";
    private Set<String> selectedDocuments = new HashSet<String>();
    private ComponentColumnDescriptorList columnList;
    private String queryStr =null;

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        columnList = new ComponentColumnDescriptorList(this,"columns");
    }

    /**
     * Количество строк с результатами в таблице на текущей странице.
     *
     * @return
     */
    public int getDisplayedRowCount(){
        Datagrid grid = (Datagrid) getControl(OBJECT_LIST_GRID);
        if(grid.getDataProvider().getResultsCount()>0) {
            return grid.getDatagridRows().size();
        }else {
            return 0;
        }
    }

    public void refreshDatagrid(String query){
        queryStr = query;
        DataProvider dataProvider = ((Datagrid)getControl(OBJECT_LIST_GRID, Datagrid.class)).getDataProvider();
        dataProvider.setDfSession(getDfSession());
        dataProvider.setQuery(query);
    }

    public void clearDatagrid(){
        queryStr = null;
        Datagrid grid = (Datagrid)getControl(OBJECT_LIST_GRID, Datagrid.class);
        DataProvider dataProvider =  grid.getDataProvider();
        int pageSize = dataProvider.getPageSize();
        boolean paged = dataProvider.isPaged();
        dataProvider = new DataProvider(grid);
        dataProvider.setPaged(paged);
        dataProvider.setPageSize(pageSize);
        grid.setDataProvider(dataProvider);
    }

    @Override
    public void onRefreshData() {
        super.onRefreshData();
        //refreshDatagrid(queryStr);
    }

    @Override
    public void updateStateFromRequest() {
        super.updateStateFromRequest();
        Checkbox checkAll = (Checkbox) getControl("checkAll");
        if(checkAll!=null) {
            if (checkAll.getValue() && !isSinglePage()) {
                DfLogger.debug(this, "SearchResultComponent:checkAll", null, null);
                fetchSelectedDocumentsFromDocbase();
            } else {
                DfLogger.debug(this, "SearchResultComponent:collectIds", null, null);
                ServletRequest request = this.getPageContext().getRequest();
                for (int i = 0; i < getDisplayedRowCount(); i++) {
                    String strHidden = request.getParameter("ccdea_vk_search_results_check_hidden_" + i);
                    if (strHidden != null) {
                        String strValue = request.getParameter("ccdea_vk_search_results_check_" + i);
                        String strObjectId = request.getParameter("ccdea_vk_search_results_objectIdStore_" + i);
                        if (strValue != null) {
                            selectedDocuments.add(strObjectId);
                            DfLogger.debug(this, "SearchResultComponent:addd:" + strObjectId, null, null);
                        } else {
                            selectedDocuments.remove(strObjectId);
                            DfLogger.debug(this, "SearchResultComponent:remove:" + strObjectId, null, null);
                        }
                    }
                    else {
                        DfLogger.debug(this, "SearchResultComponent:notFound:" + i, null, null);
                    }
                }
            }
        }
        else {
            DfLogger.debug(this, "SearchResultComponent:checkAll control not found", null, null);
        }
    }

    private void fetchSelectedDocumentsFromDocbase(){
        selectedDocuments = new HashSet<String>();
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery(queryStr);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            while(col.next()){
                selectedDocuments.add(col.getString("r_object_id"));
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if(col !=null){
                try{
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
    }

    public Set<String> getSelectedDocuments() {
        return selectedDocuments;
    }

    public String getCurrentRowSelection(){
        Datagrid grid = (Datagrid) getControl(OBJECT_LIST_GRID);
        return String.valueOf(selectedDocuments.contains(
                grid.getDataProvider().getDataField("r_object_id")));
    }
    public boolean isSinglePage(){
        Datagrid grid = (Datagrid)getControl(OBJECT_LIST_GRID, Datagrid.class);
        return grid.getDataProvider().getPageCount()<=1;
    }
}
