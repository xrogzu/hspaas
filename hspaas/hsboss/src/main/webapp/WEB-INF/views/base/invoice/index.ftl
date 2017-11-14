<!DOCTYPE html>
<html lang="en">

	<head>
		<meta http-equiv="content-type" content="text/html;charset=UTF-8" />
		<meta charset="utf-8">

		<title>融合平台</title>
		<link href="${BASE_PATH}/resources/css/bootstrap/bootstrap.min.css" rel="stylesheet">
        <link href="${BASE_PATH}/resources/css/bootstrap/style.css" rel="stylesheet">
        <link href="${BASE_PATH}/resources/js/confirm/jquery-confirm.css" rel="stylesheet">
		<link href="${BASE_PATH}/resources/css/bootstrap/font-awesome.min.css" rel="stylesheet">
		<link href="${BASE_PATH}/resources/css/bootstrap/pace.min.css" rel="stylesheet">
		<script src="${BASE_PATH}/resources/js/bootstrap/pace.min.js"></script>
        <script src="${BASE_PATH}/resources/js/common.js"></script>
    </head>

	<body>
		<div id="container" class="effect mainnav-lg navbar-fixed mainnav-fixed">
			<#include "/WEB-INF/views/main/top.ftl">
			<div class="boxed">

				<div id="content-container">

					<div class="pageheader">
						<div class="breadcrumb-wrapper"> <span class="label">所在位置:</span>
							<ol class="breadcrumb">
								<li> <a href="#"> 管理平台 </a> </li>
								<li> <a href="#">  基础信息 </a> </li>
								<li class="active"> 发票管理 </li>
							</ol>
						</div>
					</div>
					<div id="page-content">
						
						<div class="panel">
							<div class="panel-body">
								<form id="myform>
									<input type="hidden" name="pn" id="pn" value="1"/>
								    <div class="row" style="margin-top:5px">
								    	<div class="col-md-4">
								    		<div class="input-group">
								    			<span class="input-group-addon">发票信息</span>
								    			<input type="text" class="form-control" id="invoiceKeyword" name="invoiceKeyword" value="${invoiceKeyword!''}" placeholder="发票抬头/收件地址/收件人">
								    		</div>
								    	</div>
								    	<div class="col-md-4">
								    		<div class="input-group">
								    			<span class="input-group-addon">用户信息</span>
								    			<input type="text" class="form-control" name="userKeyword" value="${userKeyword!''}" id="userKeyword" placeholder="用户姓名/手机号">
								    		</div>
								    	</div>
								    	<div class="col-md-4">
								    		<button class="btn btn-primary">查&nbsp;&nbsp;&nbsp;询</button>
								    	</div>
								    </div>
							    </form>
							</div>
						</div>

						<div class="panel">
                        <div class="panel-heading">
                            <div class="pull-right" style="margin-top: 10px;margin-right: 20px;">
                                <a href="${BASE_PATH}/base/invoice/add" class="btn btn-success">新增发票</a>
                            </div>
                            <h3 class="panel-title">
                            <span>发票列表</span>
                            </h3>
                           
                        </div>
                        <div class="panel-body">
                            <table id="demo-dt-basic" class="table table-striped table-bordered" cellspacing="0" width="100%">
                                <thead>
                                    <tr>
                                        <th>编号</th>
                                        <th>发票抬头</th>
                                        <th>用户姓名</th>
                                        <th>发票类型</th>
                                        <th>状态</th>
                                        <th>开票金额</th>
                                        <th>申请时间</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                	<#list page.list as pl>
                                    <tr>
                                        <td>${(page.currentPage - 1) * page.pageSize + (pl_index+1)}</td>
                                        <td>${pl.title!''}</td>
                                        <td>
										${(pl.user.name)!''}
										</td>
										<td>
											<#if pl.type == 0>
												普通发票
											<#elseif pl.type == 1>
                                                增值税专用发票
											<#else>
												未知
											</#if>
										</td>
                                        <td>
											<#if pl.type == 0>
                                                待处理
											<#elseif pl.type == 1>
                                                已邮寄
											<#elseif pl.type == 2>
                                                已签收
											<#elseif pl.type == 3>
                                                包裹异常
											<#else>
                                                未知
											</#if>
                                        </td>
                                        <td>${pl.money!0}</td>
                                        <td>${pl.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                                        <td>
										<#if pl.status?? && (pl.status == 0 || pl.status == 3)>
                                        	<a class="btn btn-danger btn-xs" href="${BASE_PATH}/base/invoice/edit?id=${pl.id}">
												<i class="fa fa-edit"></i>&nbsp;处理
											</a>
										<#else>
                                            <a class="btn btn-info btn-xs" href="${BASE_PATH}/base/invoice/edit?id=${pl.id}">
                                                <i class="fa fa-eye"></i>&nbsp;查看
                                            </a>
										</#if>
                                        </td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                             <nav style="margin-top:-15px">
								${(page.showPageHtml)!}
							</nav>
                        </div>
                    </div>
				</div>
				<#include "/WEB-INF/views/main/left.ftl">
			</div>

		</div>
		<script src="${BASE_PATH}/resources/js/bootstrap/jquery-2.1.1.min.js"></script>
		<script src="${BASE_PATH}/resources/js/confirm/jquery-confirm.js"></script> <script src="${BASE_PATH}/resources/js/pop/jquery-migrate-1.2.1.js"></script> <script src="${BASE_PATH}/resources/js/pop/yanue.pop.js"></script>
		<script src="${BASE_PATH}/resources/js/bootstrap/bootstrap.min.js"></script>
		<script src="${BASE_PATH}/resources/js/bootstrap/scripts.js"></script>
	</body>
	<script type="text/javascript">
		function jumpPage(p){
			$('#pn').val(p);
			$('#myform').attr('action','${BASE_PATH}/base/invoice/index');
			$('#myform').submit();
		}
	</script>
</html>