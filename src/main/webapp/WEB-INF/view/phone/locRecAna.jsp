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
var gjfxCanvasImgSrc;
var gjfxCanvasMinWidth;//720.52
var gjfxCanvasMinHeight;//670.49
var gjfxCanvasMaxWidth;//2841
var gjfxCanvasMaxHeight;//2643
var gjfxCanvasStyleWidth;//gjfxCanvasMinWidth
var gjfxCanvasStyleHeight;//gjfxCanvasMinHeight
var gjfxCanvasWidth;//=gjfxCanvasMaxWidth
var gjfxCanvasHeight;//=gjfxCanvasMaxHeight
var widthScale;
var heightScale;
var lineWidth;//30
var rectWidth;
var rectHeight;
var arSpace;
var atSpace;
var fontSize;
var fontMarginLeft;
var locRecListIndex=0;
var paintInterval;
var reSizeTimeout;
var locRecList;
$(function(){
	showSSTJDiv(false);
	initEntitySelect();
	initTodayDateCalendar();
	initStartTimePickerDiv();
	initEndTimePickerDiv();
	jiSuanScale();
	initGJFXCanvasDivHeight();
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
						gjfxCanvasMinWidth=bodyWidth;
						gjfxCanvasMinHeight=gjfxCanvasMinWidth*areaLength/areaWidth;
					}
					else{
						gjfxCanvasMinHeight=bodyHeight;
						gjfxCanvasMinWidth=areaWidth*gjfxCanvasMinHeight/areaLength
					}
				}
				else{//否则地图最小尺寸就是地图区域尺寸而不是浏览器尺寸
					gjfxCanvasMinWidth=areaWidth;
					gjfxCanvasMinHeight=areaLength;
				}
				gjfxCanvasStyleWidth=gjfxCanvasMinWidth;
				gjfxCanvasStyleHeight=gjfxCanvasMinHeight;
				
				gjfxCanvasMaxWidth=area.picWidth;
				gjfxCanvasMaxHeight=area.picHeight;
				
				gjfxCanvasWidth=gjfxCanvasMaxWidth;
				gjfxCanvasHeight=gjfxCanvasMaxHeight;
				gjfxCanvasImgSrc=path+area.virtualPath;
				
				widthScale=areaWidth/gjfxCanvasWidth;//这个宽度比例永远是地图区域宽度比地图图片宽度，便于正确缩放
				heightScale=areaLength/gjfxCanvasHeight;//这个高度比例永远是地图区域高度比地图图片高度，便于正确缩放
				staffImgWidth=area.staffImgWidth;
				staffImgHeight=area.staffImgHeight;
				lineWidth=area.lineWidth;
				rectWidth=area.rectWidth;
				rectHeight=area.rectHeight;
				arSpace=area.arSpace;
				atSpace=area.atSpace;
				fontSize=area.fontSize;
				fontMarginLeft=area.fontMarginLeft;
				
				initGJFXCanvas(false,false);
			}
		}
	,"json");
}

function initGJFXCanvasDivHeight(){
	var windowHeight=$(window).height();
	var bottomDivHeight=$("#bottom_div").css("height");
	bottomDivHeight=bottomDivHeight.substring(0,bottomDivHeight.length-2);
	$("#gjfxCanvas_div").css("height",windowHeight-bottomDivHeight+"px");
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
	if(checkCookieValid()){
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
						showSSTJDiv(false);
						locRecList=data.locRecList;
						console.log("length==="+locRecList.length);
						initGJFXCanvas(true,false);
					}	
				,"json");
			}
		}
	}
}
	
function initGJFXCanvas(reloadFlag,reSizeFlag){
	var staffName=$("#staff_sel option:selected").text().split("(")[0];
	if(reSizeFlag){//改变画布大小，这时一下子画完所有轨迹就可以，不需要一点点播放
		var gjfxCanvasImg = new Image();
		//gjfxCanvasImg.src=path+"resource/image/area2d-1.png";
		gjfxCanvasImg.src=gjfxCanvasImgSrc;
		gjfxCanvas = document.createElement("canvas");
		gjfxCanvas.id="gjfxCanvas";
		gjfxCanvas.style.width=gjfxCanvasStyleWidth+"px";
		gjfxCanvas.style.height=gjfxCanvasStyleHeight+"px";
		gjfxCanvas.width=gjfxCanvasWidth;
		gjfxCanvas.height=gjfxCanvasHeight;
		gjfxCanvasContext = gjfxCanvas.getContext("2d");
		gjfxCanvasImg.onload=function(){
			gjfxCanvasContext.drawImage(gjfxCanvasImg, 0, 0, gjfxCanvasWidth, gjfxCanvasHeight);
			if(locRecList!=undefined){//判断集合是否存在，第一次访问页面是不存在的，搜索之后才存在
				for(var i=0;i<locRecList.length;i++){
					if(i>=1){
						var lr1=locRecList[i-1];
						var lr2=locRecList[i];
						setPointLocation(gjfxCanvasContext,lr1.x,lr1.y,lr2.x,lr2.y);
						if(i==locRecList.length-1)
							setEntityLocation(gjfxCanvasContext,lr2.x,lr2.y,staffName,lr2.floor);
					}
				}
			}
			var preGjfxCanvas=document.getElementById("gjfxCanvas");
			preGjfxCanvas.parentNode.removeChild(preGjfxCanvas);
			var gjfxCanvasDiv=document.getElementById("gjfxCanvas_div");
			gjfxCanvasDiv.appendChild(gjfxCanvas);
			loadGJFXCanvas(false);
		}
	}
	else{
		//当reSizeFlag为false时，说明不改变画布大小。
		//这分为两种情况：1.初次访问页面，只显示地图，不加载轨迹。这时设置reloadFlag为false;
		//2.根据条件搜索轨迹，需要重新加载地图和轨迹，这时reloadFlag为true
		if(reloadFlag){
			paintInterval=setInterval(function(){
				var gjfxCanvasImg = new Image();
				//gjfxCanvasImg.src=path+"resource/image/area2d-1.png";
				gjfxCanvasImg.src=gjfxCanvasImgSrc;
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
			
			},"1000");
		}
		else{
			showLoadMapDiv(true);
			var gjfxCanvasImg = new Image();
			//gjfxCanvasImg.src=path+"resource/image/area2d-1.png";
			gjfxCanvasImg.src=gjfxCanvasImgSrc;
			gjfxCanvas = document.createElement("canvas");
			gjfxCanvas.id="gjfxCanvas";
			gjfxCanvas.style.width=gjfxCanvasStyleWidth+"px";
			gjfxCanvas.style.height=gjfxCanvasStyleHeight+"px";
			gjfxCanvas.width=gjfxCanvasWidth;
			gjfxCanvas.height=gjfxCanvasHeight;
			gjfxCanvasContext = gjfxCanvas.getContext("2d");
			gjfxCanvasImg.onload=function(){
				showLoadMapDiv(false);
				gjfxCanvasContext.drawImage(gjfxCanvasImg, 0, 0, gjfxCanvasWidth, gjfxCanvasHeight);
				var preGjfxCanvas=document.getElementById("gjfxCanvas");
				preGjfxCanvas.parentNode.removeChild(preGjfxCanvas);
				var gjfxCanvasDiv=document.getElementById("gjfxCanvas_div");
				gjfxCanvasDiv.appendChild(gjfxCanvas);
			}
		}
	}
}

function changeCanvasSize(bigFlag,resetFlag){
	loadGJFXCanvas(true);
    var mcw=gjfxCanvasStyleWidth;
	var mch=gjfxCanvasStyleHeight;
	if(resetFlag){
		gjfxCanvasStyleWidth=gjfxCanvasMinWidth;
	}
	else{
		if(bigFlag)
			gjfxCanvasStyleWidth+=gjfxCanvasMinWidth*0.2;
		else
			gjfxCanvasStyleWidth-=gjfxCanvasMinWidth*0.2;
	}
	
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
	
	var cswSFB=mcw/gjfxCanvasStyleWidth;
	var cshSFB=mch/gjfxCanvasStyleHeight;
	lineWidth=lineWidth*cswSFB;
	rectWidth=rectWidth*cswSFB;
	rectHeight=rectHeight*cshSFB;
	arSpace=arSpace*cshSFB;
	atSpace=atSpace*cshSFB;
	fontSize=fontSize*cshSFB;
	fontMarginLeft=fontMarginLeft*cswSFB;
	initGJFXCanvas(null,true);
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
	context.lineWidth=lineWidth;
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
.xssstj_but_img{
	width:30px;
	height:25px;
	margin-top:10px;
	right:10px;
	position: fixed;
	z-index: 1;
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
	width: 100%;
	padding:1px;
	background-color: #F6F6F6;
	position: fixed;
}
.sstj_div .row_close_div{
	width: 100%;
	height: 24px;
}
.sstj_div .row_ry_div{
	width: 100%;
	height: 40px;
	line-height: 40px;
}
.sstj_div .row_sj_div{
	width: 100%;
	height: 30px;
	line-height: 30px;
}
.sstj_div .row_ysb_div{
	width: 100%;
	height: 42px;
	line-height: 42px;
	margin-bottom: 10px;
}
.sstj_div .close_but_div{
	margin-top: 3px;
	margin-right: 20px;
	color: #636468;
	float: right;
}
.sstj_div .ry_span,.sstj_div .sj_span,.sstj_div .ysb_span{
	margin-left: 15px;
	color: #636468;
	font-size: 15px;
}
.sstj_div .staff_sel{
	width: 257px;
	height: 25px;
	line-height: 25px;
	margin-left:25px;
	color: #636468;
}
.sstj_div .td_cal{
	width: 90px;
	margin-left: 25px;
	color: #636468;
	border-color: #95B8E7;
}
.sstj_div .stp_div{
	margin-top: -30px;
	margin-left: 183px;
}
.sstj_div .etp_div{
	margin-top: 5px;
	margin-left: 183px;
}
.sstj_div .stp_div select,.sstj_div .etp_div select{
	height: 25px;
	line-height: 25px;
	color: #636468;
}
.sstj_div .ysb_inp{
	width: 85px;
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
	margin-left:183px; 
	color:#fff;
	font-size:15px;
	text-align:center;
	background-color: #1777FF;
	border-radius:5px;
}
.gjfxCanvas_div{
	width: 100%;
	overflow: auto;
}
</style>
<title>轨迹分析</title>
</head>
<body>
<div class="load_map_div" id="load_map_div">
	<div class="text_div">地图加载中</div>
</div>
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
	<div class="row_ry_div">
		<span class="ry_span">人员</span>
		<select class="staff_sel" id="staff_sel">
		</select>
	</div>
	<div class="row_sj_div">
		<span class="sj_span">时间</span>
		<input  type="text" class="Wdate td_cal" id="td_cal" onclick="WdatePicker()" readonly="readonly"/>
		<div class="stp_div">
			<select id="sth_sel">
			</select>:
			<select id="stm_sel">
			</select>:
			<select id="sts_sel">
			</select>
		</div>
	</div>
	<div class="row_sj_div">
		<div class="etp_div">
			<select id="eth_sel">
			</select>:
			<select id="etm_sel">
			</select>:
			<select id="ets_sel">
			</select>
		</div>
	</div>
	<div class="row_ysb_div">
		<span class="ysb_span">压缩比</span>
		<input class="ysb_inp" id="ysb_inp" type="text" value="200" onkeyup="this.value=this.value.replace(/[^\d.]/g,'')"/>
		<div class="search_but_div" onclick="getLocationRecords();">查询</div>
	</div>
</div>
<div class="gjfxCanvas_div" id="gjfxCanvas_div">
	<canvas id="gjfxCanvas">
	</canvas>
</div>
<%@include file="nav.jsp"%>
</body>
</html>