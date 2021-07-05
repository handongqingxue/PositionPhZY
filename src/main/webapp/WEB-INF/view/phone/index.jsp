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
var staffImgWidth=100;
var staffImgHeight=70;
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
var selectedFloorValue="";
$(function(){
	showSSTJDiv(false);
	initLabelListDiv();
	jiSuanScale();
	initSSDWCanvasDivHeight();
	//initSSDWCanvas(0);
	//setInterval(function(){
		summaryOnlineData();
	//},"3000");
});

function jiSuanScale(){
	widthScale=ssdwCanvasStyleWidth/ssdwCanvasWidth;
	heightScale=ssdwCanvasStyleHeight/ssdwCanvasHeight;
}

function initSSDWCanvasDivHeight(){
	var windowHeight=$(window).height();
	var bottomDivHeight=$("#bottom_div").css("height");
	bottomDivHeight=bottomDivHeight.substring(0,bottomDivHeight.length-2);
	$("#ssdwCanvas_div").css("height",windowHeight-bottomDivHeight+"px");
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

function summaryOnlineData(){
	$.post("summaryOnlineData",
		function(data){
			var entityResult=data.entityResult;
			initFloorSel(entityResult);
			var dutyResult=data.dutyResult;
			initDutySel(dutyResult);
			initSSDWCanvas(0);
		}
	,"json");
}

function initFloorSel(result){
	var floorSel=$("#floor_sel");
	floorSel.empty();
	var name=result.name;
	var summary=result.summary;
	var online=summary.online;
	floorSel.append("<option value=\"\">"+name+" ("+online.total+")</option>");
	
	var children=result.children;
	for(var i=0;i<children.length;i++){
		var child=children[i];
		var optionValue;
		var childName=child.name;
		switch (childName) {
		case "一层":
			optionValue=1;
			break;
		case "二层":
			optionValue=2;
			break;
		case "三层":
			optionValue=3;
			break;
		case "四层":
			optionValue=4;
			break;
		case "五层":
			optionValue=5;
			break;
		}
		floorSel.append("<option value=\""+optionValue+"\" "+(selectedFloorValue==optionValue?"selected":"")+">"+childName+" ("+child.summary.online.total+")</option>");
	}
	//console.log(JSON.stringify(children));
}

function initDutySel(result){
	var dutySel=$("#duty_sel");
	dutySel.empty();
	if(result.status=="ok"){
		var dutyList=result.dutyList;
		for(var i=0;i<dutyList.length;i++){
			var duty=dutyList[i];
			dutySel.append("<option>"+duty.name+" ("+duty.onLineCount+")</option>");
		}
	}
	else{
		dutySel.append("<option>暂无数据</option>");
	}
}

function initLabelListDiv(){
	$.post("initSSDWLabelData",
		function(data){
			var labelListDiv=$("#label_list_div");
			labelListDiv.empty();
			if(data.status=="ok"){
				var list=data.list;
				for (var i = 0; i < list.length; i++) {
					var item=list[i];
					var itemStr="<div class=\"item_div\">";
							itemStr+="<input class=\"select_cb\" id=\"select_cb"+item.id+"\" type=\"checkbox\" "+(item.labelChecked?"checked":"")+" onclick=\"initSSDWCanvas(0);\"/>";
							itemStr+="<span class=\"name_span\">"+item.name+"</span>";
						itemStr+="</div>";
					labelListDiv.append(itemStr);
				}
			}
		}
	,"json");
}

function initSSDWCanvas(reSizeFlag){
	var ssdwCanvasImg = new Image();
	ssdwCanvasImg.src=path+"resource/image/003.jpg";
	ssdwCanvas = document.createElement("canvas");
	ssdwCanvas.id="ssdwCanvas";
	ssdwCanvas.style.width=ssdwCanvasStyleWidth+"px";
	ssdwCanvas.style.height=ssdwCanvasStyleHeight+"px";
	ssdwCanvas.width=ssdwCanvasWidth;
	ssdwCanvas.height=ssdwCanvasHeight;
	ssdwCanvasContext = ssdwCanvas.getContext("2d");
	ssdwCanvasImg.onload=function(){
		ssdwCanvasContext.drawImage(ssdwCanvasImg, 0, 0, ssdwCanvasWidth, ssdwCanvasHeight);

		var floorArrStr="";
		$("#floor_sel option[value!='']").each(function(i){
			floorArrStr+=","+$(this).attr("value");
		});
		var floor=$("#floor_sel").val();
		selectedFloorValue=floor;
		
		$.post("initSSDWCanvasData",
			{floor:floor,floorArrStr:floorArrStr.substring(1)},
			function(data){
				if(data.status=="ok"){
					var locationList=data.list;
					for(var i=0;i<locationList.length;i++){
						var location=locationList[i];
						//console.log(location.uid+","+location.x+location.y+location.entityType+","+location.entityName+","+","+","+location.floor);
						if($("#label_list_div #select_cb"+location.entityType).prop("checked"))
							setEntityLocation(ssdwCanvasContext,location.x,location.y,location.entityName,location.entityType,location.floor);
					}
				}
				var preSsdwCanvas=document.getElementById("ssdwCanvas");
				preSsdwCanvas.parentNode.removeChild(preSsdwCanvas);
				var ssdwCanvasDiv=document.getElementById("ssdwCanvas_div");
				ssdwCanvasDiv.appendChild(ssdwCanvas);
			}
		,"json");
		//setEntityLocation(ssdwCanvasContext,268,443,"陈广银","staff",1);
		if(reSizeFlag==1)
			loadSSDWCanvas(0);
	}
}

function changeCanvasSize(bigFlag,resetFlag){
	loadSSDWCanvas(true);
    var mcw=ssdwCanvasStyleWidth;
	var mch=ssdwCanvasStyleHeight;
	if(resetFlag){
		ssdwCanvasStyleWidth=ssdwCanvasMinWidth;
	}
	else{
		if(bigFlag==1)
			ssdwCanvasStyleWidth+=ssdwCanvasMinWidth*0.2;
		else
			ssdwCanvasStyleWidth-=ssdwCanvasMinWidth*0.2;
	}
	
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

function setEntityLocation(context,x,y,name,entityType,floor){
	var entityImg = new Image();
	if(entityType=="staff"){
		entityImg.src=path+"resource/image/007.png";
		entityImg.onload=function(){
			ssdwCanvasContext.drawImage(entityImg, x/widthScale-staffImgWidth/2, ssdwCanvasHeight-y/heightScale-staffImgHeight/2, staffImgWidth, staffImgHeight);
		}
	
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
	else if(entityType=="car"){
		entityImg.src=path+"resource/image/008.png";
		entityImg.onload=function(){
			ssdwCanvasContext.drawImage(entityImg, x/widthScale-staffImgWidth/2, ssdwCanvasHeight-y/heightScale-staffImgHeight/2, staffImgWidth, staffImgHeight);
		}
	
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
	else{
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
}

function loadSSDWCanvas(flag){
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
.sstj_div .row_dtrs_div,.sstj_div .row_duty_div{
	width: 100%;height: 40px;line-height: 40px;
}
.sstj_div .row_label_div{
	width: 100%;margin: 10px 0 10px;
}
.sstj_div .close_but_div{
	margin-top: 3px;
	margin-right: 20px;
	color: #636468;
	float: right;
}
.sstj_div .dtrs_span,.sstj_div .duty_span,.sstj_div .label_span{
	margin-left: 15px;color: #636468;font-size: 15px;
}
.sstj_div .floor_sel,.sstj_div .duty_sel{
	width: 150px;height: 25px;line-height: 25px;margin-left:25px;color: #636468;
}
.sstj_div .label_list_div{
	width: 150px;height: 270px;margin-top: -25px;margin-left: 104px;border: #999 solid 1px;border-radius:5px;overflow: auto; 
}
.sstj_div .label_list_div .item_div{
	width: 100%;height: 30px;line-height: 30px;color: #636468;
}
.sstj_div .label_list_div .item_div .select_cb{
	margin-top: 10px;
}
.sstj_div .label_list_div .item_div .name_span{
	margin-left: 5px;
}
.ssdwCanvas_div{
	width: 100%;
	overflow: auto;
}
</style>
<title>首页</title>
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
	<div class="row_dtrs_div">
		<span class="dtrs_span">地图人数</span>
		<select class="floor_sel" id="floor_sel" onchange="initSSDWCanvas(0);">
		</select>
	</div>
	<div class="row_duty_div">
		<span class="duty_span">实体人数</span>
		<select class="duty_sel" id="duty_sel">
		</select>
	</div>
	<div class="row_label_div">
		<span class="label_span">标签</span>
		<div class="label_list_div" id="label_list_div">
		</div>
	</div>
</div>
<div class="ssdwCanvas_div" id="ssdwCanvas_div">
	<canvas id="ssdwCanvas">
	</canvas>
</div>
<!-- 
<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
 -->
<%@include file="nav.jsp"%>
</body>
</html>