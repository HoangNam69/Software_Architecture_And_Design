package iuh.fit.cartservice.services.impls;

import iuh.fit.cartservice.models.Cart;
import iuh.fit.cartservice.models.CartItem;
import iuh.fit.cartservice.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class CartServiceImpl implements CartService {


    private final RedisTemplate<Object, Object> redisTemplate;
    private static final String CART_PREFIX = "cart:"; // Tiền tố khóa Redis

    @Autowired
    public CartServiceImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getCartKey(String userId) {
        return CART_PREFIX + userId;
    }

    @Override
    public Cart getCartByUserId(String userId) {
        String key = getCartKey(userId);
        return (Cart) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void addToCart(String userId, CartItem cartItem) {
        String cartKey = getCartKey(userId);
        Cart cart = getCartByUserId(userId);

        if (cart == null) {
            cart = new Cart(userId, new HashMap<>());
        }

        cart.getItems().merge(cartItem.getProductId(), cartItem, (existingItem, newItem) -> {
            existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
            return existingItem;
        });

        redisTemplate.opsForValue().set(cartKey, cart);
    }

    @Override
    public void updateCartItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getCartByUserId(userId);
        if (cart != null && cart.getItems().containsKey(productId)) {
            if (quantity > 0) {
                cart.getItems().get(productId).setQuantity(quantity);
            } else {
                cart.getItems().remove(productId);
            }
            redisTemplate.opsForValue().set(getCartKey(userId), cart);
        }
    }

    @Override
    public void removeFromCart(String userId, String productId) {
        Cart cart = getCartByUserId(userId);
        if (cart != null && cart.getItems().containsKey(productId)) {
            cart.getItems().remove(productId);
            redisTemplate.opsForValue().set(getCartKey(userId), cart);
        }
    }

    @Override
    public void clearCart(String userId) {
        redisTemplate.delete(getCartKey(userId));
    }

    @Override
    public BigDecimal getCartTotal(String userId) {
        Cart cart = getCartByUserId(userId);
        if (cart == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().values().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
