package com.carpooling.common.pojo;

import com.carpooling.common.pojo.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author LiangHanSggg
 * @date 2023-06-30 20:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEntity extends BaseEntity {

    /**
     * 用户的id
     */
    Long id;

}
