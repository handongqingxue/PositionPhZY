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
var myCanvasWidth=720;
var myCanvasHeight=670;
var canvas;
var rr=20;
var rectWidth=330;
var rectHeight=100;
var arSpace=43;
var atSpace=78;
var fontSize=50;
var fontMarginLeft=45;
$(function(){
	//https://blog.csdn.net/xuerwang/article/details/107856754
	repaint();
});

function repaint(){
	var point_img = new Image();
    point_img.src=path+"resource/image/003.jpg";
	canvas = document.getElementById("myCanvas");
	//canvas.style.width="720px";
	//canvas.style.height="670px";
	//canvas.width=720.52;
	//canvas.height=670.49;

	//canvas.style.width="1720px";
	//canvas.style.height="1670px";
	canvas.width=2841;
	canvas.height=2643;
	ctx = canvas.getContext("2d");
	point_img.onload=function(){
		//ctx.drawImage(point_img, 0, 0, 720.52, 670.49);
		ctx.drawImage(point_img, 0, 0, 2841, 2643);
		
		ctx.beginPath();

		ctx.strokeStyle = 'red'    ;//点填充
		ctx.fillStyle='red';
		ctx.lineWidth=rr*1.5;

		setEntityLocation(ctx,200,100);
		setEntityLocation(ctx,300,400);
		setEntityLocation(ctx,500,600);
		//ctx.arc(100,100,rr,0,2*Math.PI);
	    //ctx.stroke();
	    
	    /*
		ctx.beginPath();
		ctx.arc(200,200,rr,0,2*Math.PI);
	    ctx.stroke();

		ctx.beginPath();
	    ctx.lineWidth = "1";
	    ctx.fillStyle = "blue";
	    ctx.fillRect(50,55,330,100);
	    ctx.stroke();
	    
		ctx.font="50px bold 黑体";
		ctx.fillStyle = "#fff";
		ctx.fillText("李天赐(1层)",80,115);
	    ctx.stroke();
		*/
		
		/*
		ctx.beginPath();
	    ctx.strokeStyle = "red";
		ctx.arc(300,300,rr,0,2*Math.PI);
		
	    ctx.stroke();
	    */
	    
		load(0);
	}
}

function small(){
	load(1);
    var mainDiv=$("#main_div");
    mainDiv.empty();
    var mcw=myCanvasWidth;
	var mch=myCanvasHeight;
	myCanvasWidth-=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	rr=rr*mcw/myCanvasWidth;
	rectWidth=rectWidth*mcw/myCanvasWidth;
	rectHeight=rectHeight*mch/myCanvasHeight;
	arSpace=arSpace*mch/myCanvasHeight;
	atSpace=atSpace*mch/myCanvasHeight;
	fontSize=fontSize*mch/myCanvasHeight;
	fontMarginLeft=fontMarginLeft*mcw/myCanvasWidth;
    mainDiv.append("<canvas id=\"myCanvas\" style=\"width:"+myCanvasWidth+"px;height:"+myCanvasHeight+"px;\"></canvas>");
	repaint();
	
	/*
	myCanvasWidth-=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	var myCanvas=$("#myCanvas");
	myCanvas.css("width",myCanvasWidth+"px");
	myCanvas.css("height",myCanvasHeight+"px");
	*/
}

function big(){
	load(1);
    var mainDiv=$("#main_div");
    mainDiv.empty();
    var mcw=myCanvasWidth;
	var mch=myCanvasHeight;
	myCanvasWidth+=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	rr=rr*mcw/myCanvasWidth;
	rectWidth=rectWidth*mcw/myCanvasWidth;
	rectHeight=rectHeight*mch/myCanvasHeight;
	arSpace=arSpace*mch/myCanvasHeight;
	atSpace=atSpace*mch/myCanvasHeight;
	fontSize=fontSize*mch/myCanvasHeight;
	fontMarginLeft=fontMarginLeft*mcw/myCanvasWidth;
    mainDiv.append("<canvas id=\"myCanvas\" style=\"width:"+myCanvasWidth+"px;height:"+myCanvasHeight+"px;\"></canvas>");
	repaint();
	
    /*
	myCanvasWidth+=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	var myCanvas=$("#myCanvas");
	myCanvas.css("width",myCanvasWidth+"px");
	myCanvas.css("height",myCanvasHeight+"px");
	*/
}

function load(flag){
	if(flag==1){
		$("#small_but").attr("disabled",true);
		$("#big_but").attr("disabled",true);
		$("#load_div").css("display","block");
	}
	else{
		$("#small_but").attr("disabled",false);
		$("#big_but").attr("disabled",false);
		$("#load_div").css("display","none");
	}
}

function setEntityLocation(ctx,x,y){
	//x=200 y=200
	ctx.beginPath();
	ctx.strokeStyle = 'red'    ;//点填充
	ctx.fillStyle='red';
	ctx.lineWidth=rr*1.5;
	ctx.arc(x,2643-y,rr,0,2*Math.PI);
    ctx.stroke();

	ctx.beginPath();
    ctx.lineWidth = "1";
    ctx.fillStyle = "blue";
    //var rectWidth=330;
    //var rectHeight=100;
    ctx.fillRect(x-rectWidth/2,2643-y-rectHeight-arSpace,rectWidth,rectHeight);
    ctx.stroke();

	ctx.font=fontSize+"px bold 黑体";
	ctx.fillStyle = "#fff";
	ctx.fillText("李天赐(1层)",x-rectWidth/2+fontMarginLeft,2643-y-atSpace);
    ctx.stroke();
}

/*
$(document).on("mousewheel DOMMouseScroll", function (event) {
    var delta = (event.originalEvent.wheelDelta && (event.originalEvent.wheelDelta > 0 ? 1 : -1)) ||  // chrome & ie
                (event.originalEvent.detail && (event.originalEvent.detail > 0 ? -1 : 1));              // firefox

    if (delta > 0) {
    // 向上滚
    big();
    } else if (delta < 0) {
      // 向下滚
      small();
    }
});
*/
</script>
<style type="text/css">
body {
    margin: 0;
}
</style>
<title>Insert title here</title>
</head>
<body>
<div id="load_div" style="width: 100%;height:100%;text-align:center;background-color: #00ff00;position: fixed;display: none;">地图加载中...</div>
<div id="main_div" style="width: 100%;height: 600px;overflow: auto;">
	<canvas id="myCanvas" style="width:720.52px;height:670.49px;">
	</canvas>
</div>
<input type="button" id="small_but" value="缩小" onclick="small();"/>
<input type="button" id="big_but" value="放大" onclick="big();"/>
</body>
</html>