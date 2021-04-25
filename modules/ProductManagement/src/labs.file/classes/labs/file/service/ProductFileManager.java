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
package labs.file.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
//import labs.client.ResourceFormatter;
import labs.pm.data.Drink;
import labs.pm.data.Food;
import labs.pm.data.Product;
import labs.pm.data.Rateable;
import labs.pm.data.Rating;
import labs.pm.data.Review;
import labs.pm.service.ProductManagerException;
import labs.pm.service.ProductManager;

/**
 *
 * @author fabio
 */
public class ProductFileManager implements ProductManager {

    private static final ProductManager pm = new ProductFileManager();

    private final ResourceBundle config = ResourceBundle.getBundle("labs.file.service.config");

    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));

    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));

    private final Path reportsFolder = Path.of(config.getString("reports.folder"));

    private final Path dataFolder = Path.of(config.getString("data.folder"));

    private final Path tempFolder = Path.of(config.getString("temp.folder"));

    //private ResourceFormatter formatter;
    private Map<Product, List<Review>> products = new HashMap<>();
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private static final Map<String, ResourceFormatter> formatters
            = Map.of(
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "pt-BR", new ResourceFormatter(Locale.forLanguageTag("pt-BR"))
            );
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();

//    public ProductManager(Locale locale) {
//        this(locale.toLanguageTag());
//    }
    public ProductFileManager() {
        loadAllData();
    }

    public static ProductManager getInstance() {
        return pm;
    }

//    public void printProducts(Comparator<Product> sorter) {
//        this.printProducts((p) -> true, sorter);
//    }
    @Override
    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            StringBuilder txt = new StringBuilder();
            txt.append(
                products.keySet().stream()
                        .sorted(sorter)
                        .filter(filter)
                        .map(p -> formatter.formatProduct(p) + '\n')
                        .collect(Collectors.joining())
            );
            System.out.println(txt);
        }finally{
            readLock.unlock();
        }
//        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
//        //List<Product> productList = new ArrayList<>(products.keySet());
//        //Collections.sort(productList, sorter);
//        StringBuilder txt = new StringBuilder();
//        txt.append(
//                products.keySet().stream()
//                        .sorted(sorter)
//                        .filter(filter)
//                        .map(p -> formatter.formatProduct(p) + '\n')
//                        .collect(Collectors.joining())
//        );
//        //Alternative:
//        //products.keySet().stream()
////                    .sorted(sorter)
////                    .forEach(p->txt.append(formatter.formatProduct(p) + '\n'));
////        for (Product p : productList) {
////            txt.append(formatter.formatProduct(p));
////            txt.append('\n');
////        }
//        System.out.println(txt);

    }

//    private Product product;
//    private  Review[] reviews = new Review[5];
//    public void changeLocale(String languageTag) {
//        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
//    }
    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product " + ex.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error adding product " + ex.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }

        return product;
    }

    private Product reviewProduct(Product product, Rating rating, String comments) {
        List<Review> reviews = products.get(product);
        products.remove(product, reviews);
        reviews.add(new Review(rating, comments));
        product.applyRating(
                Rateable.convert(
                        (int) Math.round(
                                reviews.stream()
                                        .mapToInt(r -> r.getRating().ordinal())
                                        .average()
                                        .orElse(0)
                        )
                )
        );
//        int sum = 0;
//        for (Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
//
//        product = product.applyRating(Rateable.convert(Math.round((float) sum / reviews.size())));
        products.put(product, reviews);
        return product;
    }
    
    @Override
    public void printProductReport(int id, String languageTag, String client) {
        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error priting product report " + ex.getMessage(), ex);
        } finally {
            readLock.unlock();
        }
    }

    private void printProductReport(Product product, String languageTag, String client) throws IOException {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        Path productFile
                = reportsFolder
                        .resolve(MessageFormat
                                .format(config
                                        .getString("report.file"), product.getId(), client
                                ));
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), "UTF-8"))) {
            out.append(formatter.formatProduct(product));
            out.append('\n');

            if (reviews.isEmpty()) {
                out.append(formatter.getText("no-reviews") + System.lineSeparator());
            } else {
                out.append(
                        reviews.stream()
                                .map(r -> formatter.formatReview(r) + System.lineSeparator())
                                .collect(Collectors.joining())
                );
                //Alternative
                //reviews.forEach(r->txt.append(formatter.formatReview(r) + '\n'));
                //In parallel it may result in unpredictable behavior 
                //since using Collectors.joining above guarantees a correct result
            }
        }

//        for (Review review : reviews) {
//            txt.append(formatter.formatReview(review));
//            txt.append('\n');
//        }
//        if (reviews.isEmpty()) {
//            txt.append(formatter.getText("no-reviews"));
//            txt.append('\n');
//        }
        //System.out.println(txt);
    }

    @Override
    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])), (String) values[1]);
            //reviewProduct(
            //        Integer.parseInt((String) values[0]),
            //        Rateable.convert(Integer.parseInt((String) values[1])),
            //        (String) values[2]
            //);
        } catch (ParseException | NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parsing review '" + text + "'");
        }
        return review;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    //createProduct(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                //createProduct(id, name, price, rating, bestBefore);
            }

        } catch (ParseException
                | NumberFormatException
                | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error parsing product '" + text + "' " + ex.getMessage());
        }
        return product;
    }

    @Override
    public Product findProduct(int id) throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter((p) -> p.getId() == id)
                    .findFirst()
                    //.get();
                    .orElseThrow(()
                            -> new ProductManagerException("Product with id " + id + " not found"));
        } finally {
            readLock.unlock();
        }

        //.orElseGet(() -> null);
//        for (Product product : products.keySet()) {
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
        //return result;
    }

    @Override
    public Map<String, String> getDiscounts(String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                p -> p.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                                p -> p.getDiscount().doubleValue()
                                        ),
                                        discount -> formatter.moneyFormat.format(discount)
                                )
                        )
                );
        } finally {
            readLock.unlock();
        }
        
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;
        Path file
                = dataFolder.resolve(
                        MessageFormat.format(config.getString("reviews.data.file"), product.getId())
                );
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text))
                        .filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error loading the reviews " + ex.getMessage(), ex);
            }
        }
        return reviews;
    }

    private Product loadProduct(Path file) {
        Product product = null;
        try {
            product = parseProduct(
                    Files.lines(
                            dataFolder.resolve(file),
                            Charset.forName("UTF-8")
                    ).findFirst().orElseThrow()
            );
        } catch (IOException | NoSuchElementException ex) {
            logger.log(Level.WARNING, "Error loading the product " + ex.getMessage(), ex);
        }

        return product;
    }

    private void loadAllData() {
        try {
            products
                    = Files
                            .list(dataFolder)
                            .filter(file -> file.getFileName().toString().startsWith("product"))
                            .map(file -> loadProduct(file))
                            .collect(Collectors.toMap(product -> product, product -> loadReviews(product)));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage(), ex);
        }
    }

    private void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), Instant.now())
            );
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
                out.writeObject(products);
                //products = new HashMap<>();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error dumping data " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private void restoreData() {
        try {

            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {

                products = (HashMap) in.readObject();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage(), ex);
        }
    }
    
    
    private static class ResourceFormatter {

    private Locale locale;
    private ResourceBundle resources;
    private DateTimeFormatter dateFormat;
    public  NumberFormat moneyFormat;

    public ResourceFormatter(Locale locale) {
        this.locale = locale;
        //System.out.println("** " + this.locale.toLanguageTag());
        resources = ResourceBundle.getBundle("labs.file.service.resources", this.locale);
        //System.out.println(resources.getString("product"));
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }

    public String formatProduct(Product product) {
        return MessageFormat.format(
                resources.getString("product"),
                product.getName(),
                moneyFormat.format(product.getPrice()),
                product.getRating().getStars(),
                dateFormat.format(product.getBestBefore()));
    }

    public String formatReview(Review review) {
        return MessageFormat.format(
                resources.getString("review"),
                review.getRating().getStars(),
                review.getComments());
    }

    public String getText(String key) {
        return resources.getString(key);
    }
}


}
