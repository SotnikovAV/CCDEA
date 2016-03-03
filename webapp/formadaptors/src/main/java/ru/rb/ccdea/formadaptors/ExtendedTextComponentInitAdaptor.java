package ru.rb.ccdea.formadaptors;

import com.documentum.fc.client.DfService;
import com.documentum.fc.common.DfId;
import com.documentum.tools.adaptor.AdaptorException;
import com.documentum.tools.adaptor.IAdaptorParameter;
import com.documentum.tools.adaptor.configuration.IAdaptorConfiguration;
import com.documentum.tools.util.XMLUtility;
import com.documentum.tools.xml.FormsXMLUtil;
import com.documentum.xforms.engine.adaptor.IServiceAdaptor;
import com.documentum.xforms.engine.adaptor.datasource.IDataSourceAdaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedTextComponentInitAdaptor extends DfService implements IServiceAdaptor, IDataSourceAdaptor {

    public String[] validateParams(String[] paramArray) throws AdaptorException {
        if (paramArray.length <= 0) {
            throw new AdaptorException("Params not found");
        }

        for (String param : paramArray)
            if (param.equals("")) {
                throw new AdaptorException("Empty string param not allowed");
            }

        return paramArray;
    }

    public Document execute(IAdaptorParameter[] iAdaptorParameters) throws AdaptorException {
        String componentName = "";
        String objectId = "";
        String containerStyle = "";
        List<String> params = new ArrayList();
        for (int i = 0; i < iAdaptorParameters.length; i++) {
            String parameterName = iAdaptorParameters[i].getParameterInfo().getName();
            if (parameterName.equalsIgnoreCase("componentName")) {
                componentName = iAdaptorParameters[i].getValue();
            }
            if (parameterName.equalsIgnoreCase("objectId")) {
                objectId = iAdaptorParameters[i].getValue();
                objectId = objectId.equals("") ? DfId.DF_NULLID_STR : objectId;
            }
            if (parameterName.equalsIgnoreCase("containerStyle")) {
                containerStyle = iAdaptorParameters[i].getValue();
            }
        }
        if (componentName.equals("")) {
            throw new AdaptorException("Empty tag type");
        }

        String componentXml = "<template type='insert_component_page'>";
        componentXml += "<param name='component_id'>" + componentName + "</param>";
        if (!objectId.equals("")) {
            componentXml += "<param name='objectId'>%1$s</param>";
        }
        if (!containerStyle.equals("")) {
            componentXml += "<param name='containerStyle'>%2$s</param>";
        }
        componentXml += "</template>";
        params.addAll(processParamsToAddIncludeOptions(iAdaptorParameters, componentXml));
        String[] paramArray = params.toArray(new String[]{});
        validateParams(paramArray);
        String xmlForComponentInclude = String.format(componentXml, paramArray);

        Document doc = FormsXMLUtil.newDocument();
        Element r_data = XMLUtility.createElement(doc, "root");
        Element data = XMLUtility.createElement(r_data, "row");
        XMLUtility.createCDATAElement(data, "result", xmlForComponentInclude);
        return doc;
    }

    static List<String> processParamsToAddIncludeOptions(IAdaptorParameter[] iAdaptorParameters, String includeData) {
        String parameterName;
        List<String> includeParametersNames = getIncludeParametersNames(includeData);
        List<String> params = new ArrayList<String>(includeParametersNames.size());
        for (int i = 0; i < includeParametersNames.size(); ++i) {
            params.add("");
        }
        for (int i = 0, iAdaptorParametersLength = iAdaptorParameters.length; i < iAdaptorParametersLength; i++) {
            IAdaptorParameter iAdaptorParameter = iAdaptorParameters[i];
            parameterName = iAdaptorParameter.getParameterInfo().getName();
            if (includeParametersNames.contains(parameterName)) {
                params.set(includeParametersNames.indexOf(parameterName), iAdaptorParameter.getValue());
            }
        }
        return params;
    }

    public void init(IAdaptorConfiguration iAdaptorConfiguration) throws AdaptorException {
    }

    public void destroy() throws AdaptorException {
    }

    public void setDocbaseName(String s) {

    }

    //TODO сейчас будет работать только для случая отсутствия подчеркивания в мапируемых полях
    static List<String> getIncludeParametersNames(String includeData) {
        ArrayList<String> strings = new ArrayList<String>();
        Pattern pattern = Pattern.compile("<param.+?name.*?=.*?'([a-zA-Z]+?)'.*?>%[1-9]*?\\$s</param>");
        Matcher matcher = pattern.matcher(includeData);
        while (matcher.find()) {
            String group = matcher.group(1);
            strings.add(group);
        }
        return strings;
    }
}
