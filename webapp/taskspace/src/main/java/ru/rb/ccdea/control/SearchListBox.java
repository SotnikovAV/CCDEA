package ru.rb.ccdea.control;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Option;
import com.documentum.web.formext.component.Component;

/**
 * Контрол для фильтра "Текстовое поле + список"
 *
 * Created by ER21595 on 26.05.2015.
 */
public class SearchListBox extends ListBoxFilter {
    protected String labelField;
    protected String valueField;
    protected String fromPart;
    protected String wherePart="";


    protected String query="";
    protected String searchString="";
    protected int userSize =3;
    private String listBoxHeight="";

    public String getListBoxHeight() {
        return listBoxHeight;
    }

    public void setlistBoxHeight(String listBoxHeight) {
        this.listBoxHeight = listBoxHeight;
    }


    private static final String[] s_strEventNames = new String[]{"onchange", "onsearch"};
    protected interface OptionCreator{
        Option createOption(IDfCollection col)throws DfException;
    }

    @Override
    public String[] getEventNames() {
        return (String[])s_strEventNames.clone();
    }

    @Override
    public void setSize(String strSize) {
        userSize = Integer.parseInt(strSize);
        super.setSize(strSize);
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }


    public void setWherePart(String wherePart) {
        this.wherePart = wherePart;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public void setFromPart(String fromPart) {
        this.fromPart = fromPart;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void searchDictionaryForOptions(SearchListBox box, ArgumentList arg) {
        searchString=arg.get("searchStr");
        if(getSearchString().length()>=3) {
            StringBuilder strQuery = new StringBuilder();
            if(query==null||query.length()==0) {
                strQuery.append("select ");
                strQuery.append(labelField).append(" as lbl, ");
                strQuery.append(valueField).append(" as val from ");
                strQuery.append(fromPart).append(" where ");
                if (wherePart!=null && wherePart.length()>0) {
                    strQuery.append(wherePart).append(" and ");
                }
                strQuery.append("lower(").append(labelField).append(") like '");
                strQuery.append(searchString.toLowerCase()).append("%'");
            }else{
                strQuery.append(query.replaceAll("<searchStr>",searchString));
            }
            getOptionsFromBase(strQuery.toString(), new OptionCreator() {
                public Option createOption(IDfCollection col) throws DfException{
                    Option o = new Option();
                    o.setLabel(col.getString("lbl"));
                    o.setValue(col.getString("val"));
                    return o;
                }
            });
        }
    }


    protected void getOptionsFromBase(String queryStr, OptionCreator creator){
        IDfQuery query = new DfQuery();
        query.setDQL(queryStr);
        IDfCollection col = null;
        try {
            IDfSession sess = ((Component) getForm().getTopForm()).getDfSession();
            col = query.execute(sess, IDfQuery.DF_READ_QUERY);
            setMutable(true);
            clearOptions();
            while (col.next()) {
                addOption(creator.createOption(col));
            }
            int size=getOptions().size();
            if (size<userSize){
                size=userSize;
            }
            setSize(Integer.toString(size));
        } catch (DfException e) {
            e.printStackTrace();
        } finally {
            try {
                if (col != null) {
                    col.close();
                }
            } catch (DfException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateStateFromRequest() {
        super.updateStateFromRequest();
        String strValue = this.getRequestParameter(this.getNonRenderElementName()+"_search_box");
        if(strValue != null) {
            searchString =strValue;
        }
    }

    public void addEmptyOption(){
        setMutable(true);
        clearOptions();
        Option empty = new Option();
        String width =getWidth();
        int iWidth = 20;
        if(width!=null&&width.length()>0){
            try{
                //Эмпирическая формула, дающая приемлемые значения в диапазоне 60-150px
                iWidth = (int) Math.round((Integer.parseInt(width)-20) /3.5);
            }catch (NumberFormatException e){
                iWidth = 20;
            }
        }
        StringBuilder bld =new StringBuilder(iWidth);
        for (int i = 0; i < iWidth; i++) {
            bld.append("\u00A0");
        }
        empty.setLabel(bld.toString());
        empty.setValue("");
        addOption(empty);
    }
}
