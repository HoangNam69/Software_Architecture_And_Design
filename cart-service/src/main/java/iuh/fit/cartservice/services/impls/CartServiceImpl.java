package iuh.fit.cartservice.services.impls;

import iuh.fit.cartservice.models.Cart;
import iuh.fit.cartservice.models.CartItem;
import iuh.fit.cartservice.services.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CartServiceImpl implements CartService {



    @Override
    public Cart getCartByUserId(String userId) {

        return null;
    }

    @Override
    public void addToCart(String userId, CartItem cartItem) {

    }

    @Override
    public void updateCartItemQuantity(String userId, String productId, int quantity) {

    }

    @Override
    public void removeFromCart(String userId, String productId) {

    }

    @Override
    public void clearCart(String userId) {

    }

    @Override
    public BigDecimal getCartTotal(String userId) {
        return null;
    }
}
