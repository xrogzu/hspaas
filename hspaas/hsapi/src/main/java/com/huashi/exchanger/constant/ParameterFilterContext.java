package com.huashi.exchanger.constant;

public class ParameterFilterContext {

	// 过滤码定义
	public static final String FILTER_CODE_NODE = "filterCode";
	// 通道提供商代码定义
	public static final String PASSAGE_PROVIDER_CODE_NODE = "providerCode";
	// 针对参数是通过stream传递方式时，定义的名称
	public static final String PARAMETER_NAME_IN_STREAM = "parameterReport";

	/**
	 * 
	 * TODO 参数解析方式
	 * 
	 * @author zhengying
	 * @version V1.0
	 * @date 2016年11月30日 下午4:09:46
	 */
	public enum ParameterFilter {

		PARAMETER_MAP(1, "参数集合解析"), PARAMETER_STRAEM(2, "数据流解析"), HEADER(3,
				"数据报文头信息");

		private int code;
		private String title;

		private ParameterFilter(int code, String title) {
			this.code = code;
			this.title = title;
		}

		/**
		 * 
		 * TODO 转换
		 * 
		 * @param filterCode
		 * @return
		 */
		public static ParameterFilter parse(Integer filterCode) {
			if (filterCode == null || filterCode == 0)
				return ParameterFilter.PARAMETER_MAP;

			for (ParameterFilter pf : ParameterFilter.values()) {
				if (pf.getCode() == filterCode)
					return pf;
			}

			return ParameterFilter.PARAMETER_MAP;
		}

		public int getCode() {
			return code;
		}

		public String getTitle() {
			return title;
		}
	}
}
