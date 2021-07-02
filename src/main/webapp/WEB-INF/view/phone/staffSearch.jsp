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
var locationList;
$(function(){
	jiSuanScale();
	initRYSSCanvasDivHeight();
	initRYSSCanvas(0,1);
});

function jiSuanScale(){
	widthScale=ryssCanvasStyleWidth/ryssCanvasWidth;
	heightScale=ryssCanvasStyleHeight/ryssCanvasHeight;
}

function initRYSSCanvasDivHeight(){
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
		if(reSizeFlag==1)
			loadRYSSCanvas(0);
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

//缩放画布,bigFlag在autoFlag为false手动缩放，通过缩放比控制时不传
function changeCanvasSize(bigFlag,autoFlag){
	loadRYSSCanvas(1);
    var mcw=ryssCanvasStyleWidth;
	var mch=ryssCanvasStyleHeight;
	if(autoFlag==1){
		if(bigFlag==1)
			ryssCanvasStyleWidth+=30;
		else
			ryssCanvasStyleWidth-=30;
	}
	else{
		var scale=$("#scale_inp").val();
		ryssCanvasStyleWidth=ryssCanvasMinWidth+ryssCanvasMinWidth*(scale-100)/100;
	}
	
	if(ryssCanvasStyleWidth<ryssCanvasMinWidth){
		ryssCanvasStyleWidth=ryssCanvasMinWidth;
	}
	else if(ryssCanvasStyleWidth>ryssCanvasMaxWidth){
		ryssCanvasStyleWidth=ryssCanvasMaxWidth;
	}
	$("#scale_inp").val((ryssCanvasStyleWidth/ryssCanvasMinWidth).toFixed(2)*100);

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
	
	initRYSSCanvas(0,1);
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
	if(flag==1){
		smallButDiv.removeAttr("onclick");
		bigButDiv.removeAttr("onclick");
	}
	else{
		setTimeout(function(){
			smallButDiv.attr("onclick","changeCanvasSize(0,1)");
			bigButDiv.attr("onclick","changeCanvasSize(1,1)");
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
	width: 313px;
	margin:0 auto;
}
.tool_div .name_span{
	margin-top:8px;
	font-size:15px;
	position: absolute;
}
.tool_div .entityName_inp{
	width: 90px;
	height: 20px;
	margin-top:7px;
	margin-left:40px;
}
.tool_div .search_but_div{
	width: 50px;
	height: 30px;
	line-height: 30px;
	margin-top:-28px;
	margin-left:147px; 
	color:#fff;
	font-size:15px;
	text-align:center;
	background-color: #00f;
	border-radius:5px;
}
.scale_set_div{
	margin-top:-25px;
	float: right;
}
.scale_set_div .but_div{
	width: 20px;
	height: 20px;
	line-height: 20px;
	color:#fff;
	text-align:center; 
	background-color: #00f;
	border-radius:5px;
}
.scale_set_div .big_but_div{
	margin-top:-20px;
	margin-left: 85px;
}
.scale_set_div .scale_inp{
	width:30px;
	margin-top: -28px;
	margin-left:25px;
	text-align: center;
}
.scale_set_div .scale_fuhao_span{
	margin-top: -22px;
	margin-left:5px;
	position: absolute;
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
		<div class="scale_set_div">
			<div class="but_div" id="small_but_div" onclick="changeCanvasSize(0,1);">-</div>
			<input class="scale_inp" id="scale_inp" type="text" value="100" onkeyup="this.value=this.value.replace(/[^\d.]/g,'')" onblur="changeCanvasSize(null,0)"/>
			<span class="scale_fuhao_span">%</span>
			<div class="but_div big_but_div" id="big_but_div" onclick="changeCanvasSize(1,1);">+</div>
		</div>
	</div>
</div>
<div class="ryssCanvas_div" id="ryssCanvas_div">
	<canvas id="ryssCanvas">
	</canvas>
</div>
<%@include file="nav.jsp"%>
</body>
</html>