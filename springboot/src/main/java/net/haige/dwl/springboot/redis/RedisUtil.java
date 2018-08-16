package net.haige.dwl.springboot.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Set;

public class RedisUtil {
    static  RedisTemplate redisTemplate=new RedisTemplate();
    public static byte[][]  set2array( Set<byte[]> keys){

        RedisSerializer<String> serial = redisTemplate.getStringSerializer();

        byte[][] result=new byte[keys.size()][];
        int i=0;
        for (byte[] key:keys
             ) {
            result[i++]=key;
        }
        return result;
    }

}
