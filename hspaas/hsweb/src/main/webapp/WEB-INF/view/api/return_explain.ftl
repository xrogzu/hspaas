<!DOCTYPE HTML>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta content="华时融合平台，提供更简单，更稳定，更快速" name="keywords">
<meta content="华时融合平台，提供更简单，更稳定，更快速" name="description">
<title>华时融合平台—API&文档-返回值说明</title>
<#include "/common/assets.ftl"/>
<link href="${rc.contextPath}/assets/css/api.css" rel="stylesheet" type="text/css" />
<link href="${rc.contextPath}/assets/css/api_content.css" rel="stylesheet" type="text/css" />
</head>
<body>
	<#include "/common/header.ftl"/>
	<div class="syzn_box">
		<div class="inner clearfix">
			<div id="user" class="main">
				<nav class="apileft">
				<#include "/common/api_menu.ftl"/>    
				</nav>
				<div class="content">
				        <h2 class="page-title">返回值说明</h2>
				        <article class="doc">
				            <section class="one">
				                <h3>1、返回码总体说明</h3>
				                <pre><code>code = 0:	正确返回。可以从api返回的对应字段中取数据。
									code &gt; 0:	调用API时发生错误，需要开发者进行相应的处理。
									-50 &lt; code &lt;= -1:	权限验证失败，需要开发者进行相应的处理。
									code &lt;= -50:	系统内部错误，请联系技术支持，调查问题原因并获得解决方案。</code></pre>
				
				                <h3>2、返回值示例</h3>
				                <div class="can">
				                    <table>
				                        <tbody><tr>
				                            <th>参数名</th>
				                            <th>类型</th>
				                            <th>描述</th>
				                            <th>示例</th>
				                        </tr>
				                        <tr>
				                            <td>http_status_code</td>
				                            <td>int</td>
				                            <td>调用失败http返回码设为400</td>
				                            <td>400</td>
				                        </tr>
				                        <tr>
				                            <td>code</td>
				                            <td>int</td>
				                            <td>系统返回码</td>
				                            <td>-1</td>
				                        </tr>
				                        <tr>
				                            <td>msg</td>
				                            <td>String</td>
				                            <td>错误描述</td>
				                            <td>非法的apikey</td>
				                        </tr>
				                        <tr>
				                            <td>detail</td>
				                            <td>String</td>
				                            <td>具体错误描述或解决方法</td>
				                            <td>apikey不正确或没有授权</td>
				                        </tr>
				                    </tbody></table>
				                </div>
				                <p>API调用失败，返回错误结果示例：</p>
				                <pre><code>{
								    "http_status_code":400, //调用结束后的http_status_code值
								    "code": 3,//错误码
								    "msg": "账户余额不足",   //错误描述
								    "detail":"账户需要充值，请充值后重试" //具体错误描述或解决方法
								}</code></pre>
				                <h3>3、常见的返回码</h3>
				                <div class="can">
				                <table>
				                    <tbody><tr>
				                        <th>返回码(code)</th>
				                        <th>错误信息(msg)</th>
				                        <th>具体错误描述或解决方法(detail)</th>
				                        <th><div style="min-width: 100px"></div>问题处理人</th>
				                    </tr>
				                    <tr>
				                        <td>0</td>
				                        <td>OK</td>
				                        <td>调用成功，该值为null</td>
				                        <td>无需处理</td>
				                    </tr>
				                    <tr>
				                        <td>1</td>
				                        <td>请求参数缺失</td>
				                        <td>补充必须传入的参数</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>2</td>
				                        <td>请求参数格式错误</td>
				                        <td>按提示修改参数值的格式</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>3</td>
				                        <td>账户余额不足</td>
				                        <td>账户需要充值，请充值后重试</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>4</td>
				                        <td>关键词屏蔽</td>
				                        <td>关键词屏蔽，修改关键词后重试</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>5</td>
				                        <td>未找到对应id的模板</td>
				                        <td>模板id不存在或者已经删除</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>6</td>
				                        <td>添加模板失败</td>
				                        <td>模板有一定的规范，按失败提示修改</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>7</td>
				                        <td>模板不可用</td>
				                        <td>审核状态的模板和审核未通过的模板不可用</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>8</td>
				                        <td>同一手机号30秒内重复提交相同的内容</td>
				                        <td>请检查是否同一手机号在30秒内重复提交相同的内容</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>9</td>
				                        <td>同一手机号5分钟内重复提交相同的内容超过3次</td>
				                        <td>为避免重复发送骚扰用户，同一手机号5分钟内相同内容最多允许发3次</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>10</td>
				                        <td>手机号黑名单过滤</td>
				                        <td>手机号在黑名单列表中（你可以把不想发送的手机号添加到黑名单列表）</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>11</td>
				                        <td>接口不支持GET方式调用</td>
				                        <td>接口不支持GET方式调用，请按提示或者文档说明的方法调用，一般为POST</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>12</td>
				                        <td>接口不支持POST方式调用</td>
				                        <td>接口不支持POST方式调用，请按提示或者文档说明的方法调用，一般为GET</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>13</td>
				                        <td>营销短信暂停发送</td>
				                        <td>由于运营商管制，营销短信暂时不能发送</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>14</td>
				                        <td>解码失败</td>
				                        <td>请确认内容编码是否设置正确</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>15</td>
				                        <td>签名不匹配</td>
				                        <td>短信签名与预设的固定签名不匹配</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>16</td>
				                        <td>签名格式不正确</td>
				                        <td>短信内容不能包含多个签名【 】符号</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>17</td>
				                        <td>24小时内同一手机号发送次数超过限制</td>
				                        <td>请检查程序是否有异常或者系统是否被恶意攻击</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>18</td>
				                        <td>签名校验失败</td>
				                        <td>签名校验失败</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>19</td>
				                        <td>请求已失效</td>
				                        <td>请求已失效</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>20</td>
				                        <td>不支持的国家地区</td>
				                        <td>不支持的国家地区</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>21</td>
				                        <td>解密失败</td>
				                        <td>解密失败</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>22</td>
				                        <td>1小时内同一手机号发送次数超过限制</td>
				                        <td>1小时内同一手机号发送次数超过限制</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>23</td>
				                        <td>发往模板支持的国家列表之外的地区</td>
				                        <td>发往模板支持的国家列表之外的地区</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>24</td>
				                        <td>添加告警设置失败</td>
				                        <td>添加告警设置失败</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>25</td>
				                        <td>手机号和内容个数不匹配</td>
				                        <td>手机号和内容个数不匹配</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>26</td>
				                        <td>流量包错误</td>
				                        <td>流量包错误</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>27</td>
				                        <td>未开通金额计费</td>
				                        <td>未开通金额计费</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>28</td>
				                        <td>运营商错误</td>
				                        <td>运营商错误</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>33</td>
				                        <td>超过频率</td>
				                        <td>同一个手机号同一验证码模板每30秒只能发送一条。此规则用于防止验证码轰炸。
				                            可在后台-<a href="/admin/sys_setting/bomb_rules" target="_blank">系统设置</a>了解详情及设置。</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-1</td>
				                        <td>非法的apikey</td>
				                        <td>apikey不正确或没有授权</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-2</td>
				                        <td>API没有权限</td>
				                        <td>用户没有对应的API权限</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-3</td>
				                        <td>IP没有权限</td>
				                        <td>访问IP不在白名单之内，可在后台"账户设置-&gt;IP白名单设置"里添加该IP</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-4</td>
				                        <td>访问次数超限</td>
				                        <td>调整访问频率或者申请更高的调用量</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-5</td>
				                        <td>访问频率超限</td>
				                        <td>短期内访问过于频繁，请降低访问频率</td>
				                        <td>开发者</td>
				                    </tr>
				                    <tr>
				                        <td>-50</td>
				                        <td>未知异常</td>
				                        <td>系统出现未知的异常情况</td>
				                        <td>技术支持</td>
				                    </tr>
				                    <tr>
				                        <td>-51</td>
				                                                                <td>系统繁忙</td>
				                        <td>系统繁忙，请稍后重试</td>
				                        <td>技术支持</td>
				                    </tr>
				                    <tr>
				                        <td>-52</td>
				                        <td>充值失败</td>
				                        <td>充值时系统出错</td>
				                        <td>技术支持</td>
				                    </tr>
				                    <tr>
				                        <td>-53</td>
				                        <td>提交短信失败</td>
				                        <td>提交短信时系统出错</td>
				                        <td>技术支持</td>
				                    </tr>
				
				                    <tr>
				                        <td>-54</td>
				                        <td>记录已存在</td>
				                        <td>常见于插入键值已存在的记录</td>
				                        <td>技术支持</td>
				                    </tr>
				                    <tr>
				                        <td>-55</td>
				                        <td>记录不存在</td>
				                        <td>没有找到预期中的数据</td>
				                        <td>技术支持</td>
				                    </tr>
				                    <tr>
				                        <td>-57</td>
				                        <td>用户开通过固定签名功能，但签名未设置</td>
				                        <td>联系客服或技术支持设置固定签名</td>
				                        <td>技术支持</td>
				                    </tr>
				                </tbody></table>
				                </div>
				            </section>
				        </article>
				    </div>
			</div>
		</div>
	</div>
	<#include "/common/footer.ftl"/>
</body>
<script>
	//头部菜单选择
	service("api");
	api_service("return_explain");
</script>
</html>