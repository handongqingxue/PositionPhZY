<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
	String basePath=request.getScheme()+"://"+request.getServerName()+":"
		+request.getServerPort()+request.getContextPath()+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
.rydwxtrk_div{
	text-align: center;
	font-size: 35px;
}
</style>
<title>Insert title here</title>
</head>
<body>
<div class="rydwxtrk_div">
	<a href="${requestScope.noPwdLoginUrl }">人员定位系统入口</a>
</div>
</body>
</html>