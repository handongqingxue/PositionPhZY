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
var ryssCanvas;
var ryssCanvasMinWidth=720.52;
var ryssCanvasMinHeight=670.49;
var ryssCanvasMaxWidth=2841;
var ryssCanvasMaxHeight=2643;
var ryssCanvasStyleWidth=ryssCanvasMinWidth;
var ryssCanvasStyleHeight=ryssCanvasMinHeight;
var ryssCanvasWidth=ryssCanvasMaxWidth;
var ryssCanvasHeight=ryssCanvasMaxHeight;
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
	initRYSSCanvas(0,1);
	checkPCOrPhone();
});

function checkPCOrPhone(){
	//https://blog.csdn.net/p445098355/article/details/104272962
    var system ={};  
	var p = navigator.platform;       
	system.win = p.indexOf("Win") == 0;  
	system.mac = p.indexOf("Mac") == 0;  
	system.x11 = (p == "X11") || (p.indexOf("Linux") == 0);     
	if(system.win||system.mac||system.xll){//如果是电脑
		$("#ccs_but_div").css("display","block");
	}
	else{//如果是手机
		$("#ccs_but_div").css("display","none");
	}
}

function jiSuanScale(){
	widthScale=ryssCanvasStyleWidth/ryssCanvasWidth;
	heightScale=ryssCanvasStyleHeight/ryssCanvasHeight;
	var windowHeight=$(window).height();
	var topDivHeight=$("#top_div").css("height");
	topDivHeight=topDivHeight.substring(0,topDivHeight.length-2);
	var bottomDivHeight=$("#bottom_div").css("height");
	bottomDivHeight=bottomDivHeight.substring(0,bottomDivHeight.length-2);
	$("#ryssCanvas_div").css("height",windowHeight-topDivHeight-bottomDivHeight+"px");
}

function initRYSSCanvas(reloadFlag,reSizeFlag){
	var ryssCanvasImg = new Image();
	ryssCanvasImg.src=path+"resource/image/003.jpg";
	ryssCanvas = document.createElement("canvas");
	ryssCanvas.id="ryssCanvas";
	ryssCanvas.style.width=ryssCanvasStyleWidth+"px";
	ryssCanvas.style.height=ryssCanvasStyleHeight+"px";
	ryssCanvas.width=ryssCanvasWidth;
	ryssCanvas.height=ryssCanvasHeight;
	ryssCanvasContext = ryssCanvas.getContext("2d");
	ryssCanvasImg.onload=function(){
		ryssCanvasContext.drawImage(ryssCanvasImg, 0, 0, ryssCanvasWidth, ryssCanvasHeight);
		if(reloadFlag==1){
			if(checkEntityName()){
				var entityName=$("#entityName_inp").val();
				$.post("selectEntityLocation",
					{entityName:entityName},
					function(data){
						if(data.status=="ok"){
							var locationList=data.list;
							for(var i=0;i<locationList.length;i++){
								var location=locationList[i];
								setEntityLocation(ryssCanvasContext,location.x,location.y,location.entityName,location.floor);
							}
						}
						else{
							alert(data.message);
						}
					}
				,"json");
			}
		}
		var preRyssCanvas=document.getElementById("ryssCanvas");
		preRyssCanvas.parentNode.removeChild(preRyssCanvas);//ryssCanvas_div
		var ryssCanvasDiv=document.getElementById("ryssCanvas_div");
		ryssCanvasDiv.appendChild(ryssCanvas);
		if(reSizeFlag==1)
			loadRYSSCanvas(0);
	}
}

function changeCanvasSize(flag){
	loadSSDWCanvas(flag);
    var mcw=ssdwCanvasStyleWidth;
	var mch=ssdwCanvasStyleHeight;
	if(flag==1)
		ssdwCanvasStyleWidth+=30;
	else
		ssdwCanvasStyleWidth-=30;
	
	if(ssdwCanvasStyleWidth<ssdwCanvasMinWidth){
		ssdwCanvasStyleWidth=ssdwCanvasMinWidth;
	}
	else if(ssdwCanvasStyleWidth>ssdwCanvasMaxWidth){
		ssdwCanvasStyleWidth=ssdwCanvasMaxWidth;
	}

	if(ssdwCanvasStyleHeight<ssdwCanvasMinHeight){
		ssdwCanvasStyleHeight=ssdwCanvasMinHeight;
	}
	else if(ssdwCanvasStyleHeight>ssdwCanvasMaxHeight){
		ssdwCanvasStyleHeight=ssdwCanvasMaxHeight;
	}
	ssdwCanvasStyleHeight=ssdwCanvasStyleWidth*ssdwCanvasHeight/ssdwCanvasWidth;
	arcR=arcR*mcw/ssdwCanvasStyleWidth;
	rectWidth=rectWidth*mcw/ssdwCanvasStyleWidth;
	rectHeight=rectHeight*mch/ssdwCanvasStyleHeight;
	arSpace=arSpace*mch/ssdwCanvasStyleHeight;
	atSpace=atSpace*mch/ssdwCanvasStyleHeight;
	fontSize=fontSize*mch/ssdwCanvasStyleHeight;
	fontMarginLeft=fontMarginLeft*mcw/ssdwCanvasStyleWidth;
	initSSDWCanvas(1);
}

function checkEntityName(){
	var entityName=$("#entityName_inp").val();
	if(entityName==""||entityName==null){
		alert("请输入实体名称");
		return false;
	}
	else
		return true;
}

function changeCanvasSize(flag){
	loadRYSSCanvas(flag);
    var mcw=ryssCanvasStyleWidth;
	var mch=ryssCanvasStyleHeight;
	if(flag==1)
		ryssCanvasStyleWidth+=30;
	else
		ryssCanvasStyleWidth-=30;
	
	if(ryssCanvasStyleWidth<ryssCanvasMinWidth){
		ryssCanvasStyleWidth=ryssCanvasMinWidth;
	}
	else if(ryssCanvasStyleWidth>ryssCanvasMaxWidth){
		ryssCanvasStyleWidth=ryssCanvasMaxWidth;
	}

	if(ryssCanvasStyleHeight<ryssCanvasMinHeight){
		ryssCanvasStyleHeight=ryssCanvasMinHeight;
	}
	else if(ryssCanvasStyleHeight>ryssCanvasMaxHeight){
		ryssCanvasStyleHeight=ryssCanvasMaxHeight;
	}
	ryssCanvasStyleHeight=ryssCanvasStyleWidth*ryssCanvasHeight/ryssCanvasWidth;
	arcR=arcR*mcw/ryssCanvasStyleWidth;
	rectWidth=rectWidth*mcw/ryssCanvasStyleWidth;
	rectHeight=rectHeight*mch/ryssCanvasStyleHeight;
	arSpace=arSpace*mch/ryssCanvasStyleHeight;
	atSpace=atSpace*mch/ryssCanvasStyleHeight;
	fontSize=fontSize*mch/ryssCanvasStyleHeight;
	fontMarginLeft=fontMarginLeft*mcw/ryssCanvasStyleWidth;
	initRYSSCanvas(1,1);
}

function setEntityLocation(context,x,y,name,floor){
	context.beginPath();
	context.strokeStyle = 'red';//点填充
	context.fillStyle='red';
	context.lineWidth=arcR*1.5;
	context.arc(x/widthScale,ryssCanvasHeight-y/heightScale,arcR,0,2*Math.PI);
	context.stroke();

	context.beginPath();
	context.lineWidth = "1";
	context.fillStyle = "blue";
	context.fillRect(x/widthScale-rectWidth/2,ryssCanvasHeight-y/heightScale-rectHeight-arSpace,rectWidth,rectHeight);
	context.stroke();

	context.font=fontSize+"px bold 黑体";
	context.fillStyle = "#fff";
	context.fillText(name+"("+floor+"层)",x/widthScale-rectWidth/2+fontMarginLeft,ryssCanvasHeight-y/heightScale-atSpace);
	context.stroke();
}

function loadRYSSCanvas(flag){
	var smallBut=$("#small_but");
	var bigBut=$("#big_but");
	if(flag==1){
		smallBut.attr("disabled",true);
		bigBut.attr("disabled",true);
	}
	else{
		setTimeout(function(){
			smallBut.attr("disabled",false);
			bigBut.attr("disabled",false);
		},"1000");
	}
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.top_div{
	width: 100%;
	height: 40px;
	background-color: #eee;
}
.tool_div{
	width: 275px;
	margin:0 auto;
}
.tool_div .name_span{
	margin-top:8px;
	margin-left: 10px;
	font-size:15px;
	position: absolute;
}
.tool_div .entityName_inp{
	width: 130px;
	height: 20px;
	margin-top:7px;
	margin-left:60px;
}
.tool_div .search_but_div{
	width: 50px;
	height: 30px;
	line-height: 30px;
	margin-top:-28px;
	margin-left:210px; 
	color:#fff;
	font-size:15px;
	text-align:center;
	background-color: #00f;
	border-radius:5px;
}
.ryssCanvas_div{
	width: 100%;
	overflow: auto;
}
</style>
<title>人员搜索</title>
</head>
<body>
<div class="top_div" id="top_div">
	<div class="tool_div">
		<span class="name_span">姓名:</span>
		<input type="text" class="entityName_inp" id="entityName_inp"/>
		<div class="search_but_div" onclick="initRYSSCanvas(1,0);">搜索</div>
	</div>
</div>
<div class="ryssCanvas_div" id="ryssCanvas_div">
	<canvas id="ryssCanvas">
	</canvas>
</div>
<div id="ccs_but_div">
	<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
	<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
</div>
<%@include file="nav.jsp"%>
</body>
</html>