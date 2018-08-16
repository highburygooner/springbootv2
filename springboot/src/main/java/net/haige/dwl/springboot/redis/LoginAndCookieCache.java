package net.haige.dwl.springboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LoginAndCookieCache {
    @Autowired
    private RedisTemplate redisTemplate;
    RedisSerializer<String> serial = redisTemplate.getStringSerializer();

    /**
     * 检查令牌环是否存在,服务器端
     * @param token
     * @return
     */
    private boolean check_token(String token){


        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        byte[] user=conn.hGet(serial.serialize("login:"),serial.serialize(token));
        return user != null;

    }

    /**
     *
     * @param token 令牌环，sessionId
     * @param user   序列化后的用户信息
     * @param goods  商品
     * @return
     */
    private boolean update_token(String token,byte[] user,String goods){

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        conn.hSet(serial.serialize("login:"),serial.serialize(token),user);
        Double timestamp=Double.valueOf (System.nanoTime());
        conn.zAdd(serial.serialize("recent:"),timestamp,serial.serialize(token));//最近登录时间

        if(goods!=null){
            //最近浏览的商品
            conn.zAdd(serial.serialize("viewd:"+token),timestamp,serial.serialize(goods));
            //只保留最近的25条记录
            conn.zRemRange(serial.serialize("viewd:"+token),0,-26);
        }

        return  true;
    }

    private volatile boolean QUIT=false;
    private final  static  int LIMIT=1000000;

    /**
     *
     * @throws InterruptedException
     */
    private void clean_session() throws InterruptedException{
        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        while (!QUIT){
            long size =conn.zCard(serial.serialize("recent:"));
            if(size<LIMIT){

                TimeUnit.MILLISECONDS.sleep(100);

                continue;
            }
            long end_index=Math.min(size,100);
            Set<byte[]> tokens= conn.zRange(serial.serialize("recent:"),0,end_index);
            byte[][] viewdKeys=new byte[tokens.size()][];
            byte[][] cartKeys=new byte[tokens.size()][];
            int index=0;
            for (byte[] token:tokens
                 ) {
                viewdKeys[index]=serial.serialize("viewd:"+serial.deserialize(token));
                cartKeys[index]=serial.serialize("cart:"+serial.deserialize(token));
                        index++;
            }
            conn.del(viewdKeys);
            conn.del(cartKeys);
            conn.hDel(serial.serialize("login:"),RedisUtil.set2array(tokens));
            conn.zRem(serial.serialize("recent:"),RedisUtil.set2array(tokens));
        }
    }


    private void add_to_cast(String token,String item,int count){

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        if(count<0){
            conn.hDel(serial.serialize("cart:"+token),serial.serialize(item));
        }else {
            conn.hSet(serial.serialize("cart:"+token),serial.serialize(item),serial.serialize(String.valueOf(count)));
        }

    }




}
