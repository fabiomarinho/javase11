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
package labs.pm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;
import labs.pm.data.Product;
import labs.pm.data.Rating;
import labs.pm.service.ProductManagerException;

/**
 *
 * @author fabio
 */
public interface ProductManager {

    Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore);

    Product createProduct(int id, String name, BigDecimal price, Rating rating);

    Product findProduct(int id) throws ProductManagerException;

    Map<String, String> getDiscounts(String languageTag);

    void printProductReport(int id, String languageTag, String client);

    //    public void printProducts(Comparator<Product> sorter) {
    //        this.printProducts((p) -> true, sorter);
    //    }
    void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag);

    Product reviewProduct(int id, Rating rating, String comments);
    
}
