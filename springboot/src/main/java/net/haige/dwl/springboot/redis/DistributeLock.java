package net.haige.dwl.springboot.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class DistributeLock {

    private static JedisPool jedisPool = null;
    // Redis服务器地址
    private static String ADDR = "127.0.0.1";
    // Redis服务端口
    private static int PORT = 6379;

    @SuppressWarnings("unused")
    private static String AUTH = "123456";

    private static int MAX_ACTIVE = 1024;

    private static int MAX_IDLE = 200;

    private static int MAX_WAIT = 10000;

    private static int TIMEOUT = 10000;

    private static boolean TEST_ON_BORROW = true;
    /**
     * ��ʼ��Redis���ӳ�
     */
    static {

        FileInputStream in = null;
        try {
            Properties properties = new Properties();
            in = new FileInputStream("redis.propertites");
            properties.load(in);
            MAX_ACTIVE = Integer.parseInt(properties
                    .getProperty("redis.maxActive"));
            MAX_IDLE = Integer
                    .parseInt(properties.getProperty("redis.maxIdle"));
            MAX_WAIT = Integer
                    .parseInt(properties.getProperty("redis.maxWait"));
            TIMEOUT = Integer.parseInt(properties.getProperty("redis.timeout"));
            TEST_ON_BORROW = Boolean.parseBoolean(properties
                    .getProperty("redis.testOnBorrow"));
            AUTH = properties.getProperty("redis.pass");
            ADDR = properties.getProperty("redis.host");
            PORT = Integer.parseInt(properties.getProperty("redis.port"));
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);

            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("��ȡ������Ϣʧ�ܣ�");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ��ȡJedisʵ��
     *
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 关闭一个jedis连接
     *
     * @param jedis
     */
    public static void close(Jedis jedis) {
        try {
            jedis.close();

        } catch (Exception e) {
            if (jedis.isConnected()) {
                jedis.quit();
                jedis.disconnect();
            }
        }
    }


    /**
     *NX  SET_IF_NOT_EXISTS
     * @param jedis
     * @param lockKey
     * @param requestId   客户端锁的标识，用于解锁
     * @param expireTime  超时时间
     * @return
     */
    public  static  boolean tryGetDistributedLock(Jedis jedis,String lockKey,String requestId,int expireTime){
        if(jedis.set(lockKey,requestId,"NX","PX",expireTime).equals("OK")){
            return  true;
        }

        return  false;
    }


    public static  boolean release_distributed_lock(Jedis jedis,String lockKey,String requestId){

        String script=" if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Object o=jedis.eval(script,Collections.singletonList(lockKey),Collections.singletonList(requestId));
        if(((Long)o)==1L){
            return  true;
        }
        return  false;
    }


}
