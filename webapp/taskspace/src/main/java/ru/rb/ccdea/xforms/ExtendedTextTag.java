package ru.rb.ccdea.xforms;

import com.documentum.fc.internal.xml.IXMLParserFactory;
import com.documentum.fc.internal.xml.XMLUtilsFactory;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.FormProcessor;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.Container;
import com.documentum.web.formext.config.ConfigElement;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.control.xforms.xformscontrol.XFormsText;
import com.documentum.web.formext.control.xforms.xformscontrol.XFormsTextTag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.jsp.JspWriter;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;

public class ExtendedTextTag extends XFormsTextTag {
    public static String ELEMENT_ID_PREFIX = "extended_";
    public static String TYPE_INSERT_COMPONENT_PAGE = "insert_component_page";

    protected String m_xFormsElementId = null;

    protected void renderEnd(JspWriter out) throws IOException {
        if (m_xFormsElementId.startsWith(ELEMENT_ID_PREFIX)) {
            String controlValue = "<data>" + ((XFormsText) getControl()).getValue() + "</data>";
            ByteArrayInputStream a = new ByteArrayInputStream(controlValue.getBytes("UTF-8"));

            InputSource configSource = new InputSource(a);
            IXMLParserFactory xmlParserFactory = new XMLUtilsFactory().getParserFactory();
            DocumentBuilder documentBuilder = xmlParserFactory.getDOMParser();
            Document document = null;
            try {
                document = documentBuilder.parse(configSource);
            } catch (SAXException e) {
                e.printStackTrace();
                return;
            }
            Element element = document.getDocumentElement();
            IConfigElement templates = new ConfigElement(element, null);

            Iterator it = templates.getChildElements("template");
            while (it.hasNext()) {
                IConfigElement template = (IConfigElement) it.next();
                if (template.getAttributeValue("type").equalsIgnoreCase(TYPE_INSERT_COMPONENT_PAGE)) {
                    String strComponentId = null;
                    String containerStyle = null;
                    ArgumentList componentArgs = new ArgumentList();
                    Iterator params = template.getChildElements("param");
                    while (params.hasNext()) {
                        IConfigElement param = (IConfigElement) params.next();
                        String paramName = param.getAttributeValue("name");
                        if ("component_id".equalsIgnoreCase(paramName)) {
                            strComponentId = param.getValue();
                        }
                        else if ("containerStyle".equalsIgnoreCase(paramName)) {
                            containerStyle = param.getValue();
                        }
                        else {
                            componentArgs.add(paramName, param.getValue());
                        }
                    }

                    String strComponentName = m_xFormsElementId;//"DocumentViewContainer";//"foldertreeview";//getName();
                    String strComponentPage = "start";

                    insertComponent(out, strComponentName, strComponentId, strComponentPage, componentArgs, containerStyle);
                }
            }
            this.getControl().setVisible(false);
        }

        super.renderEnd(out);

    }

    protected void insertComponent(JspWriter out, String strComponentName, String strComponentId, String strComponentPage, ArgumentList componentArgs, String containerStyle) throws IOException {
        out.print("<div id='div_for_" + m_xFormsElementId + "'");
        if (containerStyle != null) {
            out.print(" style='" + containerStyle + "'");
        }
        out.println(">");
        try {
            Form form = getForm();
            if (!(form instanceof Component)) {
                throw new RuntimeException("componentinclude tag can only be included within a Component");
            }
            Component container = (Component) form;

            Component component = (Component) container.getControl(strComponentName);

            if (component != null) {
                container.remove(component);  //todo: Разобраться!!! Пока не понятно как подсунуть компоненте новые параметры. Скорее всего можно послать сигнал.
                //component.setComponentReturn();
            }
            component = container.createComponent(strComponentName, strComponentId, strComponentPage, componentArgs);

            //setControlProperties(component);

            component.bind(getForm().getFormRequest(), getForm().getFormResponse());

            if (!component.isInitialized()) {
                //todo: можно реализовать через net.sf.cglib.reflect.FastClass (будет работать быстрее)
                Class[] params = {Control.class, ArgumentList.class};
                Method m = FormProcessor.class.getDeclaredMethod("fireOnInitEvent", params);
                m.setAccessible(true);
                Object arglist[] = new Object[2];
                arglist[0] = component;
                arglist[1] = componentArgs;
                m.invoke(null, arglist);
            }

            String strIncludePage = component.getPageURL();

            StringBuffer buf = new StringBuffer(256);
            //buf.append("<!-- INCLUDED COMPONENT ").append("id: '").append(strComponentId).append("', url: '").append(strIncludePage).append("', page: '").append(strComponentPage).append("', class: '").append(component.getClass()).append("' -->");

            out.print(buf.toString());

            include(component, strIncludePage);

            if ((container instanceof Container)) {
                ((Container) container).updateControls();
            }
            /* теперь работает и без этого ужаса */
            /*out.println("<script type=\"text/javascript\">\n" +
            "    tag = document.getElementById('div_for_"+m_xFormsElementId+"');\n" +
            "    tag.innerHTML = tag.innerHTML.replace(/"+getForm().getName()+"_"+m_xFormsElementId+"_0/g,'"+getForm().getContainer().getElementName()+"');" +
            "</script>");*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println("</div>");
    }


    protected void include(Form form, String strSrc) throws Exception {
        Form formContainer = getForm();

        formContainer.getFormRequest().setIncluded(form);

        if (!form.isInitialized()) {
            fireFormOnInitEvent(form, getEventArgs());
        }

        if (form.isVisible()) {
            Class[] params = {Form.class};
            Method m = FormProcessor.class.getDeclaredMethod("fireOnRenderEvent", params);
            m.setAccessible(true);
            Object arglist[] = new Object[1];
            arglist[0] = form;
            m.invoke(null, arglist);

            String strRedirect = form.getFormResponse().getRedirectUrl();
            if (strRedirect != null) {
                strSrc = strRedirect;
            }

            String INCLUDING = "com.documentum.web.form.control.FormIncludeTag.Include";

            this.pageContext.setAttribute(INCLUDING, "true", 2);
            this.pageContext.include(strSrc);
            this.pageContext.removeAttribute(INCLUDING, 2);
        }

        formContainer.getFormRequest().clearIncluded();
    }

    public void setXFormsElementId(String id) {
        this.m_xFormsElementId = id;
    }
}
