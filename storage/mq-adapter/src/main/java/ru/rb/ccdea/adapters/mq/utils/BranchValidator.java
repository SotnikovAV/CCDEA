package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.common.DfUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;

import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public abstract class BranchValidator extends XmlContentValidator{
	
	private static final Log log = LogFactory.getLog(BranchValidator.class);

    public BranchValidator(IDfSession dfSession) {
        super(dfSession);
    }

    public abstract String getBranchCode(Object messageXmlContent) throws ValidationException;

    @Override
    public String getErrorDescription() {
        return "Branch not valid";
    }

    @Override
    public boolean isValid(Object messageXmlContent) throws ValidationException {
        boolean isValid = true;
        String branchCode = null;
        try {
        	branchCode = getBranchCode(messageXmlContent);
        } catch(ValidationException ex) {
        	log.warn("Ошибка при получении кода бранча", ex);
        }
        if (branchCode == null || branchCode.trim().isEmpty()) {
            isValid = false;
        }
        else {
            String dql = "select r_object_id" +
                    " from ccdea_branch" +
                    " where s_code = " + DfUtil.toQuotedString(branchCode.trim());
            IDfCollection rs = null;
            try {
                IDfQuery query = new DfQuery();
                query.setDQL(dql);
                rs = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                if (!rs.next()) {
                    isValid = false;
                }
            } catch(DfException ex) {
            	throw new ValidationException(ex);
            } finally {
                if (rs != null) {
                    try {
						rs.close();
					} catch (DfException e) {
						log.warn("Ошибка при закрытии курсора", e);
					}
                }
            }
        }
        return isValid;
    }
}
