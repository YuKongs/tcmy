package com.yukong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yukong.common.R;
import com.yukong.entity.Employee;
import com.yukong.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    /**
     *疑问：当发送ajax请求之后，这个过程是怎么实现的呢？（已解决）
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1.将页面提交的密码password进行md5加密处理, 得到加密后的字符串
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名username查询数据库中员工数据信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3.如果没有查询到, 则返回登录失败结果
        if(emp == null){
            return  R.error("登录失败！");
        }
        // 4.密码比对，如果不一致, 则返回登录失败结果
        if(!emp.getPassword().equals((password))){
            return  R.error("登录失败！");
        }
        // 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已经被禁用");
        }

        // 6.登录成功，将员工id存入Session, 并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        // 设置初始密码为123456,进行MD5加密  org.springframework.util  Spring提供的方法
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        employeeService.save(employee);
        return R.success("新增员工成功！");

    }

    /**
     * 分页条件查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}",page,pageSize,name);

        // 构建分页构造器
        Page pageInfo = new Page(page,pageSize);
        // 构建条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        // 添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        // 添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo,lqw);
        // 查询完的数据被封装在 pageInfo里，只需将pageInfo返回即可
        return R.success(pageInfo);
    }

    /**
     * 修改员工的状态
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){

        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

       /* long empId = (long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/

        employeeService.updateById(employee);
        return R.success("员工信息修改成功！");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee emp = employeeService.getById(id);
        if(emp != null){
            return R.success(emp);
        }
        return R.error("没有查到对应员工信息！");
    }
}
