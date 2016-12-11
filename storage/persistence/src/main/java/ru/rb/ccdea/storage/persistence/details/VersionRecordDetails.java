package ru.rb.ccdea.storage.persistence.details;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;

public class VersionRecordDetails {

    public static SimpleDateFormat versionDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public String eventName = null;
    public Date operationDate = null;
    public String sourceSystem = null;
    public String userName = null;
    public List<AttrHistory> attrHistoryList = null;

    public void setIdWithHistory(IDfPersistentObject document, String attrName, IDfId attrValue) throws DfException {
        IDfId oldAttrValue = document.getId(attrName);
        if (!oldAttrValue.equals(attrValue)) {
            document.setId(attrName, attrValue);
            // история по идентификаторам платформы неинформативна
            //VersionRecordDetails.AttrHistory attrHistory = versionRecordDetails.prepareAttrHistory(attrName);
            //attrHistory.attrOldValue = "";
            //attrHistory.attrValue = "";
        }
    }

    public void setStringWithHistory(IDfPersistentObject document, String attrName, String attrValue) throws DfException {
        String oldAttrValue = document.getString(attrName);
        if (!oldAttrValue.equals(attrValue)) {
            document.setString(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            attrHistory.attrOldValue = oldAttrValue;
            attrHistory.attrValue = attrValue;
        }
    }

    public void setRepeatingStringWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex, String attrValue) throws DfException {
        String oldAttrValue = null;
        if (document.getValueCount(attrName) > attrValueIndex) {
            oldAttrValue = document.getRepeatingString(attrName, attrValueIndex);
        }
        if (oldAttrValue == null || !oldAttrValue.equals(attrValue)) {
            if (document.getValueCount(attrName) > attrValueIndex) {
                document.setRepeatingString(attrName, attrValueIndex, attrValue);
            }
            else {
                document.appendString(attrName, attrValue);
            }
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            attrHistory.attrOldValue = oldAttrValue;
            attrHistory.attrValue = attrValue;
        }
    }

	public void cleanRepeatingStringWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex)
			throws DfException {
		try {
			if (document.getValueCount(attrName) > attrValueIndex) {
				String oldAttrValue = document.getRepeatingString(attrName, attrValueIndex);
				document.remove(attrName, attrValueIndex);
				AttrHistory attrHistory = prepareAttrHistory(attrName);
				attrHistory.attrOldValue = oldAttrValue;
				attrHistory.attrValue = null;
			}
		} catch (DfException ex) {
			DfLogger.debug(document,
					"Ошибка при удалении атрибута документа " + attrName + " с индексом " + attrValueIndex, null, ex);
		}
	}

    public void setTimeWithHistory(IDfPersistentObject document, String attrName, IDfTime attrValue) throws DfException {
        IDfTime oldAttrValue = document.getTime(attrName);
        if (!oldAttrValue.equals(attrValue)) {
            document.setTime(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue.getDate() != null) {
                attrHistory.attrOldValue = versionDateFormat.format(oldAttrValue.getDate());
            }
            else {
                attrHistory.attrOldValue = null;
            }
            if (attrValue.getDate() != null) {
                attrHistory.attrValue = versionDateFormat.format(attrValue.getDate());
            }
            else {
                attrHistory.attrValue = null;
            }
        }
    }

    public void setXMLGregorianCalendarWithHistory(IDfPersistentObject document, String attrName, XMLGregorianCalendar attrXmlValue) throws DfException {
        IDfTime attrValue = DfTime.DF_NULLDATE;
        if (attrXmlValue != null) {
            attrValue = new DfTime(attrXmlValue.toGregorianCalendar().getTime());
        }
        IDfTime oldAttrValue = document.getTime(attrName);
        if (!oldAttrValue.equals(attrValue)) {
            document.setTime(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue.getDate() != null) {
                attrHistory.attrOldValue = versionDateFormat.format(oldAttrValue.getDate());
            }
            else {
                attrHistory.attrOldValue = null;
            }
            if (attrValue.getDate() != null) {
                attrHistory.attrValue = versionDateFormat.format(attrValue.getDate());
            }
            else {
                attrHistory.attrValue = null;
            }
        }
    }

    public void setRepeatingXMLGregorianCalendarWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex, XMLGregorianCalendar attrXmlValue) throws DfException {
        IDfTime attrValue = DfTime.DF_NULLDATE;
        if (attrXmlValue != null) {
            attrValue = new DfTime(attrXmlValue.toGregorianCalendar().getTime());
        }
        IDfTime oldAttrValue = DfTime.DF_NULLDATE;
        if (document.getValueCount(attrName) > attrValueIndex) {
            oldAttrValue = document.getRepeatingTime(attrName, attrValueIndex);
        }
        if (!oldAttrValue.equals(attrValue)) {
            if (document.getValueCount(attrName) > attrValueIndex) {
                document.setRepeatingTime(attrName, attrValueIndex, attrValue);
            }
            else {
                document.appendTime(attrName, attrValue);
            }
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue.getDate() != null) {
                attrHistory.attrOldValue = versionDateFormat.format(oldAttrValue.getDate());
            }
            else {
                attrHistory.attrOldValue = null;
            }
            if (attrValue.getDate() != null) {
                attrHistory.attrValue = versionDateFormat.format(attrValue.getDate());
            }
            else {
                attrHistory.attrValue = null;
            }
        }
    }

	public void cleanRepeatingXMLGregorianCalendarWithHistory(IDfPersistentObject document, String attrName,
			int attrValueIndex) throws DfException {
		if (document.getValueCount(attrName) > attrValueIndex) {
			IDfTime oldAttrValue = document.getRepeatingTime(attrName, attrValueIndex);
			document.remove(attrName, attrValueIndex);
			AttrHistory attrHistory = prepareAttrHistory(attrName);
			if (oldAttrValue.getDate() != null) {
				attrHistory.attrOldValue = versionDateFormat.format(oldAttrValue.getDate());
			} else {
				attrHistory.attrOldValue = null;
			}
			attrHistory.attrValue = null;
		}
	}

    public void setBooleanWithHistory(IDfPersistentObject document, String attrName, Boolean attrValueObject) throws DfException {
        boolean attrValue = attrValueObject != null ? attrValueObject : false;
        boolean oldAttrValue = document.getBoolean(attrName);
        if (oldAttrValue != attrValue) {
            document.setBoolean(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            attrHistory.attrOldValue = oldAttrValue ? "Да" : "Нет";
            attrHistory.attrValue = attrValue ? "Да" : "Нет";
        }
    }

    public void setRepeatingBooleanWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex, Boolean attrValueObject) throws DfException {
        boolean attrValue = attrValueObject != null ? attrValueObject : false;
        Boolean oldAttrValue = null;
        if (document.getValueCount(attrName) > attrValueIndex) {
            oldAttrValue = document.getRepeatingBoolean(attrName, attrValueIndex);
        }
        if (oldAttrValue == null || oldAttrValue.booleanValue() != attrValue) {
            if (document.getValueCount(attrName) > attrValueIndex) {
                document.setRepeatingBoolean(attrName, attrValueIndex, attrValue);
            }
            else {
                document.appendBoolean(attrName, attrValue);
            }
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue != null) {
                attrHistory.attrOldValue = oldAttrValue.booleanValue() ? "Да" : "Нет";
            }
            else {
                attrHistory.attrOldValue = null;
            }
            attrHistory.attrValue = attrValue ? "Да" : "Нет";
        }
    }

    public void cleanRepeatingBooleanWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex) throws DfException {
		if (document.getValueCount(attrName) > attrValueIndex) {
			boolean oldAttrValue = document.getRepeatingBoolean(attrName, attrValueIndex);
			document.remove(attrName, attrValueIndex);
			AttrHistory attrHistory = prepareAttrHistory(attrName);
			attrHistory.attrOldValue = oldAttrValue ? "Да" : "Нет";
			attrHistory.attrValue = null;
		}
    }

    public void setIntWithHistory(IDfPersistentObject document, String attrName, Integer attrValueObject) throws DfException {
        int attrValue = attrValueObject != null ? attrValueObject : 0;
        int oldAttrValue = document.getInt(attrName);
        if (oldAttrValue != attrValue) {
            document.setInt(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            attrHistory.attrOldValue = "" + oldAttrValue;
            attrHistory.attrValue = "" + attrValue;
        }
    }

    public void setRepeatingIntWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex, Integer attrValueObject) throws DfException {
        int attrValue = attrValueObject != null ? attrValueObject : 0;
        Integer oldAttrValue = null;
        if (document.getValueCount(attrName) > attrValueIndex) {
            oldAttrValue = document.getRepeatingInt(attrName, attrValueIndex);
        }
        if (oldAttrValue == null || oldAttrValue.intValue() != attrValue) {
            if (document.getValueCount(attrName) > attrValueIndex) {
                document.setRepeatingInt(attrName, attrValueIndex, attrValue);
            }
            else {
                document.appendInt(attrName, attrValue);
            }
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue != null) {
                attrHistory.attrOldValue = "" + oldAttrValue;
            }
            else {
                attrHistory.attrOldValue = null;
            }
            attrHistory.attrValue = "" + attrValue;
        }
    }

	public void cleanRepeatingIntWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex)
			throws DfException {
		if (document.getValueCount(attrName) > attrValueIndex) {
			int oldAttrValue = document.getRepeatingInt(attrName, attrValueIndex);
			document.remove(attrName, attrValueIndex);
			AttrHistory attrHistory = prepareAttrHistory(attrName);
			attrHistory.attrOldValue = "" + oldAttrValue;
			attrHistory.attrValue = null;
		}
	}

    public void setBigDecimalWithHistory(IDfPersistentObject document, String attrName, BigDecimal attrBigDecimalValue) throws DfException {
        double oldAttrValue = document.getDouble(attrName);
        double attrValue = 0;
        if (attrBigDecimalValue != null) {
            attrValue = attrBigDecimalValue.doubleValue();
        }
        if (oldAttrValue != attrValue) {
            document.setDouble(attrName, attrValue);
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            attrHistory.attrOldValue = "" + oldAttrValue;
            attrHistory.attrValue = "" + attrValue;
        }
    }

    public void setRepeatingBigDecimalWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex, BigDecimal attrBigDecimalValue) throws DfException {
        Double oldAttrValue = null;
        double attrValue = 0;
        if (attrBigDecimalValue != null) {
            attrValue = attrBigDecimalValue.doubleValue();
        }
        if (document.getValueCount(attrName) > attrValueIndex) {
            oldAttrValue = document.getRepeatingDouble(attrName, attrValueIndex);
        }
        if (oldAttrValue == null || oldAttrValue.doubleValue() != attrValue) {
            if (document.getValueCount(attrName) > attrValueIndex) {
                document.setRepeatingDouble(attrName, attrValueIndex, attrValue);
            }
            else {
                document.appendDouble(attrName, attrValue);
            }
            AttrHistory attrHistory = prepareAttrHistory(attrName);
            if (oldAttrValue != null) {
                attrHistory.attrOldValue = "" + oldAttrValue;
            }
            else {
                attrHistory.attrOldValue = null;
            }
            attrHistory.attrValue = "" + attrValue;
        }
    }

    public void cleanRepeatingBigDecimalWithHistory(IDfPersistentObject document, String attrName, int attrValueIndex) throws DfException {
		if (document.getValueCount(attrName) > attrValueIndex) {
			double oldAttrValue = document.getRepeatingDouble(attrName, attrValueIndex);
			document.remove(attrName, attrValueIndex);
			AttrHistory attrHistory = prepareAttrHistory(attrName);
			attrHistory.attrOldValue = "" + oldAttrValue;
			attrHistory.attrValue = null;
		}
    }

    public AttrHistory prepareAttrHistory(String attrName) {
        AttrHistory result = new AttrHistory(attrName);
        getAttrHistoryList().add(result);
        return result;
    }

    public List<AttrHistory> getAttrHistoryList() {
        if (attrHistoryList == null) {
            attrHistoryList = new ArrayList<AttrHistory>();
        }
        return attrHistoryList;
    }

    public class AttrHistory {

        public String attrName = null;
        public String attrDescription = null;
        public String attrValue = null;
        public String attrOldValue = null;

        //todo получать attrDescription из файла ресурсов
        public AttrHistory(String attrName) {
            this.attrName = attrName;
            this.attrDescription = attrName;
        }
    }
}
