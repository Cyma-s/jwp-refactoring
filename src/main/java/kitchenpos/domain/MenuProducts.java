package kitchenpos.domain;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class MenuProducts {

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;

    protected MenuProducts() {
    }

    public MenuProducts(final List<MenuProduct> menuProducts, final Menu menu) {
        menuProducts.forEach(menuProduct -> menuProduct.joinMenu(menu));
        this.menuProducts = menuProducts;
    }

    public Price calculatePrice() {
        final BigDecimal result = menuProducts.stream()
            .map(MenuProduct::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Price(result);
    }

    public List<MenuProduct> getMenuProducts() {
        return menuProducts;
    }
}
