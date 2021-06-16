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
<!-- https://www.cnblogs.com/chenjy1225/p/10924293.html -->
<!-- https://www.jb51.net/article/165400.htm -->
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
$(function(){
	/*
	var c=document.getElementById("canvas");
	var cxt=c.getContext("2d");
	var img=new Image()
	var img2=new Image()
	img.src=path+"resource/image/001.png"
	img2.src=path+"resource/image/002.png"
	// 等待加载完成再绘制
    img.onload = function(){
        cxt.drawImage(img, 0,0,50, 50);
    }
    img2.onload = function(){
        cxt.drawImage(img2, 10,0,50, 50);
    }
    */
    
    var point_img = document.getElementById("point_img");
    var canvasBg = document.getElementById("bg1");
	var canvas = document.getElementById("myCanvas");
	ctx = canvas.getContext("2d");
	// 制作背景图
	var patBg = ctx.createPattern(canvasBg, "repeat");
	ctx.rect(0, 0, 750, 1180);
	ctx.fillStyle = patBg;
	ctx.fill();

	ctx.drawImage(point_img, 40, 40, 10, 10);
	ctx.drawImage(point_img, 140, 40, 10, 10);
	ctx.drawImage(point_img, 240, 40, 10, 10);
});

function small(){
	var myCanvas=$("#myCanvas");
	var width=myCanvas.css("width");
	myCanvas.css("width","250px");
	$("#myCanvas").css("height","250px");
}

function big(){
	$("#myCanvas").css("width","500px");
	$("#myCanvas").css("height","500px");
}

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
<canvas id="myCanvas" style="width:367px;height:325px;background-size: 100%, 100%;">
</canvas>
<img src="<%=basePath %>resource/image/003.jpg" id="bg1" alt="" style="display: none;">
<img src="<%=basePath %>resource/image/004.png" id="point_img" alt="" style="display: none;">
<input type="button" value="缩小" onclick="small();"/>
<input type="button" value="放大" onclick="big();"/>
</div>
<div class="bottom_div">
	<div class="item_div" onclick="goPage('ssdw')">实时定位</div>
	<div class="item_div ryss_div">人员搜索</div>
	<div class="item_div gjfx_div">轨迹分析</div>
	<div class="item_div bjtj_div" onclick="goPage('bjtj')">报警统计</div>
</div>
</body>
</html>