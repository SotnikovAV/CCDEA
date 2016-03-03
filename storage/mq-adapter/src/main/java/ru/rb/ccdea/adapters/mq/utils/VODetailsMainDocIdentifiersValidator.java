package ru.rb.ccdea.adapters.mq.utils;

import java.util.Date;
import java.util.List;

import com.documentum.fc.client.IDfSession;

import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;
import ru.rb.ccdea.adapters.mq.binding.svo.VODetailsType;
import ru.rb.ccdea.adapters.mq.receivers.ValidationException;

public class VODetailsMainDocIdentifiersValidator extends MainDocIdentifiersValidator{

    public VODetailsMainDocIdentifiersValidator(IDfSession dfSession) {
        super(dfSession);
    }

    @Override
    public String getDealPassport(Object messageXmlContent) {
        VODetailsType voDetails = (VODetailsType) messageXmlContent;
        return voDetails.getDealPassport();
    }

    @Override
    public String getContractNumber(Object messageXmlContent) {
        VODetailsType voDetails = (VODetailsType) messageXmlContent;
        return voDetails.getContractNumber();
    }

    @Override
    public Date getContractDate(Object messageXmlContent) {
        VODetailsType voDetails = (VODetailsType) messageXmlContent;
        if(voDetails != null && voDetails.getContractDate() != null) {
        	return voDetails.getContractDate().toGregorianCalendar().getTime();
        }
        return null;
    }

    @Override
    public String getErrorDescription() {
        return "DealPassport or ContractNumber/Date not valid in one of VODetail";
    }

	@Override
	public boolean isValid(Object messageXmlContent) throws ValidationException {
		try {
			boolean isValid = true;
			MCDocInfoModifySVOType svoXmlContent = (MCDocInfoModifySVOType) messageXmlContent;
			List<VODetailsType> voDetailsList = svoXmlContent.getSVODetails().getVODetails();
			if (voDetailsList != null) {
				for (VODetailsType voDetails : voDetailsList) {
					if (isValid) {
						isValid = super.isValid(voDetails);
					}
				}
			}
			return isValid;
		} catch (Exception ex) {
			throw new ValidationException("Ошибка валидации", ex);
		}
	}

}
