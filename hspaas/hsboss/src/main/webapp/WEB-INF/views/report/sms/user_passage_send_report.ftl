<!DOCTYPE html>
<html lang="en">

<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
    <meta charset="utf-8">
    <title>融合平台</title>
    <link href="${BASE_PATH}/resources/css/bootstrap/bootstrap.min.css" rel="stylesheet">
    <link href="${BASE_PATH}/resources/css/bootstrap/style.css" rel="stylesheet">
    <link href="${BASE_PATH}/resources/js/confirm/jquery-confirm.css" rel="stylesheet">
    <link href="${BASE_PATH}/resources/css/bootstrap/font-awesome.min.css" rel="stylesheet">
    <link href="${BASE_PATH}/resources/css/bootstrap/pace.min.css" rel="stylesheet">
    <script src="${BASE_PATH}/resources/js/bootstrap/pace.min.js"></script>
    <script src="${BASE_PATH}/resources/js/My97DatePicker/WdatePicker.js"></script>
    <script src="${BASE_PATH}/resources/js/common.js"></script>
</head>

<body>
<div id="container" class="effect mainnav-lg navbar-fixed mainnav-fixed">
<#include "/WEB-INF/views/main/top.ftl">
    <div class="boxed">

        <div id="content-container">

            <div class="pageheader">
                <div class="breadcrumb-wrapper"><span class="label">所在位置:</span>
                    <ol class="breadcrumb">
                        <li><a href="#"> 管理平台 </a></li>
                        <li><a href="#"> 报表管理 </a></li>
                        <li class="active">用户通道短信统计</li>
                    </ol>
                </div>
            </div>
            <div id="page-content">

                <div class="panel">
                    <div class="panel-body">
                        <form id="myform" method="post">
                            <input type="hidden" name="pn" id="pn" value="1"/>
                            <div class="row" style="margin-top:5px">
                                <div class="col-md-3">
                                    <div class="input-group">
                                        <span class="input-group-addon">所属用户</span>
                                        <input type="text" class="form-control" id="username" name="username"
                                               value="${username!''}" readonly style="background: #fff"
                                               placeholder="选择用户">
                                        <input type="hidden" name="userId" id="userId" value="${(userId)!}"/>
                                        <span class="input-group-btn">
                                            <button class="btn btn-info" type="button"
                                                    onclick="openUserList();">选择</button>
                                        </span>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="input-group">
                                        <span class="input-group-addon">开始时间</span>
                                        <input type="text" class="form-control" onClick="WdatePicker({dateFmt:'yyyy-MM-dd'})" id="startDate"
                                               readonly style="background: #fff" name="startDate" value="${startDate!''}"
                                               placeholder="选择开始时间">
                                    </div>
                                </div>
                                 <div class="col-md-3">
                                    <div class="input-group">
                                        <span class="input-group-addon">结束时间</span>
                                        <input type="text" class="form-control" onClick="WdatePicker({dateFmt:'yyyy-MM-dd'})" id="endDate"
                                               readonly style="background: #fff" name="endDate" value="${endDate!''}"
                                               placeholder="选择结束时间">
                                    </div>
                                </div>
                                 <div class="col-md-2">
                                    <a class="btn btn-primary" onclick="searchData();">查&nbsp;&nbsp;&nbsp;询</a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="panel">
                    <div class="panel-heading">
                        <div class="pull-right" style="margin-top: 10px;margin-right: 20px;">
                            <a href="javascript:void(0);" class="btn btn-success" onclick="#">图表</a>
                        </div>
                        <h3 class="panel-title">
                            <span>用户通道短信统计</span>
                        </h3>
                    </div>
                    <div class="panel-body">
                        <table id="demo-dt-basic" class="table table-striped table-bordered" cellspacing="0"
                               width="100%">
                            <thead>
                            <tr>
                                <th>客户账号</th>
                                <th>客户名称</th>
                                <th>通道名称</th>
                                <th>提交数量（个）</th>
                                <th>计费数（条）</th>
                                <th>未知数量（条）</th>
                                <th>成功数量（条）</th>
                                <th>发送失败（条）</th>
                                <th>其他数量（条）</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#assign submitCount = 0>
                            <#assign billCount = 0>
                            <#assign unknownCount = 0>
                            <#assign successCount = 0>
                            <#assign submitFailedCount = 0>
                            <#assign otherCount = 0>
                            
                            
                            <#if container??>
                            <#list container.keySet() as key>
                            	<#assign appkey = key.appkey>
                            	<#assign name = key.name>
                            	<#assign list = container.get(key)>
                            	<#if list??>
                            	<#list list as pl>
	                            <tr>
	                            	<#if pl_index == 0>
	                                <td rowspan="${(list?size)!}" style="font-weight:bold;">${(appkey)!''}</td>
	                                <td rowspan="${(list?size)!}" style="font-weight:bold;">${(name)!''}</td>
	                                </#if>
	                                <td>${(pl.passageName)!''}</td>
	                                <td title="提交数量（个）"><#assign submitCount = submitCount + pl.submitCount>${(pl.submitCount)!'0'}</td>
	                                <td title="计费数（条）"><#assign billCount = billCount + pl.billCount>${(pl.billCount)!'0'}</td>
	                                <td title="未知数量（条）"><#assign unknownCount = unknownCount + pl.unknownCount>${(pl.unknownCount)!'0'}</td>
	                                <td title="成功数量（条）"><#assign successCount = successCount + pl.successCount>${(pl.successCount)!'0'}</td>
	                                <td title="发送失败（条）"><#assign submitFailedCount = submitFailedCount + pl.submitFailedCount>${(pl.submitFailedCount)!'0'}</td>
	                                <td title="其他数量（条）"><#assign otherCount = otherCount + pl.otherCount>${(pl.otherCount)!'0'}</td>
	                            </tr>
	                            </#list>
	                            </#if>
							</#list>
							</#if>
                            <tr>
                                <td colspan="3" align="center">总计</td>
                                <td>${(submitCount)!'0'}</td>
                                <td>${(billCount)!'0'} </td>
                                <td>${(unknownCount)!'0'}</td>
                                <td>${(successCount)!'0'}</td>
                                <td>${(submitFailedCount)!'0'}</td>
                                <td>${(otherCount)!'0'}</td>
                            </tr>
                            
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        <#include "/WEB-INF/views/main/left.ftl">
        </div>

        <div class="modal fade" id="userModal">
            <div class="modal-dialog" style="width:850px">
                <div class="modal-content" style="width:850px">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">选择用户</h4>
                    </div>
                    <div class="modal-body" id="userModelBody">

                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                        <button type="button" class="btn btn-success" onclick="clearUser();">清空</button>
                    </div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal-dialog -->
        </div><!-- /.modal -->

    </div>
    <script src="${BASE_PATH}/resources/js/bootstrap/jquery-2.1.1.min.js"></script>
    <script src="${BASE_PATH}/resources/js/confirm/jquery-confirm.js"></script> <script src="${BASE_PATH}/resources/js/pop/jquery-migrate-1.2.1.js"></script> <script src="${BASE_PATH}/resources/js/pop/yanue.pop.js"></script>
    <script src="${BASE_PATH}/resources/js/bootstrap/bootstrap.min.js"></script>
    <script src="${BASE_PATH}/resources/js/bootstrap/scripts.js"></script>
    <#include "/WEB-INF/views/base/customer/pop_user_list.ftl">
	<script type="text/javascript">
	
	    function searchData() {
	        $('#myform').attr('action', '${BASE_PATH}/report/sms/user_passage_send_report');
	        $('#myform').submit();
	    }
	
	</script>
</body>
</html>