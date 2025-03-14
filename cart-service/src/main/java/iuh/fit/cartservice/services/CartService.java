package iuh.fit.cartservice.services;

import iuh.fit.cartservice.models.Cart;
import iuh.fit.cartservice.models.CartItem;

import java.math.BigDecimal;

public interface CartService {
    Cart getCartByUserId(String userId);

    void addToCart(String userId, CartItem cartItem);

    void updateCartItemQuantity(String userId, String productId, int quantity);

    void removeFromCart(String userId, String productId);

    void clearCart(String userId);

    BigDecimal getCartTotal(String userId);

}
