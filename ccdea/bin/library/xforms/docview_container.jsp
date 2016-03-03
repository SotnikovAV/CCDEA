<%@ page import="com.documentum.web.form.Form"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/dmimg_1_0.tld" prefix="dmimg" %>
<%@ page import="ru.rb.ccdea.xforms.PropertiesContainer" %>

<dmf:html>
<dmf:head>
<dmf:webform/>
<script type='text/javascript' src='<%=Form.makeUrl(request, "/wdk/include/paneset.js")%>'></script>
<script type='text/javascript' src='/taskspace/taskspace/include/splitpane.js'></script>
<script type='text/javascript' src='<%=Form.makeUrl(request, "/webcomponent/xforms/scripts/xformsdockingpanel.js")%>'></script>
<script type='text/javascript' src='<%=Form.makeUrl(request, "/taskspace/include/updatecontent.js")%>'></script>
<script type="text/javascript">
registerClientEventHandler(null, "launchUcf", onLaunchUcf);
function onLaunchUcf(action, objectId)
{
postServerEvent(null, null, null, "onaction", "action", action, "objectId", objectId);
}
</script>
</dmf:head>

<% PropertiesContainer component = (PropertiesContainer)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
%>
<dmf:frameset name="tabFrameset" rows="500,*" >
<dmf:frame name="<%=String.valueOf(System.currentTimeMillis())%>" src="<%=component.getContainedUrl()%>" scrolling="false"/>
<dmf:frame name="<%=String.valueOf(System.currentTimeMillis())%>" src="<%=component.getTabComponentUrl()%>" scrolling="true"/>
</dmf:frameset>

</dmf:html>
