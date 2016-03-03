package ru.rb.ccdea.applet;

import ru.rb.ccdea.concurrent.PrintDocumentRunner;

import java.applet.Applet;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * Апплет, получающий от сервлета документ (пока только пдф)
 * и отправляющий его на печать.<br/>
 * Перед получением документа выдаёт пользователю диалог печати<br/>
 * Параметры<br/>
 * <ul>
 *  <li>objectId --- Ид документа для печати</li>
 *  <li>objectIds --- Иды нескольких документов, через запятую, без пробелов </li>
 *  <li>login --- имя пользователя</li>
 *  <li>loginTicket --- тикет пользователя. рекомендуется генерировать долгоиграющий тикет</li>
 *  <li>servletUrl --- адрес работающего сервлета</li>
 *  <li>docbaseName --- имя репозитория</li>
 * <ul/>
 * Created by AndrievskyAA on 18.05.2015.
 */
public class PrinterApplet extends Applet {
    private static final int rowHeight = 20;
    private static final int strOffset = 5;
    private Graphics bufferGraphics;
    private Image offscreen;
    private Dimension dim;

    private String status="";
    private List<PrintDocumentRunner> runners = new ArrayList<PrintDocumentRunner>();
    private boolean drawStatus = true;

    @Override
    public void init() {
        System.out.println("Printer applet initialization...");
        dim = getSize();
        offscreen = createImage(dim.width, dim.height);
        bufferGraphics =offscreen.getGraphics();
    }

    @Override
    public void start() {
        super.start();
        System.out.println("Printer applet starting...");
        
        String objectId = getParameter("objectId");
        System.out.println("objectId = " + objectId);
        
        String objectIds = getParameter("objectIds");
        System.out.println("objectIds = " + objectIds);
        
        String login = getParameter("login");
        System.out.println("login = " + login);
        
        String loginTicket = getParameter("loginTicket");
        System.out.println("loginTicket = " + loginTicket);
        
        String servletUrl = getParameter("servletUrl");
        System.out.println("servletUrl = " + servletUrl);
        
        String docbaseName = getParameter("docbaseName");
        System.out.println("docbaseName = " + docbaseName);
        
        String contentSizes = getParameter("sizes");
        System.out.println("contentSizes = " + contentSizes);
        
        String contentNames = getParameter("names");
        System.out.println("contentNames = " + contentNames);
        
        StringBuilder servletWithParams = new StringBuilder(servletUrl);
        servletWithParams.append("?login=").append(login);
        servletWithParams.append("&ticket=").append(loginTicket);
        servletWithParams.append("&docbasename=").append(docbaseName);
        servletWithParams.append("&objectId=");
        System.out.println("login = " + login);
        
		if (objectIds == null) {
			objectIds = objectId;
		}
		if (objectIds != null && objectIds.length() > 0) {
            try {
                PrinterJob pj = PrinterJob.getPrinterJob();
                printMessage("Выберите принтер");
                if (pj.printDialog()) {
                    ExecutorService pool =  Executors.newCachedThreadPool();
                    String[] ids = objectIds.split(",");
                    String[] sizes = contentSizes.split(",");
                    String[] names = contentNames.split(",");
                    
                    if(names.length != ids.length) {
                    	names  = Arrays.copyOf(ids, ids.length);
                    }
                    
					System.out.println("ids.length = " + ids.length + "; sizes.length = " + sizes.length
							+ "; names.length = " + names.length);

                    List<PrintDocumentRunner> results =new ArrayList<PrintDocumentRunner>();
                    for (int i = 0; i < ids.length; i++) {
                        PrintDocumentRunner r = new PrintDocumentRunner(names[i],
                                servletWithParams.toString() + ids[i],Long.parseLong(sizes[i]),pj);
                        runners.add(r);
                        results.add(r);
                    }
                    
                    Thread t = new Thread(new PrintRunner());
                    t.start();
                    
                    boolean finished = false;
                    while(!finished){
                        finished = true;
                        for(PrintDocumentRunner f:results){
                            finished = finished && f.isDone();
                        }
                        drawStatus=false;
                        repaint();
                        try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
                    }
                   
                }
            } catch (MalformedURLException e) {
                drawStatus = true;
                printMessage(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                drawStatus = true;
                printMessage(e.getMessage());
                e.printStackTrace();
            } 
//            catch (InterruptedException e) {
//                drawStatus = true;
//                printMessage(e.getMessage());
//                e.printStackTrace();
//            }
        } else {
            printMessage("У выбранных документов нечего распечатывать.");
        }
        System.out.println("Printer applet finished...");
    }
    
    private class PrintRunner implements Runnable {

		@Override
		public void run() {
			for(PrintDocumentRunner r:runners) {
            	r.execute();
            }
		}
    	
    }

    @Override
    public void stop() {
        System.out.println("Printer applet stopping...");
        super.stop();
    }

    private void printMessage(String msg){
        status=msg;
        repaint();
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        bufferGraphics.clearRect(0, 0, dim.width, dim.height);
        bufferGraphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        if(drawStatus) {
            drawStr(bufferGraphics,status,0,true);
        }else {
            for (int i = 0; i < runners.size(); i++) {
                PrintDocumentRunner rr = runners.get(i);
                //if(rr.isStatusChanged()) {
                //bufferGraphics.clearRect(1,i*15,getWidth()-2,15);
                drawStr(bufferGraphics, rr.getName(), i, true);
                bufferGraphics.setColor(Color.green);
                int length = Math.round(getWidth() * rr.getPercent() / 2);
                bufferGraphics.fillRect(Math.round(getWidth() / 2), i * rowHeight + 1, length - 1, rowHeight-2);
                bufferGraphics.setColor(Color.black);
                drawStr(bufferGraphics, rr.getStatusStr(), i,false);
                //}
            }
        }
        g.drawImage(offscreen, 0, 0, this);
    }

    private void drawStr(Graphics g,String str, int rownum, boolean firstColumn){
        int xOffset =strOffset;
        if(!firstColumn){
            xOffset+=Math.round(getWidth() / 2);
        }
        int yOffset = (rownum+1)*rowHeight-strOffset;
        g.drawString(str,xOffset,yOffset);
    }
}
