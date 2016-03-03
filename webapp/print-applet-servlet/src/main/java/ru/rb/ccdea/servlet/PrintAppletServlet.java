package ru.rb.ccdea.servlet;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Сервлет, выдающий содержимое документа (пока только pdf) по
 * http по запросу<br/>
 * Параметры запроса к сервлету:<br/>
 * <ul>
 *     <li>login --- имя пользователя</li>
 *     <li>ticket --- тикет пользователя. рекомендуется создавать долгоиграющие тикеты</li>
 *     <li>docbasename --- имя репозитория</li>
 *     <li>objectId --- ид объекта, контент которого будет возвращён</li>
 * </ul>
 * Created by AndrievskyAA on 18.05.2015.
 */
@WebServlet("/getcontent")
public class PrintAppletServlet extends HttpServlet {
    private Exception ex = null;
    private String objectName ="";

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ByteArrayInputStream content;
        content = getContentFromDocbase(req.getParameter("login"),
                                        req.getParameter("ticket"),
                                        req.getParameter("docbasename"),
                                        req.getParameter("objectId"));
        if(content!=null) {
            byte[] data = new byte[content.available()];
            ((HttpServletResponse) res).setStatus(200);
            if (content.read(data) > -1) {
                res.getOutputStream().write(data);
                res.setContentType("ru.rb.ccdea.pdf");
            } else {
                customExceptionReport(req, res, ex);
            }
        }else{
            customExceptionReport(req, res, ex);
        }
    }

    private void customExceptionReport(ServletRequest req, ServletResponse res, Throwable ex) throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Error!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("login: "+req.getParameter("login")+"<br/>");
        out.println("ticket: "+req.getParameter("ticket")+"<br/>");
        out.println("docbasename: "+req.getParameter("docbasename")+"<br/>");
        out.println("objectId: "+req.getParameter("objectId")+"<br/>");
        out.println("object name: "+objectName+"<br/>");
        ex.printStackTrace(out);
        out.println("</body>");
        out.println("</html>");

    }

    private ByteArrayInputStream getContentFromDocbase(String login, String ticket, String docbaseName, String objectId){
        System.out.println("getting content for object "+objectId+", for user "+login);
        //create client objects
        IDfClientX clientx = new DfClientX();
        IDfClient client;
        ByteArrayInputStream is = null;
        try{
            client = clientx.getLocalClient();
            //create a Session Manager object
            IDfSessionManager sMgr = client.newSessionManager();
            //create an IDfLoginInfo object for user creddentials
            IDfLoginInfo loginInfo = clientx.getLoginInfo();
            loginInfo.setUser(login);
            loginInfo.setPassword(ticket);
            //bind the Session Manager to the login info
            sMgr.setIdentity(docbaseName, loginInfo);
            IDfSession session = null;
            try{
            //get the IDfSession instance by using getSession or newSession
                session = sMgr.getSession(docbaseName);
                IDfSysObject obj = (IDfSysObject) session.getObject(new DfId(objectId));
                objectName = obj.getObjectName();
                is = obj.getContentEx2(obj.getContentType(), 0, "");
            } catch (DfException e){
                ex=e;
                DfLogger.error(this, e.getMessage(),e.getArguments(), e);
            }finally{
                if (session != null) {
                    sMgr.release(session);
                }
            }
        }catch (DfServiceException e) {
            ex = e;
            DfLogger.error(this, e.getMessage(), e.getArguments(), e);
        } catch (DfException e) {
            ex=e;
            DfLogger.error(this, e.getMessage(), e.getArguments(), e);
        }
        return is;
    }
}
