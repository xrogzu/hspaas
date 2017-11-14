package com.huashi.sms.task.model;

import java.io.Serializable;
import java.util.Map;

import com.huashi.sms.passage.domain.SmsPassageAccess;

/**
 * 
  * TODO 用户路由通道信息
  * 
  * @author zhengying
  * @version V1.0   
  * @date 2016年10月11日 上午11:57:29
 */
public class SmsRoutePassage implements Serializable{

	
	private static final long serialVersionUID = 7468557377818340963L;
	private Integer userId;
	
	// 移动通道信息
	private Map<Integer, SmsPassageAccess> cmPassage;
	// 联通通道信息
	private Map<Integer, SmsPassageAccess> cuPassage;
	// 电信通道信息
	private Map<Integer, SmsPassageAccess> ctPassage;

	private String cmErrorMessage;
	private String cuErrorMessage;
	private String ctErrorMessage;
	

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Map<Integer, SmsPassageAccess> getCmPassage() {
		return cmPassage;
	}

	public void setCmPassage(Map<Integer, SmsPassageAccess> cmPassage) {
		this.cmPassage = cmPassage;
	}

	public Map<Integer, SmsPassageAccess> getCuPassage() {
		return cuPassage;
	}

	public void setCuPassage(Map<Integer, SmsPassageAccess> cuPassage) {
		this.cuPassage = cuPassage;
	}

	public Map<Integer, SmsPassageAccess> getCtPassage() {
		return ctPassage;
	}

	public void setCtPassage(Map<Integer, SmsPassageAccess> ctPassage) {
		this.ctPassage = ctPassage;
	}

	public String getCmErrorMessage() {
		return cmErrorMessage;
	}

	public void setCmErrorMessage(String cmErrorMessage) {
		this.cmErrorMessage = cmErrorMessage;
	}

	public String getCuErrorMessage() {
		return cuErrorMessage;
	}

	public void setCuErrorMessage(String cuErrorMessage) {
		this.cuErrorMessage = cuErrorMessage;
	}

	public String getCtErrorMessage() {
		return ctErrorMessage;
	}

	public void setCtErrorMessage(String ctErrorMessage) {
		this.ctErrorMessage = ctErrorMessage;
	}


}
