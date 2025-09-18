/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com) & 如来神掌工作室 (https://github.com/jeequan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP工具类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2024/1/1 10:00
 */
@Slf4j
public class HttpUtil {

    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送POST请求
     */
    public static String post(String url, String jsonBody, Map<String, String> headers) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            httpHeaders.set("User-Agent", "Jeepay/1.0");
            
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            log.info("HTTP POST请求: url={}, body={}, response={}", url, jsonBody, response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("HTTP POST请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送GET请求
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            httpHeaders.set("User-Agent", "Jeepay/1.0");
            
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            log.info("HTTP GET请求: url={}, response={}", url, response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("HTTP GET请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送带证书的POST请求（用于微信支付）
     */
    public static String postWithCert(String url, String xmlBody, String certPath, String certPassword, Map<String, String> headers) {
        try {
            // TODO: 实现证书配置的HTTP客户端
            // 这里需要配置SSL证书用于微信支付API调用
            log.info("发送带证书的POST请求: url={}, certPath={}", url, certPath);
            
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            httpHeaders.set("Accept", MediaType.APPLICATION_XML_VALUE);
            httpHeaders.set("User-Agent", "Jeepay/1.0");
            
            if (headers != null) {
                headers.forEach(httpHeaders::set);
            }

            HttpEntity<String> entity = new HttpEntity<>(xmlBody, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            log.info("带证书POST请求响应: {}", response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("带证书POST请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("带证书HTTP请求失败: " + e.getMessage(), e);
        }
    }
}