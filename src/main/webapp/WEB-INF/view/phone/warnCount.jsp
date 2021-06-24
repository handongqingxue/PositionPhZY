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
var pieSeriesDataList;
var pieLegendData=[];
var pieLegendSelected={};
$(function(){
	$.post("getDutys",
		function(data){
			alert(JSON.stringify(data));
		}
	,"json");
	initJRBJTJSLDiv();
	initBarChartDiv("date");
	initPieChartDiv("date");
});

//初始化今日报警统计数量
function initJRBJTJSLDiv(){
    var todayDate=getTodayDate();
    var nowTime=getNowTime();
	$.post("initTodayWarnCount",
		{todayDate:todayDate,nowTime:nowTime},
		function(data){
			var twList=data.todayWarnList;
			var countListDiv=$("#jrbjtjsl_div #count_list_div");
			for(var i=0;i<twList.length;i++){
				var itemStr="";
				var tw=twList[i];
				if(i%2==0){
					itemStr+="<div class=\"item_div\" style=\"margin-top:0px;margin-left:0px\">";
						itemStr+="<span class=\"text_span\">"+tw.wtName+"</span>";
						itemStr+="<span class=\"count_span\">"+tw.warnCount+"</span>";
					itemStr+="</div>";
				}
				else{
					itemStr+="<div class=\"item_div\" style=\"margin-top:-30px;margin-left:180px;\">";
						itemStr+="<div class=\"text_span\">"+tw.wtName+"</div>";
						itemStr+="<span class=\"count_span\">"+tw.warnCount+"</span>";
					itemStr+="</div>";
				}
				countListDiv.append(itemStr);
			}
		}
	,"json");
}

function getNowTime(){
	var date=new Date();
	var hour=date.getHours();
	var minute=date.getMinutes();
	var second=date.getSeconds();
	var nowTime=hour+":"+minute+":"+second;
    return nowTime;
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

function initBarChartDiv(flag){
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
    //var barStartDate=getAddDate(days);
    var barStartDate="2021-03-01";
    //var barEndDate=getTodayDate();
    var barEndDate="2021-04-01";
    //alert(barStartDate+"-"+barEndDate);
	
	//https://echarts.apache.org/examples/zh/editor.html?c=bar1
	$.post("initBJTJBarChartData",
		{startDate:barStartDate,endDate:barEndDate,flag:flag},
		function(data){
			//alert(JSON.stringify(data.seriesList));
			var legendDataList=data.legendDataList;
			var xAxisDataLabelList=data.xAxisDataLabelList;
			var seriesList=data.seriesList;
			
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
			        //data: ['蒸发量', '降水量']
				    data: legendDataList
			    },
			    xAxis: [
			        {
			            type: 'category',
			            //data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
			            data: xAxisDataLabelList,
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
			        	type:'value',
		                minInterval: 1,
		                axisLine:{
		                    lineStyle:{
		                        color:"#999",
		                        width:0.5
		                    }
		                },
		                axisLabel:{
		                    fontSize:9
		                },
		                splitLine:{
		                    lineStyle:{
		                        color:"#ddd",
		                        width:0.5
		                    }
		                }
			        }
			    ],
			    /*
			    series: [
			        {
			            name: '超速报警',
			            type: 'bar',
			            data: [2.0, 4.9, 7.0],
			            barGap:0
			        },
			        {
			            name: '按键报警',
			            type: 'bar',
			            data: [2.6, 5.9, 9.0],
			            barGap:0
			        }
			    ]
			    */
			    //series:[{\"data\":[{\"4-13\":0},{\"4-14\":0},{\"4-15\":0}],\"barGap\":0,\"name\":\"超速报警\",\"type\":\"bar\"},{\"data\":[{\"4-13\":0},{\"4-14\":0},{\"4-15\":0}],\"barGap\":0,\"name\":\"按键报警\",\"type\":\"bar\"}]
			    series:seriesList
			};

			option && myChart.setOption(option);
		}
	,"json");
}

function initPieChartDiv(flag){
	var days;
	$("#pie_search_type_div #but_div div").css("color","#000");
    $("#pie_search_type_div #but_div div").css("border-bottom","#fff solid 1px");
	if(flag=="date"){
		$("#pie_search_type_div #date_but_div").css("color","#477A8F");
        $("#pie_search_type_div #date_but_div").css("border-bottom","#497DD0 solid 1px");
        days=-1;
	}
	else if(flag=="week"){
		$("#pie_search_type_div #week_but_div").css("color","#477A8F");
        $("#pie_search_type_div #week_but_div").css("border-bottom","#497DD0 solid 1px");
        days=-7;
	}
	else if(flag=="month"){
		$("#pie_search_type_div #month_but_div").css("color","#477A8F");
        $("#pie_search_type_div #month_but_div").css("border-bottom","#497DD0 solid 1px");
        days=-30;
	}
	else if(flag=="three_month"){
		$("#pie_search_type_div #three_month_but_div").css("color","#477A8F");
        $("#pie_search_type_div #three_month_but_div").css("border-bottom","#497DD0 solid 1px");
        days=-90;
	}
    var pieStartDate=getAddDate(days);
    //var pieStartDate="2021-03-01";
    var pieEndDate=getTodayDate();
    //var pieEndDate="2021-04-01";
    //alert(pieStartDate+"-"+pieEndDate);
    
	$.post("initBJTJPieChartData",
		{startDate:pieStartDate,endDate:pieEndDate},
		function(data){
			initPieLegendData(data.seriesList);
		
			initPieOption();
		}
	,"json");
}

function initPieOption(){
	var chartDom = document.getElementById('pie_chart_div');
	var myChart = echarts.init(chartDom);
	var option;

	option = {
	    title: {
	        text: '区域报警统计',
	        left: 'center'
	    },
	    tooltip: {
	        trigger: 'item',
            formatter:function (json) {
            	//console.log(JSON.stringify(json))//2043
                //console.log("json==="+JSON.stringify(json)+","+JSON.stringify(json["data"]["bjlxList"]))
                var html="";
                html+=json["data"]["name"]+":"+json["data"]["value"];
                var bjlxList=json["data"]["bjlxList"];
                bjlxList.map((item,index)=>{
                    html+="<br/>"+item.name+":"+item.count
                });
                return html;
            }
	    },
	    legend: {
	    	bottom: 0,
            left: 'center',
            data:pieLegendData,
            selected:pieLegendSelected
	    },
	    series: [
	        {
	            type: 'pie',
	            radius: '50%',
                center: ['50%', '50%'],
                label: {
                    position: 'inner',
                    formatter:
                    	pieLegendData.length<=4
                            ?
                            function(json){
                                return ""
                            }
                            :
                            function(json){
                                return json["data"]["name"]
                            }
                },
                selectedMode: 'single',
                data:pieSeriesDataList,
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
	        }
	    ]
	};
	
	//myChart.clear();
	option && myChart.setOption(option);
}

function initPieLegendData(seriesList){
	pieSeriesDataList=seriesList;
	pieLegendData.length=0;
	pieSeriesDataList.map((item,index)=>{
		pieLegendData.push(item.name);
		pieLegendSelected[item.name]=true;
	});
}

function resetPieLegendData(showAll){
	pieSeriesDataList.map((item,index)=>{
        if(showAll){
            pieLegendSelected[item.name]=true;
        }
        else{
            if(index>=4){
                pieLegendSelected[item.name]=false;
            }
        }
    });
	
	initPieOption();
	
	if(showAll){
        $(".show_all_div .show_all_but_div").css("display","none");
        $(".show_all_div .hide_part_but_div").css("display","block");
    }
    else{
        $(".show_all_div .show_all_but_div").css("display","block");
        $(".show_all_div .hide_part_but_div").css("display","none");
    }
}

function goPage(page){
	switch (page) {
	case "index":
		location.href=phonePath+"goIndex";
		break;
	}
}
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
.bar_chart_div{
	width:100%;
	height: 300px;
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
.pie_chart_div{
	width:100%;
	height: 300px;
}
.show_all_div{
    width: 100%;
    background-color: #fff;
}
.show_all_div .but_div{
	width: 80px;
    height: 30px;
    line-height: 30px;
    margin: auto;
    text-align: center;
    color: #fff;
    background-color: #4caf50;
}
.hide_part_but_div{
    display: none;
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
        <div class="date_but_div" id="date_but_div" onclick="initBarChartDiv('date');">日</div>
        <div class="week_but_div" id="week_but_div" onclick="initBarChartDiv('week');">周</div>
        <div class="month_but_div" id="month_but_div" onclick="initBarChartDiv('month');">月</div>
    </div>
</div>
<div class="bar_chart_div" id="bar_chart_div"></div>

<div class="pie_search_type_div" id="pie_search_type_div">
    <div class="but_div" id="but_div">
        <div class="date_but_div" id="date_but_div" onclick="initPieChartDiv('date');">日</div>
        <div class="week_but_div" id="week_but_div" onclick="initPieChartDiv('week');">周</div>
        <div class="month_but_div" id="month_but_div" onclick="initPieChartDiv('month');">月</div>
        <div class="three_month_but_div" id="three_month_but_div" onclick="initPieChartDiv('three_month');">三个月</div>
    </div>
</div>
<div class="pie_chart_div" id="pie_chart_div"></div>
<div class="show_all_div">
    <div class="but_div show_all_but_div" onclick="resetPieLegendData(true)">查看全部</div>
    <div class="but_div hide_part_but_div" onclick="resetPieLegendData(false)">隐藏部分</div>
</div>
</body>
</html>