package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yukong.common.R;
import com.yukong.entity.User;
import com.yukong.service.UserService;
import com.yukong.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession session, @RequestBody User user){
        // 获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            // 生成随机的四位验证码

            String code = ValidateCodeUtils.generateValidateCode(6).toString();

            // 调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("同城买药","",phone,code);

            // 需要将生成的验证码保存到session
//            session.setAttribute(phone,code);

            // 将生成的验证码缓存到Redis中
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            log.info(code);
            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(HttpSession session, @RequestBody Map map){
        log.info(map.toString());

        // 获取手机号
        String  phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();
        // 从session中获取保存的验证码
//        Object codeInSession = session.getAttribute(phone);
        // 从Redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        // 进行验证码比对（页面提交的验证码和session中保存的验证码比对）
        if(codeInSession != null && codeInSession.equals(code)){
            // 如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);

            User user = userService.getOne(lqw);
            if (user == null){
                // 判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            // 登录成功，将员工id存入Session, 并返回登录成功结果
            session.setAttribute("user",user.getId());

            // 如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("登录失败");
    }

    /**
     * 移动端用户退出
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request){
        // 清理Session中保存的当前登录用户的id
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
