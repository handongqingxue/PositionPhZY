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
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
var selectedFloorValue="";
$(function(){
	jiSuanScale();
	initSSDWCanvas(0);
	//setInterval(function(){
		summaryOnlineData();
	//},"3000");
});

function jiSuanScale(){
	widthScale=ssdwCanvasStyleWidth/ssdwCanvasWidth;
	heightScale=ssdwCanvasStyleHeight/ssdwCanvasHeight;
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
						//console.log(location.x+location.y+location.entityName+","+","+","+location.floor);
						setEntityLocation(ssdwCanvasContext,location.x,location.y,location.entityName,location.floor);
					}
				}
				var preSsdwCanvas=document.getElementById("ssdwCanvas");
				preSsdwCanvas.parentNode.removeChild(preSsdwCanvas);
				var ssdwCanvasDiv=document.getElementById("ssdwCanvas_div");
				ssdwCanvasDiv.appendChild(ssdwCanvas);
			}
		,"json");
		//setEntityLocation(ssdwCanvasContext,268,443,"陈广银",1);
		if(reSizeFlag==1)
			loadSSDWCanvas(0);
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

function showDutySel(){
	var dutySel=$("#duty_sel");
	var display=dutySel.css("display");
	if(display=="none"){
		$("#show_duty_but_div").text("<");
		dutySel.css("display","block");
	}
	else{
		$("#show_duty_but_div").text(">");
		dutySel.css("display","none");
	}
}

function goPage(page){
	switch (page) {
	case "ryss":
		location.href=phonePath+"goStaffSearch";
		break;
	case "gjfx":
		location.href=phonePath+"goLocRecAna";
		break;
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
.main_div{
	width: 100%;
}
.main_div .tool_div{
	width: 100%;
}
.show_duty_but_div{
	width: 30px;height: 30px;line-height: 30px;margin-top:-30px;margin-left:90px;text-align: center;
}
.duty_sel{
	height: 30px;margin-top:-30px;margin-left: 120px;display: none;
}
.show_label_but_div{
	width: 100px;height: 30px;line-height: 30px;text-align: center;
}
.label_list_div{
	width: 150px;margin-left: 120px;margin-top: -30px;position: absolute;
}
.label_list_div .item_div{
	width: 100%;height: 30px;line-height: 30px;
}
.label_list_div .item_div .select_cb{
	margin-top: 10px;
}
.label_list_div .item_div .name_span{
	margin-left: 5px;
}
.ssdwCanvas_div{
	width: 100%;height: 600px;overflow: auto;
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
<div class="main_div" id="main_div">
	<div class="tool_div">
		<div>
			<select id="floor_sel" onchange="initSSDWCanvas(0);">
				<!-- 
				<option value="">总图</option>
				<option value="1">1层</option>
				<option value="2">2层</option>
				<option value="3">3层</option>
				<option value="4">4层</option>
				<option value="5">5层</option>
				 -->
			</select>
			<div class="show_duty_but_div" id="show_duty_but_div" onclick="showDutySel();">></div>
			<select class="duty_sel" id="duty_sel">
			</select>
		</div>
		<div>
			<div class="show_label_but_div">显示标签</div>
			<div class="label_list_div">
				<div class="item_div">
					<input class="select_cb" type="checkbox"/>
					<span class="name_span">aaa</span>
				</div>
				<div class="item_div">
					<input class="select_cb" type="checkbox"/>
					<span class="name_span">aaa</span>
				</div>
				<div class="item_div">
					<input class="select_cb" type="checkbox"/>
					<span class="name_span">aaa</span>
				</div>
			</div>
		</div>
	</div>
	<div class="ssdwCanvas_div" id="ssdwCanvas_div">
		<canvas id="ssdwCanvas">
		</canvas>
	</div>
</div>
<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
<div class="bottom_div">
	<div class="item_div" onclick="goPage('ssdw')">实时定位</div>
	<div class="item_div ryss_div" onclick="goPage('ryss')">人员搜索</div>
	<div class="item_div gjfx_div" onclick="goPage('gjfx')">轨迹分析</div>
	<div class="item_div bjtj_div" onclick="goPage('bjtj')">报警统计</div>
</div>
</body>
</html>