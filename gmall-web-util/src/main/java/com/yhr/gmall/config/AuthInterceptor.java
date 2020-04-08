package com.yhr.gmall.config;

import com.alibaba.fastjson.JSON;
import com.yhr.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 多个拦截器执行的顺序
 *      跟配置文件中，配置拦截器的顺序有关
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

    //拦截之前执行！用户进入控制器之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");

        if(token!=null) {

            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }

        if(token==null){

            token = CookieUtil.getCookieValue(request, "token", false);
        }

        if(token!=null){

            //读取token
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }
        //在拦截器上获取方法的注解
        HandlerMethod handlerMethod=(HandlerMethod)handler;

        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);

        if(loginRequire!=null){

            String salt=request.getParameter("X-forwarded-for");

            //调用verify方法认证

            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS +
                    "?token=" + token + "&salt=" + salt);

            if("success".equals(result)){

                //读取token
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);

                return true;
            }else {

                //认证失败，并且loginRequire.autoRedirect()=true，必须登录

                if(loginRequire.autoRedirect()){

                    String  requestURL = request.getRequestURL().toString();

                    //将url进行转换
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");

                    //重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;

                }
            }

        }

        return true;
    }

    private Map getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }


    //进入控制器之后，渲染视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    //视图渲染之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
