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
<script type="text/javascript" src="<%=basePath %>resource/js/echarts.min.js"></script>
<script type="text/javascript">
var path='<%=basePath %>';
var phonePath=path+"phone/";
var zhAlignWithLabel=false;
var alignWithLabel=false;
var zhxzzh=10;//综合X轴字号
var xzzh;
$(function(){
	/*
	$.post("summaryWarn",
		function(data){
			
		}
	,"json");
	*/
	initJRBJTJSLDiv();
	initBarChartDiv();
});

//初始化今日报警统计数量
function initJRBJTJSLDiv(){
	$.post("getWarnTriggers",
		function(data){
			var countListDiv=$("#jrbjtjsl_div #count_list_div");
			var wtList=data.result;
			for(var i=0;i<wtList.length;i++){
				var itemStr="";
				var wt=wtList[i];
				if(i%2==0){
					itemStr+="<div class=\"item_div\" style=\"margin-top:0px;margin-left:0px\">";
						itemStr+="<span class=\"text_span\">"+wt.name+"</span>";
						itemStr+="<span class=\"count_span\">0</span>";
					itemStr+="</div>";
				}
				else{
					itemStr+="<div class=\"item_div\" style=\"margin-top:-30px;margin-left:180px;\">";
						itemStr+="<div class=\"text_span\">"+wt.name+"</div>";
						itemStr+="<span class=\"count_span\">0</span>";
					itemStr+="</div>";
				}
				countListDiv.append(itemStr);
			}
		}
	,"json");
}

function initBarList(flag){
	var days;
	$("#bar_search_type_div #but_div div").css("color","#000");
    $("#bar_search_type_div #but_div div").css("border-bottom","#fff solid 1px");
	if(flag=="date"){
		$("#bar_search_type_div #date_but_div").css("color","#477A8F");
        $("#bar_search_type_div #date_but_div").css("border-bottom","#497DD0 solid 1px");
        days=-7;
        xzzh=10;
        alignWithLabel=true;
	}
	else if(flag=="week"){
		$("#bar_search_type_div #week_but_div").css("color","#477A8F");
        $("#bar_search_type_div #week_but_div").css("border-bottom","#497DD0 solid 1px");

        days=-30;
        xzzh=9;
        alignWithLabel=false;
	}
	else if(flag=="month"){
		$("#bar_search_type_div #month_but_div").css("color","#477A8F");
        $("#bar_search_type_div #month_but_div").css("border-bottom","#497DD0 solid 1px");

        days=-365;
        xzzh=9;
        alignWithLabel=true;
	}
    var barStartDate=getAddDate(days);
    var barEndDate=getTodayDate();
    alert(barStartDate+"-"+barEndDate);
}

function getTodayDate(){
	var date=new Date();
	var year=date.getFullYear();
	var month=date.getMonth()+1;
	var dateOfMonth=date.getDate();
	var todayDate=year+"-"+(month<10?"0"+month:month)+"-"+(dateOfMonth<10?"0"+dateOfMonth:dateOfMonth);
    return todayDate;
}

function getAddDate(days){
	var date=new Date();
    date=new Date(date.setDate(date.getDate()+days));
    var year=date.getFullYear();
    var month=date.getMonth()+1;
    var dateOfMonth=date.getDate();
    return year+"-"+(month<10?"0"+month:month)+"-"+(dateOfMonth<10?"0"+dateOfMonth:dateOfMonth);
}

function initBarChartDiv(){
	//https://echarts.apache.org/examples/zh/editor.html?c=bar1
	var chartDom = document.getElementById('bar_chart_div');
	var myChart = echarts.init(chartDom);
	var option;
	option = {
	    tooltip: {
	        trigger: 'axis'
	    },
	    legend: {
               itemWidth:10,
               itemHeight:10,
               x:'center',
               y: '15px',
               textStyle:{
                   fontSize:9
               },
	        data: ['蒸发量', '降水量']
	    },
	    xAxis: [
	        {
	            type: 'category',
	            data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
	            axisTick:{alignWithLabel:zhAlignWithLabel},
	            axisLine:{
	                lineStyle:{
	                    color:"#999",
	                    width:0.5
	                }
	            },
	            axisLabel: {
	                fontSize:zhxzzh,
	                interval:0
	                //rotate:45
	            }
	        }
	    ],
	    yAxis: [
	        {
	            type: 'value'
	        }
	    ],
	    series: [
	        {
	            name: '蒸发量',
	            type: 'bar',
	            data: [2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3]
	        },
	        {
	            name: '降水量',
	            type: 'bar',
	            data: [2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3]
	        }
	    ]
	};

	option && myChart.setOption(option);
}

function goPage(page){
	switch (page) {
	case "index":
		location.href=phonePath+"goIndex";
		break;
	}
}
/*
var date=new Date();
date.setTime("1605060088742");
alert(date);
*/
</script>
<style type="text/css">
body{
	margin: 0;
}
.top_div{
    width: 100%;
    height: 40px;
    line-height: 40px;
    color:#fff;
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    background-color:#154E6C;
}
.back_but_div{
    width: 60px;
    height: 40px;
    line-height: 40px;
    margin-top: -40px;
    text-align: center;
    color: #fff;
}


.jrbjtjsl_div{
	width: 100%;
	height: auto;
	margin-top: 10px;
	background-color:#fff;
}
.jrbjtjsl_div .jrbjsl_tit_div{
    height: 40px;
    line-height: 40px;
    margin-left: 10px;
    color: #000;
    font-size: 16px;
    font-weight: bold;
}
.jrbjtjsl_div .count_list_div{
	width: 100%;
	height: auto;
	margin: 0 10px 0 10px;
}
.jrbjtjsl_div .count_list_div .item_div{
    width: 160px;
    height: 30px;
    line-height: 30px;
}
.jrbjtjsl_div .count_list_div .item_div .text_span{
    width: 130px;
    margin-left: 1px;
    text-align: left;
    color: #828282;
    position: absolute;
}
.jrbjtjsl_div .count_list_div .item_div .count_span{
    width: 30px;
    margin-left: 130px;
    text-align: right;
    color: #343434;
    position: absolute;
}

.bar_search_type_div{
    width: 100%;
    height: 40px;
    line-height: 40px;
    background-color:#fff;
    border-bottom: #eee solid 1px;
}
.bar_search_type_div .but_div{
    width: 210px;
    margin: auto;
}
.bar_search_type_div .but_div .date_but_div{
	width: 50px;
	height: 39px;
	text-align: center;
	color: #477A8F;
	font-weight: bold;
	border-bottom: #497DD0 solid 1px;
}
.bar_search_type_div .but_div  .week_but_div{
	width: 50px;
	height: 39px;
	margin-top: -40px;
	margin-left: 80px;
	text-align: center;
	color: #000;
	font-weight: bold;
	border-bottom: #fff solid 1px;
}
.bar_search_type_div .but_div .month_but_div{
    width: 50px;
    height: 39px;
    margin-top: -40px;
    margin-left: 160px;
    text-align: center;
    color: #000;
    font-weight: bold;
    border-bottom: #fff solid 1px;
}

.pie_search_type_div{
    width: 100%;
    height: 40px;
    line-height: 40px;
    background-color:#fff;
    border-bottom: #eee solid 1px;
  }
.pie_search_type_div .but_div{
  	width: 290px;
  	margin: auto;
}
.pie_search_type_div .but_div .date_but_div{
  	width: 50px;
  	height: 39px;
	text-align: center;
	color: #477A8F;
	font-weight: bold;
	border-bottom: #497DD0 solid 1px;
}
.pie_search_type_div .but_div .week_but_div{
  	width: 50px;
	height: 39px;
	margin-top: -40px;
	margin-left: 80px;
	text-align: center;
	color: #000;
	font-weight: bold;
	border-bottom: #fff solid 1px;
}
.pie_search_type_div .but_div .month_but_div{
  	width: 50px;
	height: 39px;
	margin-top: -40px;
	margin-left: 160px;
	text-align: center;
	color: #000;
	font-weight: bold;
	border-bottom: #fff solid 1px;
}
.pie_search_type_div .but_div .three_month_but_div{
	width: 50px;
	height: 39px;
	margin-top: -40px;
	margin-left: 240px;
	text-align: center;
	color: #000;
	font-weight: bold;
	border-bottom: #fff solid 1px;
}
</style>
<title>报警统计</title>
</head>
<body>
<div class="top_div">报警统计</div>
<div class="back_but_div" onClick="goPage('index')">&lt;返回</div>
<div class="jrbjtjsl_div" id="jrbjtjsl_div">
    <div class="jrbjsl_tit_div">今日报警</div>
    <div class="count_list_div" id="count_list_div">
    </div>
</div>
<div class="bar_search_type_div" id="bar_search_type_div">
    <div class="but_div" id="but_div">
        <div class="date_but_div" id="date_but_div" onclick="initBarList('date');">日</div>
        <div class="week_but_div" id="week_but_div" onclick="initBarList('week');">周</div>
        <div class="month_but_div" id="month_but_div" onclick="initBarList('month');">月</div>
    </div>
</div>
<div id="bar_chart_div" style="width:100%;height: 300px;"></div>
<div class="pie_search_type_div" id="pie_search_type_div">
    <div class="but_div" id="but_div">
        <div class="date_but_div" id="date_but_div">日</div>
        <div class="week_but_div" id="week_but_div">周</div>
        <div class="month_but_div" id="month_but_div">月</div>
        <div class="three_month_but_div" id="three_month_but_div">三个月</div>
    </div>
</div>
</body>
</html>