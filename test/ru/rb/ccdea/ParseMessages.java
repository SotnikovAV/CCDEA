/**
 * 
 */
package ru.rb.ccdea;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import ru.rb.ccdea.adapters.mq.binding.contract.MCDocInfoModifyContractType;
import ru.rb.ccdea.adapters.mq.binding.docput.DocPutType;
import ru.rb.ccdea.adapters.mq.binding.passport.MCDocInfoModifyPSType;
import ru.rb.ccdea.adapters.mq.binding.pd.MCDocInfoModifyPDType;
import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;
import ru.rb.ccdea.adapters.mq.binding.spd.MCDocInfoModifySPDType;
import ru.rb.ccdea.adapters.mq.binding.svo.MCDocInfoModifySVOType;

/**
 * @author ER19391
 *
 */
public class ParseMessages {

	@Test
	public void test() {
		File parentFolder = new File("C:/Development/temp/ccdea");
		for (File folder : parentFolder.listFiles()) {
			if (folder.isDirectory()) {

				for (File file : folder.listFiles()) {
					try {
						System.out.print(file.getAbsolutePath() + " : ");
						Class<?> clazz = null;
						String folderName = folder.getName();
						if ("docPut".equals(folderName)) {
							clazz = DocPutType.class;
						} else if ("MCDocInfoModifyCO".equals(folderName)) {
							clazz = MCDocInfoModifyContractType.class;
						} else if ("MCDocInfoModifyPD".equals(folderName)) {
							clazz = MCDocInfoModifyPDType.class;
						} else if ("MCDocInfoModifySPD".equals(folderName)) {
							clazz = MCDocInfoModifySPDType.class;
						} else if ("MCDocInfoModifyPS".equals(folderName)) {
							clazz = MCDocInfoModifyPSType.class;
						} else if ("MCDocInfoModifySVO".equals(folderName)) {
							clazz = MCDocInfoModifySVOType.class;
						} else if ("MCDocInfoModifyZA".equals(folderName)) {
							clazz = MCDocInfoModifyZAType.class;
						}
						JAXBContext jc = JAXBContext.newInstance(clazz);
						Unmarshaller unmarshaller = jc.createUnmarshaller();
						Object xmlObject = unmarshaller.unmarshal(new StreamSource(file), clazz).getValue();
						System.out.println(xmlObject.toString());
					} catch (JAXBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Test
	public void testParseMessage() {
		try {
			String filePath = "c:/Development/Workspaces/CCDEA_GITHUB/test/Contract/contract1.xml";
			System.out.println(filePath);

			JAXBContext jc = JAXBContext.newInstance(MCDocInfoModifyContractType.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			MCDocInfoModifyContractType xmlObject = unmarshaller
					.unmarshal(new StreamSource(filePath), MCDocInfoModifyContractType.class).getValue();
			
			XMLGregorianCalendar xmlCalendar = xmlObject.getContractDetails().getDocDate();
			xmlCalendar.setTimezone(0);
			System.out.println(xmlCalendar.toString());
			System.out.println(xmlCalendar.getTimeZone(xmlCalendar.getTimezone()));
			
			GregorianCalendar c = xmlCalendar.toGregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
						
			System.out.println(c.getTime());
			
			
			

			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
