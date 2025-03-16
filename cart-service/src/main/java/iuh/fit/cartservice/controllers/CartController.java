package iuh.fit.cartservice.controllers;

import iuh.fit.cartservice.models.Cart;
import iuh.fit.cartservice.models.CartItem;
import iuh.fit.cartservice.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @GetMapping("/cart/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/cart/{userId}")
    public ResponseEntity<Void> addToCart(@PathVariable String userId, @RequestBody CartItem cartItem) {
        cartService.addToCart(userId, cartItem);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/cart/{productId}")
    public ResponseEntity<Void> updateCartItem(@PathVariable String userId,
                                               @PathVariable String productId,
                                               @RequestParam int quantity) {
        cartService.updateCartItemQuantity(userId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/cart/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable String userId, @PathVariable String productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable("userId") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<BigDecimal> getCartTotal(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCartTotal(userId));
    }
}
