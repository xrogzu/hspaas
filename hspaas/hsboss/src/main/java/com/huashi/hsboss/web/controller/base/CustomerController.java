package com.huashi.hsboss.web.controller.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.huashi.bill.pay.constant.PayContext;
import com.huashi.common.passage.context.TemplateEnum;
import com.huashi.common.settings.context.SettingsContext.PushConfigType;
import com.huashi.common.settings.context.SettingsContext.SystemConfigType;
import com.huashi.common.settings.domain.PushConfig;
import com.huashi.common.settings.domain.SystemConfig;
import com.huashi.common.settings.service.IPushConfigService;
import com.huashi.common.settings.service.ISystemConfigService;
import com.huashi.common.user.context.UserContext.Source;
import com.huashi.common.user.context.UserContext.UserBalanceType;
import com.huashi.common.user.context.UserSettingsContext.SmsMessagePass;
import com.huashi.common.user.context.UserSettingsContext.SmsNeedTemplate;
import com.huashi.common.user.context.UserSettingsContext.SmsPickupTemplate;
import com.huashi.common.user.context.UserSettingsContext.SmsReturnRule;
import com.huashi.common.user.context.UserSettingsContext.SmsSignatureSource;
import com.huashi.common.user.domain.User;
import com.huashi.common.user.domain.UserBalance;
import com.huashi.common.user.domain.UserDeveloper;
import com.huashi.common.user.domain.UserFluxDiscount;
import com.huashi.common.user.domain.UserPassage;
import com.huashi.common.user.domain.UserProfile;
import com.huashi.common.user.domain.UserSmsConfig;
import com.huashi.common.user.model.RegisterModel;
import com.huashi.common.user.service.IRegisterService;
import com.huashi.common.user.service.IUserBalanceService;
import com.huashi.common.user.service.IUserDeveloperService;
import com.huashi.common.user.service.IUserFluxDiscountService;
import com.huashi.common.user.service.IUserPassageService;
import com.huashi.common.user.service.IUserService;
import com.huashi.common.user.service.IUserSmsConfigService;
import com.huashi.common.vo.BossPaginationVo;
import com.huashi.fs.passage.domain.FluxPassageGroup;
import com.huashi.fs.passage.service.IFsPassageGroupService;
import com.huashi.hsboss.annotation.ViewMenu;
import com.huashi.hsboss.config.plugin.spring.Inject.BY_NAME;
import com.huashi.hsboss.constant.MenuCode;
import com.huashi.hsboss.web.controller.common.BaseController;
import com.huashi.sms.passage.domain.SmsPassageGroup;
import com.huashi.sms.passage.service.ISmsPassageAccessService;
import com.huashi.sms.passage.service.ISmsPassageGroupService;
import com.jfinal.ext.route.ControllerBind;
import org.apache.commons.lang3.StringUtils;

/**
 * 客户基本信息
 *
 * @author ym
 * @created_at 2016年6月28日下午4:03:47
 */
@ViewMenu(code = MenuCode.MENU_CODE_1001001)
@ControllerBind(controllerKey = "/base/customer")
public class CustomerController extends BaseController {

    @BY_NAME
    private IUserService iUserService;
    @BY_NAME
    private IRegisterService iRegisterService;
    @BY_NAME
    private ISystemConfigService iSystemConfigService;
    @BY_NAME
    private IPushConfigService iPushConfigService;
    @BY_NAME
    private IUserFluxDiscountService iUserFluxDiscountService;
    @BY_NAME
    private IUserBalanceService iUserBalanceService;
    @BY_NAME
    private IUserPassageService iUserPassageService;
    @BY_NAME
    private ISmsPassageGroupService iSmsPassageGroupService;
    @BY_NAME
    private IFsPassageGroupService iFsPassageGroupService;
    @BY_NAME
    private IUserSmsConfigService iUserSmsConfigService;
    @BY_NAME
    private ISmsPassageAccessService iSmsPassageAccessService;
    @BY_NAME
    private IUserDeveloperService iUserDeveloperService;

    public void index() {
        String fullName = getPara("fullName");
        String mobile = getPara("mobile");
        String company = getPara("company");
        String cardNo = getPara("cardNo");
        String state = getPara("state");
        String appkey = getPara("appkey");
        BossPaginationVo<UserProfile> page = iUserService.findPage(getPN(),
                fullName, mobile, company, cardNo, state,appkey);

        setAttr("fullName", fullName);
        setAttr("mobile", mobile);
        setAttr("company", company);
        setAttr("cardNo", cardNo);
        setAttr("state", state);
        setAttr("page", page);
        setAttr("appkey",appkey);

    }

    public void add() {
        List<SystemConfig> balanceConfigList = iSystemConfigService.findByType(SystemConfigType.USER_REGISTER_BALANCE.name());
        Map<String, Object> balanceConfigMap = new HashMap<String, Object>();
        for (SystemConfig config : balanceConfigList) {
            balanceConfigMap.put(config.getAttrKey(), config);
        }
        List<SystemConfig> fluxConfigList = iSystemConfigService.findByType(SystemConfigType.USER_REGISTER_FLUX_DISCOUNT.name());

        List<SystemConfig> defaultGroupList = iSystemConfigService.findByType(SystemConfigType.USER_DEFAULT_PASSAGE_GROUP.name());
        List<SmsPassageGroup> smsPassageGroupList = iSmsPassageGroupService.findAll();
//        List<FluxPassageGroup> fxPassageGroupList = iFsPassageGroupService.findAll();

        setAttr("balanceConfigMap", balanceConfigMap);
        setAttr("defaultGroupList", defaultGroupList);
        setAttr("fluxConfigList", fluxConfigList);
        setAttr("smsPassageGroupList", smsPassageGroupList);
        
        setAttr("smsReturnRules", SmsReturnRule.values());
        setAttr("smsMessagePass", SmsMessagePass.values());
        setAttr("smsNeedTemplates", SmsNeedTemplate.values());
        setAttr("smsPickupTemplates", SmsPickupTemplate.values());
        setAttr("smsSignatureSources", SmsSignatureSource.values());
//        setAttr("fxPassageGroupList", fxPassageGroupList);
    }

    public void create() {
        User user = getModel(User.class, "user");
        boolean checkMobile = iUserService.isUserExistsByMobile(user.getMobile());
        if(checkMobile){
            renderResultJson(false,"该手机号码已经存在！");
            return;
        }
        if(StringUtils.isNotBlank(user.getEmail())){
            boolean checkEmail = iUserService.isUserExistsByEmail(user.getEmail());
            if(checkEmail){
                renderResultJson(false,"该Email已经存在！");
                return;
            }
        }

        UserProfile userProfile = getModel(UserProfile.class, "userProfile");
        String gender = getPara("gender");
        userProfile.setGender(gender);
        String sms_amount = getPara("sms_amount");
        List<UserBalance> balanceList = new ArrayList<UserBalance>();
        UserBalance userSmsBalance = new UserBalance();
        userSmsBalance.setBalance(Double.valueOf(sms_amount));
        userSmsBalance.setType(UserBalanceType.SMS.getValue());
        userSmsBalance.setPaySource(PayContext.PaySource.BOSS_INPUT);
        userSmsBalance.setPayType(getParaToInt("smsPayType"));
        balanceList.add(userSmsBalance);
        String flux_money = getPara("flux_money");
        UserBalance userFsBalance = new UserBalance();
        userFsBalance.setBalance(Double.valueOf(flux_money));
        userFsBalance.setType(UserBalanceType.FS.getValue());
        userFsBalance.setPaySource(PayContext.PaySource.BOSS_INPUT);
        userFsBalance.setPayType(getParaToInt("fsPayType"));
        balanceList.add(userFsBalance);
        String voice_amount = getPara("voice_amount");
        UserBalance userVsBalance = new UserBalance();
        userVsBalance.setBalance(Double.valueOf(voice_amount));
        userVsBalance.setType(UserBalanceType.VS.getValue());
        userVsBalance.setPaySource(PayContext.PaySource.BOSS_INPUT);
        userVsBalance.setPayType(getParaToInt("vsPayType"));
        balanceList.add(userVsBalance);

        List<UserPassage> passageList = new ArrayList<UserPassage>();
        UserPassage smsPassage = new UserPassage();
        smsPassage.setCreateTime(new Date());
        smsPassage.setType(TemplateEnum.PassageTemplateType.SMS.getValue());
        smsPassage.setPassageGroupId(getParaToInt("sms_amount_group_id"));
        passageList.add(smsPassage);

        UserPassage vsPassage = new UserPassage();
        vsPassage.setCreateTime(new Date());
        vsPassage.setType(TemplateEnum.PassageTemplateType.VS.getValue());
        vsPassage.setPassageGroupId(getParaToInt("voice_amount_group_id"));
        passageList.add(vsPassage);

        UserPassage fsPassage = new UserPassage();
        fsPassage.setCreateTime(new Date());
        fsPassage.setType(TemplateEnum.PassageTemplateType.FS.getValue());
        fsPassage.setPassageGroupId(getParaToInt("flux_money_group_id"));
        passageList.add(fsPassage);

        UserFluxDiscount userFluxDiscount = new UserFluxDiscount();
        userFluxDiscount.setGlobalCmOff(Double.valueOf(getPara("global_cm")));
        userFluxDiscount.setGlobalCtOff(Double.valueOf(getPara("global_ct")));
        userFluxDiscount.setGlobalCuOff(Double.valueOf(getPara("global_cu")));

        userFluxDiscount.setLocalCmOff(Double.valueOf(getPara("local_cm")));
        userFluxDiscount.setLocalCtOff(Double.valueOf(getPara("local_ct")));
        userFluxDiscount.setLocalCuOff(Double.valueOf(getPara("local_cu")));

        String smsSendUrl = getPara("smsSendUrl");
        int sendUrlFlag = getParaToInt("sendUrlFlag",0);
        int sendUrlType = getParaToInt("sendUrlType",0);
        if(sendUrlFlag == 1){
            sendUrlFlag = sendUrlType;
            if(sendUrlType == 0){
                smsSendUrl = "";
            }
        }

        String smsUpUrl = getPara("smsUpUrl");
        String fluxUrl = getPara("fluxUrl");
        String voiceUrl = getPara("voiceUrl");

        List<PushConfig> configList = new ArrayList<PushConfig>();
        PushConfig smsConfig = new PushConfig();
        smsConfig.setUrl(smsSendUrl);
        smsConfig.setStatus(sendUrlFlag);
        smsConfig.setType(PushConfigType.SMS_STATUS_REPORT.getCode());
        configList.add(smsConfig);

        PushConfig smsUpConfig = new PushConfig();
        smsUpConfig.setUrl(smsUpUrl);
        smsUpConfig.setType(PushConfigType.SMS_MO_REPORT.getCode());
        configList.add(smsUpConfig);

        PushConfig fsConfig = new PushConfig();
        fsConfig.setUrl(fluxUrl);
        fsConfig.setType(PushConfigType.FS_CHARGE_REPORT.getCode());
        configList.add(fsConfig);

        PushConfig vsConfig = new PushConfig();
        vsConfig.setUrl(voiceUrl);
        vsConfig.setType(PushConfigType.VS_SEND_REPORT.getCode());
        configList.add(vsConfig);

        RegisterModel registerModel = new RegisterModel(Source.BOSS_INPUT, user);
        registerModel.setUserBalances(balanceList);
        registerModel.setUserProfile(userProfile);
        registerModel.setUserFluxDiscount(userFluxDiscount);
        registerModel.setPushConfigs(configList);
        registerModel.setSendEmail(getParaToInt("sendEmailFlag", 0) == 1);
        registerModel.setUserSmsConfig(getModel(UserSmsConfig.class, "userSmsConfig"));
        registerModel.setPassageList(passageList);

        boolean result = iRegisterService.register(registerModel);
        renderResultJson(result);
    }

    public void edit() throws Exception {
        int userId = getParaToInt("id");
        UserProfile profile = iUserService.getProfileByUserId(userId);
        User user = iUserService.getById(userId);

        List<PushConfig> configList = iPushConfigService.findByUserId(userId);
        Map<String, Object> configMap = new HashMap<String, Object>();
        for (PushConfig config : configList) {
            configMap.put("config_" + config.getType(), config.getUrl());
        }
        setAttr("config_constants", PushConfigType.values());
        setAttr("configMap", configMap);
        setAttr("configList", configList);

        List<SystemConfig> fluxConfigList = iSystemConfigService.findByType(SystemConfigType.USER_REGISTER_FLUX_DISCOUNT.name());
        setAttr("fluxConfigList", fluxConfigList);

        UserFluxDiscount fluxDiscount = iUserFluxDiscountService.getByUserId(userId);
        Map<String, Object> fluxMap = new LinkedHashMap<String, Object>();
        String[] discountProperty = {"localCmOff", "localCtOff", "localCuOff", "globalCmOff", "globalCtOff", "globalCuOff"};
        String[] discountName = {"省内移动折扣", "省内电信折扣", "省内联通折扣", "全国移动折扣", "全国电信折扣", "全国联通折扣"};
        for (String property : discountProperty) {
            String firstLetter = property.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + property.substring(1);
            Method method = fluxDiscount.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(fluxDiscount, new Object[]{});
            fluxMap.put(property, value);
        }

        List<UserPassage> passageList = iUserPassageService.findByUserId(userId);
        setAttr("passageList",passageList);
        List<SmsPassageGroup> smsPassageGroupList = iSmsPassageGroupService.findAll();
        setAttr("smsPassageGroupList", smsPassageGroupList);

        setAttr("discountName", discountName);
        setAttr("discountProperty", discountProperty);
        setAttr("fluxMap", fluxMap);

        List<UserBalance> balanceList = iUserBalanceService.findByUserId(userId);
        Map<String, Object> balanceMap = new HashMap<String, Object>();
        Map<String, Object> payTypeMap = new HashMap<String, Object>();
        for (UserBalance balance : balanceList) {
            balanceMap.put("balance_" + balance.getType(), balance.getBalance());
            payTypeMap.put("payType_" + balance.getType(), balance.getPayType());
        }
        setAttr("balanceMap", balanceMap);
        setAttr("payTypeMap", payTypeMap);


        UserDeveloper developer = iUserDeveloperService.getByUserId(userId);
        setAttr("developer",developer);

        setAttr("userProfile", profile);
        setAttr("user", user);
        setAttr("smsReturnRules", SmsReturnRule.values());
        setAttr("smsMessagePass", SmsMessagePass.values());
        setAttr("smsNeedTemplates", SmsNeedTemplate.values());
        setAttr("smsPickupTemplates", SmsPickupTemplate.values());
        setAttr("smsSignatureSources", SmsSignatureSource.values());
        setAttr("userSmsConfig", iUserSmsConfigService.getByUserId(userId));
    }

    //更新帐号信息
    public void updateUser() {
        User user = getModel(User.class, "user");

        User mobileUser = iUserService.getByMobile(user.getMobile());
        if(mobileUser != null && !mobileUser.getId().equals(user.getId())){
            renderResultJson(false,"该手机号码已经存在！");
            return;
        }
        if(StringUtils.isNotBlank(user.getEmail())){
            User emailUser = iUserService.getByEmail(user.getEmail());
            if(emailUser != null && !emailUser.getId().equals(user.getId())){
                renderResultJson(false,"该Email已经存在！");
                return;
            }
        }


        UserDeveloper developer = getModel(UserDeveloper.class,"developer");
        renderResultJson(iUserService.updateUserInfo(user,developer));
    }

    //更新基础信息
    public void updateBasic() {
    	UserProfile userProfile = getModel(UserProfile.class, "userProfile");
        String gender = getPara("gender");
        userProfile.setGender(gender);
        renderResultJson(iUserService.updateUserProfile(userProfile));
    }

    //更新短信
    public void updateSms() {
        int userId = getParaToInt("userId");
        String smsUpUrl = getPara("smsUpUrl");
        String balance = getPara("balance_1");

        String smsSendUrl = getPara("smsSendUrl");
        int sendUrlFlag = getParaToInt("sendUrlFlag",0);
        int sendUrlType = getParaToInt("sendUrlType",0);
        if(sendUrlFlag == 1){
            sendUrlFlag = sendUrlType;
            if(sendUrlType == 0){
                smsSendUrl = "";
            }
        }

        List<PushConfig> configList = new ArrayList<PushConfig>();
        PushConfig smsConfig = new PushConfig();
        smsConfig.setUrl(smsSendUrl);
        smsConfig.setStatus(sendUrlFlag);
        smsConfig.setType(PushConfigType.SMS_STATUS_REPORT.getCode());
        smsConfig.setUserId(userId);
        configList.add(smsConfig);

        PushConfig smsUpConfig = new PushConfig();
        smsUpConfig.setUrl(smsUpUrl);
        smsUpConfig.setType(PushConfigType.SMS_MO_REPORT.getCode());
        smsUpConfig.setUserId(userId);
        configList.add(smsUpConfig);
        
        UserSmsConfig userSmsConfig = getModel(UserSmsConfig.class, "userSmsConfig");
        userSmsConfig.setUserId(userId);
        iUserSmsConfigService.update(userSmsConfig);
        boolean result = iUserService.updateSms(userId, balance,getParaToInt("smsPayType"), configList,getParaToInt("smsGroupId"));
        if(result){
            iSmsPassageAccessService.updateByModifyUser(userId);
        }
        renderResultJson(result);
    }

    //更新流量
    public void updateFs() {
        String fluxUrl = getPara("fluxUrl");
        int userId = getParaToInt("userId");
        String balance = getPara("balance_2");

        PushConfig pushConfig = new PushConfig();
        pushConfig.setUrl(fluxUrl);
        pushConfig.setType(PushConfigType.FS_CHARGE_REPORT.getCode());

        UserFluxDiscount discount = getModel(UserFluxDiscount.class, "userFluxDiscount");

        boolean result = iUserService.updateFs(userId, balance,getParaToInt("fsPayType"), pushConfig, discount,getParaToInt("fsGroupId"));
        renderResultJson(result);
    }

    //更新语音
    public void updateVs() {
        String voiceUrl = getPara("voiceUrl");
        int userId = getParaToInt("userId");
        String balance = getPara("balance_3");

        PushConfig pushConfig = new PushConfig();
        pushConfig.setUrl(voiceUrl);
        pushConfig.setType(PushConfigType.VS_SEND_REPORT.getCode());

        boolean result = iUserService.updateVs(userId, balance,getParaToInt("vsPayType"), pushConfig,getParaToInt("vsGroupId"));
        renderResultJson(result);
    }

    public void disabled() {
        renderResultJson(iUserService.changeStatus(getParaToInt("id"), getPara("flag")));
    }

    //设置通道
    public void setPassage() {
        List<UserPassage> list = iUserPassageService.findByUserId(getParaToInt("userId"));
        setAttr("list", list);
    }

    public void selectPasage() {
        int type = getParaToInt("type");
        if (type == 1) {
            List<SmsPassageGroup> groupList = iSmsPassageGroupService.findAll();
            setAttr("groupList", groupList);
        } else if (type == 2) {
            List<FluxPassageGroup> groupList = iFsPassageGroupService.findAll();
            setAttr("groupList", groupList);
        } else if (type == 3) {
            //TODO 实现语音的通道组
        }
    }

    //更新通道
    public void updatePassage() {
        List<UserPassage> list = new ArrayList<UserPassage>();
        String[] groupIds = getPara("groupIds").split(",");
        int type = 0;
        for (String groupId : groupIds) {
            type++;
            if (groupId.equals("-1")) {
                continue;
            }
            UserPassage passage = new UserPassage();
            passage.setPassageGroupId(Integer.valueOf(groupId));
            passage.setType(type);
            list.add(passage);
        }
        boolean result = iUserPassageService.save(getParaToInt("userId"), list);
        renderResultJson(result);
    }

    public void commonUserList() {
        String fullName = getPara("fullName");
        String mobile = getPara("mobile");
        String company = getPara("company");
        String cardNo = getPara("cardNo");
        String state = getPara("state");
        BossPaginationVo<UserProfile> page = iUserService.findPage(getPN(),
                fullName, mobile, company, cardNo, state, null);
        page.setJumpPageFunction("userJumpPage");
        setAttr("fullName", fullName);
        setAttr("mobile", mobile);
        setAttr("company", company);
        setAttr("cardNo", cardNo);
        setAttr("state", state);
        setAttr("page", page);
        setAttr("userId", getParaToInt("userId", -1));
    }

    /**
     * 密码生成
     */
    public void generatePassword(){
    	renderResultJson(true,"生成成功",iUserService.generatePassword());
    }
}
