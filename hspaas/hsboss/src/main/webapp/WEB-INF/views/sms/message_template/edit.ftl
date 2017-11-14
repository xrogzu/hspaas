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
								<li> <a href="#"> 短信管理 </a> </li>
								<li class="active">模板编辑</li>
							</ol>
						</div>
					</div>
					<div id="page-content">
						<div class="panel">
                            <!-- Panel heading -->
                            <div class="panel-heading">
                                <h3 class="panel-title">模板编辑</h3>
                            </div>
                            <!-- Panel body -->
                               <form id="myform" class="form-horizontal">
                                  <input type="hidden" name="messageTemplate.status" value="1" />
                                  <input type="hidden" name="messageTemplate.id" value="<#if messageTemplate??>${messageTemplate.id}</#if>">
                                  <div class="panel-body">
	                                  <div class="form-group">
					    			  		<label class="col-xs-2 control-label">开户用户</label>
								    		<div class="col-xs-4">
								    			<select id="userId" name="messageTemplate.userId" class="form-control">
								    				<#if userList??>
										    			<#list userList as u>
										    				<option value="${u.userId!''}" <#if u.userId==messageTemplate.userId>selected</#if>>${u.name!''}-${u.username!''}</option>
										    			</#list>
										    		</#if>
								    			</select>
								    		</div>
								    	</div>
							    		<div class="form-group">
							    			<label class="col-xs-2 control-label">路由类型</label>
			                                <div class="col-xs-4">
			                                    <select class="form-control" id="type" name="messageTemplate.routeType">
				                                    <#if routeTypes??>
										    			<#list routeTypes as a>
										    				<option value="${a.getValue()!''}" <#if messageTemplate.routeType==a.getValue()>selected</#if>>${a.getName()!''}</option>
										    			</#list>
										    		</#if>
			                                    </select>
			                                </div>
							    		</div>
							    		<div class="form-group">
	                                        <label class="col-xs-2 control-label">提交时间间隔</label>
	                                        <div class="col-xs-4">
	                                            <input type="text" class="form-control validate[required,maxSize[7],custom[number]]" name="messageTemplate.submitInterval" id="submitInterval" value="${messageTemplate.submitInterval!''}" placeholder="请输入短信提交时间间隔（同一号码）">
	                                        </div>
				                        </div>
				                        <div class="form-group">
	                                        <label class="col-xs-2 control-label">提交次数上限</label>
	                                        <div class="col-xs-4">
	                                            <input type="text" class="form-control validate[required,maxSize[5],custom[number]]" name="messageTemplate.limitTimes" id="limitTimes" value="${messageTemplate.limitTimes!''}" placeholder="请输入短信每天提交次数上限（同一号码）">
	                                        </div>
				                        </div>
				                        <div class="form-group">
			                                <label class="col-xs-2 control-label">优先级</label>
			                                <div class="col-xs-4">
			                                    <input type="text" class="form-control validate[required,maxSize[10],custom[number],min[0]]"
			                                           name="messageTemplate.priority" id="priority"
			                                           placeholder="请输入模板优先级（越大越优先）" value="${messageTemplate.priority!''}"/>
			                                </div>
			                            </div>
				                        <div class="form-group">
	                                        <label class="col-xs-2 control-label">敏感词白名单</label>
	                                        <div class="col-xs-4">
	                                        	<#if forbiddenWords?? && forbiddenWords != "">
	                                        	<#else>
	                                        		<input type="text" class="form-control validate[maxSize[256]]" name="messageTemplate.whiteWord" id="whiteWord" value="${messageTemplate.whiteWord!''}" placeholder="请输入敏感词白名单 |符号隔开">
	                                        	</#if>
	                                            
	                                        </div>
				                        </div>
	                                    <div class="form-group">
	                                        <label class="col-xs-2 control-label">模板内容</label>
	                                        <div class="col-xs-6">
	                                             <textarea class="form-control validate[required,maxSize[1000]]" name="messageTemplate.content" id="content" rows="3">${messageTemplate.content!''}</textarea>
	                                        </div>
	                                    </div>
	                                    <div class="form-group">
			                                <label class="col-xs-2 control-label">扩展号码</label>
			                                <div class="col-xs-4">
			                                    <input type="text" style="width: 590px;" class="form-control validate[maxSize[20]]"
			                                           name="messageTemplate.extNumber" id="extNumber" value="${messageTemplate.extNumber!''}" placeholder="模板扩展号码" />
			                                </div>
			                            </div>
	                                    <div class="form-group">
	                                        <div class="col-xs-9 col-xs-offset-3">
	                                            <button type="button" onclick="formSubmit();" class="btn btn-primary" name="buttonSubmit">提交</button>
	                                        </div>
	                                    </div>
                                </div>
                             </form>
                        </div>
                    </div>
				</div>
				<#include "/WEB-INF/views/main/left.ftl">
			</div>
		</div>
	</body>
	<script src="${BASE_PATH}/resources/js/bootstrap/jquery-2.1.1.min.js"></script>
	<script src="${BASE_PATH}/resources/js/confirm/jquery-confirm.js"></script> <script src="${BASE_PATH}/resources/js/pop/jquery-migrate-1.2.1.js"></script> <script src="${BASE_PATH}/resources/js/pop/yanue.pop.js"></script>
	<#include "/WEB-INF/views/common/form_validation.ftl">
	<script src="${BASE_PATH}/resources/js/bootstrap/bootstrap.min.js"></script>
	<script src="${BASE_PATH}/resources/js/bootstrap/scripts.js"></script>
	<script type="text/javascript">
		$(function(){
			$('#myform').validationEngine('attach',{promptPosition : "centerRight"});
		});
		
		function formSubmit(){
			var allCheck = $('#myform').validationEngine('validate');
			if(!allCheck){
				return;
			}
			$.ajax({
	  			url:'${BASE_PATH}/sms/message_template/update',
	  			dataType:'json',
	  			data:$('#myform').serialize(),
	  			type:'post',
	  			success:function(data){
	  				if(data){
                        Boss.alertToCallback('提交成功！',function(){
                            <#if task??>
                    			location.href = "${BASE_PATH}/sms/record/under_way_list";
	                    	<#else>
	                    		location.href = "${BASE_PATH}/sms/message_template";
	                    	</#if>
						});
	  				}else{
                        Boss.alert('提交失败！');
	  				}
	  			},error:function(data){
                    Boss.alert('系统异常!请稍后重试！');
	  			}
	  		});
		}
	</script>
</html>