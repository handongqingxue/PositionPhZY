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
$(function(){
	/*
	$.post("summaryWarn",
		function(data){
			
		}
	,"json");
	*/
	initBarChartDiv();
});

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
	            data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
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
<div class="bar_search_type_div" id="bar_search_type_div">
    <div class="but_div" id="but_div">
        <div class="date_but_div" id="date_but_div">日</div>
        <div class="week_but_div" id="week_but_div">周</div>
        <div class="month_but_div" id="month_but_div">月</div>
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