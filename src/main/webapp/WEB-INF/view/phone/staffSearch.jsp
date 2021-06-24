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
});

function jiSuanScale(){
	widthScale=ryssCanvasStyleWidth/ryssCanvasWidth;
	heightScale=ryssCanvasStyleHeight/ryssCanvasHeight;
}

function initRYSSCanvas(reSizeFlag){
	if(checkEntityName()){
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
			var entityName=$("#entityName_inp").val();
			$.post("getEntityLocation",
				{entityName:entityName},
				function(data){
					if(data.status=="ok"){
						var location=data.location;
						setEntityLocation(ryssCanvasContext,location.x,location.y,location.entityName,location.floor);
						var preRyssCanvas=document.getElementById("ryssCanvas");
						preRyssCanvas.parentNode.removeChild(preRyssCanvas);
						var mainDiv=document.getElementById("main_div");
						mainDiv.appendChild(ryssCanvas);
					}
					else{
						alert(data.message);
					}
				}
			,"json");
			if(reSizeFlag==1)
				loadRYSSCanvas(0);
		}
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
	initRYSSCanvas(1);
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
		名称:<input type="text" id="entityName_inp"/>
		<input type="button" value="搜索" onclick="initRYSSCanvas(0);"/>
	</div>
	<canvas id="ryssCanvas">
	</canvas>
</div>
<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
</body>
</html>