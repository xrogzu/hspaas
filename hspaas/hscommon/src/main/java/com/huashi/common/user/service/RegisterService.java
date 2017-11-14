package com.huashi.common.user.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.huashi.bill.pay.constant.PayContext.PaySource;
import com.huashi.common.notice.context.MessageContext.ReadStatus;
import com.huashi.common.notice.dao.EmailSendRecordMapper;
import com.huashi.common.notice.dao.NotificationMessageMapper;
import com.huashi.common.notice.domain.EmailSendRecord;
import com.huashi.common.notice.domain.EmailTemplate;
import com.huashi.common.notice.domain.NotificationMessage;
import com.huashi.common.notice.service.IEmailSendService;
import com.huashi.common.notice.service.IEmailTemplateService;
import com.huashi.common.notice.util.EmailSendUtil;
import com.huashi.common.settings.context.SettingsContext.NotificationMessageTemplateType;
import com.huashi.common.settings.context.SettingsContext.SystemConfigType;
import com.huashi.common.settings.dao.PushConfigMapper;
import com.huashi.common.settings.domain.PushConfig;
import com.huashi.common.settings.domain.SystemConfig;
import com.huashi.common.settings.service.ISystemConfigService;
import com.huashi.common.user.context.UserContext.BalancePayType;
import com.huashi.common.user.dao.UserFluxDiscountMapper;
import com.huashi.common.user.domain.User;
import com.huashi.common.user.domain.UserBalance;
import com.huashi.common.user.domain.UserDeveloper;
import com.huashi.common.user.domain.UserFluxDiscount;
import com.huashi.common.user.domain.UserSmsConfig;
import com.huashi.common.user.model.RegisterModel;
import com.huashi.common.util.DateUtil;
import com.huashi.sms.passage.service.ISmsPassageAccessService;

@Service
public class RegisterService implements IRegisterService {

	private Logger logger = LoggerFactory.getLogger(RegisterService.class);
	
	@Autowired
	private IEmailSendService emailSendService;

	@Autowired
	private IUserService userService;
	@Autowired
	private EmailSendUtil emailSendUtil;
	@Autowired
	private IUserBalanceService userBalanceService;
	@Autowired
	private IUserSmsConfigService userSmsConfigService; 
	@Autowired
	private UserFluxDiscountMapper userFluxDiscountMapper;
	@Autowired
	private PushConfigMapper pushConfigMapper;
	@Autowired
	private NotificationMessageMapper notificationMessageMapper;
	@Autowired
	private IEmailTemplateService emailTemplateService;
	@Autowired
	private EmailSendRecordMapper emailSendRecordMapper;
	@Autowired
	private IUserPassageService userPassageService;
	@Autowired
	private ISystemConfigService systemConfigService;
	@Reference
	private ISmsPassageAccessService iSmsPassageAccessService;
	
	@Override
	public boolean sendEmailBeforeVerify(String email) {
		return emailSendService.sendRegisterVerifyContent(email);
	}

	@Override
//	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean register(RegisterModel model) {
		if (!validate(model))
			return false;

		try {
			UserDeveloper developer = userService.save(model.getUser(), model.getUserProfile());
			if (developer == null) {
				logger.error("保存用户失败");
				return false;
			}
			
			if(!saveUserBalance(model.getUserBalances(), developer.getUserId()))
				return false;
			
			if(!saveFluxDiscount(model.getUserFluxDiscount(), developer.getUserId()))
				return false;
			
			if(!saveUserSmsConfig(model.getUserSmsConfig(), developer.getUserId()))
				return false;
			
			if(!userPassageService.initUserPassage(developer.getUserId(), model.getPassageList()))
				return false;
			
			if(!iSmsPassageAccessService.updateByModifyUser(developer.getUserId()))
				return false;
			
			if(!userPushCallbackConfig(model.getPushConfigs(), developer.getUserId()))
				return false;
			
			if(!addRegisterFinishedNotificationMessage(developer.getUserId()))
				return false;
			
			// 如果发送邮件开关打开，则需要发送邮件 
			if(model.isSendEmail())
				sendEmail(model.getUser(), developer);

			return true;
		} catch (Exception e) {
			logger.error("注册或添加客户失败", e);
//			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
	
	/**
	 * 
	 * TODO 初始化用户流量折扣信息（省内移动、省内联通、省内电信、全国移动等..）
	 * 
	 * @param discount
	 *            折扣信息（按小数，如98折，则存储0.98）
	 * @param userId
	 *            用户序列号
	 * @return
	 */
	private boolean saveFluxDiscount(UserFluxDiscount discount, int userId) {
		if (discount == null) {
			List<SystemConfig> configs = systemConfigService.findByType(SystemConfigType.USER_REGISTER_FLUX_DISCOUNT.name());
			if (CollectionUtils.isEmpty(configs))
				throw new RuntimeException("用户流量折扣默认值信息为空，无法初始化");

			//  设置流量计费折扣（本地移动，本地联通，本地电信，全国移动，全国联通，全国电信）
			double localcm = 1.0d, localcu = 1.0d, localct = 1.0d, globalcm = 1.0d, globalcu = 1.0d, globalct = 1.0d;
			for (SystemConfig config : configs) {
				if(config.getAttrKey().contains("local_cm"))
					localcm = Double.parseDouble(config.getAttrValue());
				else if(config.getAttrKey().contains("local_cu"))
					localcu = Double.parseDouble(config.getAttrValue());
				else if(config.getAttrKey().contains("local_ct"))
					localct = Double.parseDouble(config.getAttrValue());
				else if(config.getAttrKey().contains("global_cm"))
					globalcm = Double.parseDouble(config.getAttrValue());
				else if(config.getAttrKey().contains("global_cu"))
					globalcu = Double.parseDouble(config.getAttrValue());
				else if(config.getAttrKey().contains("global_ct"))
					globalct = Double.parseDouble(config.getAttrValue());
			}
			discount = new UserFluxDiscount();
			discount.setUserId(userId);
			discount.setLocalCmOff(localcm);
			discount.setLocalCuOff(localcu);
			discount.setLocalCtOff(localct);
			discount.setGlobalCmOff(globalcm);
			discount.setGlobalCuOff(globalcu);
			discount.setGlobalCtOff(globalct);
			discount.setCreateTime(new Date());
			return userFluxDiscountMapper.insertSelective(discount) > 0;
		} else {
//			discount = new UserFluxDiscount();
			discount.setUserId(userId);
			discount.setCreateTime(new Date());
			return userFluxDiscountMapper.insertSelective(discount) > 0;
		}
	}
	
	/**
	 * 
	   * TODO 保存用户信息配置信息（如计费字数，通道组等）
	   * @param userSmsConfig
	   * @param userId
	   * @return
	 */
	private boolean saveUserSmsConfig(UserSmsConfig userSmsConfig, int userId) {
		if(userSmsConfig == null) {
			logger.error("用户: {} 短信配置信息为空", userId);
			return false;
		}
		
		userSmsConfig.setUserId(userId);
		
		return userSmsConfigService.save(userSmsConfig);
	}
	
	/**
	 * 
	 * TODO 验证参数有效性
	 * 
	 * @param model
	 * @return
	 */
	private boolean validate(RegisterModel model) {
		return true;
	}

	/**
	 * 
	 * TODO 初始化用户业务量余额信息
	 * 
	 * @param balances
	 *            用户业务量余额信息（短信剩余条数，流量剩余钱数，语音剩余条数）
	 * @param userId
	 *            用户ID
	 * @return
	 */
	private boolean saveUserBalance(List<UserBalance> balances, int userId) {
		try {
			if (CollectionUtils.isEmpty(balances)) {
				List<SystemConfig> configs = systemConfigService
						.findByType(SystemConfigType.USER_REGISTER_BALANCE.name());
				if (CollectionUtils.isEmpty(configs))
					throw new RuntimeException("用户余额默认值信息为空，无法初始化");

				UserBalance balance = null;
				for (SystemConfig config : configs) {
					balance = new UserBalance();
					balance.setType(Integer.parseInt(config.getRemark()));
					balance.setBalance(Double.valueOf(config.getAttrValue()));
					balance.setUserId(userId);
					balance.setCreateTime(new Date());
					balance.setPayType(BalancePayType.PREPAY.getValue());
					balance.setPaySource(PaySource.USER_ACCOUNT_EXCHANGE);
					userBalanceService.saveBalance(balance);
				}
			} else {
				for (UserBalance balance : balances) {
					balance.setUserId(userId);
					userBalanceService.saveBalance(balance);
				}
			}
			
			return true;
		} catch (Exception e) {
			logger.error("saveUserBalance error, error is {} ", e.getMessage());
			throw new RuntimeException(e);
		}
	}

    /**
     * TODO 初始化各平台回调URL信息
     * @param pushConfigs
     * @param usreId
     * @return
     */
	private boolean userPushCallbackConfig(List<PushConfig> pushConfigs, int usreId) {
		try {
			if(CollectionUtils.isEmpty(pushConfigs))
				return true;
				
			for(PushConfig pushConfig : pushConfigs){
				pushConfig.setUserId(usreId);
				pushConfigMapper.insertSelective(pushConfig);
			}
			return true;
		} catch (Exception e) {
			logger.error("userPushCallbackConfig error, error is {} ", e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	   * TODO 发送注册/开户成功站内私信
	   * 
	   * @param userId
	   * @return
	 */
	private boolean addRegisterFinishedNotificationMessage(int userId) {
		SystemConfig config = systemConfigService.findByTypeAndKey(SystemConfigType.NOTIFICATION_MESSAGE_TEMPLATE.name(), 
				NotificationMessageTemplateType.REGISTER_SUCCESS.name());
		
		NotificationMessage message = new NotificationMessage();
		message.setUserId(userId);
		message.setStatus(ReadStatus.UNREAD.getCode());
		message.setType(NotificationMessageTemplateType.REGISTER_SUCCESS.getCode());
		if(config == null) {
			message.setTitle(NotificationMessageTemplateType.REGISTER_SUCCESS.getTitle());
			message.setContent(String.format(NotificationMessageTemplateType.REGISTER_SUCCESS.getContent(),
					DateUtil.getMinuteStr(new Date())));
		} else {
			message.setTitle(config.getRemark());
			message.setContent(String.format(config.getAttrValue(), DateUtil.getMinuteStr(new Date())));
		}
		
		message.setCreateTime(new Date());
		return notificationMessageMapper.insert(message) > 0;
	}
	
	/**
	 * 
	   * TODO 异步发送注册成功邮件
	   * 		
	   * @param user
	   * @param developer
	 */
	private void sendEmail(User user, UserDeveloper developer) {
		// 7、异步发送短信和邮件（邮件中需将 接口账号和密码发送，并将接口协议文档作为附件发送[后台可配置开关]）
		try {
			// 邮箱为空则不发送
			if(StringUtils.isEmpty(user.getEmail()))
				return;
			
			// 用户名默认以手机号为准，如果手机号为空则以邮箱
			EmailTemplate template = emailTemplateService.getRegisterSuceessContent(
					StringUtils.isEmpty(user.getMobile()) ? user.getEmail() : user.getMobile(), 
					developer.getAppKey(), developer.getAppSecret());
			
			// 异步发送邮件
			emailSendUtil.sendEmail(user.getEmail(), template.getSubject(), template.getContent());
			
			// 生成发送邮件记录
			emailSendRecordMapper.insertSelective(new EmailSendRecord(developer.getUserId(), user.getEmail(), 
					template.getSubject(), template.getContent(), new Date()));
			
			
		} catch (Exception e) {
			logger.error("sendEmail error, error is {} ", e.getMessage());
		}
	}
}
