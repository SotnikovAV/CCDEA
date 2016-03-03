package ru.rb.ccdea.adapters.mq.utils;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

public class XmlContentProcessor {

    private Class xmlContentClass = null;

    public XmlContentProcessor(Class xmlContentClass) {
        this.xmlContentClass = xmlContentClass;
    }

    public Object unmarshalSysObjectContent(IDfSysObject sysObject, UnifiedResult result) {
        try {
            JAXBContext jc = JAXBContext.newInstance(xmlContentClass);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setEventHandler(
                    new ValidationEventHandler() {
                        @Override
                        public boolean handleEvent(ValidationEvent event) {
                            DfLogger.error(this, "Unmarshal error: " + event.getMessage(), null, event.getLinkedException());
                            return false;
                        }
                    }
            );
            /*
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                unmarshaller.setSchema(schemaFactory.newSchema(Class.forName("ru.rb.ccdea.adapters.mq.binding.docput.DocPutType").getClassLoader().getResource("docPut.xsd")));
            }
            catch (Exception ex) {
                DfLogger.error(this, "Schema error", null, ex);
            }
            */
            return unmarshaller.unmarshal(new StreamSource(sysObject.getContent()), xmlContentClass).getValue();
        }
        catch (Exception ex) {
            result.setError(UnifiedResult.VALIDATE_ERROR_CODE, "Validation error: " + ex.getMessage());
            DfLogger.error(this, "Validation error", null, ex);
            return null;
        }
    }

}
