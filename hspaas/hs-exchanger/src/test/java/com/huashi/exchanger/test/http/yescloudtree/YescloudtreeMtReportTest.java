package com.huashi.exchanger.test.http.yescloudtree;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huashi.exchanger.resolver.http.custom.yescloudtree.YescloudtreePassageResolver;
import com.huashi.exchanger.template.handler.RequestTemplateHandler;
import com.huashi.sms.passage.domain.SmsPassageParameter;
import com.huashi.sms.record.domain.SmsMtMessageDeliver;

public class YescloudtreeMtReportTest {

private Logger logger = LoggerFactory.getLogger(getClass());
	
	YescloudtreePassageResolver resolver = null;
	SmsPassageParameter parameter = null;
	
	@Before
	public void init() {
		resolver = new YescloudtreePassageResolver();
		parameter = new SmsPassageParameter();
		
		JSONObject pam = new JSONObject();
		
		pam.put("account", "hzhs");
		pam.put("password", "hzhs0814");
		pam.put("custom", "yescloudtree");
		
		parameter.setUrl("http://www.yescloudtree.cn:28002/");
		parameter.setParams(pam.toJSONString());
		parameter.setSuccessCode("DELIVRD");
		
//		http://www.yescloudtree.cn:28001/?Action=MoSms&UserName=hzhs&Password=43f7c7f5fb0fc7fd1d503b95969e4be3
	}
	
	@Test
	public void test() {
		List<SmsMtMessageDeliver> list = resolver.mtPullDeliver(RequestTemplateHandler.parse(parameter.getParams()), 
				parameter.getUrl(), parameter.getSuccessCode());
		
		logger.info(JSON.toJSONString(list));
		
//		Assert.assertTrue("回执数据失败", CollectionUtils.isNotEmpty(list));
		
	}
}
