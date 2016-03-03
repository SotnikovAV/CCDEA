package ru.rb.ccdea.control;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;

/**
 * Контрол для тега, отображающего апплет для печати.
 * Created by ER21595 on 11.06.2015.
 */
public class PrintControl extends Control {
    private boolean print= false;
    private String objectToPrint ="";
    private String contentSizes ="";
    private String contentNames ="";
    private IDfSession session;
    private int contentCount =0;

    public void setSession(IDfSession session) {
        this.session = session;
    }

    public void setObjectToPrint(String objectToPrint) {
        this.objectToPrint = objectToPrint;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean canPrint(){ return print;}

    public String getObjectsToPrint(){
        return objectToPrint;
    }

    public String getContentSizes() {
        return contentSizes;
    }

    public void setContentSizes(String contentSizes) {
        this.contentSizes = contentSizes;
    }

    public String getContentNames() {
        return contentNames;
    }

    public void setContentNames(String contentNames) {
        this.contentNames = contentNames;
    }

    public int getContentCount() {
        return contentCount;
    }

    public void setContentCount(int contentCount) {
        if(contentCount==0){
            this.contentCount=1;
        }else {
            this.contentCount = contentCount;
        }
    }

    public String  getLogin() throws DfException {
        return session.getLoginUserName();
    }

    public String getTicket() throws DfException {
        return session.getLoginTicket();
    }

    public String getDocbaseName() throws DfException {
        return session.getDocbaseName();
    }

    public String getServletUrl(){
        return "http://" + getPageContext().getRequest().getServerName() +
                ':' + getPageContext().getRequest().getServerPort() +
                Form.makeUrl(getPageContext().getRequest(), "/getcontent");
    }

    public String getAppletClass(){
        return "ru.rb.ccdea.applet.PrinterApplet.class";
    }

    public String getAppletCodebase(){
        return Form.makeUrl(getPageContext().getRequest(), "/ccdea/applet/" );
    }

    public String getAppletArchiveName(){
        return "ccdea-print-applet.jar";
    }
}
