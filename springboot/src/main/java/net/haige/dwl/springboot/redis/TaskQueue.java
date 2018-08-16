package net.haige.dwl.springboot.redis;

import net.haige.dwl.util.JsonUtil;
import redis.clients.jedis.Jedis;

import java.util.List;

public class TaskQueue {


    private void send_sold_email_via_queue(Jedis jedis,String seller,String item,String buyer,double price){

        email e=new email(seller,item,price,buyer,System.nanoTime());
        jedis.rpush("queue:email", JsonUtil.getJsonString(e));

    }



    private void process_sold_email_queue(Jedis jedis){

        while (true){
            List<String> packed=jedis.blpop(30,"queue:mail");
            for (String to_send:packed
                 ) {
                email e=JsonUtil.json2Obj(to_send,email.class);
                //fetch_data_and_send_sold_email(e);
            }
        }

    }






}



class email{

    private String seller_id;
    private String item_id;
    private double price;
    private String buyer_id;
    private long time;

    public email(String seller_id, String item_id, double price, String buyer_id, long time) {
        this.seller_id = seller_id;
        this.item_id = item_id;
        this.price = price;
        this.buyer_id = buyer_id;
        this.time = time;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}