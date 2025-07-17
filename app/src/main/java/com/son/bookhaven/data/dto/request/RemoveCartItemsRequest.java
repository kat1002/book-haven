package com.son.bookhaven.data.dto.request;

import java.util.List;

public class RemoveCartItemsRequest {
    public List<Integer> itemIds;

    public List<Integer> getCartItemIds() {
        return itemIds;
    }

    public void setCartItemIds(List<Integer> cartItemIds) {
        this.itemIds = cartItemIds;
    }
}
