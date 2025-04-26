package iuh.fit.cartservice.services.impls;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RedisExample implements CommandLineRunner {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        listExample();
    }

    public void valueExample() {
        redisTemplate.opsForValue().set("cart:user01", "Hello World");

        System.out.println("cart:user01 = " + redisTemplate.opsForValue().get("cart:user01"));
    }

    public void listExample() {
//        List<String> list = new ArrayList<String>();
//        list.add("Hello");
//        list.add("Redis");
//
//        redisTemplate.opsForList().rightPushAll("cart:user02", list);
//        System.out.println("Size of list: " + redisTemplate.opsForList().size("cart:user02"));
        List<String> list = redisTemplate.opsForList().range("cart:user02", 0, -1);

        for (String item : list) {
            System.out.println("Item: " + item);
        }
    }

}
