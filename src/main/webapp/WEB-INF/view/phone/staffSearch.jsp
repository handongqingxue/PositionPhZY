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
var ssdwCanvas;
var ssdwCanvasMinWidth=720.52;
var ssdwCanvasMinHeight=670.49;
var ssdwCanvasMaxWidth=2841;
var ssdwCanvasMaxHeight=2643;
var ssdwCanvasStyleWidth=ssdwCanvasMinWidth;
var ssdwCanvasStyleHeight=ssdwCanvasMinHeight;
var ssdwCanvasWidth=ssdwCanvasMaxWidth;
var ssdwCanvasHeight=ssdwCanvasMaxHeight;
var widthScale;
var heightScale;
var arcR=20;
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
$(function(){
	jiSuanScale();
});

function jiSuanScale(){
	widthScale=ssdwCanvasStyleWidth/ssdwCanvasWidth;
	heightScale=ssdwCanvasStyleHeight/ssdwCanvasHeight;
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.main_div{
	width: 100%;height: 600px;overflow: auto;
}
.main_div .tool_div{
	width: 100%;
}
</style>
<title>人员搜索</title>
</head>
<body>
<div class="main_div" id="main_div">
	<div class="tool_div">
		人员<input type="text" id="entityName"/>
		<input type="button" value="搜索"/>
	</div>
	<canvas id="ssdwCanvas">
	</canvas>
</div>
</body>
</html>