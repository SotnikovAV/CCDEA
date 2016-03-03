package ru.rb.ccdea.formadaptors;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.common.DfException;
import com.documentum.tools.util.XMLUtility;
import org.w3c.dom.Element;

/**
 * Created by ER21595 on 01.07.2015.
 */
public class SumAdaptor extends ContragentAdaptor {

    protected enum OutputParams{
        d_sums_amount_r, s_sums_currency_code_r
    }

    protected String getDql( String value){
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
        bld.append(InputParams.r_object_id).append(" = '");
        bld.append(value).append("'");
        bld.append(" and ").append(OutputParams.d_sums_amount_r);
        bld.append(" is not null ");
        bld.append(" order by i_position desc ");
        bld.append(" enable(ROW_BASED)");

        return bld.toString();
    }

    protected void processResults(Element root, IDfCollection col) throws DfException {
        Element data = XMLUtility.createElement(root, "row");
        for(OutputParams param:OutputParams.values()) {
            XMLUtility.createCDATAElement(data, param.toString(),
                    col.getString(param.toString()));
        }
    }
}
