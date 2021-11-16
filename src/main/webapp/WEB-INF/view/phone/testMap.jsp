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
</head>
<body>
<div id="cesiumContainer" style="width: 1500px; height:700px"></div>  
<input type="button" value="移动" onclick="moveEntities()"/>
<input type="button" value="复位" onclick="restoreEntities()"/>
<script>  
var rectImg1,rectImg2;
var textLongitude=-75.166493;
var textLatitude=39.9060534;
var ellipseImgLong=-103.0;
var ellipseImgLat=40;
var milkTruckEnLong=119.55265849594228;
var milkTruckEnLat=37.04175799355465;
$(function(){
	//initWorldViewer();
	initViewer();
	//loadEntitiesEllipseImg();
	//loadEntitiesFlagImg();
	//loadEntitiesRectImg(-110.0, 10.0, -90.0, 30.0,'http://localhost:8080/PositionPhZY/upload/area2d-1.png',1);
	//loadEntitiesRectImg(-100.0, -20.0, -90.0, -10.0,'http://localhost:8080/PositionPhZY/upload/area2d-1.png',2);
	//loadEntitiesRectImg(-150.0, 10.0, -120.0, 30.0,'http://localhost:8080/PositionPhZY/upload/IMG_20200823_151415.jpg',3);
	//loadArea2d();
	loadTileset();
	//loadEntitiesText();
	//loadImageryLayers()
	setInterval(() => {
		moveEntities();
	}, 500);
});

function moveEntities(){
	var milkTruckEn=viewer.entities.getById("milkTruck");
	milkTruckEn.position=Cesium.Cartesian3.fromDegrees(milkTruckEnLong,milkTruckEnLat, 20);
	milkTruckEnLong-=0.00001;
	if(milkTruckEnLong<119.551111111111)
		milkTruckEnLong=119.55265849594228;
	
	/*
	var ellipseImg=viewer.entities.getById("ellipseImg");
	ellipseImg.position=Cesium.Cartesian3.fromDegrees(ellipseImgLong,ellipseImgLat);
	ellipseImgLong-=1;
	if(ellipseImgLong<-180.166493)
		ellipseImgLong=-103.0;
	*/
	//viewer.entities.remove(textEn);
}

function restoreEntities(){
	var textEn=viewer.entities.getById("text");
	textEn.position=Cesium.Cartesian3.fromDegrees(-75.166493, 39.9060534);
	//viewer.entities.add(rectImg1);
	//viewer.entities.add(rectImg2);
}

//https://blog.csdn.net/yk583443123/article/details/103523311
//https://blog.csdn.net/u013594477/article/details/79578748
var viewer;
Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJkZWIzYTUxYy0xMmRkLTRiYTEtODE1My1kMjE1NzAyZDQwMmIiLCJpZCI6NzMyNDUsImlhdCI6MTYzNjY5NTEzOX0.rgwvu7AcuwqpYTO3kTKuZ7Pzebn1WNu2x8bKiqgbTcM'; 

function initWorldViewer(){
	viewer = new Cesium.Viewer('cesiumContainer', {  
	   terrainProvider: Cesium.createWorldTerrain()  
	});  
}

function initViewer(){
	viewer = new Cesium.Viewer('cesiumContainer');
	
	//获取经纬度、高度链接：https://www.cnblogs.com/telwanggs/p/11289455.html
	//获取事件触发所在的  html Canvas容器
    var canvas=viewer.scene.canvas;

    //获取事件句柄
    var handler=new Cesium.ScreenSpaceEventHandler(canvas);

    //处理事件函数
    handler.setInputAction(function(movement){

        //拾取笛卡尔坐标
        var ellipsoid=viewer.scene.globe.ellipsoid;//全局椭球体
        var cartesian=viewer.scene.camera.pickEllipsoid(movement.endPosition,ellipsoid)//拾取鼠标在椭圆上的结束点笛卡尔坐标点
        //转化笛卡尔坐标 为经纬度
        var mesDom=document.getElementById('mes');
        if(cartesian){
            var cartographic=ellipsoid.cartesianToCartographic(cartesian);//笛卡尔坐标转制图坐标
            //var coordinate="经度:"+Cesium.Math.toDegrees(cartographic.longitude).toFixed(2)+",纬度:"+Cesium.Math.toDegrees(cartographic.latitude).toFixed(2)+
                    "相机高度:"+Math.ceil(viewer.camera.positionCartographic.height);
            var coordinate="经度:"+Cesium.Math.toDegrees(cartographic.longitude)+",纬度:"+Cesium.Math.toDegrees(cartographic.latitude)+
            "相机高度:"+Math.ceil(viewer.camera.positionCartographic.height);
			console.log("coordinate==="+coordinate);
        }else{
        	
        }
    },Cesium.ScreenSpaceEventType.MOUSE_MOVE);//监听的是鼠标滑动事件
}

function loadEntitiesText(){
	var citizensBankPark = viewer.entities.add( {
		id:"text",
	    name : 'Citizens Bank Park',
	    position : Cesium.Cartesian3.fromDegrees( 39.166493, 116.9060534 ),
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

function loadEntitiesEllipseImg(longitude,latitude){
	var entity = viewer.entities.add({
	  id:"ellipseImg",
	  position: Cesium.Cartesian3.fromDegrees(-103.0, 40.0),
	  ellipse : {
	    semiMinorAxis : 250000.0,
	    semiMajorAxis : 400000.0,
	    //material : Cesium.Color.BLUE.withAlpha(0.5)
	    material:'http://localhost:8080/PositionPhZY/upload/area2d-1.png'
	  }
	});
	viewer.zoomTo(viewer.entities);

	//var ellipse = entity.ellipse; // Fo

	//ellipse.material = 'http://localhost:8080/PositionPhZY/upload/area2d-1.png';
}

function loadEntitiesFlagImg(){
	viewer.entities.add({
	    rectangle : {
	        coordinates : Cesium.Rectangle.fromDegrees(-100.0, 20.0, -90.0, 30.0),
	        material : new Cesium.StripeMaterialProperty({
	            evenColor: Cesium.Color.WHITE,
	            oddColor: Cesium.Color.BLUE,
	            repeat: 5
	        })
	    }
	});
}

//https://blog.csdn.net/sinat_31213021/article/details/120024000
function loadEntitiesRectImg(west,south,east,north,url,index){
	var rectImg=viewer.entities.add({
	    rectangle : {
	        coordinates : Cesium.Rectangle.fromDegrees(west,south,east,north),
	        material : url
	    }
	});
	if(index==1)
		rectImg1=rectImg;
	else if(index==2)
		rectImg2=rectImg;
}

function loadTileset(){
	var tileset = new Cesium.Cesium3DTileset({
	   url: "http://localhost:8080/PositionPhZY/upload/b3dm/tileset.json",
	   shadows:Cesium.ShadowMode.DISABLED,//去除阴影
	});
	console.log(tileset)
	viewer.scene.primitives.add(tileset);
	tileset.readyPromise.then(function(tileset) {
	   viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0, -0.5, 0));
	   //viewer.scene.primitives.remove(tileset);
	}).otherwise(function(error) {
	    throw(error);
	});
	
	var position = Cesium.Cartesian3.fromDegrees(milkTruckEnLong,milkTruckEnLat, 20);
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
	
	/*
	tileset = new Cesium.Cesium3DTileset({
	   url: "http://localhost:8080/PositionPhZY/upload/model2/tileset.json",
	   shadows:Cesium.ShadowMode.DISABLED,//去除阴影
	});
	viewer.scene.primitives.add(tileset);
	tileset.readyPromise.then(function(tileset) {
	   viewer.camera.viewBoundingSphere(tileset.boundingSphere, new Cesium.HeadingPitchRange(0, -0.5, 0));
	   var cartographic = Cesium.Cartographic.fromCartesian(tileset.boundingSphere.center);
	   console.log(cartographic);
	   setTimeout(function(){
		   //viewer.scene.primitives.remove(tileset);
		   //viewer.scene.primitives.removeAll();
	   },"10000");
	}).otherwise(function(error) {
	    throw(error);
	});
	*/
}

function loadArea2d(){
	var rectangle=Cesium.Rectangle.fromDegrees(112.2588781098,39.5047262435,112.2981585251,39.5233702752);
	viewer.imageryLayers.addImageryProvider(new Cesium.SingleTileImageryProvider({
		url:"http://localhost:8080/PositionPhZY/upload/area2d-1.png",
		rectangle: rectangle
	}))
	var layers = viewer.imageryLayers;  
	viewer._cesiumWidget._creditContainer.style.display="none";
	viewer.camera.setView({
		destination: Cesium.Rectangle.fromDegrees(112.2770508586,39.5050978677,112.2776657605,39.5193995362) //定位坐标点，建议使用谷歌地球坐标位置无偏差
	});
}

function loadImage(){
	var position = Cesium.Cartesian3.fromDegrees(119.39, 39.9, 0)
	viewer.entities.add({
	    show: true,
	    position: position,
	    ellipse : {
	        semiMinorAxis : 80000.0,
	        semiMajorAxis : 80000.0,
	        material: new Cesium.ImageMaterialProperty({
	            image:'http://localhost:8080/PositionPhZY/upload/area2d-1.png',    // 图片以材质的方式填充
	            color: Cesium.Color.RED,
	            repeat : new Cesium.Cartesian2(1, 1)
	        }),
	       
	        // rotation: 45,
	        // 利用下面这个属性设置素材填充时的选装角度（顺时针方向）
	        stRotation : 45,
	      },
	 
	});
	viewer.zoomTo(viewer.entities);
}

function loadImageryLayers(){
	viewer.imageryLayers.addImageryProvider(
	          new this.Cesium.WebMapServiceImageryProvider({
	            url: 'http://localhost:8080/PositionPhZY/upload/area2d-1.png',
	            layers: '',
	            parameters: {
	              format: 'image/png',
	              transparent: true,
	            }
	          })
	        )
}
 </script>
</body>
</html>