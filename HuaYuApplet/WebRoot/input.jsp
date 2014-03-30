<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Pragma" content="no-cache"> 
	<meta http-equiv="Cache-Control" content="no-cache"> 
	<meta http-equiv="Expires" content="0">
<title>串口设置程序</title>
</head>
<body>
<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td>&nbsp;</td>
      </tr>
      <tr>
        <td>		<applet codebase="applet" width="200" height="25" 
						code="net.mate.comm.applet.AppletWeightTextInput.class"  archive="huyu_beta_v1.0.jar,comm.jar">
		  <param name="commPort" value="COM5" />
<param name="commAppname" value="HuaYuDiBang" />
<param name="commBaud" value="150" />
<param name="commDatabits" value="8" />
<param name="commStopbits" value="2" />
<param name="commParity" value="NONE" />
        </applet</td>
      </tr>
      <tr>
        <td>&nbsp;</td>
      </tr>
</table>
</body>
</html>
