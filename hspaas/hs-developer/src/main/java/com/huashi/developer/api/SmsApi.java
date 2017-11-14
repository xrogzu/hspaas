package com.huashi.developer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.huashi.constants.OpenApiCode.CommonApiCode;
import com.huashi.developer.exception.ValidateException;
import com.huashi.developer.model.PassportModel;
import com.huashi.developer.model.SmsModel;
import com.huashi.developer.model.SmsP2PModel;
import com.huashi.developer.model.SmsP2PTemplateModel;
import com.huashi.developer.prervice.PassportPervice;
import com.huashi.developer.prervice.SmsPrervice;
import com.huashi.developer.response.sms.SmsBalanceResponse;
import com.huashi.developer.response.sms.SmsSendResponse;
import com.huashi.developer.util.IpUtil;
import com.huashi.developer.validator.SmsP2PTemplateValidator;
import com.huashi.developer.validator.SmsP2PValidator;
import com.huashi.developer.validator.SmsValidator;

@RestController
@RequestMapping(value = "/sms")
public class SmsApi extends BasicApiSupport {

	@Autowired
	private SmsPrervice smsPrervice;
	@Autowired
	private SmsValidator smsValidator;
	@Autowired
	private SmsP2PValidator smsP2PValidator;
	@Autowired
	private SmsP2PTemplateValidator smsP2PTemplateValidator;
	@Autowired
	private PassportPervice passportPervice;

	/**
	 * 
	 * TODO 发送短信
	 * 
	 * @return
	 */
//	@ApiOperation(value="单条/批量短信发送", notes="")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "appkey", value = "用户接口账号", required = true, dataType = "String"),
//		@ApiImplicitParam(name = "mobile", value = "数字签名(接口密码、手机号、时间戳32位MD5加密生成)MD5(password+mobile+timestamp)", required = true, dataType = "String"),
//		@ApiImplicitParam(name = "mobile", value = "手机号码(多个号码之间用半角逗号隔开，最大不超过1000个号码)", required = true, dataType = "String"),
//		@ApiImplicitParam(name = "timestamp", value = "时间戳，短信发送当前时间毫秒数，生成数字签名用，有效时间30秒", required = true, dataType = "String"),
//		@ApiImplicitParam(name = "content", value = "短信内容", required = true, dataType = "String"),
//		@ApiImplicitParam(name = "extNumber", value = "扩展号，必须为数字", required = false, dataType = "Integer"),
//		@ApiImplicitParam(name = "attach", value = "自定义信息，状态报告如需推送，将携带本数据一同推送", required = false, dataType = "String"),
//		@ApiImplicitParam(name = "callback", value = "自定义状态报告推送地址，如果用户推送地址设置为固定推送地址，则本值无效，以系统绑定的固定地址为准，否则以本地址为准", required = false, dataType = "String")
//	})
	@RequestMapping(value = "/send")
	public SmsSendResponse send() {
		try {
			SmsModel model = smsValidator.validate(request.getParameterMap(), getClientIp());
			model.setApptype(getAppType());
			
			return smsPrervice.sendMessage(model);
			
		} catch (ValidateException e) {
			return saveInvokeFailedRecord(e.getMessage());
		} catch(Exception e) {
			logger.error("用户短信发送失败", e);
			return new SmsSendResponse(CommonApiCode.COMMON_SERVER_EXCEPTION);
		}
	}

	/**
	 * 
	   * TODO 普通点对点提交短信
	   * @return
	 */
	@RequestMapping(value = "/p2p")
	public SmsSendResponse p2pSend() {
		try {
			SmsP2PModel model = smsP2PValidator.validate(request.getParameterMap(), getClientIp());
			model.setApptype(getAppType());
			
			return smsPrervice.sendP2PMessage(model);
			
		} catch (ValidateException e) {
			return saveInvokeFailedRecord(e.getMessage());
		} catch(Exception e) {
			logger.error("用户普通点对点短信发送失败", e);
			return new SmsSendResponse(CommonApiCode.COMMON_SERVER_EXCEPTION);
		}
	}
	
	/**
	 * 
	   * TODO 保存错误信息
	   * @param message
	   * @return
	 */
	private SmsSendResponse saveInvokeFailedRecord(String message) {
		SmsSendResponse response = new SmsSendResponse(JSON.parseObject(message));
		try {
			// 如果处理失败则持久化到DB
			smsPrervice.saveErrorLog(response.getCode(), request.getRequestURL().toString(), IpUtil.getClientIp(request),
								request.getParameterMap(), getAppType());
		} catch (Exception e) {
			logger.error("持久化提交接口错误失败", e);
		}
		
		return response;
	}
	
	/**
	 * 
	   * TODO 模板点对点提交短信
	   * @return
	 */
	@RequestMapping(value = "/p2p_template")
	public SmsSendResponse p2pTemplateSend() {
		try {
			SmsP2PTemplateModel model = smsP2PTemplateValidator.validate(request.getParameterMap(), getClientIp());
			model.setApptype(getAppType());
			
			return smsPrervice.sendP2PTemplateMessage(model);
			
		} catch (Exception e) {
			logger.error("用户模板点对点短信发送失败", e);
			
			if(e instanceof ValidateException)
				return saveInvokeFailedRecord(e.getMessage());
			
			return new SmsSendResponse(CommonApiCode.COMMON_SERVER_EXCEPTION);
		}
	}

	/**
	 * 
	 * TODO 获取余额
	 * 
	 * @return
	 */
//	@ApiOperation(value="获取用户余额", notes="根据开发者编号查询短信余额")
//    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long")
	@RequestMapping(value = "/balance")
	public SmsBalanceResponse getBalance() {
		try {
			PassportModel model = passportPervice.getPassport(request.getParameterMap(), getClientIp());
			model.setAppType(getAppType());
			
			return smsPrervice.getBalance(model.getUserId());
		} catch (Exception e) {
			// 如果失败则存储错误日志
			String code = CommonApiCode.COMMON_SERVER_EXCEPTION.getCode();
			if(e instanceof ValidateException) {
				SmsSendResponse response = saveInvokeFailedRecord(e.getMessage());
				if(response != null)
					code = response.getCode();
			}
			return new SmsBalanceResponse(code);
		}
		
	}
}
