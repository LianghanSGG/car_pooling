package com.carpooling.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carpooling.common.mapper.StudentCertMapper;
import com.carpooling.common.pojo.db.StudentCert;
import com.carpooling.common.pojo.vo.StuCertStateVO;
import com.carpooling.common.pojo.vo.StuCertVO;
import com.carpooling.common.prefix.RedisPrefix;
import com.carpooling.common.properties.NumberConstants;
import com.carpooling.common.service.StudentCertService;
import com.carpooling.common.util.RedisUtil;
import com.carpooling.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangHanSggg
 * @date 2023-07-28 17:13
 */
@Service
public class StudentCertServiceImpl extends ServiceImpl<StudentCertMapper, StudentCert> implements StudentCertService {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ApplicationContext applicationContext;


    @Override
    public StuCertStateVO checkState() {
        StuCertStateVO stuCertStateVO = new StuCertStateVO();

        StudentCert studentCert = getLastOne();

        if (Objects.isNull(studentCert)) {
            stuCertStateVO.setState(3);
            return stuCertStateVO;
        }

        int state = studentCert.getState().intValue();

        stuCertStateVO.setState(state);
        if (state == 2) {
            stuCertStateVO.setMessage(studentCert.getMessage());
        }
        stuCertStateVO.setName(studentCert.getUserName());
        stuCertStateVO.setSchoolId(studentCert.getUserSchoolId());
        return stuCertStateVO;
    }

    @Override
    public Integer existReviewed() {

        StudentCert studentCert = getLastOne();
        if (Objects.isNull(studentCert)) return 0;

        return studentCert.getState().intValue() == 2 ? 0 : 1;
    }

    // 实际上不论如何都会创建一个新用户
    @Override
    public boolean saveStu(StuCertVO stuCertVO) {
        Long id = UserContext.get().getId();
        String openid = UserContext.get().getOpenid();

        LambdaQueryWrapper<StudentCert> eq = Wrappers.lambdaQuery(StudentCert.class)
                .eq(StudentCert::getUserId, id);

        long count = count(eq);
        StudentCert newStudentCert = new StudentCert();
        BeanUtil.copyProperties(stuCertVO, newStudentCert);
        newStudentCert.setUserId(id);
        newStudentCert.setUserOpenid(openid);
        //  新用户
        if (count == 0) {
            return save(newStudentCert);
        }
        // 判断上一条的状态
        StudentCert lastOne = getLastOne();

        boolean res;

        if (lastOne.getState().intValue() == 0) {
            lastOne.setState(3);
            // 注意失效问题
            res = applicationContext.getBean(this.getClass()).changeLastOne(lastOne, newStudentCert);
        } else {
            res = save(newStudentCert);
        }

        if (!res) return false;

        redisUtil.StringAdd(RedisPrefix.STUDENT_CERT_TIME + id, "1", NumberConstants.STU_TIME_30_DAY, TimeUnit.DAYS);

        return true;
    }

    public StudentCert getLastOne() {
        Long userId = UserContext.get().getId();
        LambdaQueryWrapper<StudentCert> select = Wrappers.lambdaQuery(StudentCert.class)
                .eq(StudentCert::getUserId, userId)
                .orderByDesc(StudentCert::getCreateTime)
                .last("limit 1")
                .select(StudentCert::getState, StudentCert::getMessage, StudentCert::getId,StudentCert::getUserName,StudentCert::getUserSchoolId);

        return getOne(select);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public boolean changeLastOne(StudentCert lastOne, StudentCert newOne) {
        return updateById(lastOne) && save(newOne);
    }
}
