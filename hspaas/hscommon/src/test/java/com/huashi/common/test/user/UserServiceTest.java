package com.huashi.common.test.user;

import java.util.List;

import org.junit.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huashi.common.test.BaseTest;
import com.huashi.common.user.model.UserModel;
import com.huashi.common.user.service.IUserService;

public class UserServiceTest extends BaseTest{

	@Reference(check = false ,mock = "return null")
	private IUserService userService;
//	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Test	
	public void getUser() {
		List<UserModel> list = userService.findUserModels();
		
		for(UserModel um : list)
			System.out.println(JSON.toJSONString(um));
	}
	
//	@Test
	public void redis() {
		stringRedisTemplate.opsForValue().set("test", "hello world");
		
	}
}
