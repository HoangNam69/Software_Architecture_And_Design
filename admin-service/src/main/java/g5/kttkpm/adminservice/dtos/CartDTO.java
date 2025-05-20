package g5.kttkpm.adminservice.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class CartDTO {
    private String userId;
    private Map<String, CartItemDTO> items;
}