package ru.rb.ccdea.formadaptors;

import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfModule;
import com.documentum.tools.adaptor.AdaptorException;
import com.documentum.tools.adaptor.IAdaptorParameter;
import com.documentum.tools.adaptor.configuration.IAdaptorConfiguration;
import com.documentum.xforms.engine.adaptor.IServiceAdaptor;
import com.documentum.xforms.engine.adaptor.datasource.IDataSourceAdaptor;

/**
 * Created by ER21595 on 01.07.2015.
 */
public abstract class ContractAdaptor extends DfService implements IServiceAdaptor, IDataSourceAdaptor, IDfModule {

    protected String docbaseName;

    @Override
    public void setDocbaseName(String s) {
        docbaseName = s;
    }

    @Override
    public void init(IAdaptorConfiguration iAdaptorConfiguration) throws AdaptorException {

    }

    @Override
    public void destroy() throws AdaptorException {

    }

    protected String getParameter(IAdaptorParameter[] iAdaptorParameters, String paramName) throws AdaptorException {
        for (int i=0; i<iAdaptorParameters.length;i++){
            if(iAdaptorParameters[i].getParameterInfo().getName().equals(paramName)){
                return iAdaptorParameters[i].getValue();
            }
        }
        throw new AdaptorException("Incorrect input param "+paramName);
    }
}
