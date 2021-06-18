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
$(function(){
	//https://blog.csdn.net/xuerwang/article/details/107856754
    var point_img = new Image();
    point_img.src=path+"resource/image/003.jpg";
	var canvas = document.getElementById("myCanvas");
	//canvas.style.width="720px";
	//canvas.style.height="670px";
	//canvas.width=720;
	//canvas.height=670;

	//canvas.style.width="1720px";
	//canvas.style.height="1670px";
	canvas.width=2841;
	canvas.height=2643;
	ctx = canvas.getContext("2d");
	point_img.onload=function(){
		//ctx.drawImage(point_img, 0, 0, 720, 670);
		ctx.drawImage(point_img, 0, 0, 2841, 2643);
		
		ctx.beginPath();

		ctx.stokeStyle = 'red'    ;//点填充

		ctx.arc(100,100,10,0,2*Math.PI);
	    ctx.stroke();
	    
		ctx.beginPath();
		ctx.arc(200,200,10,0,2*Math.PI);
	    ctx.stroke();
		
		ctx.beginPath();
		ctx.arc(300,300,10,0,2*Math.PI);
		
	    ctx.stroke();
	}
});

function small(){
	myCanvasWidth-=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	var myCanvas=$("#myCanvas");
	myCanvas.css("width",myCanvasWidth+"px");
	myCanvas.css("height",myCanvasHeight+"px");
}

function big(){
	myCanvasWidth+=30;
	myCanvasHeight=myCanvasWidth*2643/2841;
	var myCanvas=$("#myCanvas");
	myCanvas.css("width",myCanvasWidth+"px");
	myCanvas.css("height",myCanvasHeight+"px");
}

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
</script>
<style type="text/css">
body {
    margin: 0;
}
</style>
<title>Insert title here</title>
</head>
<body>
<div style="width: 100%;height: 600px;overflow: auto;">
	<canvas id="myCanvas" style="width:720px;height:670px;">
	</canvas>
</div>
<input type="button" value="缩小" onclick="small();"/>
<input type="button" value="放大" onclick="big();"/>
</body>
</html>