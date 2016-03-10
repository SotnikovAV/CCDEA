package ru.rb.ccdea.concurrent;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import ru.rb.ccdea.net.CountingUrlWrapper;
import ru.rb.ccdea.pdf.CPDDocument;

/**
 * Обработчик отдельного документа.<br/>
 * Получает пдф от сервлета и отправляет его на печать.<br/>
 * В любой момент времени можно узнать статус задания.
 *
 * Created by ER21595 on 16.06.2015.
 */
public class PrintDocumentRunner implements Runnable {

	private String name;
	private CountingUrlWrapper documentUrl = null;
	private PrinterJob pj = null;
	private long contentLength = 0;
	private volatile String errorStatus = null;
	private volatile String status = null;
	private volatile boolean haveError = false;
	private boolean statusChanged = true;
	private boolean done = false;

	public PrintDocumentRunner(String name, String url, long contentSize, PrinterJob job) throws MalformedURLException {
		contentLength = contentSize;
		this.name = createName(name, contentSize);
		documentUrl = new CountingUrlWrapper(url);
		pj = job;
		status = "Инициализация ";
	}

	public String getName() {
		return name;
	}

	private String createName(String fileName, Long fileSize) {
		String dimension = "б";
		int divisor = 1;
		if (fileSize > 1024 * 1024) {
			dimension = "Мб";
			divisor = 1024 * 1024;
		} else if (fileSize > 1024) {
			dimension = "Кб";
			divisor = 1024;
		}
		if (fileSize / divisor > 10) {
			return fileName + " (" + fileSize / divisor + " " + dimension + ")";
		} else {
			return fileName + " (" + Math.round(fileSize * 10 / divisor) / 10.0 + " " + dimension + ")";
		}
	}

	public boolean isStatusChanged() {
		if (!"Загрузка документа: ".equals(status) && statusChanged) {
			statusChanged = false;
			return true;
		} else {
			return statusChanged;
		}
	}

	public float getPercent() {
		if (documentUrl.getStream() == null) {
			return 0;
		}
		return (float) (Math.round(documentUrl.getStream().getByteCount() * 100 / contentLength) / 100.0);
	}

	public String getStatusStr() {
		if (haveError) {
			return errorStatus;
		} else if ("Загрузка документа: ".equals(status)) {
			return status + getPercent() * 100 + "%";
		} else {
			return status;
		}
	}

	public void run() {
		execute();
	}

	public void execute() {
		try {
			status = "Загрузка документа: ";
			PDDocument document = CPDDocument.load(documentUrl);

			status = "Подготовка документа ";
			final List<PDPage> pdfPages = document.getDocumentCatalog().getAllPages();

			statusChanged = true;
			pj.setPrintable(new Printable() {

				@Override
				public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
					if (page >= pdfPages.size())
						return NO_SUCH_PAGE;
					try {
						g.drawImage(pdfPages.get(page).convertToImage(), (int) pf.getImageableX(),
								(int) pf.getImageableY(), (int) pf.getImageableWidth(), (int) pf.getImageableHeight(),
								null);
					} catch (IOException e) {
						e.printStackTrace();
						throw new PrinterException(e.toString()); 
					}
					return PAGE_EXISTS;
				}
			});
			status = "Отправка на принтер ";
			statusChanged = true;
			PrinterException ex = new PrinterException("Начинаем печать ...");
			int counter = 0;
			while (ex != null) {
				try {
					ex = null;
					pj.print();
				} catch (PrinterException exp) {
					if (counter > 10) {
						throw exp;
					}
					ex = exp;
					ex.printStackTrace();
					counter++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			status = "Документ отправлен на принтер";
			statusChanged = true;
		} catch (PrinterException e) {
			e.printStackTrace();
			errorStatus = e.getMessage();
			haveError = true;
		} catch (IOException e) {
			e.printStackTrace();
			errorStatus = e.getMessage();
			if (e.getMessage().startsWith("Error: Header doesn't contain versioninfo")) {
				errorStatus = "Ошибка: Формат содержимого не PDF.";
			}
			haveError = true;
		} finally {
			done = true;
		}
	}

	public boolean isDone() {
		return done;
	}

	// The number of CMs per Inch
	public static final double CM_PER_INCH = 0.393700787d;
	// The number of Inches per CMs
	public static final double INCH_PER_CM = 2.545d;
	// The number of Inches per mm's
	public static final double INCH_PER_MM = 25.45d;

	/**
	 * Converts the given pixels to cm's based on the supplied DPI
	 *
	 * @param pixels
	 * @param dpi
	 * @return
	 */
	public static double pixelsToCms(double pixels, double dpi) {
		return inchesToCms(pixels / dpi);
	}

	/**
	 * Converts the given cm's to pixels based on the supplied DPI
	 *
	 * @param cms
	 * @param dpi
	 * @return
	 */
	public static double cmsToPixel(double cms, double dpi) {
		return cmToInches(cms) * dpi;
	}

	/**
	 * Converts the given cm's to inches
	 *
	 * @param cms
	 * @return
	 */
	public static double cmToInches(double cms) {
		return cms * CM_PER_INCH;
	}

	/**
	 * Converts the given inches to cm's
	 *
	 * @param inch
	 * @return
	 */
	public static double inchesToCms(double inch) {
		return inch * INCH_PER_CM;
	}
}
