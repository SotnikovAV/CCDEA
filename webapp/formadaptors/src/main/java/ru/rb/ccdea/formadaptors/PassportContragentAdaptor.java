package ru.rb.ccdea.formadaptors;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfUtil;
import com.documentum.tools.adaptor.AdaptorException;
import com.documentum.tools.adaptor.IAdaptorParameter;
import com.documentum.tools.util.XMLUtility;
import com.documentum.tools.xml.FormsXMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by ER21595 on 30.06.2015.
 */
public class PassportContragentAdaptor extends ContractAdaptor {
    //protected String fromType ="ccdea_contract";

    protected enum InputParams{
        r_object_id, from_type
    }
    private enum OutputParams{
        s_contractor_name_r, s_contractor_country_code_r
    }
    @Override
    public Document execute(IAdaptorParameter[] iAdaptorParameters) throws AdaptorException {
        String objectId=getParameter(iAdaptorParameters, InputParams.r_object_id.toString());
        String objectType=getParameter(iAdaptorParameters, InputParams.from_type.toString());

        Document doc = FormsXMLUtil.newDocument();
        Element r_data = XMLUtility.createElement(doc, "root");

        IDfSession session = null;
        IDfCollection col = null;
        try{
            session = getSession(docbaseName);
            IDfQuery query = new DfQuery(getDql(objectId, objectType));
            col = query.execute(session,IDfQuery.DF_READ_QUERY);
            while(col.next()){
                processResults(r_data, col);
            }

        } catch (DfServiceException e) {
            throw new AdaptorException("cannot obtain session", e);
        } catch (DfException e) {
            e.printStackTrace();
        } finally {
            if(col!=null) {
                try {
                    col.close();
                } catch (DfException e) {
                    e.printStackTrace();
                }
            }
            try{
                releaseSession(session);
            } catch (DfServiceException e) {
                throw new AdaptorException(e);
            }
        }

        return doc;
    }

    protected String getDql( String objectId, String fromType){
        StringBuilder bld = new StringBuilder("select ");
        String prefix ="";
        for(OutputParams param: OutputParams.values()){
            bld.append(prefix);
            prefix =", ";
            bld.append(param);
        }
        bld.append(" from ");
        bld.append(fromType);
        bld.append(" where ");
        bld.append(InputParams.r_object_id).append(" = ");
        bld.append(DfUtil.toQuotedString(objectId));
        bld.append(" and ").append(OutputParams.s_contractor_name_r);
        bld.append(" is not null ");
        bld.append(" order by i_position desc ");
        bld.append(" enable(ROW_BASED)");
        return bld.toString();
    }

    protected void processResults(Element root, IDfCollection col) throws DfException {
        Element data = XMLUtility.createElement(root,"row");
        for(OutputParams param: OutputParams.values()) {
            XMLUtility.createCDATAElement(data, param.toString(),
                    col.getString(param.toString()));
        }
    }
}
