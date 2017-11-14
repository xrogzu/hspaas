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
								<li> <a href="#">  短信管理 </a> </li>
								<li class="active"> 优先级词库 </li>
							</ol>
						</div>
					</div>
					<div id="page-content">
						
						<div class="panel">
							<div class="panel-body">
								<form id="myform">
									<input type="hidden" name="pn" id="pn" value="1"/>
								    <div class="row" style="margin-top:5px">
								    	<div class="col-md-4">
								    		<div class="input-group">
								    			<span class="input-group-addon">词库内容</span>
								    			<input type="text" class="form-control" id="content" name="content" value="${content!''}" placeholder="输入词库内容...">
								    		</div>
							    		</div>
								    	<div class="col-md-4">
								    		<div class="input-group">
								    			<span class="input-group-addon">用户名称</span>
								    			<select id="userId" name="userId" class="form-control">
								    				<option value="-1">全部</option> 
								    				<#if userList??>
										    			<#list userList as u>
										    				<option value="${u.id!''}" <#if userId??><#if u.id==userId>selected</#if></#if> >${u.name!''}-${u.userName!''}</option>
										    			</#list>
									    			</#if>
								    			</select>
								    		</div>
							    		</div>
								    	<div class="col-md-4">
								    		<a class="btn btn-primary" onclick="jumpPage(1);">查&nbsp;&nbsp;&nbsp;询</a>
								    	</div>
								    </div>
							    </form>
							</div>
						</div>

						<div class="panel">
                        <div class="panel-heading">
                        	 <div class="pull-right"  style="margin-top: 10px;margin-right: 20px;">
                                <button class="btn btn-success" onclick="location.href='${BASE_PATH}/sms/priority_words/create'">新增优先级词库</button>
                            </div>
                            <h3 class="panel-title">
                            	<span>优先级词库列表</span>
                            </h3>
                        </div>
                        <div class="panel-body">
                            <table id="demo-dt-basic" class="table table-striped table-bordered" cellspacing="0" width="100%">
                                <thead>
                                    <tr>
                                        <th>编号</th>
                                        <th>状态</th>
                                        <th>用户名称</th>
                                        <th>优先词库</th>
                                        <th>优先等级</th>
                                        <th>操作时间</th>
                                        <th>操&nbsp;&nbsp;&nbsp;&nbsp;作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                	<#list page.list as pl>
                                    <tr>
                                        <td>${(page.currentPage - 1) * page.pageSize + (pl_index+1)}</td>
                                        <td><#if pl.status==0>开启<#elseif pl.status==1>关闭</#if></td>
                                        <td>${(pl.userModel.name)!}-${(pl.userModel.username)!}</td>
                                        <td>${pl.content!''}</td>
										<td>${pl.priority!''}</td>
                                        <td>${pl.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                                        <td>
                                        	<a class="btn btn-primary btn-xs" href="${BASE_PATH}/sms/priority_words/edit?id=${pl.id}"><i class="fa fa-edit"></i>&nbsp;编辑 </a>
                                        	<#if pl.status==0>
                                        		<a class="btn btn-default btn-xs" href="javascript:enableAndisable(${pl.id},1)"><i class="fa fa-edit"></i>&nbsp关闭</a>
                                        	<#else>
                                        		<a class="btn btn-default btn-xs" href="javascript:enableAndisable(${pl.id},0)"><i class="fa fa-edit"></i>&nbsp开启</a>
                                        	</#if>
                                        	<a class="btn btn-danger btn-xs" href="javascript:void(0);" onclick="deleteById(${pl.id});"><i class="fa fa-trash"></i>&nbsp;删除 </a>
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
		 <form id="mySubmitform" class="form-horizontal">
		 	  <input type="hidden" id="idHide" name="smsPriorityWords.id"/>
		 	  <input type="hidden" id="statusHide" name="smsPriorityWords.status"/>
		 </form>
		<script src="${BASE_PATH}/resources/js/bootstrap/jquery-2.1.1.min.js"></script>
		<script src="${BASE_PATH}/resources/js/confirm/jquery-confirm.js"></script> <script src="${BASE_PATH}/resources/js/pop/jquery-migrate-1.2.1.js"></script> <script src="${BASE_PATH}/resources/js/pop/yanue.pop.js"></script>
		<script src="${BASE_PATH}/resources/js/bootstrap/bootstrap.min.js"></script>
		<script src="${BASE_PATH}/resources/js/bootstrap/scripts.js"></script>
	</body>
	<script type="text/javascript">
		function jumpPage(p){
			$('#pn').val(p);
			$('#myform').attr('action','${BASE_PATH}/sms/priority_words/index');
			$('#myform').submit();
		}
		
		
		function deleteById(id){
            Boss.confirm('确定要删除当前优先级词库吗？',function(){
				$.ajax({
					url:'${BASE_PATH}/sms/priority_words/delete',
					data:{id:id},
					dataType:'json',
					type:'post',
					success:function(data){
                        Boss.alertToCallback(data.message,function(){
                            if(data.result){
                                location.reload();
                            }
						});
					},error:function(data){
                        Boss.alert('删除当前优先级词库请求失败！');
					}
				})
			});
		}
		
	function enableAndisable(id,status){
		$("#idHide").val(id);
		$("#statusHide").val(status);
		var message="开启成功！";
		if(status==1){
			message="关闭成功！";
		}
        $.ajax({
            url:'${BASE_PATH}/sms/priority_words/enableAndisable',
            dataType:'json',
            data:$('#mySubmitform').serialize(),
            type:'post',
            success:function(data){
                if(data.result){
                    Boss.alertToCallback(message,function(){
                        location.href = "${BASE_PATH}/sms/priority_words"
                    });
                }else{
                    Boss.alert(data.message);
                }
            },error:function(data){
                Boss.alert('开启/关闭失败!');
            }
        });
    }
	</script>
</html>