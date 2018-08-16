package net.haige.dwl.springboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RediSemaphore {

    @Autowired
    private RedisTemplate redisTemplate;
    RedisSerializer<String> serial = redisTemplate.getStringSerializer();
    //semaphore:remote          zset
    //进程1                     时间戳
    //进程2.....


    /**
     *
     * @param semname　进程名
     * @param limit　　限制访问的进程数
     * @param timeout　超时时间
     * @return　是否成功获取锁
     */
    private  String aquire_semaphore(String semname,int limit ,int timeout){

        String uuid=UUID.randomUUID().toString();
        Long now=System.nanoTime();
        redisTemplate.executePipelined(new RedisCallback<Object>() {

            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.openPipeline();

                redisConnection.zRemRangeByScore(serial.serialize(semname),0,now-timeout);//清理过期的键

                redisConnection.zAdd(serial.serialize(semname),now,serial.serialize(uuid));
                long rank= redisConnection.zRank(serial.serialize(semname),serial.serialize(uuid));
                if(rank<limit){
                    return uuid;
                }

                redisConnection.closePipeline();
                redisConnection.zRem(serial.serialize(semname),serial.serialize(uuid));//获取不成功，则删除
                return null;
            }
        });

        return  null;
    }

    /**   semophore:remote:owner      zset
     *    进程1                        counter
     * 释放信号量
     * @param sename
     * @param uuid
     * @return
     */
    private boolean release_semaphore(String sename,String uuid){

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        conn.zRem(serial.serialize(sename),serial.serialize(uuid));

        return  false;
    }


    private String acquire_semaphore_fair(String sename,int limit,int timeout){

        String uuid=UUID.randomUUID().toString();
        Long now=System.nanoTime();

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        String czset=sename+":owner";
        String ctr=sename+":counter";


        conn.openPipeline();
        try {
            conn.zRemRangeByScore(serial.serialize(sename),0,now-timeout);
            conn.zInterStore(serial.serialize(czset),RedisZSetCommands.Aggregate.MIN
                    ,null,serial.serialize(czset),serial.serialize(sename));

            Long counter=conn.incr(serial.serialize(ctr));//计数器+1
            conn.zAdd(serial.serialize(sename),now,serial.serialize(uuid));
            conn.zAdd(serial.serialize(czset),counter,serial.serialize(uuid));

            long rank=conn.zRank(serial.serialize(czset),serial.serialize(uuid));
            if(rank<limit){
                return  uuid;
            }
            conn.zRem(serial.serialize(sename),serial.serialize(uuid));
            conn.zRem(serial.serialize(czset),serial.serialize(uuid));
        }
        finally {
            conn.closePipeline();
        }
        return  uuid;
    }



    private void release_fair_semaphore(String sename,String uuid){

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        conn.openPipeline();
        try {
            conn.zRem(serial.serialize(sename),serial.serialize(uuid));
            conn.zRem(serial.serialize(sename+":owner"),serial.serialize(uuid));

        }
        finally {
            conn.closePipeline();
        }



    }

    private boolean refresh_fair_semphore(String sename,String uuid){
        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        if(conn.zAdd(serial.serialize(sename),System.nanoTime(),serial.serialize(uuid))){
            return  true;
        }else {

            release_fair_semaphore(sename,uuid);
            return  false;
        }

    }

    // semaphore.lua
    //  redis.call('zremrangebyscore',KEYS[1],0,ARGV[1])//删除过期的锁
    // if（redis.call('zcard',KEYS[1]))<tonumber(ARGV[2]) then   //小于k
    //     redis.call('zadd',KEYS[1],ARGV[3],ARGV[4])
    //     return ARGV[3]
    //  end
    //

    /**
     *   .lua脚本以原子的方式执行
     * @param file       semaphore.lua
     * @param semname    semaphore:
     * @param limit      5
     * @param timeout    10
     * @return
     */
    private String acquire_semaphore_lua(String file,String semname,int limit,int timeout){

        DefaultRedisScript<String> script=new DefaultRedisScript<>();
        ClassPathResource resource=new ClassPathResource(file);
        script.setScriptSource(new ResourceScriptSource(resource));
        script.setResultType(String.class);
        List<byte[]> keys=new ArrayList<>();
        keys.add(serial.serialize(semname));
        Object result=redisTemplate.execute(script,keys,new String[]
                {String.valueOf(System.nanoTime()-timeout)
                        ,String.valueOf(limit)
                        ,String.valueOf(System.nanoTime())
                        ,UUID.randomUUID().toString()});
        if(result instanceof  String){
            return (String)result;
        }
        return  null;
    }





}
