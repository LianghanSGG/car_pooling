package com.carpooling.common.pojo.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 黑名单
 *
 * @author LiangHanSggg
 * @date 2023-07-25 19:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("blacklist")
public class BlackList extends BaseEntity {

    @TableId
    Long id;


    Long userId;


}
