/**************************************************************************
 * Copyright (c) 2015-2016 HangZhou Huashi, Inc.
 * All rights reserved.
 * 
 * 项目名称：华时短信平台
 * 版权说明：本软件属杭州华时科技有限公司所有，在未获得杭州华时科技有限公司正式授权
 *           情况下，任何企业和个人，不能获取、阅读、安装、传播本软件涉及的任何受知
 *           识产权保护的内容。                            
 ***************************************************************************/
package com.huashi.sms.settings.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.huashi.common.user.service.IUserService;
import com.huashi.common.vo.BossPaginationVo;
import com.huashi.common.vo.PaginationVo;
import com.huashi.sms.config.cache.redis.constant.SmsRedisConstant;
import com.huashi.sms.config.cache.redis.constant.SmsRedisConstant.MessageAction;
import com.huashi.sms.settings.constant.MobileBlacklistType;
import com.huashi.sms.settings.dao.SmsMobileBlackListMapper;
import com.huashi.sms.settings.domain.SmsMobileBlackList;

/**
 * TODO 黑名单服务接口类
 *
 * @author zhengying
 * @version V1.0.0
 * @date 2016年2月20日 下午12:30:55
 */
@Service
public class SmsMobileBlackListService implements ISmsMobileBlackListService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private IUserService userService;
	@Autowired
	private SmsMobileBlackListMapper smsMobileBlackListMapper;
	@Resource
	private StringRedisTemplate stringRedisTemplate;

	// 全局手机号码（与REDIS 同步采用广播模式）
	public static Set<String> GLOBAL_MOBILE_BLACKLIST = new HashSet<String>();
	
	private static Map<String, Object> response(String code, String msg) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result_code", code);
		resultMap.put("result_msg", msg);
		return resultMap;
	}
	
	/**
	 * 
	   * TODO 拼接重复错误报告信息
	   * @param lineNo
	   * @param mobile
	   * @return
	 */
	private static String appendRepeatReport(Integer lineNo, String mobile) {
		return String.format("重复行：%d 手机号码：%s;", lineNo, mobile);
	}
	
	@Override
	@Transactional
	public Map<String, Object> batchInsert(SmsMobileBlackList black) {
		StringBuilder mobileRepeatReport = new StringBuilder();
		if (StringUtils.isEmpty(black.getMobile()))
			return response("-2", "参数不能为空！");
		
		int existsCount = 0;
		List<SmsMobileBlackList> list = new ArrayList<>();
		try {
			// 前台默认是多个手机号码换行添加
			String str[] = black.getMobile().split("\n");
			SmsMobileBlackList mbl = null;
			for (int i = 0; i < str.length; i++) {
				if(StringUtils.isEmpty(str[i]) || StringUtils.isEmpty(str[i].trim()))
					continue;
				
				// 判断是否重复 重复则不保存
				if (isMobileBelongtoBlacklist(str[i].trim())) {
					mobileRepeatReport.append(appendRepeatReport(i + 1, str[i]));
					existsCount ++;
					continue;
				}
				
				mbl = new SmsMobileBlackList();
				mbl.setMobile(str[i]);
				mbl.setType(black.getType());
				mbl.setRemark(black.getRemark());
				list.add(mbl);
			}
			
			// 如果手机号码中包含已存在的数据
			if (existsCount > 0)
				return response("fail", mobileRepeatReport.toString());
			
			// 批量添加黑名单
			if(CollectionUtils.isNotEmpty(list)) {
				smsMobileBlackListMapper.batchInsert(list);
				// 批量操作无误后添加至缓存REDIS
				for(SmsMobileBlackList ml : list)
					publishToRedis(ml.getMobile(), MessageAction.ADD);
			}
			
			return  response("success", "成功！");
		} catch (Exception e) {
			logger.info("添加黑名单失败", e);
			return response("exption", "操作失败");
		}
	}

	@Override
	public PaginationVo<SmsMobileBlackList> findPage(String mobile,
			String startDate, String endDate, String currentPage) {

		int _currentPage = PaginationVo.parse(currentPage);

		Map<String, Object> params = new HashMap<>();
		if (StringUtils.isNotEmpty(mobile)) {
			params.put("mobile", mobile);
		}
		params.put("startDate", startDate);
		params.put("endDate", endDate);

		int totalRecord = smsMobileBlackListMapper.getCount(params);
		if (totalRecord == 0)
			return null;

		params.put("startPage", PaginationVo.getStartPage(_currentPage));
		params.put("pageRecord", PaginationVo.DEFAULT_RECORD_PER_PAGE);

		List<SmsMobileBlackList> list = smsMobileBlackListMapper
				.findPageList(params);
		if (list == null || list.isEmpty())
			return null;
		return new PaginationVo<SmsMobileBlackList>(list, _currentPage,
				totalRecord);
	}

	@Override
	public int deleteByPrimaryKey(int id) {
		try {
			SmsMobileBlackList SmsMobileBlackList = smsMobileBlackListMapper.selectByPrimaryKey(id);
			publishToRedis(SmsMobileBlackList.getMobile(), MessageAction.REMOVE);

		} catch (Exception e) {
			logger.warn("Redis 删除黑名单数据信息失败, id : {}", id, e);
		}

		return smsMobileBlackListMapper.deleteByPrimaryKey(id);
	}

	@Override
	public boolean isMobileBelongtoBlacklist(String mobile) {
		if (StringUtils.isEmpty(mobile))
			return false;

		try {
			return GLOBAL_MOBILE_BLACKLIST.contains(mobile);
		} catch (Exception e) {
			logger.warn("Redis 手机号黑名单查询失败", e);
		}

		return smsMobileBlackListMapper.selectByMobile(mobile) > 0;
	}

	@Override
	public List<String> findAll() {
		try {
			if (CollectionUtils.isNotEmpty(GLOBAL_MOBILE_BLACKLIST))
				return Arrays.asList(GLOBAL_MOBILE_BLACKLIST.toArray(new String[] {}));

		} catch (Exception e) {
			logger.warn("Rdis敏感词加载异常.", e);
		}

		// 如果REDIS失败，则加载到REDIS缓存，方便后续使用
		List<String> list = smsMobileBlackListMapper.selectAllMobiles();
		try {
			stringRedisTemplate.opsForSet().add(SmsRedisConstant.RED_MOBILE_BLACKLIST,
					list.toArray(new String[] {}));
		} catch (Exception e) {
			logger.warn("Rdis敏感词添加异常.", e);
		}
		return list;

	}

	@Override
	public List<String> filterBlacklistMobile(List<String> mobiles) {
		try {
			List<String> blackList = new ArrayList<String>(mobiles);

			blackList.retainAll(GLOBAL_MOBILE_BLACKLIST);

			mobiles.removeAll(blackList);

			return blackList;

		} catch (Exception e) {
			logger.error("黑名单解析失败", e);
			return mobiles;
		}
	}

	@Override
	public BossPaginationVo<SmsMobileBlackList> findPage(int pageNum,
			String keyword) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("keyword", keyword);
		BossPaginationVo<SmsMobileBlackList> page = new BossPaginationVo<SmsMobileBlackList>();
		page.setCurrentPage(pageNum);
		int total = smsMobileBlackListMapper.findCount(paramMap);
		if (total <= 0) {
			return page;
		}
		page.setTotalCount(total);
		paramMap.put("start", page.getStartPosition());
		paramMap.put("end", page.getPageSize());
		
		List<SmsMobileBlackList> list = smsMobileBlackListMapper.findList(paramMap);
		if(CollectionUtils.isEmpty(list))
			return null;
		
		for(SmsMobileBlackList sbl : list) {
			sbl.setTypeText(MobileBlacklistType.parse(sbl.getType()));
		}
		
		page.getList().addAll(list);
		return page;
	}

	/**
	 * 
	 * TODO REDIS队列数据操作(订阅发布模式)
	 * 
	 * @param mobile
	 * @param action
	 * @return
	 */
	private boolean publishToRedis(String mobile, MessageAction action) {
		try {
			stringRedisTemplate.convertAndSend(SmsRedisConstant.BROADCAST_MOBILE_BLACKLIST_TOPIC,
					String.format("%d:%s", action.getCode(), mobile));
			return true;
		} catch (Exception e) {
			logger.error("加入黑名单数据错误", e);
			return false;
		}
	}

	private boolean pushToRedis(List<String> list) {
		try {
			// 加载黑名单数据到JVM
			GLOBAL_MOBILE_BLACKLIST.addAll(list);

			long size = stringRedisTemplate.opsForSet().size(
					SmsRedisConstant.RED_MOBILE_BLACKLIST);
			if (size == list.size()) {
				logger.info("手机黑名单数据与DB数据一致，无需重载");
				return true;
			}

			stringRedisTemplate.delete(SmsRedisConstant.RED_MOBILE_BLACKLIST);
			List<Object> result = stringRedisTemplate.execute(
					new RedisCallback<List<Object>>() {

						@Override
						public List<Object> doInRedis(RedisConnection connection)
								throws DataAccessException {
							RedisSerializer<String> serializer = stringRedisTemplate
									.getStringSerializer();
							byte[] key = serializer.serialize(SmsRedisConstant.RED_MOBILE_BLACKLIST);
							connection.openPipeline();
							for (String m : list) {
								connection.sAdd(key, serializer.serialize(m));
							}
							return connection.closePipeline();
						}

					}, false, true);

			return CollectionUtils.isNotEmpty(result);
		} catch (Exception e) {
			logger.error("REDIS数据LOAD手机号码黑名单失败", e);
			return false;
		}
	}

	@Override
	public boolean reloadToRedis() {
		List<String> list = smsMobileBlackListMapper.selectAllMobiles();
		if (CollectionUtils.isEmpty(list)) {
			logger.warn("未找到手机号码黑名单数据");
			return false;
		}

		return pushToRedis(list);
	}
	
}
