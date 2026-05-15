package com.banban.login_security.util;

import com.banban.login_security.domain.LoginWebDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommonFunction {

    private final UserAgentAnalyzer userAgentAnalyzer;

    public static String getClassName(){
        return Thread.currentThread().getStackTrace()[2].getClassName();
    }

    public static String getMethodName(){
        return Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    public static String getCookie(HttpServletRequest request, String name){
        Cookie[] cookies = request.getCookies();

        if(cookies != null && name != null){
            for(Cookie cookie : cookies){
                if(name.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    public LoginWebDetails toLoginWebDetails(HttpServletRequest request){
        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");
        String deviceId = CommonFunction.getCookie(request, "deviceId");

        UserAgent userAgent = userAgentAnalyzer.parse(agent);
        String agentName = userAgent.getValue("AgentName");

        return new LoginWebDetails(ip, agentName, deviceId);
    }

}
