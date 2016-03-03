package ru.rb.ccdea.tags;

import com.documentum.web.common.BrandingService;
import com.documentum.web.form.Control;
import ru.rb.ccdea.control.ListBoxFilter;
import ru.rb.ccdea.control.SearchListBox;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Тег для фильтра "Текстовое поле + список"
 *
 * Created by ER21595 on 26.05.2015.
 */
public class SearchListBoxFilterTag extends ListBoxFilterTag {
    protected final static String imagesource = "icons/search/search-go.gif";
    private final static String TOO_SHORT_SEARCH_STRING_WARNING="Слишком короткая строка поиска!";
    private String labelField;
    private String valueField;
    private String fromPart;
    private String wherePart;
    private String query;
    private String tempWidth;

    public void setlabelfield(String labelField) { this.labelField = labelField; }

    public void setvaluefield(String valueField) {
        this.valueField = valueField;
    }

    public void setfrompart(String fromPart) {
        this.fromPart = fromPart;
    }

    public void setwherepart(String wherePart) {
        this.wherePart = wherePart;
    }

    public void setquery(String query) {
        this.query = query;
    }

    @Override
    protected Class getControlClass() {
        return SearchListBox.class;
    }

    @Override
    protected void setControlProperties(Control control) {
        super.setControlProperties(control);
        SearchListBox box =(SearchListBox)control;
        box.setLabelField(labelField);
        box.setValueField(valueField);
        box.setFromPart(fromPart);
        box.setWherePart(wherePart);
        box.setQuery(query);
    }

    protected void renderScrollbars(JspWriter out) throws IOException {
        SearchListBox list = (SearchListBox) getControl();
        String height =list.getListBoxHeight();
        if(height==null||height.length()==0) {
            height=list.getHeight();
            if (height == null || height.length() == 0) {
                try {
                    int size = Integer.parseInt(list.getSize());
                    height = Integer.toString(size * 20);
                } catch (NumberFormatException e) {
                    height = "80";
                }
            }
            list.setlistBoxHeight(height);
        }
        tempWidth = list.getWidth();
        if(tempWidth!=null&&tempWidth.length()>0){
            out.println("<div style=\"overflow:auto;width:"+tempWidth+";height:"+height+"\">");
            list.setWidth("");
        }else{
            out.println("<div style=\"overflow:auto\">");
        }
        String style= getControl().getCssStyle();
        if(style ==null ||style.length()==0){
            list.setCssStyle("overflow:visible");
        }else{
            list.setCssStyle("overflow:visible;" + style);
        }
    }
    @Override
    protected void renderHead(JspWriter out) throws IOException {
        SearchListBox list = (SearchListBox) getControl();
        list.setEventHandler("onsearch", "searchDictionaryForOptions", getControl());
        out.println("<table>");
        out.println(getCaptionHTML());
        out.println(getSearchPartHTML());
        out.println("<tr><td>");
        if(list.getOptions().size()==0){
            list.addEmptyOption();
        }
        renderScrollbars(out);
    }

    @Override
    protected void renderFilterFeatures(JspWriter out) throws IOException, JspTagException {
        if(tempWidth!=null&&tempWidth.length()>0){
            ((ListBoxFilter)getControl()).setWidth(tempWidth);
        }
        out.println("</div>");
        super.renderFilterFeatures(out);
    }

    protected String getSearchPartHTML() throws IOException {
        String name = getControl().getElementName();
        StringBuilder bld = new StringBuilder();
        bld.append("<tr><td>");
        bld.append("<input type='text' name='");
        bld.append(name).append("_search_box' value='");
        bld.append(((SearchListBox) getControl()).getSearchString()).append("'/>");
        String strImage = BrandingService.getThemeResolver().getResourcePath(imagesource,
                this.getForm().getPageContext(), true);
        bld.append("</td><td>");
        bld.append("<img src='").append(strImage).append("' width=15 height=14 onclick='");
        bld.append(name).append("_search();' style='cursor: pointer'/>");
        bld.append("<script type=\"text/javascript\">");
        bld.append(getSearchFunctionScript(name+"_search", "\"searchStr\""));
        bld.append("</script>");
        bld.append("</td></tr>");
        return bld.toString();
    }

    protected String getSearchFunctionScript(String functionName, String valueName) throws IOException {
        StringBuilder bld = new StringBuilder();
        bld.append("function ").append(functionName).append("(){");
        bld.append("var searchValue=document.getElementsByName(\"").append(functionName).append("_box\")[0].value;");
        bld.append("if(searchValue.length<3){");
        bld.append("alert(\"").append(TOO_SHORT_SEARCH_STRING_WARNING).append("\");");
        bld.append("} else {");
        renderServerEventCall2(bld, "onsearch", null, getControl(),
                valueName, "searchValue");
        bld.append("}");
        bld.append("}");

        return bld.toString();
    }

}
