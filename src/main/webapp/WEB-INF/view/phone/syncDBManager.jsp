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
<script type="text/javascript" src="<%=basePath %>resource/js/jquery-3.3.1.js"></script>
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
//var tenantId="sc21090414";
//var userId="test";
//var password="test";
var tenantId='${requestScope.tenantId }';
var userId='${requestScope.userId }';
var password='${requestScope.password }';
$(function(){
	login();
});

function login(){
	$.post(phonePath+"login",
		{tenantId:tenantId,userId:userId,password:password},
		function(data){
			if(data.status=="ok"){
				makeSync();
			}
		}
	,"json");
}

function makeSync(){
	$.post(phonePath+"syncDBManager/makeSync",
		function(){
		
		}
	,"json");
}
</script>
<title>Insert title here</title>
</head>
<body>

</body>
</html>