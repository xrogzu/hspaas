package com.huashi.developer.validator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huashi.constants.OpenApiCode.CommonApiCode;
import com.huashi.developer.exception.ValidateException;
import com.huashi.developer.util.EncodingUtil;

public class Validator {

	protected Logger logger = LoggerFactory.getLogger(Validator.class);

	protected void validteAndParseFields(Object obj, Map<String, String[]> paramMap) throws ValidateException {
		if (MapUtils.isEmpty(paramMap)) {
			logger.error("对接数据为空");
			throw new ValidateException(CommonApiCode.COMMON_REQUEST_EXCEPTION);
		}

		// 解析注解信息
		Map<String, String> param = parseAnotation(obj.getClass(), paramMap);

		try {
			org.apache.commons.beanutils.BeanUtils.populate(obj, param);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("接收短信参数转换MODEL异常", e);
		}
	}
	
	/**
	 * 
	   * TODO 获取需要解析的直接列（包含父类）
	   * 
	   * @param clazz
	   * @return
	 */
	private Field[] getClassFiled(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		
		Field[] parentFields = clazz.getSuperclass().getDeclaredFields();
		
		Field[] finalFields = new Field[fields.length + parentFields.length];
		
		System.arraycopy(fields, 0, finalFields, 0, fields.length);  
		System.arraycopy(parentFields, 0, finalFields, fields.length, parentFields.length);
		
		return finalFields;
	}

	/**
	 * 
	 * TODO 解析需要验证的注解列信息
	 * 
	 * @param clazz
	 * @param paramMap
	 * @return
	 * @throws ValidateException 
	 */
	private Map<String, String> parseAnotation(Class<?> clazz,
			Map<String, String[]> paramMap) throws ValidateException {
		Field[]  fields = getClassFiled(clazz);	
		if (fields == null || fields.length == 0)
			return null;

		Map<String, String> param = new HashMap<>();
		Set<String> keys = paramMap.keySet();
		for (Field field : fields) {
			ValidateField vf = field.getAnnotation(ValidateField.class);
			if (vf == null)
				continue;

			// 参数中不包含必要的参数，抛错误码
			if (vf.necessary() && !keys.contains(vf.value()))
				throw new ValidateException(CommonApiCode.COMMON_REQUEST_EXCEPTION);

			// 如果参数要求是UTF-8编码，需要验证
			if (vf.utf8() && !EncodingUtil.isUtf8(paramMap.get(vf.value())[0]))
				throw new ValidateException(CommonApiCode.COMMON_REQUEST_ENCODING_ERROR);
			
			// 内容不能为空
			if(vf.notEmpty() && StringUtils.isEmpty(paramMap.get(vf.value())[0]))
				throw new ValidateException(CommonApiCode.COMMON_REQUEST_EXCEPTION);

			// 数字暂不校验
//			if (vf.number())
//				continue;
			
			if(paramMap.get(vf.value()) == null)
				continue;
			
			param.put(vf.value(), paramMap.get(vf.value())[0]);
		}

		return param;
	}

}
