package ru.rb.ccdea.tags;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.form.Control;
import com.documentum.web.form.ControlTag;
import ru.rb.ccdea.control.PrintControl;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Тег, выдающий в отдельном окне апплет, печатающий контент.<br/>
 * Параметры печати задаются в соответствующем контроле.
 *
 *
 * Created by ER21595 on 11.06.2015.
 */
public class PrintTag extends ControlTag {
    private final static int rowHeight = 20;
    @Override
    protected Class<? extends Control> getControlClass() {
        return PrintControl.class;
    }

    @Override
    protected void renderEnd(JspWriter out) throws IOException, JspTagException {
        PrintControl control = (PrintControl) getControl();
        if(control.canPrint()){
            try {
                int tableHeight = control.getContentCount()*rowHeight;
                out.println("<script type=\"text/javascript\">");
                out.println("var w = 520;");
                out.println("var h = "+tableHeight+";");
                out.println("var left = (screen.width - w)/2;");
                out.println("var top = (screen.height - h)/2;");
                out.println("var printWindow=window.open(\"\",\"Print_"+System.currentTimeMillis()+"\",\"width=\"+w+\",height=\"+h+\",left=\"+left+\",top=\"+top);");
                out.println("printWindow.document.title=\"Печать документов\";");
                out.println("printWindow.document.write(\"<html><head><title>Печать документов</title></head><body>\");");

                out.println("printWindow.document.write(\"<applet code=\'" + control.getAppletClass() +
                        "\' archive=\'" + control.getAppletArchiveName() + "\' codebase=\'"+control.getAppletCodebase() +
                        "\' width=\'500\' height=\'" + tableHeight+"\'>\" );");
                out.println("printWindow.document.write(\"<param name=\'objectIds\' value=\'"+control.getObjectsToPrint()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'sizes\' value=\'"+control.getContentSizes()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'names\' value=\'"+control.getContentNames()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'login\' value=\'" + control.getLogin() + "\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'loginTicket\' value=\'"+control.getTicket()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'docbaseName\' value=\'"+control.getDocbaseName()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'servletUrl\' value=\'"+control.getServletUrl()+"\'/>\");");
                out.println("printWindow.document.write(\"<param name=\'cache_archive\' value=\'"+control.getAppletArchiveName()+"\'/>\");");
                out.println("printWindow.document.write(\"</applet></body></html>\");");
                out.println("</script>");
                control.setPrint(false);
            } catch (DfException e){
                DfLogger.error(this, e.getMessage(), null, e);
            }
        }
    }
}
