<%@ page import="com.documentum.web.form.Form"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/dmimg_1_0.tld" prefix="dmimg" %>

<dmf:html>
<dmf:head>
<dmf:webform/>
<script type='text/javascript' src='<%=Form.makeUrl(request, "/wdk/include/paneset.js")%>'></script>
<script type='text/javascript' src='/taskspace/taskspace/include/splitpane.js'></script>
</dmf:head>
<dmf:body>
<dmf:form>

<div id="tabs_area" style="overflow:visible">

<div class="taskspaceOpenItemNavBackground" style="height:30px;" id="tabsRow">
<div class="modalnavbg tskMgrContainerLeftPadding" style="padding-top:6px;">
<dmf:tabbar name="tabs" value="dossier_tab" onclick="onTabSelected" tabposition='top' align="left"/>
</div>
</div>

<div class="contentBackground" id="tabInclude">
<dmfx:containerinclude/>
</div>

</div>
<script>
    var tabsDiv = document.getElementById("tabs_area");
    var tabsRow = document.getElementById("tabsRow");
    tabsRow.style.width=tabsDiv.offsetWidth;
</script>
</dmf:form>
</dmf:body>
</dmf:html>
