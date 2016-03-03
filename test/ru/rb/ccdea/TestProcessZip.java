/**
 * 
 */
package ru.rb.ccdea;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

/**
 * @author ER19391
 *
 */
public class TestProcessZip {

	
	public static void main(String [] args) {
		ArchiveInputStream archiveIn = null;
		InputStream is = null;
		try {
			is = new FileInputStream("c:/Development/Workspaces/CCDEA/TestData/arj.arj");
			archiveIn = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is);
						
			ArchiveEntry zipEntry = null;
			int entryIndex = 0;
			while ((zipEntry = archiveIn.getNextEntry()) != null) {
				String zipEntryName = zipEntry.getName();
              if (zipEntryName.trim().length() > 3) {
                  String zipEntryType = zipEntryName.substring(zipEntryName.length() - 3);
                  System.out.println("name = " + zipEntryName + " ; type = " + zipEntryType);
              }
              else {
              	System.out.println("Wrong format found in archive entry: name = " + zipEntryName);
              }
			}

		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(archiveIn != null) {
				try {
					archiveIn.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
//		ZipArchiveInputStream zipInputStream = null;
//		InputStream is = null;
//		try {
//			is = new FileInputStream("c:/Development/Workspaces/CCDEA/TestData/blank_spravka-currency.zip");
//			zipInputStream = new ZipArchiveInputStream(is);
//			
//			ZipArchiveEntry zipEntry = null;
//			int entryIndex = 0;
//			while ((zipEntry = zipInputStream.getNextZipEntry()) != null) {
//				String zipEntryName = zipEntry.getName();
//              if (zipEntryName.trim().length() > 3) {
//                  String zipEntryType = zipEntryName.substring(zipEntryName.length() - 3);
//                  System.out.println("name = " + zipEntryName + " ; type = " + zipEntryType);
//              }
//              else {
//              	System.out.println("Wrong format found in archive entry: name = " + zipEntryName);
//              }
//			}
//
//		} catch (IOException ioEx) {
//			ioEx.printStackTrace();
//		} finally {
//			if(is != null) {
//				try {
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if(zipInputStream != null) {
//				try {
//					zipInputStream.close();
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
		
//		InputStream is = null;
//		try {
//			is = new FileInputStream("c:/Development/Workspaces/CCDEA/TestData/blank_spravka-currency.zip");
//            ZipInputStream zipInputStream = new ZipInputStream(is);
//            ZipEntry zipEntry = null;
//            int entryIndex = 0;
//            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
//                String zipEntryName = zipEntry.getName();
//                if (zipEntryName.trim().length() > 3) {
//                    String zipEntryType = zipEntryName.substring(zipEntryName.length() - 3);
//                    System.out.println("name = " + zipEntryName + " ; type = " + zipEntryType);
//                }
//                else {
//                	System.out.println("Wrong format found in archive entry: name = " + zipEntryName);
//                }
//            }
//        }
//        catch (IOException ioEx) {
//            ioEx.printStackTrace();
//        } finally {
//        	if(is != null) {
//        		try {
//					is.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	}
//        }
	}
	
}
