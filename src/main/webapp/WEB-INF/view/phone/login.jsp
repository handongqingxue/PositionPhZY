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
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<script type="text/javascript" src="<%=basePath %>resource/js/jquery-3.3.1.js"></script>
<script type="text/javascript">
$(function(){
	$.post("getCode",
		{},function(){
		
		}
	,"json");
	/*
	$.post("http://139.196.143.225:8081/position/public/embeded.smd",
		{"jsonrpc":"2.0","params":{"tenantId":"sc19070007","userId":"yyc"},"method":"getCode", "id":1},
		function(data){
			alert(data);
		}
	,"json");
	*/
});

function submit(){
	
}
</script>
<style type="text/css">
.loginPage_div{
	background-color:#fff;
}
.main_div{
	width: 300px;
	height: 250px;
	margin:65px auto 0;
}
  .title_h1{
    font-size:18px;
    text-align: center;
  }
  .username_div, .password_div {
    width: 230px;
    height: 48px;
    line-height: 48px;
    margin: 20px auto 0;
  }
  .username_inp_div,.password_inp_div{
    width: 200px;
    height: 48px;
    margin-top: -48px;
    margin-left: 28px;
    background-color: #fff;
    border-bottom: 3px solid #eee;
  }
  .username_inp,.password_inp{
    width: 180px;
    height: 45px;
    padding-left: 10px;
    padding-right: 10px;
    border: 0;
  }
  .loginBut_div{
    width: 230px;
    height: 38px;
    line-height: 38px;
    margin: 25px auto 0;
    font-size: 16px;
    color: #fff;
    text-align: center;
    background-color: #4caf50;
    border-radius: 4px;
  }
</style>
<title>Insert title here</title>
</head>
<body>
<div class="loginPage_div">
	<div class="main_div">
       <h1 class="title_h1">人员定位系统手机版</h1>
       <div class="username_div">
           <img alt="" src="<%=basePath %>resource/image/001.png" style="width: 20px;height:26px;">
           <div class="username_inp_div">
               <input class="username_inp" ref="username_inp" placeholder="请输入用户名"/>
           </div>
       </div>
       <div class="password_div">
           <img alt="" src="<%=basePath %>resource/image/002.png" style="width: 24px;height:23px;">
           <div class="password_inp_div">
               <input class="password_inp" ref="password_inp" type="password" placeholder="请输入密码"/>
           </div>
       </div>
       <div class="loginBut_div" onclick="submit()">登录</div>
   </div>
</div>
</body>
</html>