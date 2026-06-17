package com.lifelink.reward.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.reward.entity.Reward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RewardMapper extends BaseMapper<Reward> {

    @Update("""
            UPDATE rewards
            SET redeemed_count = redeemed_count + 1,
                status = CASE
                    WHEN stock IS NOT NULL AND redeemed_count + 1 >= stock THEN 'SOLD_OUT'
                    ELSE status
                END,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND status = 'ACTIVE'
              AND (stock IS NULL OR redeemed_count < stock)
            """)
    int incrementRedemptionIfAvailable(@Param("id") Long id);
}
