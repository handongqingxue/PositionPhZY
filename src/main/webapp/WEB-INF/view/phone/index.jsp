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
var path='<%=basePath %>';
var phonePath=path+"phone/";
$(function(){
	
});

function goPage(page){
	switch (page) {
	case "bjtj":
		location.href=phonePath+"goWarnCount";
		break;
	}
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.bottom_div{
	width: 100%;height: 50px;line-height: 50px;background-color: #eee;bottom: 0;position: fixed;
}
.bottom_div .item_div{
	width: 25%;height: 50px;text-align: center;
}
.bottom_div .ryss_div{
	margin-top: -50px;margin-left: 25%;
}
.bottom_div .gjfx_div{
	margin-top: -50px;margin-left: 50%;
}
.bottom_div .bjtj_div{
	margin-top: -50px;margin-left: 75%;
}
</style>
<title>首页</title>
</head>
<body>
<div style="width: 100%;height: 500px;text-align: center;">这是显示定位地图
</div>
<div class="bottom_div">
	<div class="item_div" onclick="goPage('ssdw')">实时定位</div>
	<div class="item_div ryss_div">人员搜索</div>
	<div class="item_div gjfx_div">轨迹分析</div>
	<div class="item_div bjtj_div" onclick="goPage('bjtj')">报警统计</div>
</div>
</body>
</html>