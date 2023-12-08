package WebProject.WebProject.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Entity
public class RevenueStatic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Product ID must be positive")
    private int productId;

    private Long totalQuantity;
    // Consider using a larger numeric type

    private int year;
    private int month;
    private int weekNum;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate startOfWeek;
    private LocalDate endOfWeek;

    private LocalDate startOfMonth;
    private LocalDate endOfMonth;

    // Full-argument constructor
    public RevenueStatic(int productId, Long totalQuantity, int year, int month, int weekNum,
                         LocalDate startDate, LocalDate endDate, LocalDate startOfWeek,
                         LocalDate endOfWeek, LocalDate startOfMonth, LocalDate endOfMonth) {
        this.productId = productId;
        this.totalQuantity = totalQuantity;
        this.year = year;
        this.month = month;
        this.weekNum = weekNum;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startOfWeek = startOfWeek;
        this.endOfWeek = endOfWeek;
        this.startOfMonth = startOfMonth;
        this.endOfMonth = endOfMonth;
        // Add validations if needed
    }
    public RevenueStatic(LocalDate startDate, Long totalQuantity) {
        this.startDate = startDate;
        this.totalQuantity = totalQuantity;
        // Initialize other fields if needed
    }
    // No-args constructor for JPA
    public RevenueStatic() {
    }
}
