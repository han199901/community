package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author qhhu
 * @date 2019/10/26 - 12:41
 */
@Mapper
@Repository
public interface LoginTicketMapper {

    String TABLE_NAME = " login_ticket ";
    String INSERT_FIELDS = " user_id,ticket,status,expired ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS, ")", " values(#{userId},#{ticket},#{status},#{expired})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);

    @Update({"update", TABLE_NAME, " set status=#{status} where ticket=#{ticket}"})
    int updateStatus(String ticket, int status);

}
