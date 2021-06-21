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
<script type="text/javascript" src="<%=basePath%>resource/js/calendar/calendar.js"></script>
<script type="text/javascript" src="<%=basePath %>resource/js/calendar/WdatePicker.js"></script>
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
var gjfxCanvas;
var gjfxCanvasMinWidth=720.52;
var gjfxCanvasMinHeight=670.49;
var gjfxCanvasMaxWidth=2841;
var gjfxCanvasMaxHeight=2643;
var gjfxCanvasStyleWidth=gjfxCanvasMinWidth;
var gjfxCanvasStyleHeight=gjfxCanvasMinHeight;
var gjfxCanvasWidth=gjfxCanvasMaxWidth;
var gjfxCanvasHeight=gjfxCanvasMaxHeight;
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
	initGJFXCanvas();
	initEntitySelect();
	initStartTimePickerDiv();
	initEndTimePickerDiv();
});

function jiSuanScale(){
	widthScale=gjfxCanvasStyleWidth/gjfxCanvasWidth;
	heightScale=gjfxCanvasStyleHeight/gjfxCanvasHeight;
}

function initGJFXCanvas(){
	var gjfxCanvasImg = new Image();
	gjfxCanvasImg.src=path+"resource/image/003.jpg";
	gjfxCanvas = document.createElement("canvas");
	gjfxCanvas.id="gjfxCanvas";
	gjfxCanvas.style.width=gjfxCanvasStyleWidth+"px";
	gjfxCanvas.style.height=gjfxCanvasStyleHeight+"px";
	gjfxCanvas.width=gjfxCanvasWidth;
	gjfxCanvas.height=gjfxCanvasHeight;
	gjfxCanvasContext = gjfxCanvas.getContext("2d");
	gjfxCanvasImg.onload=function(){
		gjfxCanvasContext.drawImage(gjfxCanvasImg, 0, 0, gjfxCanvasWidth, gjfxCanvasHeight);
		setEntityLocation(gjfxCanvasContext,268,443,"陈广银",1);
		var preGjfxCanvas=document.getElementById("gjfxCanvas");
		preGjfxCanvas.parentNode.removeChild(preGjfxCanvas);
		var gjfxCanvasDiv=document.getElementById("gjfxCanvas_div");
		gjfxCanvasDiv.appendChild(gjfxCanvas);
	}
}

function initEntitySelect(){
	$.post("initEntitySelect",
		{entityType:"staff"},
		function(data){
			var staffSel=$("#staff_sel");
			var staffList=data.list;
			for(var i=0;i<staffList.length;i++){
				var staff=staffList[i];
				staffSel.append("<option value=\""+staff.tagId+"\">"+staff.name+"("+staff.pid+")</option>");
			}
		}
	,"json");
}

function initStartTimePickerDiv(){
	var sthSel=$("#sth_sel");
	for(var i=0;i<24;i++){
		sthSel.append("<option>"+i+"</option>");
	}
	var stmSel=$("#stm_sel");
	var stsSel=$("#sts_sel");
	for(var i=0;i<60;i++){
		stmSel.append("<option>"+i+"</option>");
		stsSel.append("<option>"+i+"</option>");
	}
}

function initEndTimePickerDiv(){
	var ethSel=$("#eth_sel");
	for(var i=0;i<24;i++){
		ethSel.append("<option value=\""+i+"\">"+i+"</option>");
	}
	var etmSel=$("#etm_sel");
	var etsSel=$("#ets_sel");
	for(var i=0;i<60;i++){
		etmSel.append("<option value=\""+i+"\">"+i+"</option>");
		etsSel.append("<option value=\""+i+"\">"+i+"</option>");
	}
	
	var date=new Date();
	var hour=date.getHours();
	var minute=date.getMinutes();
	var second=date.getSeconds();
	$("#eth_sel option[value=\""+hour+"\"]").attr("selected",true);
	$("#etm_sel option[value=\""+minute+"\"]").attr("selected",true);
	$("#ets_sel option[value=\""+second+"\"]").attr("selected",true);
	//var nowTime=hour+":"+minute+":"+second;
}

function setEntityLocation(context,x,y,name,floor){
	context.beginPath();
	context.strokeStyle = 'red';//点填充
	context.fillStyle='red';
	context.lineWidth=arcR*1.5;
	context.arc(x/widthScale,gjfxCanvasHeight-y/heightScale,arcR,0,2*Math.PI);
	context.stroke();

	context.beginPath();
	context.lineWidth = "1";
	context.fillStyle = "blue";
	context.fillRect(x/widthScale-rectWidth/2,gjfxCanvasHeight-y/heightScale-rectHeight-arSpace,rectWidth,rectHeight);
	context.stroke();

	context.font=fontSize+"px bold 黑体";
	context.fillStyle = "#fff";
	context.fillText(name+"("+floor+"层)",x/widthScale-rectWidth/2+fontMarginLeft,gjfxCanvasHeight-y/heightScale-atSpace);
	context.stroke();
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
.gjfxCanvas_div{
	width: 100%;height: 600px;overflow: auto;
}
</style>
<title>轨迹分析</title>
</head>
<body>
<div class="main_div" id="main_div">
	<div class="tool_div">
		<div>
			人员
			<select id="staff_sel">
			</select>
		</div>
		<div style="margin-top: 5px;">
			时间：
			<input  type="text" class="Wdate" id="st" style="width: 90px;" onclick="WdatePicker()" readonly="readonly"/>
			<div class="stp_div" style="margin-left: 160px;margin-top: -25px;">
				<select id="sth_sel">
				</select>:
				<select id="stm_sel">
				</select>:
				<select id="sts_sel">
				</select>
			</div>
			<div class="etp_div" style="margin-left: 160px;margin-top: 5px;">
				<select id="eth_sel">
				</select>:
				<select id="etm_sel">
				</select>:
				<select id="ets_sel">
				</select>
			</div>
		</div>
		<div style="margin-top: 5px;">
			压缩比<input type="number" size="10" value="200"/>
			<input type="button" value="查询"/>
		</div>
	</div>
	<div class="gjfxCanvas_div" id="gjfxCanvas_div">
		<canvas id="gjfxCanvas">
		</canvas>
	</div>
</div>
</body>
</html>