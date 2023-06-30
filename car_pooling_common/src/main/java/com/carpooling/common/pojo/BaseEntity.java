package com.carpooling.common.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author LiangHanSggg
 * @desc 实体类对象的基类，用于对公共字段的抽离
 * @date 2023-05-12 13:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

    /**
     * 逻辑删除
     *
     * @ignore
     */
    @JsonIgnore
    Integer deleted;

    /**
     * 创建时间
     *
     * @ignore
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT)
    LocalDateTime createTime;


    /**
     * 最后更新时间
     *
     * @ignore
     */
    @JsonIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime updateTime;

}
