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
var ryssCanvasStyleWidth=ryssCanvasMinWidth;//画布缩放时对应的宽度，默认是画布最小宽度，之后每次缩放都会变化
var ryssCanvasStyleHeight=ryssCanvasMinHeight;//画布缩放时对应的长度，默认是画布最小长度，之后每次缩放都会变化
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
var reSizeTimeout;
var locationList;
$(function(){
	showSSTJDiv(false);
	jiSuanScale();
	initRYSSCanvasDivHeight();
	initRYSSCanvas(false,true);
});

function jiSuanScale(){
	widthScale=ryssCanvasStyleWidth/ryssCanvasWidth;
	heightScale=ryssCanvasStyleHeight/ryssCanvasHeight;
}

function initRYSSCanvasDivHeight(){
	var windowHeight=$(window).height();
	var bottomDivHeight=$("#bottom_div").css("height");
	bottomDivHeight=bottomDivHeight.substring(0,bottomDivHeight.length-2);
	$("#ryssCanvas_div").css("height",windowHeight-bottomDivHeight+"px");
}

function showSSTJDiv(flag){
	var xssstjButImg=$("#xssstj_but_img");
	var sstjDiv=$("#sstj_div");
	if(flag){
		xssstjButImg.css("display","none");
		sstjDiv.css("display","block");
	}
	else{
		xssstjButImg.css("display","block");
		sstjDiv.css("display","none");
	}
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
		if(reloadFlag){
			if(checkEntityName()){
				var entityName=$("#entityName_inp").val();
				$.post("selectEntityLocation",
					{entityName:entityName},
					function(data){
						showSSTJDiv(false);
						if(data.status=="ok"){
							locationList=data.list;
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
		else{
			if(locationList!=undefined){//判断集合是否存在，第一次访问页面是不存在的，搜索之后才存在
				for(var i=0;i<locationList.length;i++){
					var location=locationList[i];
					setEntityLocation(ryssCanvasContext,location.x,location.y,location.entityName,location.floor);
				}
			}
		}
		var preRyssCanvas=document.getElementById("ryssCanvas");
		preRyssCanvas.parentNode.removeChild(preRyssCanvas);//ryssCanvas_div
		var ryssCanvasDiv=document.getElementById("ryssCanvas_div");
		ryssCanvasDiv.appendChild(ryssCanvas);
		if(reSizeFlag)
			loadRYSSCanvas(false);
	}
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

function changeCanvasSize(bigFlag,resetFlag){
	loadRYSSCanvas(true);
    var mcw=ryssCanvasStyleWidth;
	var mch=ryssCanvasStyleHeight;
	if(resetFlag){
		ryssCanvasStyleWidth=ryssCanvasMinWidth;
	}
	else{
		if(bigFlag)
			ryssCanvasStyleWidth+=ryssCanvasMinWidth*0.2;
		else
			ryssCanvasStyleWidth-=ryssCanvasMinWidth*0.2;
	}
	
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
	
	initRYSSCanvas(false,true);
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
	var smallButDiv=$("#small_but_div");
	var bigButDiv=$("#big_but_div");
	if(flag){
		smallButDiv.removeAttr("onclick");
		bigButDiv.removeAttr("onclick");
	}
	else{
		reSizeTimeout=setTimeout(function(){
			smallButDiv.attr("onclick","changeCanvasSize(false,false)");
			bigButDiv.attr("onclick","changeCanvasSize(true,false)");
			clearTimeout(reSizeTimeout);
		},"1000");
	}
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.xssstj_but_img{
	width:30px;height:25px;margin-top:10px;right:10px;position: fixed;z-index: 1;
}

.scale_set_div{
	width:30px;
	height:100px;
	right:10px;
	bottom:60px;
	position: fixed;
}
.scale_set_div .but_div{
	width: 30px;
	height: 30px;
	line-height: 27px;
	color:#999;
	font-size:25px;
	text-align:center; 
	background-color: #F6F6F6;
}
.scale_set_div .reset_but_div{
	line-height: 30px;
}
.scale_set_div .reset_but_div img{
	margin-top: 9px;
}
.scale_set_div .big_but_div{
	margin-top:3px;
}
.scale_set_div .small_but_div{
	border-top: #999 solid 1px;
}

.sstj_div{
	width: 100%;
	padding:1px;
	background-color: #F6F6F6;
	position: fixed;
}
.sstj_div .row_close_div{
	width: 100%;height: 24px;
}
.sstj_div .row_name_div{
	width: 100%;height: 40px;line-height: 40px;margin-bottom: 10px;
}
.sstj_div .close_but_div{
	margin-top: 3px;
	margin-right: 20px;
	color: #636468;
	float: right;
}
.sstj_div .name_span{
	margin-left: 15px;color: #636468;font-size: 15px;
}
.sstj_div .entityName_inp{
	width: 130px;
	height: 23px;
	line-height: 23px;
	margin-left: 10px;
	color: #636468;
}
.sstj_div .search_but_div{
	width: 50px;
	height: 30px;
	line-height: 30px;
	margin-top:-34px;
	margin-left:213px; 
	color:#fff;
	font-size:15px;
	text-align:center;
	background-color: #1777FF;
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
<img class="xssstj_but_img" id="xssstj_but_img" alt="" src="<%=basePath %>resource/image/005.png" onclick="showSSTJDiv(true);">
<div class="scale_set_div">
	<div class="but_div reset_but_div" onclick="changeCanvasSize(null,true)">
		<img alt="" src="<%=basePath %>resource/image/006.png">
	</div>
	<div class="but_div big_but_div" id="big_but_div" onclick="changeCanvasSize(true,false);">+</div>
	<div class="but_div small_but_div" id="small_but_div" onclick="changeCanvasSize(false,false);">-</div>
</div>
<div class="sstj_div" id="sstj_div">
	<div class="row_close_div">
		<div class="close_but_div" onclick="showSSTJDiv(false);">X</div>
	</div>
	<div class="row_name_div">
		<span class="name_span">姓名</span>
		<input type="text" class="entityName_inp" id="entityName_inp"/>
		<div class="search_but_div" onclick="initRYSSCanvas(true,false);">搜索</div>
	</div>
</div>
<div class="ryssCanvas_div" id="ryssCanvas_div">
	<canvas id="ryssCanvas">
	</canvas>
</div>
<%@include file="nav.jsp"%>
</body>
</html>