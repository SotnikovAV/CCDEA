<%--
~  ******************************************************************************
~  * Copyright ? 2008, EMC Corporation.  All Rights Reserved.
~  ******************************************************************************
~  *
~  * Project        WDK
~  * File           genericxform.jsp
~  * Description
~  * Created on     06/02/2008
~  * Tab width      3
~  *
~  ******************************************************************************
~
--%>
<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ page import="com.documentum.webcomponent.xforms.XFormsComponent" %>
<%@ page import="com.documentum.web.form.Form"%>
<%@ page import="ru.rb.ccdea.xforms.PropertiesComponent"%>

<dmf:html>
<dmf:head>

    <dmf:webform/>

</dmf:head>
<dmf:body>
<dmf:form>
    <table><tr>
    <td align="right">
        <table><tr>
        <td>
                <dmfx:actionbutton action="ucfview" nlsid="MSG_SHOW_CONTENT">
                    <dmfx:argument name="objectId" contextvalue="contentObjectId"/>
                    <dmf:argument name="type" value="ccdea_doc_content"/>
                </dmfx:actionbutton>
        </td>
        <td>
                <dmfx:actionbutton action="changeDossier" nlsid="MSG_CHANGE_DOSSIER">
                    <dmfx:argument name="objectId" contextvalue="objectId"/>
                </dmfx:actionbutton>
        </td>
        <td>
                <dmf:button name="closeDossierButton" onclick="onCloseDossier" nlsid="MSG_CLOSE_DOSSIER"/>
        </td>
        <td>
                <dmf:button name="openDossierButton" onclick="onOpenDossier" nlsid="MSG_OPEN_DOSSIER"/>
        </td>
        </tr>
        </table>
    </td>
    </tr>
    <tr><td>

    <dmfx:xforms name='<%=XFormsComponent.XFORM%>' autoResize='true' style='overflow: auto; width: 100%; height: 100%'/>

    </td></tr>
    </table>
    <% PropertiesComponent component = (PropertiesComponent)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);%>
    <script>
    <%=component.getRefreshScript()%>
    </script>
</dmf:form>
</dmf:body>
</dmf:html>
