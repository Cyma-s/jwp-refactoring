package kitchenpos.order.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.repository.MenuRepository;
import kitchenpos.order.application.dto.OrderChangeOrderStatusRequest;
import kitchenpos.order.application.dto.OrderCreateRequest;
import kitchenpos.order.application.dto.OrderLineItemCreateRequest;
import kitchenpos.order.application.dto.OrderResponse;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.domain.OrderTable;
import kitchenpos.order.repository.OrderRepository;
import kitchenpos.order.repository.OrderTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(final MenuRepository menuRepository, final OrderRepository orderRepository,
                        final OrderTableRepository orderTableRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderResponse create(final OrderCreateRequest request) {
        validateAllExistOrderLineItems(request.getOrderLineItems());
        final List<OrderLineItem> orderLineItems = makeOrderLineItems(request.getOrderLineItems());
        final OrderTable orderTable = orderTableRepository.getById(request.getOrderTableId());
        validateEmptyOrderTable(orderTable);
        final Order order = Order.forSave(OrderStatus.COOKING, orderLineItems, orderTable.getId());

        return OrderResponse.from(orderRepository.save(order));
    }

    private void validateAllExistOrderLineItems(final List<OrderLineItemCreateRequest> orderLineItemIds) {
        final List<Long> menuIds = orderLineItemIds.stream()
            .map(OrderLineItemCreateRequest::getMenuId)
            .collect(Collectors.toList());
        final boolean existsAll = menuRepository.countByIds(menuIds) == orderLineItemIds.size();

        if (!existsAll) {
            throw new IllegalArgumentException("존재하지 않는 주문 항목이 포함되어 있습니다.");
        }
    }

    private List<OrderLineItem> makeOrderLineItems(final List<OrderLineItemCreateRequest> orderLineItemCreateRequests) {
        final List<OrderLineItem> orderLineItems = new ArrayList<>();
        for (final OrderLineItemCreateRequest request : orderLineItemCreateRequests) {
            final Menu menu = menuRepository.getById(request.getMenuId());
            final OrderLineItem orderLineItem = OrderLineItem.forSave(request.getQuantity(), menu.getName(),
                                                                      menu.getPrice(), menu.getId());
            orderLineItems.add(orderLineItem);
        }

        return orderLineItems;
    }

    private void validateEmptyOrderTable(final OrderTable orderTable) {
        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException("주문 테이블이 비어있습니다.");
        }
    }

    public List<OrderResponse> list() {
        return orderRepository.findAll().stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId, final OrderChangeOrderStatusRequest request) {
        final Order order = orderRepository.getById(orderId);
        order.changeOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));

        return OrderResponse.from(order);
    }
}
