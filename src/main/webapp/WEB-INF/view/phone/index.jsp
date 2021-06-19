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
<!-- https://segmentfault.com/a/1190000016819776 -->
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
var ssdwCanvas;
var ssdwCanvasStyleWidth=720.52;
var ssdwCanvasStyleHeight=670.49;
var ssdwCanvasWidth=2841;
var ssdwCanvasHeight=2643;
var widthScale;
var heightScale;
var arcR=20;
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
var xl=187;
$(function(){
	initSSDWCanvas();
	jiSuanScale();
	setInterval(function(){
		xl+=3;
		if(xl>300)
			xl=187;
		initSSDWCanvas(0);
	},"3000");
});

function jiSuanScale(){
	widthScale=ssdwCanvasStyleWidth/ssdwCanvasWidth;
	heightScale=ssdwCanvasStyleHeight/ssdwCanvasHeight;
}

function initSSDWCanvas(reSizeFlag){
	var ssdwCanvasImg = new Image();
	ssdwCanvasImg.src=path+"resource/image/003.jpg";
	ssdwCanvas = document.getElementById("ssdwCanvas");
	ssdwCanvas.style.width=ssdwCanvasStyleWidth+"px";
	ssdwCanvas.style.height=ssdwCanvasStyleHeight+"px";
	ssdwCanvas.width=ssdwCanvasWidth;
	ssdwCanvas.height=ssdwCanvasHeight;
	ssdwCanvasContext = ssdwCanvas.getContext("2d");
	ssdwCanvasImg.onload=function(){
		ssdwCanvasContext.drawImage(ssdwCanvasImg, 0, 0, ssdwCanvasWidth, ssdwCanvasHeight);
		setEntityLocation(ssdwCanvasContext,xl,448,"龚永强",1);
		//setEntityLocation(ssdwCanvasContext,268,443,"陈广银",1);
		if(reSizeFlag==1)
			loadSSDWCanvas(0);
	}
}

function changeCanvasSize(flag){
	loadSSDWCanvas(flag);
    var mainDiv=$("#main_div");
    mainDiv.empty();
    var mcw=ssdwCanvasStyleWidth;
	var mch=ssdwCanvasStyleHeight;
	if(flag==1)
		ssdwCanvasStyleWidth+=30;
	else
		ssdwCanvasStyleWidth-=30;
	ssdwCanvasStyleHeight=ssdwCanvasStyleWidth*ssdwCanvasHeight/ssdwCanvasWidth;
	arcR=arcR*mcw/ssdwCanvasStyleWidth;
	rectWidth=rectWidth*mcw/ssdwCanvasStyleWidth;
	rectHeight=rectHeight*mch/ssdwCanvasStyleHeight;
	arSpace=arSpace*mch/ssdwCanvasStyleHeight;
	atSpace=atSpace*mch/ssdwCanvasStyleHeight;
	fontSize=fontSize*mch/ssdwCanvasStyleHeight;
	fontMarginLeft=fontMarginLeft*mcw/ssdwCanvasStyleWidth;
    mainDiv.append("<canvas id=\"ssdwCanvas\"></canvas>");
	//repaint();
	initSSDWCanvas(1);
}

function setEntityLocation(context,x,y,name,floor){
	context.beginPath();
	context.strokeStyle = 'red';//点填充
	context.fillStyle='red';
	context.lineWidth=arcR*1.5;
	context.arc(x/widthScale,ssdwCanvasHeight-y/heightScale,arcR,0,2*Math.PI);
	context.stroke();

	context.beginPath();
	context.lineWidth = "1";
	context.fillStyle = "blue";
	context.fillRect(x/widthScale-rectWidth/2,ssdwCanvasHeight-y/heightScale-rectHeight-arSpace,rectWidth,rectHeight);
	context.stroke();

	context.font=fontSize+"px bold 黑体";
	context.fillStyle = "#fff";
	context.fillText(name+"("+floor+"层)",x/widthScale-rectWidth/2+fontMarginLeft,ssdwCanvasHeight-y/heightScale-atSpace);
	context.stroke();
}

function loadSSDWCanvas(flag){
	var smallBut=$("#small_but");
	var bigBut=$("#big_but");
	var loadDiv=$("#load_div");
	if(flag==1){
		smallBut.attr("disabled",true);
		bigBut.attr("disabled",true);
		loadDiv.css("display","block");
	}
	else{
		smallBut.attr("disabled",false);
		bigBut.attr("disabled",false);
		loadDiv.css("display","none");
	}
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
<div id="load_div" style="width: 100%;height:100%;text-align:center;background-color: rgba(0,0,0,0.8);position: fixed;display: none;">
	<div class="text_div" style="width: 100%;margin-top: 100px;color: #fff;text-align: center;">地图加载中...</div>
</div>
<div id="main_div" style="width: 100%;height: 600px;overflow: auto;">
	<canvas id="ssdwCanvas">
	</canvas>
</div>
<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
<div class="bottom_div">
	<div class="item_div" onclick="goPage('ssdw')">实时定位</div>
	<div class="item_div ryss_div">人员搜索</div>
	<div class="item_div gjfx_div">轨迹分析</div>
	<div class="item_div bjtj_div" onclick="goPage('bjtj')">报警统计</div>
</div>
</body>
</html>