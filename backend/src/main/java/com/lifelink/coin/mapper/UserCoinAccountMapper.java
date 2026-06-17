package com.lifelink.coin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifelink.coin.entity.UserCoinAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCoinAccountMapper extends BaseMapper<UserCoinAccount> {

    @Update("""
            UPDATE user_coin_accounts
            SET balance = balance + #{amount},
                total_earned = total_earned + #{amount},
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = #{userId}
            """)
    int addEarned(@Param("userId") Long userId, @Param("amount") Integer amount);

    @Update("""
            UPDATE user_coin_accounts
            SET balance = balance - #{amount},
                total_spent = total_spent + #{amount},
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = #{userId}
              AND balance >= #{amount}
            """)
    int spendIfEnough(@Param("userId") Long userId, @Param("amount") Integer amount);
}
