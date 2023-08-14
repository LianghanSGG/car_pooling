package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangHanSggg
 * @date 2023-07-23 16:11
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("questions")
public class Question extends BaseEntity {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    Integer id;

    /**
     * 问题描述
     */
    String question;

    /**
     * 回答
     */
    String answers;


}
