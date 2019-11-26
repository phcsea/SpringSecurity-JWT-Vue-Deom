package com.bugaugaoshu.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-25 22:21
 */
public class TokenAuthenticationHelper {
    private static final long EXPIRATION_TIME = 1000 * 60 * 30;
    private static final String SECRET_KEY = "ThisIsASpringSecurityDemo";
    private static final String COOKIE_TOKEN = "COOKIE-TOKEN";

    /**
     * 设置登陆成功后令牌返回
     * */
    public static void addAuthentication(HttpServletResponse res, Authentication authResult) throws IOException {
        // 获取用户登陆角色
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        // 遍历用户角色
        StringBuffer stringBuffer = new StringBuffer();
        authorities.forEach(authority -> {
            stringBuffer.append(authority.getAuthority()).append(",");
        });

        System.out.println(authResult.getCredentials());
        System.out.println(authResult.getDetails());
        System.out.println(authResult.getPrincipal());

        String jwt = Jwts.builder()
                // Subject 设置用户名
                .setSubject(authResult.getName())
                // 设置用户权限
                .claim("authorities", stringBuffer)
                // 过期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // 签名算法
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
        Cookie cookie = new Cookie(COOKIE_TOKEN, jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        res.addCookie(cookie);
        // 向前端写入数据
//        Map<String, Object> map = new HashMap<>();
//        map.put(COOKIE_BEARER, jwt);
//        map.put("msg", "登陆成功！");
//        res.setContentType("application/json; charset=UTF-8");
//        PrintWriter out = res.getWriter();
//        out.write(new ObjectMapper().writeValueAsString(map));
//        out.flush();
//        out.close();
    }

    /**
     * 对请求的验证
     * */
    public static Authentication getAuthentication(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, COOKIE_TOKEN);
        String token = cookie != null ? cookie.getValue() : null;

        if (token != null) {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            // 获取用户权限
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("authorities").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            String userName = claims.getSubject();
            // 传数据进行校验
            return userName != null ? new UsernamePasswordAuthenticationToken(userName, null, authorities) : null;
        }
        return null;
    }
}