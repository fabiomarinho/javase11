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

/**
 *
 * @author fabio
 */
public class Review implements Comparable<Review>, Serializable{
    
    private Rating rating;
    
    private String comments;

    public Review(Rating rating, String comments) {
        this.rating = rating;
        this.comments = comments;
    }

    /**
     * Get the value of comments
     *
     * @return the value of comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Get the value of rating
     *
     * @return the value of rating
     */
    public Rating getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "Review(" + "rating="+ rating + ", comments=" + comments + ")";
    }

    @Override
    public int compareTo(Review other) {
        return other.getRating().ordinal() - this.getRating().ordinal();
    }
    
    

}
