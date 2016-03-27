package ru.rb.ccdea.tags;

import com.documentum.nls.NlsResourceBundle;
import com.documentum.web.common.LocaleService;
import com.documentum.web.env.AbstractEnvironment;
import com.documentum.web.env.EnvironmentService;
import com.documentum.web.env.IRender;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.ListBoxTag;
import com.documentum.web.util.SafeHTMLString;
import ru.rb.ccdea.control.ListBoxFilter;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Класс для контрола "Список" с возможностью выбора
 * нескольких элементов и кнопок для автоматического
 * выделения всех, или ни одного пункта.
 *
 * Created by ER21595 on 25.05.2015.
 */
public class ListBoxFilterTag extends ListBoxTag {
    protected final static NlsResourceBundle controlNlsBundle = 
            new NlsResourceBundle("ru.rb.ccdea.control.ListBoxFilterNlsProp");
    private String captionNls=null;
    private String m_strOnselect = null;

    public void setcaptionnlsid(String captionnlsid){
        this.captionNls = captionnlsid;
    }

    @Override
    public void setOnselect(String strOnselect) {
        this.m_strOnselect = strOnselect;
    }

    @Override
    protected Class getControlClass() { return ListBoxFilter.class; }

    private String getCaption(){
        return getControl().getForm().getString(captionNls);
    }

    protected String getCaptionHTML(){
        Control ctrl = getControl();
        StringBuilder bld = new StringBuilder();
        if(captionNls!=null){
            bld.append("<tr><td colspan=\'2\'>");
            bld.append("<span");
            if(ctrl.getCssClass() != null) {
                bld.append(" class=").append(ctrl.getCssClass());
            } else {
                bld.append(" class=").append("defaultLabelStyle");
            }

            if(ctrl.getCssStyle() != null) {
                bld.append(" style=\'").append(ctrl.getCssStyle()).append('\'');
            }
            bld.append(">");
            bld.append(formatText(getCaption()));
            bld.append("</span></td></tr>");
        }
        return bld.toString();
    }

    protected String getStrOnselect() {
        return m_strOnselect;
    }

    protected String getButtonsHTML() throws IOException {
//        String allBtnName = controlNlsBundle.getString("MSG_BTN_ALL", LocaleService.getLocale());
        String noneBtnName = controlNlsBundle.getString("MSG_BTN_NONE", LocaleService.getLocale());
        String controlName=getControl().getElementName();

        StringBuilder bld = new StringBuilder();
        bld.append("<table><tr><td class=\"widget-control\">");
//        bld.append("<button type=\"button\" name='");
//        bld.append(controlName).append("_btnSelectAll'");
//        bld.append(" class=\"button default\" onclick='");
//        bld.append(controlName).append("_selectAll();' >");
//        bld.append(SafeHTMLString.escapeAttribute(allBtnName)).append("</button>");
//        bld.append("</td></tr><tr><td class=\"widget-control\">");
        bld.append("<button type=\"button\" name='");
        bld.append(controlName).append("_btnSelectNone'");
        if(!getControl().isVisible()) {
        	bld.append(" style='display: none;'");
        } else {
        	bld.append(" class=\"button default\" ");
        }
        bld.append(" onclick='");
        bld.append(controlName).append("_deselectAll();' >");
        bld.append(SafeHTMLString.escapeAttribute(noneBtnName)).append("</button>");
        bld.append("</td></tr></table>\r\n");
        bld.append("<script type=\"text/javascript\">\r\n");
        //скрипт для выделения всех элементов списка
//        bld.append("function ").append(controlName).append("_selectAll(){");
//        bld.append("var obj = document.getElementsByName(\"").append(controlName).append("_value\")[0];");
//        bld.append("obj.value=\"").append(ListBoxFilter.ALL_VALUES).append("\";");
//        if(m_strOnselect != null) {
//            renderServerEventCall2(bld, "onselect", m_strOnselect, getControl(), null,null);
//        }else {
//            bld.append("var obj = document.getElementsByName(\"").append(controlName).append("\")[0];");
//            bld.append("var htmlStr = obj.parentNode.innerHTML;");
//            bld.append("var re = new RegExp('<OPTION selected', 'g');");
//            bld.append("htmlStr = htmlStr.replace(re,'<OPTION');");
//            bld.append("re = new RegExp('<OPTION', 'g');");
//            bld.append("htmlStr = htmlStr.replace(re,'<OPTION selected'); ");
//            bld.append("obj.parentNode.innerHTML = htmlStr;");
//
//            bld.append(attachEventScript());
//        }
//        bld.append("}\r\n");
        //скрипт, убирающий выделение
        bld.append("function ").append(controlName).append("_deselectAll(){");
        bld.append("var obj = document.getElementsByName(\"").append(controlName).append("_value\")[0];");
        bld.append("obj.value=\"").append(ListBoxFilter.NO_VALUES).append("\";");
        if(m_strOnselect != null) {
            renderServerEventCall2(bld, "onselect", m_strOnselect, getControl(), null,null);
        }else {
            bld.append("var obj = document.getElementsByName(\"").append(controlName).append("\")[0];");
            bld.append("var htmlStr = obj.parentNode.innerHTML;");
            bld.append("var re = new RegExp('<OPTION selected', 'g');");
            bld.append("htmlStr = htmlStr.replace(re,'<OPTION'); ");
            bld.append("obj.parentNode.innerHTML = htmlStr;");
            bld.append(attachEventScript());
        }

        bld.append("}\r\n");
        bld.append("</script>");
        return bld.toString();
    }

    protected void renderHead(JspWriter out) throws IOException {
        out.println("<table>");
        out.println(getCaptionHTML());
        out.println("<tr><td>");
    }

    @Override
    protected void renderStart(JspWriter out) throws IOException {
        renderHead(out);
        super.renderStart(out);
    }

    @Override
    protected void renderEnd(JspWriter out) throws IOException, JspTagException {
        super.renderEnd(out);
        renderFilterFeatures(out);
    }

    /**
     * добавление к листбоксу HTML-кода, специфического для посиковых фильтров
     *
     * @param out
     * @throws IOException
     * @throws JspTagException
     */
    protected void renderFilterFeatures(JspWriter out) throws IOException, JspTagException {
        String controlName = getControl().getElementName();
        out.println("</td><td>");
        out.println(getButtonsHTML());

        StringBuilder buf = new StringBuilder(256);
        buf.append("<input type=\'hidden\' name=\'");
        buf.append(controlName).append("_value");
        buf.append("\'/>");

        out.println(buf.toString());
        //сохранение выбранных элементов
        StringBuilder bld = new StringBuilder();
        bld.append("<script type=\"text/javascript\">\r\n");
        bld.append("function ").append(controlName).append("_save(){");
        bld.append("var lst = document.getElementsByName(\"").append(controlName).append("\")[0];");
        bld.append("var valStorage = document.getElementsByName(\"").append(controlName).append("_value\")[0];");
        bld.append("valStorage.value=\"\";");
        bld.append("var noItemsSelected=true;");
        bld.append("for( i=0;i<lst.options.length;i++){");
        bld.append("if(lst.options[i].selected){");
        bld.append("noItemsSelected=false;");
        bld.append("valStorage.value=valStorage.value+\",\"+lst.options[i].value;}  ");
        bld.append("}");
        bld.append("if(noItemsSelected){");
        bld.append("valStorage.value=\"").append(ListBoxFilter.NO_VALUES).append("\";}");
        if(m_strOnselect != null) {
            renderServerEventCall2(bld, "onselect", m_strOnselect, getControl(), null,null);
        }
        bld.append("}\r\n");

        //восстановление выбранных элементов после обновления компонента
        bld.append("var box= document.forms.");
        bld.append(getControl().getForm().getTopForm().getElementName((String) null));
        bld.append(".").append(controlName).append(";");
        bld.append("var storedVal = \"").append(((ListBoxFilter) getControl()).getRawValue()).append("\";");
        bld.append("if(storedVal.length>0){");
        bld.append("var selected= storedVal.substring(1).split(',');");
        bld.append("var s = box.parentNode.innerHTML;");
        bld.append("var re = new RegExp('<OPTION selected', 'g');");
        bld.append("s = s.replace(re,'<OPTION'); ");
        bld.append("for (i in selected){");
        bld.append("s = s.replace(\"<OPTION value=\"+selected[i], \"<OPTION selected value=\"+selected[i]);");
        bld.append("noItemsSelected = false;");
        bld.append("}");
        bld.append("box.parentNode.innerHTML = s;");
        bld.append("}else{box.selectedIndex=-1;}\r\n");
        bld.append(attachEventScript());
        bld.append("</script>");

        out.println(bld);

        out.println("</td></tr>");
        out.println("</table>");
    }

    /**
     * добавление листенера, вызывающего функцию, сохраняющую
     * выделенные элементы в спрятанном инпуте. используется
     *  при создании тега и во всех скриптах, манипулирующих
     *  html-кодом страницы, так как при этом листенеры пропадают
     *  (экспериментальный факт)
     * @return
     */
    protected String attachEventScript(){
        String controlName = getControl().getElementName();
        StringBuilder bld = new StringBuilder();
        bld.append("var box= document.forms.");
        bld.append(getControl().getForm().getTopForm().getElementName((String) null));
        bld.append(".").append(controlName).append(";");
        bld.append("if(box.addEventListener){");
        bld.append("box.addEventListener(\"change\",").append(controlName).append("_save);");
        bld.append("} else if(box.attachEvent){");
        bld.append("box.attachEvent(\"onchange\",").append(controlName).append("_save);");
        bld.append("}");
        return bld.toString();
    }

    /**
     * Честно скопированный из RenderUtils метод для создания вызова
     * серверной процедуры со странички.
     *
     * @param writer
     * @param strEvent
     * @param control
     * @throws IOException
     */
    protected void renderServerEventCall2(Appendable writer, String strEvent,String method, Control control,
                                          String argName, String argValue) throws IOException {
        AbstractEnvironment env = EnvironmentService.getEnvironment();
        String strJsMethodName = "postServerEvent2";
        String strMethod = control.getEventHandlerMethod(strEvent);
        if(strMethod==null||strMethod.length()==0){
            strMethod=method;
        }
        if(env != null && env instanceof IRender && ((IRender)env).preProcess()) {
            strJsMethodName = ((IRender)env).namespaceJsMethodName(strJsMethodName);
        }

        writer.append("safeCall").append('(');
        writer.append(strJsMethodName);
        writer.append(',');
        writer.append('\"');
        writer.append(control.getForm().getTopForm().getElementName());
        writer.append("\",");
        writer.append("null");
        writer.append(",");
        writer.append('\"');
        if(control.getName() != null) {
            writer.append(control.getElementName());
        }

        writer.append("\",");
        Control handler = control.getEventHandler(strEvent);
        writer.append('\"');
        writer.append(handler.getElementName());
        writer.append("\",");
        writer.append('\"');
        writer.append(strMethod);
        writer.append('\"');
        if(argName!=null) {
            writer.append(',');
            writer.append(argName);
            writer.append(',');
            writer.append(argValue);
        }

        writer.append(");");
    }
	
}
