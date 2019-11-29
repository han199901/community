package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author qhhu
 * @date 2019/11/29 - 14:55
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyMMdd");

    // 将指定的IP计入UV
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的key
        List<String> redisKeyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(calendar.getTime()));
            redisKeyList.add(redisKey);
            calendar.add(Calendar.DATE, 1); // 当前日期加一天
        }

        // 合并每天的UV
        String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
        redisTemplate.opsForHyperLogLog().union(redisKey, redisKeyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    // 将指定用户加入DAU
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(new Date()));
        // 在位图索引为userID的位置进行标记
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的key
        List<byte[]> redisKeyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(calendar.getTime()));
            redisKeyList.add(redisKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 合并每天的DAU
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKeyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }

}
