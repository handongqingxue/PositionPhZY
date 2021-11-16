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
<script type="text/javascript" src="<%=basePath %>resource/js/jquery-3.3.1.js"></script>
<script src="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Cesium.js"></script>  
<link href="https://cesiumjs.org/releases/1.56.1/Build/Cesium/Widgets/widgets.css" rel="stylesheet"> 
<title>Insert title here</title>
<script>  
var longitude=139.9942785523;
var latitude=116.3675724221;
$(function(){
	initViewer();
	createCesiumAir();
	createCesiumMilkTruck();
	loadEntitiesText();
	setInterval(function(){
		var milkTruckEn=viewer.entities.getById("milkTruck");
		milkTruckEn.position=Cesium.Cartesian3.fromDegrees(longitude,latitude, 10);
		longitude-=0.000001;
		console.log(longitude);
		if(longitude<-139.9942785023)
			longitude=-139.9942785523;
	}, "500");
});

function initViewer(){
	viewer = new Cesium.Viewer('cesiumContainer');
}

function createCesiumAir(){
   var position = Cesium.Cartesian3.fromDegrees(139.9942785653,116.3675724221, 0);
   var heading = Cesium.Math.toRadians(135);
   var pitch = 0;
   var roll = 0;
   var hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll);
   var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);

   var entity = viewer.entities.add({
       position : position,
       orientation : orientation,
       model : {
           //uri: "http://localhost:8080/PositionPhZY/upload/CesiumMilkTruck.gltf",
           uri: "http://localhost:8080/PositionPhZY/upload/Cesium_Air.glb",
           minimumPixelSize : 128,
           maximumScale : 20000
       }
   });
   viewer.trackedEntity = entity;
}

function createCesiumMilkTruck(){
   var position = Cesium.Cartesian3.fromDegrees(longitude,latitude, 10);
   var heading = Cesium.Math.toRadians(135);
   var pitch = 0;
   var roll = 0;
   var hpr = new Cesium.HeadingPitchRoll(heading, pitch, roll);
   var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
 
   var entity = viewer.entities.add({
	   id:"milkTruck",
       position : position,
       orientation : orientation,
       model : {
           uri: "http://localhost:8080/PositionPhZY/upload/CesiumMilkTruck.gltf",
           //uri: "http://localhost:8080/PositionPhZY/upload/Cesium_Air.glb",
           minimumPixelSize : 128,
           maximumScale : 20000
       }
   });
   viewer.trackedEntity = entity;
}

function loadEntitiesText(){
	var citizensBankPark = viewer.entities.add( {
		id:"text",
	    name : 'Citizens Bank Park',
	    position : Cesium.Cartesian3.fromDegrees( 139.9942785523, 116.3675724221,20),
	    point : { //点
	        pixelSize : 5,
	        color : Cesium.Color.RED,
	        outlineColor : Cesium.Color.WHITE,
	        outlineWidth : 2
	    },
	    label : { //文字标签
	        text : 'Citizens Bank Park',
	        font : '14pt monospace',
	        style : Cesium.LabelStyle.FILL_AND_OUTLINE,
	        outlineWidth : 2,
	        verticalOrigin : Cesium.VerticalOrigin.BOTTOM, //垂直方向以底部来计算标签的位置
	        pixelOffset : new Cesium.Cartesian2( 0, -9 )   //偏移量
	    }
	    /*
	    billboard : { //图标
	        image : 'http://localhost:8080/PositionPhZY/upload/area2d-1.png',
	        width : 64,
	        height : 64
	    }
	    */
	} );
}
</script>  
</head>
<body>
<div id="cesiumContainer" style="width: 1500px; height:700px"></div>
<script>  
//https://sandcastle.cesium.com/?src=Particle%20System%20Weather.html&label=Tutorials
Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJkZWIzYTUxYy0xMmRkLTRiYTEtODE1My1kMjE1NzAyZDQwMmIiLCJpZCI6NzMyNDUsImlhdCI6MTYzNjY5NTEzOX0.rgwvu7AcuwqpYTO3kTKuZ7Pzebn1WNu2x8bKiqgbTcM';
</script>
</body>
</html>