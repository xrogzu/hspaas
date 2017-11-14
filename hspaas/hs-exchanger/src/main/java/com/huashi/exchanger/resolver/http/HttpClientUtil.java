package com.huashi.exchanger.resolver.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huashi.exchanger.exception.DataEmptyException;

public class HttpClientUtil {

	// 从连接池获取连接的timeout
	public static final int CONNECTION_REQUEST_TIMEOUT = 5 * 1000;
	// 和服务器建立连接的timeout
	public static final int CONNECTION_TIMEOUT = 10 * 1000;
	// 从服务器读取数据的timeout
	public static final int SOCKET_TIMEOUT = 60 * 1000;
	
    
    private volatile static Map<String, CloseableHttpClient> LOCAL_HTTP_CLIENT_FACTORY = new HashMap<String, CloseableHttpClient>();
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    
    // 默认最大连接池数量（针对整个域名主机）
    public static final Integer DEFAULT_MAX_TOTAL = 300;
    // 每个路由最大连接数（真正限制）
    public static final Integer DEFAULT_MAX_PER_ROUTE = 300;

    private final static Object syncLock = new Object();
    // 默认编码
    public static final String ENCODING_UTF_8 = "UTF-8";
    
    /**
     * 获取HttpClient对象
     * 
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient getHttpClient(String url, Integer maxTotal, Integer maxPerRoute) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        
        String key = String.format("%s_%d", hostname, port);
    	
    	if(!LOCAL_HTTP_CLIENT_FACTORY.containsKey(key)) {
    		synchronized (syncLock) {
   			   LOCAL_HTTP_CLIENT_FACTORY.put(key, createHttpClient(maxTotal, maxPerRoute));
   			   logger.info("URL:{} 连接池初始化", url);
   			   logger.info("KEY：{} 连接池初始化成功，连接池：{} 连接数： {}", url, key, maxTotal, maxPerRoute);
           }
    	}
    	
    	return LOCAL_HTTP_CLIENT_FACTORY.get(key);
    }
    
    /**
     * 创建HttpClient对象
     * 
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", plainsf)
                .register("https", sslsf).build();
        
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
        
        
//        HttpHost httpHost = new HttpHost(hostname, port);
        
//        // 将目标主机的最大连接数增加
//        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
//        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
//            public boolean retryRequest(IOException exception,
//                    int executionCount, HttpContext context) {
//                if (executionCount >= 2) {// 如果已经重试了2次，就放弃
//                    return false;
//                }
//                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
//                    return true;
//                }
//                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
//                    return false;
//                }
//                if (exception instanceof InterruptedIOException) {// 超时
//                    return false;
//                }
//                if (exception instanceof UnknownHostException) {// 目标服务器不可达
//                    return false;
//                }
//                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
//                    return false;
//                }
//                if (exception instanceof SSLException) {// SSL握手异常
//                    return false;
//                }
//
//                HttpClientContext clientContext = HttpClientContext.adapt(context);
//                HttpRequest request = clientContext.getRequest();
//                // 如果请求是幂等的，就再次尝试
//                if (!(request instanceof HttpEntityEnclosingRequest)) {
//                    return true;
//                }
//                return false;
//            }
//        };

//        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        return httpClient;
    }

    /**
     * GET请求URL获取内容
     * 
     * @param url
     * @return
     * @author SHANHY
     * @create 2015年12月18日
     */
    public static String get(String url) {
        HttpGet httpget = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpget.setConfig(requestConfig);
        
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient(url, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE).execute(httpget, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
    	
    	String url = "http://www.yescloudtree.cn:28001/";
    	
    	try {
    		post(url, null, "sssss");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }

    /**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @return
     */
    public static String post(String url, Map<String, Object> params) {
    	return post(url, null, params);
    }
    
    /**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @param headers 
       * @return
     */
    public static String post(String url, Map<String, Object> headers, Map<String, Object> params) {
    	return post(url, headers, params, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
    }
    
    /**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @return
     */
    public static String post(String url, Map<String, Object> params, Integer maxPerRoute) {
    	return post(url, null, params, maxPerRoute, SOCKET_TIMEOUT);
    }
    
    /**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @return
     */
    public static String post(String url, Map<String, Object> headers, Map<String, Object> params, Integer maxPerRoute, Integer socketTimeout) {
    	return post(url, headers, params, DEFAULT_MAX_TOTAL, maxPerRoute, socketTimeout);
    }
    
    /**
     * 
       * TODO 设置HTTP参数信息
       * @param httpPost
       * @param params
     */
    private static void setHttpParameters(HttpPost httpPost, Map<String, Object> params){
    	setHttpParameters(httpPost, params, ENCODING_UTF_8);
    }
    
    /**
     * 
       * TODO 设置HTTP参数信息（包含编码）
       * @param httpPost
       * @param params
       * @param encoding
     */
    private static void setHttpParameters(HttpPost httpPost, Map<String, Object> params, String encoding){
    	if (MapUtils.isEmpty(params))
			return;
    	
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
		for (String key : params.keySet()) {
			pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, encoding));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("设置HTTP请求参数编码异常", e);
		}
    }
    
    /**
     * 
       * TODO 设置HTTP JSO包体信息（包含编码）
       * @param httpPost
       * @param jsonBody
       * @param encoding
     */
    private static void setHttpJsonBody(HttpPost httpPost, String jsonBody, String encoding){
    	if (StringUtils.isBlank(jsonBody))
			return;
    	
    	httpPost.setEntity(new StringEntity(jsonBody, Charset.forName(encoding)));
    }
    
    /**
     * 
       * TODO 设置HTTP头信息
       * @param httpPost
       * @param params
     */
    private static void setHttpHeaders(HttpPost httpPost, Map<String, Object> headers){
    	setHttpHeaders(httpPost, headers, ENCODING_UTF_8);
    }
    
    /**
     * 
       * TODO 获取HttpPost
       * @param url
       * @return
     */
    public static HttpPost getHttpPost(String url) {
		return getHttpPost(url, CONNECTION_REQUEST_TIMEOUT, CONNECTION_TIMEOUT, SOCKET_TIMEOUT);
    }
    
    /**
     * 
       * TODO 获取HttpPost
       * @param url
       * @param readTimeout
       * @return
     */
    public static HttpPost getHttpPost(String url, int socketTimeout) {
		return getHttpPost(url, CONNECTION_REQUEST_TIMEOUT, CONNECTION_TIMEOUT, socketTimeout);
    }
    
    /**
     * 
       * TODO 获取HttpPost
       * @param url
       * @param conectionTimeout
       * 		连接URL超时时间
       * @param readTimeout
       * 		服务处理超时时间
       * @param socketTimeout
       * 		发送数据超时时间
       * @return
     */
    public static HttpPost getHttpPost(String url, int connectionRequestTimeout, int connectionTimeout, int socketTimeout) {
    	HttpPost httpPost = new HttpPost(url);
    	
    	if(connectionRequestTimeout == 0)
    		connectionRequestTimeout = CONNECTION_REQUEST_TIMEOUT;
    	
    	if(connectionTimeout == 0)
    		connectionTimeout = CONNECTION_TIMEOUT;
    	
    	if(socketTimeout == 0)
    		socketTimeout = SOCKET_TIMEOUT;
		
//		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)
//                .setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();
		
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)
                .setConnectTimeout(connectionTimeout).build();
		httpPost.setConfig(requestConfig);
		
		return httpPost;
    }
    
    /**
     * 
       * TODO 设置HTTP头信息（包含编码）
       * @param httpPost
       * @param headers
       * @param encoding
     */
    private static void setHttpHeaders(HttpPost httpPost, Map<String, Object> headers, String encoding){
    	if (MapUtils.isEmpty(headers))
			return;
    	
		for (String key : headers.keySet()) {
			httpPost.addHeader(new BasicHeader(key, headers.get(key).toString()));
		}
    }
    
    /**
	 * 
	 * TODO HTTP 发送
	 * 
	 * @param url
	 *            网络URL
	 * @param bulider
	 * 
	 * @return
	 */
	public static String post(String url, Map<String,Object> headers, Map<String, Object> params, 
			Integer maxTotal, Integer maxPerRoute, Integer socketTimeout) {
		long startTime = System.currentTimeMillis();
		HttpPost httpPost = getHttpPost(url, socketTimeout);
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		try {
			
			// 设置头信息
			setHttpHeaders(httpPost, headers);
			// 设置参数信息
			setHttpParameters(httpPost, params);
			
			httpClient = HttpClientUtil.getHttpClient(url, maxTotal, maxPerRoute);
			// 提交post请求
			response = httpClient.execute(httpPost);
			// 获取响应内容
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();// 响应码
			// String reasonPhrase = statusLine.getReasonPhrase();// 响应信息
			
			if(statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException(String.format("返回状态码失败，状态码为：%d", statusCode));
			}
			
			HttpEntity entity = response.getEntity();
	        String result = null;
	        if (entity != null) {
	            result = EntityUtils.toString(entity, "UTF-8");
	        }
	        EntityUtils.consume(entity);
	        response.close();
	        return result;
	        
		} catch (Exception e) {
			httpPost.abort();
			logger.error("URL:{} 请求处理失败", url, e);
			throw new RuntimeException(String.format("URL: %s 调用失败！", url));
		} finally {
			logger.info("URL：{} 请求耗时：{} ms", url, System.currentTimeMillis() - startTime);
			// 释放资源
			httpPost.releaseConnection();
			if (url.startsWith("https") && httpClient != null&& httpClient instanceof CloseableHttpClient) {
                try {
					((CloseableHttpClient) httpClient).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	/**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @return
     */
    public static String postJson(String url, Map<String,Object> headers, String jsonBody) {
    	return postJson(url, headers, jsonBody, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
    }
	
	/**
     * 
       * TODO POST请求（默认连接数）
       * @param url
       * @param params
       * @return
     */
    public static String postJson(String url, Map<String,Object> headers, String jsonBody, Integer maxPerRoute) {
    	return postJson(url, headers, jsonBody, DEFAULT_MAX_TOTAL, maxPerRoute);
    }
    
	
	/**
	 * 
	   * TODO HTTP发送JSON报文
	   * @param url
	   * @param headers
	   * @param jsonBody
	   * @param maxTotal
	   * @param maxPerRoute
	   * @return
	 */
	public static String postJson(String url, Map<String,Object> headers, String jsonBody, Integer maxTotal, Integer maxPerRoute) {
		if (StringUtils.isEmpty(jsonBody))
			throw new DataEmptyException("用户JSON报文为空");

		long startTime = System.currentTimeMillis();
		HttpPost httpPost = getHttpPost(url);
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		try {
			
			// 设置头信息
			setHttpHeaders(httpPost, headers);
			// 设置json 报文信息
			setHttpJsonBody(httpPost, jsonBody, ENCODING_UTF_8);
			
			httpClient = HttpClientUtil.getHttpClient(url, maxTotal, maxPerRoute);
			// 提交post请求
			response = httpClient.execute(httpPost);
			// 获取响应内容
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();// 响应码
			// String reasonPhrase = statusLine.getReasonPhrase();// 响应信息
			
			if(statusCode != 200) {
//				httpPost.abort();
				logger.error("Http回执状态码异常，statusCode : {}", statusCode);
			}
			
			HttpEntity entity = response.getEntity();
	        String result = null;
	        if (entity != null) {
	            result = EntityUtils.toString(entity, "UTF-8");
	        }
	        EntityUtils.consume(entity);
	        response.close();
	        return result;
	        
		} catch (Exception e) {
			httpPost.abort();
			logger.error("URL:{} 请求处理失败", url, e);
			throw new RuntimeException(String.format("URL: %s 调用失败！", url));
		} finally {
			logger.info("URL：{} 请求耗时：{} ms", url, System.currentTimeMillis() - startTime);
			// 释放资源
			httpPost.releaseConnection();
			if (url.startsWith("https") && httpClient != null&& httpClient instanceof CloseableHttpClient) {
                try {
					((CloseableHttpClient) httpClient).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	/**
	 * 
	   * TODO 调用连接池大小
	   * @param url
	   * @param headers
	   * @param params
	   * @param encoding
	   
	   * @return
	 */
	public static String post(String url, Map<String, Object> params, String encoding) {
		return post(url, null, params, encoding, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
	}
	
	/**
	 * 
	   * TODO 调用连接池大小
	   * @param url
	   * @param headers
	   * @param params
	   * @param encoding
	   * @return
	 */
	public static String post(String url, Map<String, Object> headers, Map<String, Object> params, String encoding) {
		return post(url, headers, params, encoding, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
	}
	
	/**
	 * 
	   * TODO 调用POST
	   * 
	   * @param url
	   * @param params
	   * @param encoding
	   * 		编码方式
	   * @return
	 */
	public static String post(String url, Map<String,Object> headers, Map<String, Object> params, 
			String encoding, Integer maxTotal, Integer maxPerRoute) {
		if (MapUtils.isEmpty(params))
			throw new DataEmptyException("用户参数为空");

		long startTime = System.currentTimeMillis();
		HttpPost httpPost = getHttpPost(url);
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		try {
			
			// 设置头信息
			setHttpHeaders(httpPost, headers, encoding);
			// 设置参数信息
			setHttpParameters(httpPost, params, encoding);

			httpClient = HttpClientUtil.getHttpClient(url, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
			// 提交post请求
			response = httpClient.execute(httpPost);
			// 获取响应内容
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();// 响应码
			// String reasonPhrase = statusLine.getReasonPhrase();// 响应信息
			if(statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException(String.format("返回状态码失败，状态码为：%d", statusCode));
			}
			
			HttpEntity entity = response.getEntity();
	        String result = null;
	        if (entity != null) {
	            result = EntityUtils.toString(entity, encoding);
	        }
	        EntityUtils.consume(entity);
	        response.close();
	        return result;
	        
		} catch (Exception e) {
			httpPost.abort();
			logger.error("URL:{} 请求处理失败", url, e);
			throw new RuntimeException(String.format("URL: %s 调用失败！", url));
		} finally {
			logger.info("URL：{} 请求耗时：{} ms", url, System.currentTimeMillis() - startTime);
			// 释放资源
			httpPost.releaseConnection();
			if (url.startsWith("https") && httpClient != null&& httpClient instanceof CloseableHttpClient) {
                try {
					((CloseableHttpClient) httpClient).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	/**
	 * 
	   * TODO 发送报文信息
	   * @param url
	   * @param content
	   * @param headers
	   * @return
	 */
	public static String postReport(String url, String content) {
		return postReport(url, content, ENCODING_UTF_8, null);
	}
	
	/**
	 * 
	   * TODO 发送报文信息
	   * @param url
	   * @param content
	   * @param headers
	   * @return
	 */
	public static String postReport(String url, String content, Map<String,Object> headers) {
		return postReport(url, content, ENCODING_UTF_8, headers);
	}
	
	/**
	 * 
	 * TODO HTTP 发送
	 * 
	 * @param url
	 *            网络URL
	 * @param content
	 * @param headers
	 * 
	 * 
	 * @return
	 */
	public static String postReport(String url, String content, String encoding, Map<String,Object> headers) {
		if (StringUtils.isEmpty(content))
			throw new DataEmptyException("用户参数为空");

		long startTime = System.currentTimeMillis();
		HttpPost httpPost = getHttpPost(url);
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClientUtil.getHttpClient(url, DEFAULT_MAX_TOTAL, DEFAULT_MAX_PER_ROUTE);
			
			// 设置头信息
			setHttpHeaders(httpPost, headers);
			
		    BasicHttpEntity httpEntity = new BasicHttpEntity();
	        httpEntity.setContent(new ByteArrayInputStream(content.getBytes(encoding)));
	        httpPost.setEntity(httpEntity);
			
			// 提交post请求
			response = httpClient.execute(httpPost);
			// 获取响应内容
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();// 响应码
			// String reasonPhrase = statusLine.getReasonPhrase();// 响应信息
			if(statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException(String.format("返回状态码失败，状态码为：%d", statusCode));
			}
			
			HttpEntity entity = response.getEntity();
	        String result = null;
	        if (entity != null) {
	            result = EntityUtils.toString(entity, encoding);
	        }
	        EntityUtils.consume(entity);
	        response.close();
	        return result;
	        
		} catch (Exception e) {
			httpPost.abort();
			logger.error("URL:{} 请求处理失败", url, e);
			throw new RuntimeException(String.format("URL: %s 求处理失败！", url));
		} finally {
			logger.info("URL：{} 请求耗时：{} ms", url, System.currentTimeMillis() - startTime);
			// 释放资源
			httpPost.releaseConnection();
			if (url.startsWith("https") && httpClient != null&& httpClient instanceof CloseableHttpClient) {
                try {
					((CloseableHttpClient) httpClient).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
}
