package net.haige.dwl.springboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoFill {


    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *
     */
    RedisSerializer<String> serial = redisTemplate.getStringSerializer();


    //abc开头                  abb{  和abc{ 之间

    //String valid_chara

    private String[] find_prefix(String prefix){

        char c=prefix.charAt(prefix.length()-1);//找到最后一个字符

        char pre_c=(char)(c-1);

        String s1=prefix.substring(0,prefix.length()-2);

        return  new String[]{s1+pre_c+"{",prefix+"{"};

    }


    private Set<byte[]> autocomplete_on_prefix(String guild,String prefix){

        String start=find_prefix(prefix)[0];
        String end=find_prefix(prefix)[1];
        String identifier=UUID.randomUUID().toString();

       start+=identifier;
        end+=identifier;

        String zsetname="members:"+guild;//工会

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();

       // conn.zAdd(serial.serialize(zsetname),0,serial.serialize(start) );
       // conn.zAdd(serial.serialize(zsetname),0,serial.serialize(end));
        Set<RedisZSetCommands.Tuple> set=new HashSet<>();
        set.add(new defaultTuple(start));
        set.add(new defaultTuple(end));
        conn.zAdd(serial.serialize(zsetname),set);

        Set<byte[]> items=null;
        while(true){


            try {
                conn.watch(serial.serialize(zsetname));
                long sindex=conn.zRank(serial.serialize(zsetname),serial.serialize(start));
                long eindex=conn.zRank(serial.serialize(zsetname),serial.serialize(end));

                long erange=Math.max(sindex+9,eindex-2);
                conn.multi();

                conn.zRem(serial.serialize(zsetname),serial.serialize(start));
                conn.zRem(serial.serialize(zsetname),serial.serialize(end));

                items=conn.zRange(serial.serialize(zsetname),sindex,erange);

                conn.exec();

               break;

            }catch (RedisSystemException e){
                continue;
            }finally {
                conn.unwatch();

            }

        }
        return  items;
    }


    class defaultTuple implements  RedisZSetCommands.Tuple{

        private String val;

        public defaultTuple(String val) {
            this.val = val;
        }

        @Override
        public byte[] getValue() {
            return serial.serialize(val);
        }

        @Override
        public Double getScore() {
            return 0D;
        }

        @Override
        public int compareTo(Double o) {
            return 0;
        }
    }


}



