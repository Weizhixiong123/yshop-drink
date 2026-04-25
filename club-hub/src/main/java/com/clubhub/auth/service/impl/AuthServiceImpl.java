package com.clubhub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clubhub.auth.request.CustomerLoginRequest;
import com.clubhub.auth.request.StaffLoginRequest;
import com.clubhub.auth.request.StaffWxLoginRequest;
import com.clubhub.auth.response.CustomerLoginResponse;
import com.clubhub.auth.response.StaffLoginResponse;
import com.clubhub.auth.service.AuthService;
import com.clubhub.dto.Result;
import com.clubhub.entity.Member;
import com.clubhub.entity.Staff;
import com.clubhub.mapper.MemberMapper;
import com.clubhub.mapper.StaffMapper;
import com.clubhub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StaffMapper staffMapper;
    private final MemberMapper memberMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.miniapp.app-id:}")
    private String wechatMiniAppId;

    @Value("${wechat.miniapp.app-secret:}")
    private String wechatMiniAppSecret;

    /**
     * 本地开发使用：配置后会跳过微信接口，直接用该手机号匹配员工表。
     */
    @Value("${wechat.miniapp.mock-phone:}")
    private String wechatMockPhone;

    @Value("${owner.account.username:}")
    private String ownerUsername;

    @Value("${owner.account.password:}")
    private String ownerPassword;

    @Value("${owner.account.name:店东}")
    private String ownerName;

    private String cachedWechatAccessToken;
    private long cachedWechatAccessTokenExpireAt;

    @Override
    public Result<?> staffLogin(StaffLoginRequest request) {
        String configuredUsername = trimToNull(ownerUsername);
        String configuredPassword = trimToNull(ownerPassword);
        if (configuredUsername == null || configuredPassword == null) {
            return Result.fail("未配置店东账号或密码");
        }

        String username = trimToNull(request.getUsername());
        if (!Objects.equals(configuredUsername, username)
                || !Objects.equals(configuredPassword, request.getPassword())) {
            return Result.fail("店东账号或密码错误");
        }

        String token = jwtUtil.generateToken("owner", "owner");
        return Result.ok(new StaffLoginResponse(token, trimToNull(ownerName) == null ? "店东" : ownerName.trim(), "owner"));
    }

    @Override
    public Result<?> staffWxLogin(StaffWxLoginRequest request) {
        Result<String> phoneResult = resolveWechatPhone(request.getCode());
        if (phoneResult.getCode() != 200) {
            return Result.fail(phoneResult.getMsg());
        }

        String phone = phoneResult.getData();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }

        Staff staff = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>()
                        .eq(Staff::getStatus, 1)
                        .eq(Staff::getRole, "staff")
                        .eq(Staff::getPhone, phone));
        if (staff == null) {
            return Result.fail("该手机号未添加为店员");
        }

        String token = jwtUtil.generateToken(staff.getId(), staff.getRole());
        return Result.ok(new StaffLoginResponse(token, staff.getName(), staff.getRole()));
    }

    @Override
    public Result<?> customerLogin(CustomerLoginRequest request) {
        String phone = request.getPhone();
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, phone));
        if (member == null) {
            return Result.fail("未找到会员信息");
        }
        String token = jwtUtil.generateToken(String.valueOf(member.getId()), "customer");
        return Result.ok(new CustomerLoginResponse(token, phone));
    }

    private Result<String> resolveWechatPhone(String code) {
        String mockPhone = trimToNull(wechatMockPhone);
        if (mockPhone != null) {
            return Result.ok(mockPhone);
        }

        if (trimToNull(wechatMiniAppId) == null || trimToNull(wechatMiniAppSecret) == null) {
            return Result.fail("未配置微信小程序 app-id/app-secret");
        }

        Result<String> accessTokenResult = getWechatAccessToken();
        if (accessTokenResult.getCode() != 200) {
            return accessTokenResult;
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.weixin.qq.com/wxa/business/getuserphonenumber")
                .queryParam("access_token", accessTokenResult.getData())
                .toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, Map.of("code", code), Map.class);
            if (response == null) {
                return Result.fail("微信手机号接口无响应");
            }

            int errcode = asInt(response.get("errcode"), 0);
            if (errcode != 0) {
                return Result.fail("微信手机号授权失败：" + getWechatErrorMessage(response));
            }

            Object phoneInfoObj = response.get("phone_info");
            if (!(phoneInfoObj instanceof Map<?, ?> phoneInfo)) {
                return Result.fail("微信未返回手机号信息");
            }

            String phone = trimToNull(Objects.toString(phoneInfo.get("purePhoneNumber"), ""));
            if (phone == null) {
                phone = trimToNull(Objects.toString(phoneInfo.get("phoneNumber"), ""));
            }
            if (phone == null) {
                return Result.fail("微信未返回手机号");
            }
            return Result.ok(phone);
        } catch (RestClientException e) {
            return Result.fail("调用微信手机号接口失败：" + e.getMessage());
        }
    }

    private synchronized Result<String> getWechatAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedWechatAccessToken != null && now < cachedWechatAccessTokenExpireAt) {
            return Result.ok(cachedWechatAccessToken);
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.weixin.qq.com/cgi-bin/token")
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", wechatMiniAppId)
                .queryParam("secret", wechatMiniAppSecret)
                .toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return Result.fail("微信 access_token 接口无响应");
            }

            int errcode = asInt(response.get("errcode"), 0);
            if (errcode != 0) {
                return Result.fail("获取微信 access_token 失败：" + getWechatErrorMessage(response));
            }

            String accessToken = trimToNull(Objects.toString(response.get("access_token"), ""));
            if (accessToken == null) {
                return Result.fail("微信未返回 access_token");
            }

            int expiresIn = asInt(response.get("expires_in"), 7200);
            cachedWechatAccessToken = accessToken;
            cachedWechatAccessTokenExpireAt = now + Math.max(60, expiresIn - 300) * 1000L;
            return Result.ok(accessToken);
        } catch (RestClientException e) {
            return Result.fail("调用微信 access_token 接口失败：" + e.getMessage());
        }
    }

    private String getWechatErrorMessage(Map<String, Object> response) {
        Object errcode = response.get("errcode");
        Object errmsg = response.get("errmsg");
        if (errcode == null) {
            return Objects.toString(errmsg, "未知错误");
        }
        return errcode + " " + Objects.toString(errmsg, "未知错误");
    }

    private int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
