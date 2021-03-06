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
var windowWidth;
var windowHeight;
var ryssCanvas;
var ryssCanvasImgSrc;
var ryssCanvasMinWidth;//720.52
var ryssCanvasMinHeight;//670.49
var ryssCanvasMaxWidth;//2841
var ryssCanvasMaxHeight;//2643
var ryssCanvasStyleWidth;//ryssCanvasMinWidth
var ryssCanvasStyleHeight;//ryssCanvasMinHeight
var ryssCanvasWidth;//ryssCanvasMaxWidth
var ryssCanvasHeight;//ryssCanvasMaxHeight
var widthScale;
var heightScale;
var staffImgWidth;
var staffImgHeight;
var carImgWidth=100;
var carImgHeight=64;
var arcR=20;
var rectWidth;
var rectHeight;
var arSpace;
var atSpace;
var fontSize;
var fontMarginLeft;
var reSizeTimeout;
var locationList;
$(function(){
	jiSuanScale();
	initRYSSCanvasDivHeight();
});

function jiSuanScale(){
	$.post("getRootAreas",
		function(data){
			//alert(JSON.stringify(data));
			if(data.status=="ok"){
				var result=data.result;
				var area=result[0];
				
				var areaWidth=area.width;
				var areaLength=area.length;
				var bodyWidth=$("body").width();
				var bodyHeight=$("body").height()-$("#bottom_div").height();

				if(areaWidth<bodyWidth||areaLength<bodyHeight){//在地图区域宽度或高度小于浏览器宽度或高度时执行下面逻辑
					if(areaWidth/areaLength<bodyWidth/bodyHeight){//为了不出现空白区域，把地图最小宽度和高度设置为浏览器宽度和高度
						ryssCanvasMinWidth=bodyWidth;
						ryssCanvasMinHeight=ryssCanvasMinWidth*areaLength/areaWidth;
					}
					else{
						ryssCanvasMinHeight=bodyHeight;
						ryssCanvasMinWidth=areaWidth*ryssCanvasMinHeight/areaLength
					}
				}
				else{//否则地图最小尺寸就是地图区域尺寸而不是浏览器尺寸
					ryssCanvasMinWidth=areaWidth;
					ryssCanvasMinHeight=areaLength;
				}
				ryssCanvasStyleWidth=ryssCanvasMinWidth;//画布缩放时对应的宽度，默认是画布最小宽度，之后每次缩放都会变化
				ryssCanvasStyleHeight=ryssCanvasMinHeight;//画布缩放时对应的长度，默认是画布最小长度，之后每次缩放都会变化
				
				ryssCanvasMaxWidth=area.picWidth;
				ryssCanvasMaxHeight=area.picHeight;
				
				ryssCanvasWidth=ryssCanvasMaxWidth;
				ryssCanvasHeight=ryssCanvasMaxHeight;
				ryssCanvasImgSrc=path+area.virtualPath;
				
				widthScale=areaWidth/ryssCanvasWidth;
				heightScale=areaLength/ryssCanvasHeight;
				staffImgWidth=area.staffImgWidth;
				staffImgHeight=area.staffImgHeight;
				rectWidth=area.rectWidth;
				rectHeight=area.rectHeight;
				arSpace=area.arSpace;
				atSpace=area.atSpace;
				fontSize=area.fontSize;
				fontMarginLeft=area.fontMarginLeft;
				
				initRYSSCanvas(false,true);
			}
		}
	,"json");
}

function initRYSSCanvasDivHeight(){
	windowWidth=$(window).width();
	windowHeight=$(window).height();
	var sstjDivHeight=$("#sstj_div").css("height");
	sstjDivHeight=sstjDivHeight.substring(0,sstjDivHeight.length-2);
	var bottomDivHeight=$("#bottom_div").css("height");
	bottomDivHeight=bottomDivHeight.substring(0,bottomDivHeight.length-2);
	$("#ryssCanvas_div").css("height",windowHeight-sstjDivHeight-bottomDivHeight-2+"px");
}

function initRYSSCanvas(reloadFlag,reSizeFlag){
	if(checkCookieValid()){
		if(!reSizeFlag)
			showLoadMapDiv(true);
		var ryssCanvasImg = new Image();
		//ryssCanvasImg.src=path+"resource/image/003.jpg";
		ryssCanvasImg.src=ryssCanvasImgSrc;
		ryssCanvas = document.createElement("canvas");
		ryssCanvas.id="ryssCanvas";
		ryssCanvas.style.width=ryssCanvasStyleWidth+"px";
		ryssCanvas.style.height=ryssCanvasStyleHeight+"px";
		ryssCanvas.width=ryssCanvasWidth;
		ryssCanvas.height=ryssCanvasHeight;
		ryssCanvasContext = ryssCanvas.getContext("2d");
		ryssCanvasImg.onload=function(){
			if(!reSizeFlag)
				showLoadMapDiv(false);
			ryssCanvasContext.drawImage(ryssCanvasImg, 0, 0, ryssCanvasWidth, ryssCanvasHeight);
			if(reloadFlag){
				if(checkEntityName()){
					var entityName=$("#entityName_inp").val();
					$.post("selectEntityLocation",
						{entityName:entityName},
						function(data){
							if(data.status=="ok"){
								locationList=data.list;
								for(var i=0;i<locationList.length;i++){
									var location=locationList[i];
									setEntityLocation(ryssCanvasContext,location.x,location.y,location.entityName,location.entityType,location.floor);
									changeScrollPosition(location.x,location.y);
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
						setEntityLocation(ryssCanvasContext,location.x,location.y,location.entityName,location.entityType,location.floor);
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
}

function changeScrollPosition(x,y){
	var ryssCanvasDiv=$("#ryssCanvas_div");
	var widthBL=ryssCanvasMinWidth/ryssCanvasStyleWidth;
	ryssCanvasDiv.scrollLeft(x/widthBL-windowWidth/2);
	var heightBL=ryssCanvasMinHeight/ryssCanvasStyleHeight;
	ryssCanvasDiv.scrollTop(ryssCanvasStyleHeight-y/heightBL-windowHeight/2);
}

function checkCookieValid(){
	var flag;
	$.ajaxSetup({async:false});
	$.post("checkCookieValid",
		function(data){
			if(data.status=="ok"){
				flag=true;
			}
			else{
				if(confirm(data.message)){
					location.href=phonePath+"goPage?page=login";
				}
				flag=false;
			}
		}
	,"json");
	return flag;
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

	var cswSFB=mcw/ryssCanvasStyleWidth;
	var cshSFB=mch/ryssCanvasStyleHeight;
	staffImgWidth=staffImgWidth*cswSFB;
	staffImgHeight=staffImgHeight*cshSFB;
	
	carImgWidth=carImgWidth*cswSFB;
	carImgHeight=carImgHeight*cshSFB;
	
	arcR=arcR*cswSFB;
	
	rectWidth=rectWidth*cswSFB;
	rectHeight=rectHeight*cshSFB;
	
	arSpace=arSpace*cshSFB;
	atSpace=atSpace*cshSFB;
	
	fontSize=fontSize*cshSFB;
	fontMarginLeft=fontMarginLeft*cswSFB;
	
	initRYSSCanvas(false,true);
}

function setEntityLocation(context,x,y,name,entityType,floor){
	var entityImg = new Image();
	if(entityType=="staff"){
		entityImg.src=path+"resource/image/007.png";
		entityImg.onload=function(){
			ryssCanvasContext.drawImage(entityImg, x/widthScale-staffImgWidth/2, ryssCanvasHeight-y/heightScale-staffImgHeight/2, staffImgWidth, staffImgHeight);
		}
	
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
	else if(entityType=="car"){
		entityImg.src=path+"resource/image/008.png";
		entityImg.onload=function(){
			ryssCanvasContext.drawImage(entityImg, x/widthScale-carImgWidth/2, ryssCanvasHeight-y/heightScale-carImgHeight/2, carImgWidth, carImgHeight);
		}
	
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
	else{
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
		},"3000");
	}
}

function showLoadMapDiv(flag){
	$("#load_map_div").css("display",flag?"block":"none");
}
</script>
<style type="text/css">
body{
	margin: 0;
}
.load_map_div{
	width: 100%;height:100%;background-color: rgba(0,0,0,0.5);position: fixed;display:none;z-index: 1;
}
.load_map_div .text_div{
	width: 100%;color:#fff;text-align:center;font-size:25px;top:45%;position: absolute;
}
.scale_set_div{
	width:30px;
	height:112px;
	right:10px;
	bottom:60px;
	position: fixed;
}
.scale_set_div .but_div{
	width: 35px;
	height: 35px;
	line-height: 30px;
	color:#999;
	font-size:30px;
	text-align:center; 
	background-color: #F6F6F6;
}
.scale_set_div .reset_but_div img{
	width:21px;
	margin-top: 7px;
}
.scale_set_div .big_but_div{
	margin-top:3px;
}
.scale_set_div .small_but_div{
	border-top: #999 solid 1px;
}

.sstj_div{
	width: 99%;
	height: 40px;
	padding:1px;
	background-color: #F6F6F6;
}
.sstj_div .entityName_inp{
	width: 230px;
	height: 23px;
	line-height: 23px;
	margin-top: 5px;
	margin-left: 10px;
	padding-left:10px;
	color: #636468;
}
.sstj_div .search_but_div{
	width: 50px;
	height: 30px;
	line-height: 30px;
	margin-top:5px;
	margin-right:20px; 
	float: right;
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
<div class="load_map_div" id="load_map_div">
	<div class="text_div">地图加载中</div>
</div>
<div class="scale_set_div">
	<div class="but_div reset_but_div" onclick="changeCanvasSize(null,true)">
		<img alt="" src="<%=basePath %>resource/image/006.png">
	</div>
	<div class="but_div big_but_div" id="big_but_div" onclick="changeCanvasSize(true,false);">+</div>
	<div class="but_div small_but_div" id="small_but_div" onclick="changeCanvasSize(false,false);">-</div>
</div>
<div class="sstj_div" id="sstj_div">
	<input type="text" class="entityName_inp" id="entityName_inp" placeholder="请输入人员姓名"/>
	<div class="search_but_div" onclick="initRYSSCanvas(true,false);">搜索</div>
</div>
<div class="ryssCanvas_div" id="ryssCanvas_div">
	<canvas id="ryssCanvas">
	</canvas>
</div>
<%@include file="nav.jsp"%>
</body>
</html>