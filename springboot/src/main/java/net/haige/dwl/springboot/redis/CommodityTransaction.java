package net.haige.dwl.springboot.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;


public class CommodityTransaction {

    @Autowired
    private RedisTemplate redisTemplate;

    RedisSerializer<String> serial = redisTemplate.getStringSerializer();
    //user:17        hash                         inventory:17             set
    //name           Frank                         ItemL
    //funds          43                            ItemM
    //                                             ItemN

    //user:27        hash                         inventory:27             set
    //name           Bill                          ItemO
    //funds          125                           ItemP
    //                                             ItemQ

    //market:        zset
    //ItemA.4        35
    //ItemC.7        48
    //ItemE.2        60



    private boolean list_item(String itemid,String sellerid,double price){

        String inventory=String.format("inventory:%s",sellerid);//inventory
        String item=String.format("%s.%s",itemid,sellerid);//market
        long end=System.nanoTime()+5;//截止时间

        RedisConnection conn=redisTemplate.getConnectionFactory().getConnection();
        conn.openPipeline();

        while(System.nanoTime()<end){
            conn.watch(serial.serialize(inventory));
            if(!conn.sIsMember(serial.serialize(inventory),serial.serialize(itemid))){
                conn.unwatch();
                return  false;
            }
            try {
                conn.multi();
                //添加到市场
                conn.zAdd(serial.serialize("market:"),price,serial.serialize(item));
                conn.sRem(serial.serialize(inventory),serial.serialize(itemid));
                conn.exec();
                return  true;
            }catch (Exception e){
                return  false;
            }finally {
                conn.closePipeline();
            }

        }
        return  false;
    }


    private boolean purchase_item(String buyerid,String itemid,String sellerid,double price){

        String buyer=String.format("users:%s",buyerid);//买家
        String seller=String.format("users:%s",sellerid);//卖家
        String item=String.format("%s.%s",itemid,sellerid);//market
        String buyerInventory=String.format("inventory:%s",buyerid);

        Long end=System.nanoTime()+10;
        RedisConnection conn =redisTemplate.getConnectionFactory().getConnection();
        conn.openPipeline();

        while(System.nanoTime()<end){
            conn.watch(serial.serialize(item));
            double score=conn.zScore(serial.serialize("market:"),serial.serialize(item));//获取市场上商品的价格
            double funds= Double.valueOf(serial.deserialize(conn.hGet(serial.serialize(buyer),serial.serialize("funds"))))  ;
            if(price!=score||price>funds){
                conn.unwatch();
                return false;

            }
            try {
                conn.multi();
                conn.hIncrBy(serial.serialize(seller),serial.serialize("funds"),price);
                conn.hIncrBy(serial.serialize(buyer),serial.serialize("funds"),price*(-1));
                conn.sAdd(serial.serialize(buyerInventory),serial.serialize(itemid));
                conn.zRem(serial.serialize("market:"),serial.serialize(item));
                conn.exec();
                return true;
            }catch(Exception e){
                return  false;
            }finally {
                conn.unwatch();
            }
        }
        return false;
    }






}
