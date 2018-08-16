package net.haige.dwl.springboot.redis;

import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class VotingApp {


    @Autowired
    private RedisTemplate<String, ?> redisTemplate;

    private final  static  long ONE_WEEK_IN_SECONDS=7*86400;
    private final  static  int VOTE_SCORE=432;

    RedisSerializer<String> serial = redisTemplate.getStringSerializer();
    /**
     *
     * @param user  example/   user:234487
     * @param article  example/ article:100408
     */
    private void article_vote(String user,String article){

        long cutoff=System.nanoTime()-ONE_WEEK_IN_SECONDS;

        Double score=redisTemplate.execute(new RedisCallback<Double>() {
            //获取time：的Zset中 article的分值，即存储文章的时间
            @Override
            public Double doInRedis(RedisConnection redisConnection) throws DataAccessException {

                return redisConnection.zScore(serial.serialize("time:"), serial.serialize(article));
            }
        });
        if(score<cutoff){//超过一周，投票过期
            return;
        }

        String article_id=article.split(":")[0];

       redisTemplate.execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.openPipeline();
                try{
                    Long added = redisConnection.sAdd(serial.serialize("voted:" + article_id), serial.serialize(user));
                    if (added > 0) {
                        redisConnection.zIncrBy(serial.serialize("score:"),VOTE_SCORE, serial.serialize(article));
                        redisConnection.hIncrBy(serial.serialize(article), serial.serialize("votes"),1);
                    }
                }catch(Exception e){
                    return  false;
                }
                finally {
                    redisConnection.closePipeline();
                    return  true;
                }
            }
        });
    }



    private String post_article(String user,String title,String link){


        RedisConnection con=redisTemplate.getConnectionFactory().getConnection();
        String article_id=String.valueOf(con.incr(serial.serialize("article:")));
        con.sAdd(serial.serialize("voted:"+article_id), serial.serialize(user));//将发布文章的人添加到已投票用户
        con.expire(serial.serialize("voted:"+article_id),ONE_WEEK_IN_SECONDS);

        ConcurrentHashMap<byte[],byte[]> content=new ConcurrentHashMap<byte[],byte[]>();
        content.put(serial.serialize("title"), serial.serialize(title));
        content.put(serial.serialize("link"), serial.serialize(link));
        content.put(serial.serialize("time"), serial.serialize(String.valueOf(System.nanoTime())));
        content.put(serial.serialize("poster"), serial.serialize(user));
        content.put(serial.serialize("votes"), serial.serialize("1"));
        con.hMSet(serial.serialize("article:"+article_id),content);



        con.zAdd(serial.serialize("score:")
                ,(double) (System.nanoTime()+VOTE_SCORE), serial.serialize("article:"+article_id));
        con.zAdd(serial.serialize("time:"),(double) (System.nanoTime()), serial.serialize("article:"+article_id));
        return  article_id;
    }


    private final  static  long ARTICLES_PER_PAGE=25;

    /**
     *
     * @param page
     * @param order  score:
     * @return
     */
    private List<Map<String,String>> get_articles(int page,String order){

        long start=(page-1)*ARTICLES_PER_PAGE;
        long end=start+ARTICLES_PER_PAGE-1;

        RedisConnection con=redisTemplate.getConnectionFactory().getConnection();
        Set<byte[]> ids=con.zRevRange(serial.serialize(order),start,end);
        List<Map<String,String>>  articles=new ArrayList<Map<String,String>>();
        for (byte[] id:ids
             ) {
            Map<byte[], byte[]> map= con.hGetAll(id);
            Map<String, String> mapStr=new ConcurrentHashMap<>();
            mapStr.put("title",serial.deserialize(map.get(serial.serialize("title"))));
            mapStr.put("link",serial.deserialize(map.get(serial.serialize("link"))));
            mapStr.put("time",serial.deserialize(map.get(serial.serialize("time"))));
            mapStr.put("poster",serial.deserialize(map.get(serial.serialize("poster"))));
            mapStr.put("votes",serial.deserialize(map.get(serial.serialize("votes"))));
            articles.add(mapStr);
        }
        return  articles;

    }


    private void add_remove_groups(String article_id,String to_add,String to_remove) {

        String article = "article:" + article_id;
        RedisConnection con = redisTemplate.getConnectionFactory( ).getConnection( );
        if (! StringUtil.isBlank( to_add ))
            con.sAdd( serial.serialize( "group:" + to_add ),serial.serialize( article ) );
        if (! StringUtil.isBlank( to_remove ))
            con.sRem( serial.serialize( "group:" + to_remove ),serial.serialize( article ) );

    }

    /**  set                   zset            zset
     * group:programming      score:       score:programming
     * 取交集
     * @param page
     * @param order
     * @return
     */
    private List<Map<String,String>> get_group_articles(String group,int page,String order){

        String key="score:"+group;
        RedisConnection con=redisTemplate.getConnectionFactory().getConnection();
        int[] ints=new int[0];
        if(!con.exists(serial.serialize(key))){
            con.zInterStore(serial.serialize(key),RedisZSetCommands.Aggregate.MAX,null,serial.serialize("score:"),serial.serialize("group:"+group));
        }
        con.expire(serial.serialize(key),60);
        return  get_articles(page,key);

    }




}
