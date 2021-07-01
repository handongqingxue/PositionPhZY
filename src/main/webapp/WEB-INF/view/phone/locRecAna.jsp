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
var arcR=10;
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
var locRecListIndex=0;
var paintInterval;
var locRecList;
$(function(){
	initEntitySelect();
	initTodayDateCalendar();
	initStartTimePickerDiv();
	initEndTimePickerDiv();
	jiSuanScale();
});

function jiSuanScale(){
	widthScale=gjfxCanvasStyleWidth/gjfxCanvasWidth;
	heightScale=gjfxCanvasStyleHeight/gjfxCanvasHeight;
}

function checkStaff(){
	var tagId=$("#staff_sel").val();
	if(tagId==""||tagId==null){
		alert("请选择人员");
		return false;
	}
	else
		return true;
}

function checkYsb(){
	var ysb=$("#ysb_inp").val();
	if(ysb==""||ysb==null){
		alert("请输入压缩比");
		return false;
	}
	else
		return true;
}

function getLocationRecords(){
	if(checkStaff()){
		if(checkYsb()){
			var tagId=$("#staff_sel").val();
			var todayDate=$("#td_cal").val();
			var sth=$("#sth_sel").val();
			var stm=$("#stm_sel").val();
			var sts=$("#sts_sel").val();
			var startTime=sth+":"+stm+":"+sts+":000";
			var eth=$("#eth_sel").val();
			var etm=$("#etm_sel").val();
			var ets=$("#ets_sel").val();
			var endTime=eth+":"+etm+":"+ets+":000";
			var ysb=$("#ysb_inp").val();
			$.post("getLocationRecords",
				{tagId:tagId,todayDate:todayDate,startTime:startTime,endTime:endTime,ysb:ysb},
				function(data){
					locRecList=data.locRecList;
					console.log("length==="+locRecList.length);
					initGJFXCanvas(0);
				}	
			,"json");
		}
	}
}
	
function initGJFXCanvas(reSizeFlag){
	var staffName=$("#staff_sel option:selected").text().split("(")[0];
	if(reSizeFlag==1){
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
			for(var i=0;i<locRecList.length;i++){
				if(i>=1){
					var lr1=locRecList[i-1];
					var lr2=locRecList[i];
					setPointLocation(gjfxCanvasContext,lr1.x,lr1.y,lr2.x,lr2.y);
					if(i==locRecList.length-1)
						setEntityLocation(gjfxCanvasContext,lr2.x,lr2.y,staffName,lr2.floor);
				}
			}
			var preGjfxCanvas=document.getElementById("gjfxCanvas");
			preGjfxCanvas.parentNode.removeChild(preGjfxCanvas);
			var gjfxCanvasDiv=document.getElementById("gjfxCanvas_div");
			gjfxCanvasDiv.appendChild(gjfxCanvas);
			loadGJFXCanvas(0);
		}
	}
	else{
		paintInterval=setInterval(function(){
			var gjfxCanvasImg = new Image();
			gjfxCanvasImg.src=path+"resource/image/003.jpg";
			gjfxCanvas = document.createElement("canvas");
			gjfxCanvas.id="gjfxCanvas";
			gjfxCanvas.style.width=gjfxCanvasStyleWidth+"px";
			gjfxCanvas.style.height=gjfxCanvasStyleHeight+"px";
			gjfxCanvas.width=gjfxCanvasWidth;
			gjfxCanvas.height=gjfxCanvasHeight;
			gjfxCanvasContext = gjfxCanvas.getContext("2d");
			gjfxCanvasContext.clearRect(0,0,gjfxCanvasWidth,gjfxCanvasHeight); 
			gjfxCanvasImg.onload=function(){
				gjfxCanvasContext.drawImage(gjfxCanvasImg, 0, 0, gjfxCanvasWidth, gjfxCanvasHeight);
				for(var i=0;i<=locRecListIndex;i++){
					if(i>=1){
						var lr1=locRecList[i-1];
						var lr2=locRecList[i];
						setPointLocation(gjfxCanvasContext,lr1.x,lr1.y,lr2.x,lr2.y);
						//if(i==locRecList.length-1){
						if(i==locRecListIndex){
							setEntityLocation(gjfxCanvasContext,lr2.x,lr2.y,staffName,lr2.floor);
						}
					}
				}
				var preGjfxCanvas=document.getElementById("gjfxCanvas");
				preGjfxCanvas.parentNode.removeChild(preGjfxCanvas);
				var gjfxCanvasDiv=document.getElementById("gjfxCanvas_div");
				gjfxCanvasDiv.appendChild(gjfxCanvas);
				if(locRecListIndex==locRecList.length-1){
					//console.log(222);
					locRecListIndex=0;
					clearInterval(paintInterval);
				}
				else{
					//console.log("locRecListIndex==="+locRecListIndex);
					locRecListIndex++;
				}
			}
		
		},"100");
	}
}

function changeCanvasSize(flag){
	loadGJFXCanvas(flag);
    var mcw=gjfxCanvasStyleWidth;
	var mch=gjfxCanvasStyleHeight;
	if(flag==1)
		gjfxCanvasStyleWidth+=30;
	else
		gjfxCanvasStyleWidth-=30;
	
	if(gjfxCanvasStyleWidth<gjfxCanvasMinWidth){
		gjfxCanvasStyleWidth=gjfxCanvasMinWidth;
	}
	else if(gjfxCanvasStyleWidth>gjfxCanvasMaxWidth){
		gjfxCanvasStyleWidth=gjfxCanvasMaxWidth;
	}

	if(gjfxCanvasStyleHeight<gjfxCanvasMinHeight){
		gjfxCanvasStyleHeight=gjfxCanvasMinHeight;
	}
	else if(gjfxCanvasStyleHeight>gjfxCanvasMaxHeight){
		gjfxCanvasStyleHeight=gjfxCanvasMaxHeight;
	}
	gjfxCanvasStyleHeight=gjfxCanvasStyleWidth*gjfxCanvasHeight/gjfxCanvasWidth;
	arcR=arcR*mcw/gjfxCanvasStyleWidth;
	rectWidth=rectWidth*mcw/gjfxCanvasStyleWidth;
	rectHeight=rectHeight*mch/gjfxCanvasStyleHeight;
	arSpace=arSpace*mch/gjfxCanvasStyleHeight;
	atSpace=atSpace*mch/gjfxCanvasStyleHeight;
	fontSize=fontSize*mch/gjfxCanvasStyleHeight;
	fontMarginLeft=fontMarginLeft*mcw/gjfxCanvasStyleWidth;
	initGJFXCanvas(1);
}

function initEntitySelect(){
	$.post("initEntitySelect",
		{entityType:"staff"},
		function(data){
			var staffSel=$("#staff_sel");
			staffSel.append("<option value=\"\">请选择</option>");
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
		sthSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
	}
	var stmSel=$("#stm_sel");
	var stsSel=$("#sts_sel");
	for(var i=0;i<60;i++){
		stmSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
		stsSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
	}
}

function initEndTimePickerDiv(){
	var ethSel=$("#eth_sel");
	for(var i=0;i<24;i++){
		ethSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
	}
	var etmSel=$("#etm_sel");
	var etsSel=$("#ets_sel");
	for(var i=0;i<60;i++){
		etmSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
		etsSel.append("<option value=\""+(i<10?"0"+i:i)+"\">"+(i<10?"0"+i:i)+"</option>");
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

function initTodayDateCalendar(){
	var date=new Date();
	var year=date.getFullYear();
	var month=date.getMonth()+1;
	var dateOfMonth=date.getDate();
	var todayDate=year+"-"+(month<10?"0"+month:month)+"-"+(dateOfMonth<10?"0"+dateOfMonth:dateOfMonth);
	$("#td_cal").val(todayDate);
}

function setPointLocation(context,x1,y1,x2,y2){
	/*
	context.beginPath();
	context.strokeStyle = 'red';//点填充
	context.fillStyle='red';
	context.lineWidth=arcR*1.5;
	context.arc(x/widthScale,gjfxCanvasHeight-y/heightScale,arcR,0,2*Math.PI);
	context.stroke();
	*/

	//console.log(x1+","+y1+","+x2+","+y2);
	context.strokeStyle = 'red';//点填充
	context.fillStyle='red';
	context.lineWidth=arcR*1.5;
	context.beginPath();
	context.moveTo(x1/widthScale, gjfxCanvasHeight-y1/heightScale);//起始位置
	context.lineTo(x2/widthScale, gjfxCanvasHeight-y2/heightScale);//停止位置
	context.stroke();
}

function setEntityLocation(context,x,y,name,floor){
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

function loadGJFXCanvas(flag){
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
			<input  type="text" class="Wdate" id="td_cal" style="width: 90px;" onclick="WdatePicker()" readonly="readonly"/>
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
			压缩比<input type="number" id="ysb_inp" size="10" value="200"/>
			<input type="button" value="查询" onclick="getLocationRecords();"/>
		</div>
	</div>
	<div class="gjfxCanvas_div" id="gjfxCanvas_div">
		<canvas id="gjfxCanvas">
		</canvas>
	</div>
</div>
<input type="button" id="small_but" value="缩小" onclick="changeCanvasSize(0);"/>
<input type="button" id="big_but" value="放大" onclick="changeCanvasSize(1);"/>
<%@include file="nav.jsp"%>
</body>
</html>