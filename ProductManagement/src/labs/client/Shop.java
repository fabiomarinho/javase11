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
package labs.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import labs.pm.data.Drink;
import labs.pm.data.Food;
import labs.pm.data.Product;
import labs.file.service.ProductFileManager;
import static labs.pm.data.Rating.*;

/**
 * Oracle Shop - represents an application to message products
 *
 * @version 1.0
 * @author fabio
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
//        var pm = new ProductManager(
//        //      Locale.forLanguageTag("pt-BR")
//        //      Locale.UK
//        //      "pt-BR"
//                "en-GB"
//        );
        var pm = ProductFileManager.getInstance();
        AtomicInteger clientCount = new AtomicInteger(0);

        Callable<String> client = () -> {
            String clientId = "Client" + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(4) + 101;
            String languageTag = ProductFileManager
                    .getSupportedLocales()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(ProductFileManager.getSupportedLocales().size() - 1
                    ))
                    .findFirst()
                    .get();
            StringBuilder log = new StringBuilder();
            log.append(clientId + threadName + "\n-\tstart of log\t-\n");
            log.append(
                    pm.getDiscounts(languageTag)
                            .entrySet()
                            .stream()
                            .map(entry -> entry.getKey() + "\t" + entry.getValue())
                            .collect(Collectors.joining("\n"))
            );
            Product product = pm.reviewProduct(productId, FOUR_STAR, "Yet another review");
            log.append(
                    product != null ? "\nProduct: " + productId + "reviewed\n" : "\nProduct: " + productId + "not reviewed\n"
            );

            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId + " generated report for " + productId + " product");
            log.append("\n-\tend of log\t-\n");
            return log.toString();
        };

        List<Callable<String>> clients = Stream
                .generate(() -> client)
                .limit(5)
                .collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();
            results.stream().forEach(result->{
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retrieving client log", ex);
                }
            });
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients", ex);
        }
        

        //pm.printProductReport(101, "en-GB");
        //pm.printProductReport(101, "ru-RU");
        //pm.createProduct(101, "Tea", BigDecimal.valueOf(2.99), NOT_RATED);
        //pm.parseProduct("D,101,Tea,2.99,0,2019-09-19");
        //Exception -> pm.parseProduct("D,101,Tea,2.99,0,2019-09-19");
        //pm.parseProduct("F,103,Cake,2.99,0,2019-09-19");
        //pm.printProductReport(103);
        //pm.printProductReport(101);
        //To produce exceptions
        //pm.printProductReport(42);
        //pm.reviewProduct(42, FOUR_STAR, "Nice hot cup of tea");
        //pm.parseReview("101,4,Nice hot cup of tea");
        //To throw exception:
        //pm.parseReview("101,X,Nice hot cup of tea");
        //pm.printProductReport(101);
        //pm.reviewProduct(101, FOUR_STAR, "Nice hot cup of tea");
        //pm.reviewProduct(101, TWO_STAR, "Rather weak tea");
        //pm.reviewProduct(101, FOUR_STAR, "Fine tea");
        //pm.reviewProduct(101, FOUR_STAR, "Good tea");
        //pm.reviewProduct(101, FIVE_STAR, "Perfect tea");
        //pm.reviewProduct(101, THREE_STAR, "Just add some lemon");
//        pm.parseReview("101,4,Nice hot cup of tea");
//        pm.parseReview("101,2,Rather weak tea");
//        pm.parseReview("101,4,Fine tea");
//        pm.parseReview("101,4,Good tea");
//        pm.parseReview("101,5,Perfect tea");
//        pm.parseReview("101,3,Just add some lemon");
//pm.dumpData();
//pm.restoreData();
//        pm.printProductReport(101);
//        
//        pm.createProduct(102, "Coffee", BigDecimal.valueOf(2.99), NOT_RATED);
//        pm.reviewProduct(102, THREE_STAR, "Coffee was ok");
//        pm.reviewProduct(102, ONE_STAR, "Where is the molk?");
//        pm.reviewProduct(102, FIVE_STAR, "It's perfect with ten spoons of sugar!");
//        pm.printProductReport(102);
//pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), FIVE_STAR, LocalDate.now().plusDays(2));
//Comparator<Product> ratingSorter = (p1,p2)->p2.getRating().ordinal() - p1.getRating().ordinal();
//Comparator<Product> priceSorter = (p1,p2)->p2.getPrice().compareTo(p1.getPrice());
//pm.printProducts(p->p instanceof Food,ratingSorter);
//pm.printProducts(priceSorter.thenComparing(ratingSorter.reversed()));
//pm.getDiscounts() .entrySet()
//                .stream().forEach(e->System.out.println(e.getKey() + "->" + e.getValue() + "\n"));
//pm.getDiscounts().forEach((r,d)->System.out.println(r+'\t'+d));
//        var p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), THREE_STAR);
//        var p2 = pm.createProduct(102, "Coffe", BigDecimal.valueOf(1.99), FOUR_STAR);
//        var p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(1.99), FIVE_STAR, LocalDate.now().plusDays(2));
//        var p4 = pm.createProduct(105, "Cookie", BigDecimal.valueOf(3.99), TWO_STAR, LocalDate.now());
//        var p5 = p3.applyRating(3);
//        var p6 = pm.createProduct(104, "Chocolate", BigDecimal.valueOf(2.99), FIVE_STAR);
//        var p7 = pm.createProduct(104, "Chocolate", BigDecimal.valueOf(2.99), FIVE_STAR, LocalDate.now().plusDays(2));
//        var p8 = p4.applyRating(5);
//        var p9 = p1.applyRating(2);
//        
//        pm.printProductReport();
//        
//        DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
//        
//        System.out.println(dtf.format(p1.getBestBefore()));
//        System.out.println(p3.getBestBefore());
//
//
//
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p3);
//        System.out.println(p4);
//        System.out.println(p5);
//        System.out.println(p6);
//        System.out.println(p7);
//        System.out.println(p8);
//        System.out.println(p9);
    }

}
