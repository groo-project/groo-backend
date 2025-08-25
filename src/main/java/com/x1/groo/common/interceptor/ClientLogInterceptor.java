package com.x1.groo.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class ClientLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ClientLogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = request.getRemoteAddr();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");

        /*
            인터셉터 포맷, xml 패턴, service 파일에서의 정규식 파싱이 모두 일치해야 함

            인터셉터 포맷: 어떤 문자열을 남기는가?
            logback 패턴: 인터셉터에서 남긴 문자열 앞뒤에 무엇을 붙이는가?
            정규식 파서: 최종 문자열을 어떻게 해석할 것인가?
        */
        logger.info("Client Request - IP: {}, Method: {}, URI: {}, User-Agent: {}",
                clientIp, method, uri, userAgent);

        return true;
    }
}
