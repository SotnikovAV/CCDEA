package ru.rb.ccdea.tags;

import com.documentum.web.common.BrandingService;
import com.documentum.web.common.LocaleService;
import com.documentum.web.form.Control;
import com.documentum.web.formext.control.action.ActionButton;
import com.documentum.web.util.SafeHTMLString;
import ru.rb.ccdea.control.ClientListBox;
import ru.rb.ccdea.control.ListBoxFilter;
import ru.rb.ccdea.control.SearchListBox;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Тег для фильтра "Клиент"
 *
 * Created by ER21595 on 27.05.2015.
 */
public class ClientListBoxFilterTag extends SearchListBoxFilterTag {
    private String numberField;

    public void setnumberfield(String numberField) { this.numberField = numberField; }

    @Override
    protected Class getControlClass() { return ClientListBox.class; }

    @Override
    protected void setControlProperties(Control control) {
        super.setControlProperties(control);
        ClientListBox box =(ClientListBox)control;
        box.setNumberField(numberField);
    }

    @Override
    protected void renderHead(JspWriter out) throws IOException {
        SearchListBox list = (SearchListBox) getControl();
        list.setEventHandler("onsearch", "searchDictionaryForOptions", getControl());
        out.println("<table>");
        out.println(getCaptionHTML());
        out.println("<tr><td>\r\n");
        out.println(getSearchPartHTML());
        out.println("</td><td><table><tr><td>");
        if(list.getOptions().size()==0){
            list.addEmptyOption();
        }
        renderScrollbars(out);
    }

    @Override
    protected void renderFilterFeatures(JspWriter out) throws IOException, JspTagException {
        super.renderFilterFeatures(out);
        out.println("</td></tr></table>");
    }

    @Override
    protected String getSearchPartHTML() throws IOException {
        ClientListBox control = (ClientListBox) getControl();
        String name = control.getElementName();
        String numberLabel = controlNlsBundle.getString("MSG_NUMBER", LocaleService.getLocale());
        String nameLabel = controlNlsBundle.getString("MSG_NAME", LocaleService.getLocale());

        StringBuilder bld = new StringBuilder();

        bld.append("<table><tr><td align=\"left\">");
        bld.append("<span class=defaultLabelStyle>");
        bld.append(SafeHTMLString.escapeAttribute(numberLabel));
        bld.append("</span></td>");

        bld.append("<td><input type='text' name='");
        bld.append(name).append("_number_search_box' value='");
        bld.append(control.getNumberSearchString()).append("'/>");
        String strImage = BrandingService.getThemeResolver().getResourcePath(imagesource,
                this.getForm().getPageContext(), true);
        bld.append("</td></tr>\r\n");

        bld.append("<tr><td>");
        bld.append("<span class=defaultLabelStyle>");
        bld.append(SafeHTMLString.escapeAttribute(nameLabel));
        bld.append("</span></td>");

        bld.append("<td><input type='text' name='");
        bld.append(name).append("_name_search_box' value='");
        bld.append(control.getSearchString()).append("'/>");
        bld.append("</tr></table>\r\n");

        bld.append("</td><td>");
        bld.append("<table><tr><td>");
        bld.append("<img src='").append(strImage).append("' width=15 height=14 onclick='");
        bld.append(name).append("_number_search();' style='cursor: pointer'/>");
        bld.append("</td></tr>");
        bld.append("<tr><td>");
        bld.append("<img src='").append(strImage).append("' width=15 height=14 onclick='");
        bld.append(name).append("_name_search();' style='cursor: pointer'/></td>");
        bld.append("</td></tr></table>");

        bld.append("<script type=\"text/javascript\">");
        bld.append(getSearchFunctionScript(name+"_number_search", "\"numberSearchStr\""));
        bld.append(getSearchFunctionScript(name+"_name_search", "\"searchStr\""));

        bld.append("</script>");

        return bld.toString();
    }

    @Override
    protected String getButtonsHTML() throws IOException {
        String controlName=getControl().getElementName();

        StringBuilder bld = new StringBuilder();
        bld.append("<button type=\"button\" name='");
        bld.append(controlName).append("_btnOpenDialog'");
        bld.append(" class=\"button default\" onclick='");
        bld.append("var valStorage = document.getElementsByName(\"").append(controlName).append("_value\")[0];");
        //function __x2aonclick(obj){safeCall(postServerEvent2,"GenericFormContainer_0",null,"ccdea_vk_search_contents_searchButton_0","GenericFormContainer_ccdea_vk_search_contents_0","onClickSearch");}
        //bld.append("setKeys(event);safeCall(beginModalPopupMode,null,\"GenericFormContainer_0\",\"medium\",\"always\");safeCall(postServerEvent2,\"GenericFormContainer_0\",null,\"\",\"GenericFormContainer_0\",\"onaction\",\"selectedCustomerNumbers\",valStorage.value,\"action\",\"selectCustomer\",\"isForeignObj\",\"false\");").append("' >");
        bld.append("setKeys(event);safeCall(beginModalPopupMode,null,\"GenericFormContainer_0\",\"medium\",\"always\");safeCall(postServerEvent2,\"GenericFormContainer_0\",null,\"" + controlName + "\",\"GenericFormContainer_ccdea_vk_search_contents_0\",\"onSelectCustomerClick\");").append("' >");
        bld.append(SafeHTMLString.escapeAttribute("Select")).append("</button>");
        return bld.toString();
    }

}
