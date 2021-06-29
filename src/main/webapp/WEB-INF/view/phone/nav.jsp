<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<script type="text/javascript">
function goPage(page){
	switch (page) {
	case "ssdw":
		location.href=phonePath+"goPage?page=index";
		break;
	case "ryss":
		location.href=phonePath+"goPage?page=staffSearch";
		break;
	case "gjfx":
		location.href=phonePath+"goPage?page=locRecAna";
		break;
	case "bjtj":
		location.href=phonePath+"goPage?page=warnCount";
		break;
	case "test":
		location.href=phonePath+"goPage?page=test";
		break;
	}
}
</script>
<style type="text/css">
.bottom_space_div{
	width: 100%;height: 50px;
}
.bottom_div{
	width: 100%;height: 50px;line-height: 50px;bottom: 0;position: fixed;
}
.bottom_div .item_div{
	width: 25%;height: 50px;text-align: center;background-color: #eee;
}
.bottom_div .selected{
	color: #fff;background-color: #00f;
}
.bottom_div .ryss_div{
	margin-top: -50px;margin-left: 25%;
}
.bottom_div .gjfx_div{
	margin-top: -50px;margin-left: 50%;
}
.bottom_div .bjtj_div{
	margin-top: -50px;margin-left: 75%;
}
</style>
<title>Insert title here</title>
</head>
<body>
<div class="bottom_space_div"></div>
<div class="bottom_div">
	<div class="item_div ${param.page eq 'index'?'selected':''}" onclick="goPage('ssdw')">实时定位</div>
	<div class="item_div ryss_div ${param.page eq 'staffSearch'?'selected':''}" onclick="goPage('ryss')">人员搜索</div>
	<div class="item_div gjfx_div ${param.page eq 'locRecAna'?'selected':''}" onclick="goPage('gjfx')">轨迹分析</div>
	<div class="item_div bjtj_div ${param.page eq 'warnCount'?'selected':''}" onclick="goPage('bjtj')">报警统计</div>
</div>
</body>
</html>