/*
 * Copyright (C) 2021 fabio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.pm.data;

import java.io.Serializable;
import java.math.BigDecimal;
import static java.math.RoundingMode.HALF_UP;
import java.time.LocalDate;
import static labs.pm.data.Rating.*;

/**
 * Class that represent product
 *
 * @author fabio
 */
public abstract class Product implements Rateable<Product>, Serializable {

    private final int id;
    private final String name;
    private final BigDecimal price;
    private final Rating rating;

    {
        //    System.out.println("A new product is being created");
    }

    Product() {
        this(0, "no name", BigDecimal.ZERO, NOT_RATED);
    }

    Product(int id, String name, BigDecimal price, Rating rating) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    Product(int id, String name, BigDecimal price) {
        this(id, name, price, NOT_RATED);

    }

    public static final BigDecimal discount = BigDecimal.valueOf(0.1);

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDiscount() {
        return price.multiply(discount).setScale(2, HALF_UP);
    }

    @Override
    public Rating getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return id + ", " + name + ", " + price + ", "
                + getDiscount() + ", " + rating.getStars() + ", "
                + getBestBefore();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Product) {
            final Product other = (Product) obj;
            return this.id == other.id;
        }
        return false;
    }

    public LocalDate getBestBefore() {
        return LocalDate.now();
    }

}
