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
                        <li> <a href="#"> 基础信息 </a> </li>
                        <li class="active"> 参数配置管理 </li>
                    </ol>
                </div>
            </div>
            <div id="page-content">

            <div class="panel">
                <div class="panel-body">
                    <form action="${BASE_PATH}/base/system_config/index" method="post">
                    <div class="row" style="margin-top:5px">
                        <div class="col-md-4">
                            <div class="input-group">
                                <span class="input-group-addon">参数类型</span>
                                <select class="form-control" name="currentType">
                                    <#list types as ts>
                                        <option value="${ts.name()}" <#if currentType == ts.name()>selected</#if>>${ts.getTitle()}</option>
                                    </#list>

                                </select>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <button type="submit" class="btn btn-primary">查&nbsp;&nbsp;&nbsp;询</button>
                        </div>
                    </div>
                    </form>
                </div>
            </div>
                <div class="panel">
                    <div class="panel-heading">
                        <div class="pull-right" style="margin-top: 10px;margin-right: 20px;">
                        <#--<a class="btn btn-success" href="${BASE_PATH}/base/notification/add">发布公告</a>-->
                        </div>
                        <h3 class="panel-title">
                            <span>参数配置列表</span>
                        </h3>

                    </div>
                    <div class="panel-body">
                        <table id="demo-dt-basic" class="table table-striped table-bordered" cellspacing="0" width="100%">
                            <thead>
                            <tr>
                                <th>编号</th>
                                <th>参数名</th>
                                <th>参数key</th>
                                <th>参数值</th>
                                <th>备注</th>
                                <th>修改时间</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list configList as pl>
                            <tr>
                                <td>${(pl_index+1)}</td>
                                <td>${pl.attrName!''}</td>
                                <td>${pl.attrKey!''} </td>
                                <td>${pl.attrValue!''}</td>
                                <td>${pl.remark!''}</td>
                                <td>
                                    <#assign showTime = pl.createTime>
                                <#if pl.modifyTime??>
                                    <#assign showTime = pl.modifyTime>
                                </#if>
                                ${showTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                                <td>
                                    <a class="btn btn-primary btn-xs" href="${BASE_PATH}/base/system_config/edit?id=${pl.id}"><i class="fa fa-edit"></i>&nbsp;编辑 </a>
                                </td>
                            </tr>
                            </#list>
                            </tbody>
                        </table>
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

    function deleteById(id){
        Boss.confirm("确定要删除该配置吗？",function(){
            $.ajax({
                url:'${BASE_PATH}/base/system_config/delete',
                data:{'id':id},
                dataType:'json',
                type:'post',
                success:function(data){
                    Boss.alertToCallback(data.message,function(){
                        if(data.result){
                            location.reload();
                        }
                    });
                },error:function(data){
                    Boss.alert('删除配置异常！');
                }
            });
        });
    }
</script>
</html>